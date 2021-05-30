package com.gtp.hunter.process.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.AccessTimeout;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.hibernate.Hibernate;
import org.slf4j.Logger;

import com.gtp.hunter.core.annotation.qualifier.InsertQualifier;
import com.gtp.hunter.core.annotation.qualifier.UpdateQualifier;
import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.DocumentTransport;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.model.util.Products;
import com.gtp.hunter.process.model.util.Things;
import com.gtp.hunter.process.repository.DocumentFieldRepository;
import com.gtp.hunter.process.repository.DocumentItemRepository;
import com.gtp.hunter.process.repository.DocumentModelRepository;
import com.gtp.hunter.process.repository.DocumentRepository;
import com.gtp.hunter.process.repository.DocumentThingRepository;
import com.gtp.hunter.process.repository.PropertyRepository;
import com.gtp.hunter.process.repository.ThingRepository;
import com.gtp.hunter.process.service.solar.AGLConvertService;
import com.gtp.hunter.process.util.InventoryComparison;

import io.reactivex.annotations.Nullable;

@Stateless
@AccessTimeout(value = 15000)
public class DocumentService {

	@Inject
	private AGLConvertService			aglSvc;

	@Inject
	private DocumentRepository			dcRep;

	@Inject
	private DocumentFieldRepository		dfRep;

	@Inject
	private DocumentItemRepository		diRep;

	@Inject
	private DocumentModelRepository		dmRep;

	@Inject
	private DocumentThingRepository		dtRep;

	@Inject
	private DocumentTransportService	dtrSvc;

	@Inject
	private ThingRepository				thRep;

	@Inject
	private PropertyRepository			prRep;

	@Inject
	private transient Logger			logger;

	public Document createChild(Document dMaster, String masterStatus, String childModelMeta, String childStatus, String childCodePrefix, @Nullable Thing t, @Nullable Address a, @Nullable DocumentModelField dmf, @Nullable User user) {
		DocumentModel childModel = dmRep.findByMetaname(childModelMeta);

		if (childModel != null) {
			Document dChild = new Document(childModel, childModel.getName() + " " + dMaster.getCode(), childCodePrefix + dMaster.getCode(), childStatus);

			if (dmf != null && a != null) {
				DocumentField dfChildAddr = new DocumentField();

				dfChildAddr.setDocument(dChild);
				dfChildAddr.setField(dmf);
				dfChildAddr.setValue(a.getId().toString());
				dfChildAddr.setCreatedAt(Calendar.getInstance().getTime());
				dfChildAddr.setUpdatedAt(Calendar.getInstance().getTime());
				dChild.getFields().add(dfChildAddr);
			}

			if (t != null) {
				DocumentThing dt = new DocumentThing(dChild, t, childStatus);

				dChild.getThings().add(dt);
			}
			dChild.setParent(dMaster);
			dChild.setCreatedAt(Calendar.getInstance().getTime());
			dChild.setUpdatedAt(Calendar.getInstance().getTime());
			dChild.setUser(user);
			dMaster.setStatus(masterStatus);
			dMaster.getSiblings().add(dChild);
			dMaster.setUser(user);
			dcRep.persist(dMaster);
			return dChild;
		}
		return null;
	}

	public DocumentThing createDocumentThing(Document doc, Thing t) {
		DocumentThing dt = new DocumentThing(doc, t, doc.getStatus());

		dt.setName(t.getName());
		dt.setCreatedAt(Calendar.getInstance().getTime());
		dt.setUpdatedAt(Calendar.getInstance().getTime());
		doc.getThings().add(dt);
		return dt;
	}

	public Document createDocumentWithThing(Document parent, DocumentModel model, String dStatus, Address addr, Prefix prefix, Thing t, String dtStatus) {
		String code = prefix.getCode();
		Date now = Calendar.getInstance().getTime();
		Document d = new Document(model, model.getName() + " " + code, prefix.getPrefix() + code, dStatus);
		DocumentThing dt = new DocumentThing(d, t, dtStatus);
		Thing ts = t.getSiblings().parallelStream().findAny().get();
		Product p = ts.getProduct();
		double qty = Things.getDoubleProperty(ts, "QUANTITY", 0d);
		String um = Products.getStringField(p, "GROUP_UM");
		DocumentItem di = new DocumentItem(d, p, qty, "NOVO", um);

		dt.setCreatedAt(now);
		dt.setUpdatedAt(now);
		d.getThings().add(dt);
		d.getItems().add(di);
		d.setCreatedAt(now);
		d.setUpdatedAt(now);
		if (parent != null) d.setParent(parent);
		dcRep.persist(d);
		aglSvc.sendDocToWMS(d, "POST");
		return d;
	}

	public Document detachEntity(Document d) {
		dcRep.detach(d);
		return d;
	}

	@Transactional(value = TxType.REQUIRED)
	public Document dirtyFullInsert(Document d, boolean fireEvent) {
		if (d.getId() == null) d.setId(UUID.randomUUID());
		// SALVA DOC
		this.dcRep.dirtyInsert(d.getId(), d.getMetaname(), d.getName(), d.getStatus(), d.getCreatedAt(), d.getUpdatedAt(), d.getCode(), d.getModel() != null ? d.getModel().getId() : null, d.getParent() != null ? d.getParent().getId() : null, d.getUser() != null ? d.getUser().getId() : null, d.getPerson() != null ? d.getPerson().getId() : null, fireEvent);
		// SALVA DOCITEM
		for (DocumentItem di : d.getItems()) {
			if (di.getId() == null) di.setId(UUID.randomUUID());
			diRep.dirtyInsert(di.getId(), di.getMetaname(), di.getName(), di.getStatus(), di.getCreatedAt(), di.getUpdatedAt(), di.getMeasureUnit(), di.getQty(), d.getId(), di.getProduct().getId());
			if (di.getProperties() != null) for (String key : di.getProperties().keySet()) {
				String value = di.getProperties().get(key);

				diRep.dirtyInsertProperty(di.getId(), key, value);
			}
		}
		// SALVA THING E DOCTHING
		for (DocumentThing dt : d.getThings()) {
			if (dt != null) {
				if (dt.getId() == null) dt.setId(UUID.randomUUID());

				Thing t = thRep.dirtyInsert(dt.getThing());
				for (Property pty : t.getProperties())
					if (pty != null && pty.getField() != null && pty.getField().getId() != null)
						prRep.quickInsert(t.getId(), pty.getField().getId(), pty.getValue());
				dtRep.quickInsert(d.getId(), dt.getThing().getId(), dt.getStatus());
			}
		}
		// SALVA DOCFIELD
		for (DocumentField df : d.getFields()) {
			if (df.getId() == null) df.setId(UUID.randomUUID());
			dfRep.quickInsertDocumentField(d.getId(), df.getField().getId(), df.getValue());
		}
		// SALVA DOCTRANSPORT
		for (DocumentTransport dtr : d.getTransports()) {
			if (dtr != null) {
				if (dtr.getId() == null) dtr.setId(UUID.randomUUID());
				UUID thingId = dtr.getThing() == null ? null : dtr.getThing().getId();
				UUID addressId = dtr.getAddress() == null ? null : dtr.getAddress().getId();
				UUID originId = dtr.getOrigin() == null ? null : dtr.getOrigin().getId();

				if (originId == null && dtr.getThing() != null && dtr.getThing().getAddress() != null)
					originId = dtr.getThing().getAddress().getId();
				dtrSvc.dirtyInsert(dtr.getId(), dtr.getMetaname(), dtr.getName(), dtr.getStatus(), dtr.getCreatedAt(), dtr.getUpdatedAt(), dtr.getSeq(), d.getId(), thingId, addressId, originId);
			}
		}
		// SALVA SIBLING
		for (Document sib : d.getSiblings()) {
			if (sib.getId() == null) sib.setId(UUID.randomUUID());
			sib = dirtyFullInsert(sib, false);
		}
		// GET JPA DOC
		d = dcRep.findById(d.getId());
		System.out.println("Dirty Insert = " + (d == null ? " NOT INSERTED " : d.getCode()));
		// DISPARA EVENT
		if (fireEvent && d != null)
			fireInsert(d);
		// RETORNA DOC
		return d;
	}

	public Document findById(UUID id) {
		return dcRep.findById(id);
	}

	public Document findByModelAndCode(DocumentModel dm, String code) {
		return dcRep.findByModelAndCode(dm, code);
	}

	public Document findByModelAndCodeAndPersonCode(DocumentModel dm, String code, String personCode) {
		return dcRep.findByModelAndCodeAndPersonCode(dm, code, personCode);
	}

	public Document findByTypeAndCode(String type, String code) {
		return dcRep.findByTypeAndCode(type, code);
	}

	public Document findLastByTypeAndStatusAndFieldValue(DocumentModel model, String status, String fieldMeta, String fieldValue) {
		return dcRep.findLastByTypeAndStatusAndFieldValue(model, status, fieldMeta, fieldValue);
	}

	public List<Document> listByParent(UUID id) {
		return id == null ? null : dcRep.listByField("parent_id", id);
	}

	public List<Document> listByStatus(String status) {
		return dcRep.listByField("status", status);
	}

	public List<Document> listByTypeAndStatus(String type, String status) {
		return dcRep.listByTypeAndStatus(type, status);
	}

	public void multiPersist(List<Document> sibs) {
		dcRep.multiPersist(sibs);
	}

	public Document persist(Document d) {
		dcRep.persist(d);
		return d;
	}

	public Document quickFindByCodeAndModelMetaname(String docCode, String metaDocModel) {
		return dcRep.quickFindByCodeAndModelMetaname(docCode, metaDocModel);
	}

	public Document quickFindById(UUID doc) {
		return dcRep.quickFindById(doc);
	}

	public Document quickFindByMetaname(String metaname) {
		return dcRep.quickFindByMetaname(metaname);
	}

	public Document quickFindByPropertyValue(String prop, String value) {
		return dcRep.getQuickDocumentByPropertyValue(prop, value);
	}

	public Document quickFindParentDoc(String id) {
		return dcRep.quickFindParentDoc(id);
	}

	public Document quickFindParentDoc(UUID id) {
		return id == null ? null : quickFindParentDoc(id.toString());
	}

	public void quickUpdateStatus(UUID id, String status) {
		dcRep.quickUpdateStatus(id, status);
	}

	public List<Document> listByThingStatus(String status) {
		return dcRep.listByThingStatus(status);
	}

	public List<Document> listAll() {
		return dcRep.listAll();
	}

	public List<Document> quickListByTypeStatus(String type, String status) {
		return dcRep.quickListByTypeStatus(type, status);
	}

	public List<Document> quickListByTypeStatusFieldValue(String type, String status, String fieldMeta, String fieldValue) {
		return dcRep.quickListByTypeStatusFieldValue(type, status, fieldMeta, fieldValue);
	}

	public List<Document> quickListByTypeStatusListFieldValue(String type, List<String> statusList, String fieldMeta, String fieldValue) {
		return dcRep.quickListByTypeStatusListFieldValue(type, statusList, fieldMeta, fieldValue);
	}

	public List<Document> quickListByTypeStatusParentFieldValue(String type, String status, String fieldMeta, String fieldValue) {
		return dcRep.quickListByTypeStatusParentFieldValue(type, status, fieldMeta, fieldValue);
	}

	public List<Document> quickListByTypeThingNoUnit(String metaname) {
		return dcRep.quickListByTypeThingNoUnit(metaname);
	}

	public List<Document> getQuickListByTypeThingNoUnitStatus(String type, String status) {
		return dcRep.quickListByTypeThingNoUnitStatus(type, status);
	}

	public List<Document> listByModelAndThingStatus(DocumentModel dm, String status) {
		return dcRep.listByModelAndThingStatus(dm, status);
	}

	public Document dirtyInsert(Document d, boolean fireEvent) {
		if (d.getId() == null) d.setId(UUID.randomUUID());

		dcRep.dirtyInsert(d.getId(), d.getMetaname(), d.getName(), d.getStatus(), d.getCreatedAt(), d.getUpdatedAt(), d.getCode(), d.getModel() == null ? null : d.getModel().getId(), d.getParent() == null ? null : d.getParent().getId(), d.getUser() == null ? null : d.getUser().getId(), d.getPerson() == null ? null : d.getPerson().getId(), fireEvent);
		return d;
	}

	public void removeById(UUID id) {
		dcRep.removeById(id);
	}

	public List<UUID> listIdsByModelMetanameAndTagIDAndNotStatus(String metaname, String tagid, String status) {
		return dcRep.listIdsByModelMetanameAndTagIDAndNotStatus(metaname, tagid, status);
	}

	public List<Document> customIncompleteQuickListOrphanByPersonTypeAndCode(String personModel, String personCode) {
		return dcRep.customIncompleteQuickListOrphanByPersonTypeAndCode(personModel, personCode);
	}

	public UUID quickFindIDByCodeAndModelMetanameAndPerson(String code, String modelMetaname, String person) {
		return dcRep.quickFindIDByCodeAndModelMetanameAndPerson(code, modelMetaname, person);
	}

	public void fireUpdate(Document d) {
		dcRep.fireEvent(new UpdateQualifier(), d);
	}

	public void fireInsert(Document d) {
		dcRep.fireEvent(new InsertQualifier(), d);
	}

	public Document refresh(Document d) {
		return dcRep.refresh(d);
	}

	public void flush() {
		dcRep.flush();
	}

	public Document findParent(UUID id) {
		if (id != null) {
			Document qp = quickFindParentDoc(id);

			if (qp != null)
				return findById(qp.getId());
		}
		return null;
	}

	public Document findParent(Document d) {
		return dcRep.findParent(d);
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public void completeInventory(UUID invId) {
		Document inv = dcRep.findById(invId);
		String codePat = "CNT" + inv.getCode().replace("INV", "") + "%03d-";

		diRep.quickRemoveByIds(inv.getItems().parallelStream().map(di -> di.getId()).collect(Collectors.toList()));
		inv.getItems().clear();
		int maxArea = inv.getSiblings().parallelStream()
						.mapToInt(ds -> Integer.parseInt(ds.getCode().replace("CNT" + inv.getCode().replace("INV", ""), "").substring(0, 3)))
						.distinct()
						.max()
						.getAsInt();

		logger.info("CodePat: " + codePat + " MaxArea: " + maxArea);
		for (int i = 1; i <= maxArea; i++) {
			final String codePfx = String.format(codePat, i);
			List<Document> contList = inv.getSiblings().parallelStream()
							.filter(ds -> ds.getCode().startsWith(codePfx))
							.sorted((ds1, ds2) -> {
								return ds2.getCode().compareTo(ds1.getCode());
							})
							.collect(Collectors.toList());
			Set<UUID> addSet = contList.parallelStream()
							.flatMap(d -> d.getSiblings().parallelStream())
							.flatMap(ds -> ds.getFields().stream())
							.filter(df -> df.getField().getMetaname().equals("INVADDRESS") && df.getValue() != null && !df.getValue().isEmpty())
							.map(df -> UUID.fromString(df.getValue()))
							.collect(Collectors.toSet());//Inicia com todos os endereços divergentes
			Set<UUID> addOkSet = new HashSet<>();

			logger.info("Code Prefix " + codePfx + ": " + contList.size() + " Addresses: " + addSet.size());
			while (!contList.isEmpty()) {
				Document doc1 = contList.remove(0);

				for (Document doc2 : contList) {
					logger.info("Comparing " + doc1.getCode() + " with " + doc2.getCode());
					InventoryComparison invComp = new InventoryComparison(doc1, doc2).compareAdd();

					addOkSet.addAll(invComp.getSibsOk().parallelStream()
									.flatMap(d -> d.getFields().stream())
									.filter(df -> df.getField().getMetaname().equals("INVADDRESS") && !df.getValue().isEmpty())
									.map(df -> UUID.fromString(df.getValue()))
									.collect(Collectors.toSet()));//Remove todos os endereços OK

					Map<Product, List<DocumentItem>> diMap = invComp.getSibsOk().parallelStream()
									.flatMap(d -> d.getItems().parallelStream())
									.collect(Collectors.groupingBy(di -> di.getProduct()));//Mapa de Produtos com Di para soma

					for (Entry<Product, List<DocumentItem>> e : diMap.entrySet()) {
						Product p = e.getKey();
						List<DocumentItem> diList = e.getValue();
						double qty = diList.parallelStream().mapToDouble(di -> di.getQty()).sum();
						Optional<DocumentItem> optPDi = inv.getItems().parallelStream()
										.filter(di -> di.getProduct().getId().equals(p.getId()))
										.findAny();
						DocumentItem pDi = optPDi.isPresent() ? optPDi.get() : new DocumentItem(inv, p, qty, "CALCULADO", diList.get(0).getMeasureUnit());

						if (optPDi.isPresent()) {
							pDi.setQty(pDi.getQty() + qty);
							inv.getItems().add(pDi);
						} else {
							diRep.persist(pDi);
							inv.getItems().add(pDi);
						}
					}
				}
				addSet.removeIf(a -> addOkSet.contains(a));
				logger.info("Div: " + addSet.size());
			}
		}
		diRep.multiPersist(inv.getItems());
		inv.setStatus("CONTADO");
		persist(inv);
	}

	public boolean contains(Document doc) {
		return dcRep.contains(doc);
	}

	public List<Document> listById(Collection<UUID> idList) {
		return dcRep.listById(idList);
	}

	public List<Document> listByPropertyValue(String fieldMeta, String value) {
		List<Document> ret = new ArrayList<>();
		List<Document> tmpList = dcRep.quickListByPropertyValue(fieldMeta, value);

		ret.addAll(dcRep.listById(tmpList.stream().map(d -> d.getId()).collect(Collectors.toList())));
		return ret;
	}

	public Document persist(Document d, boolean fireEvent) {
		dcRep.persist(d, fireEvent);
		return d;
	}

	public boolean checkExistence(DocumentModel recusaNF, String c) {
		Document d = dcRep.quickFindByCodeAndModelMetaname(c, recusaNF.getMetaname());
		return d != null;
	}

	public List<Document> listByThingId(UUID thingId) {
		List<Document> qList = dcRep.quickListByThingId(thingId);

		return dcRep.listById(qList.parallelStream().map(d -> d.getId()).collect(Collectors.toSet()));
	}

	public List<Document> quickListByThingId(UUID thingId) {
		return dcRep.quickListByThingId(thingId);
	}

	public List<Document> listByTransportThingId(UUID thId) {
		return dcRep.listByTransportThingId(thId);
	}

	public boolean isParentInitialized(Document doc) {
		return Hibernate.isInitialized(doc.getParent());
	}

	public List<Document> listByModelMeta(String type) {
		return dcRep.listByModelMeta(type);
	}

	public List<Document> listOrphanByPersonTypeAndCode(String personType, String personCode) {
		return dcRep.listOrphanByPersonTypeAndCode(personType, personCode);
	}

	public List<Document> listByTypeFieldValue(String model, String field, String value) {
		List<DocumentField> dfList = dfRep.listByModelMetaValue(field, value);
		List<Document> docList = dfList.parallelStream()
						.filter(df -> df.getDocument() != null)
						.map(df -> df.getDocument())
						.distinct()
						.collect(Collectors.toList());

		return docList.isEmpty() ? new ArrayList<Document>() : dcRep.listById(docList.parallelStream()
						.map(d -> d.getId())
						.distinct()
						.collect(Collectors.toList()))
						.parallelStream()
						.filter(d -> Documents.getStringField(d, field).equalsIgnoreCase(value))
						.collect(Collectors.toList());
	}

	public List<Document> getQuickChildrenListByTypeStatus(UUID fromString, String type, String status) {
		return getQuickChildrenListByTypeStatus(fromString, type, status);
	}

	public List<Document> getQuickChildrenListByType(UUID fromString, String type) {
		return getQuickChildrenListByType(fromString, type);
	}

	public List<Document> getQuickListOrphanedByType(String type) {
		return getQuickListOrphanedByType(type);
	}

	public List<Document> quickListByPropertyValue(String fieldMeta, String value) {
		return dcRep.quickListByPropertyValue(fieldMeta, value);
	}

	public List<Document> listByTypeFrom(String model, Date created) {
		return dcRep.listByTypeFrom(model, created);
	}
}