package com.gtp.hunter.process.service.solar;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.hibernate.Hibernate;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.common.util.RestUtil;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.jsonstubs.AGLAddress;
import com.gtp.hunter.process.jsonstubs.AGLAddressProps;
import com.gtp.hunter.process.jsonstubs.AGLCustomer;
import com.gtp.hunter.process.jsonstubs.AGLDocModelComboItem;
import com.gtp.hunter.process.jsonstubs.AGLDocModelField;
import com.gtp.hunter.process.jsonstubs.AGLDocModelForm;
import com.gtp.hunter.process.jsonstubs.AGLDocModelItem;
import com.gtp.hunter.process.jsonstubs.AGLDocModelProps;
import com.gtp.hunter.process.jsonstubs.AGLDocTransport;
import com.gtp.hunter.process.jsonstubs.AGLProd;
import com.gtp.hunter.process.jsonstubs.AGLProdModel;
import com.gtp.hunter.process.jsonstubs.AGLThing;
import com.gtp.hunter.process.jsonstubs.AGLTruck;
import com.gtp.hunter.process.jsonstubs.AGLUnit;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.AddressField;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.DocumentTransport;
import com.gtp.hunter.process.model.Person;
import com.gtp.hunter.process.model.PersonField;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductField;
import com.gtp.hunter.process.model.ProductModel;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.service.RegisterService;

@Stateless
public class AGLConvertService {

	private static SimpleDateFormat	sdf	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Inject
	private RegisterService			regSvc;

	@Inject
	private transient Logger		logger;

	public AGLDocModelForm convertDocToAgl(Document doc) {

		if (doc != null && doc.getId() != null) {
			Profiler prof = new Profiler("Convert Test Lazy");
			AGLDocModelForm ret = new AGLDocModelForm();

			ret = convertDocToAgl(doc, null);
			prof.done("Converted", false, false).forEach(logger::info);
			return ret;
		}
		return null;
	}

	public AGLDocModelForm convertDocToAgl(Document doc, UUID parentId) {
		AGLDocModelForm translate = new AGLDocModelForm();

		translate.setId(doc.getId() == null ? null : doc.getId().toString());
		translate.setName(doc.getName());
		// translate.setCreatedAt(d.getCreatedAt());
		try {
			translate.setCreated_at(sdf.format(doc.getCreatedAt()));
		} catch (NullPointerException npe) {
			logger.error("Invalid Date: " + new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create().toJson(doc));
		}
		translate.setUpdated_at(sdf.format(doc.getUpdatedAt()));
		translate.setMetaname(doc.getModel().getMetaname());
		translate.setStatus(doc.getStatus());
		if (doc.getModel().getMetaname().equals("APOCHEGADA")) {
			translate.setMetaname("TRANSPORT_STEP");
			translate.setStatus("CAMINHAO NA PORTARIA");
		} else if (doc.getModel().getMetaname().equals("APOCHAMADA")) {
			translate.setMetaname("TRANSPORT_STEP");
			translate.setStatus("CAMINHAO NA ENTRADA");
		} else if (doc.getModel().getMetaname().equals("APOENTRADA")) {
			translate.setMetaname("TRANSPORT_STEP");
			translate.setStatus("CAMINHAO NO PATIO");
		} else if (doc.getModel().getMetaname().equals("APODOCA")) {
			translate.setMetaname("TRANSPORT_STEP");
			translate.setStatus("CAMINHAO NA DOCA");
		} else if (doc.getModel().getMetaname().equals("APODESCARGA")) {
			translate.setMetaname("TRANSPORT_STEP");
			translate.setStatus("CAMINHAO DESCARREGADO");
		} else if (doc.getModel().getMetaname().equals("APOSAIDA")) {
			translate.setMetaname("TRANSPORT_STEP");
			translate.setStatus("CAMINHAO NA RUA");
		} else if (doc.getModel().getMetaname().equals("TRANSPORT") && doc.getStatus().equals("LIBERADO")) {
			translate.setStatus("CAMINHAO NA RUA");
		}
		translate.setCode(doc.getCode());
		if (doc.getParent_id() == null) {
			Hibernate.initialize(doc.getParent());
		}
		translate.setParent_id(doc.getParent_id());
		if (doc.getPerson() != null) {
			translate.setPerson_id(doc.getPerson().getId().toString());
		}

		for (DocumentField df : doc.getFields()) {
			DocumentModelField dmf = df.getField();
			AGLDocModelField admf = new AGLDocModelField();

			admf.setAttrib(dmf.getMetaname().toLowerCase());
			admf.setName(dmf.getName());
			admf.setType(dmf.getType().toString());
			admf.setOrdem(dmf.getOrdem());
			if (dmf.getParams() != null) {
				Gson g = new Gson();
				Type type = new TypeToken<List<AGLDocModelComboItem>>() {
				}.getType();
				List<AGLDocModelComboItem> lst = g.fromJson(dmf.getParams(), type);
				admf.setOptions(lst);
			}
			admf.setValue(df.getValue());
			translate.getProps().put(dmf.getMetaname().toLowerCase(), df.getValue());
			translate.getModel().add(admf);
		}

		for (DocumentModelField dmf : doc.getModel().getFields()) {
			if (!translate.getProps().containsKey(dmf.getMetaname().toLowerCase())) {
				AGLDocModelField admf = new AGLDocModelField();

				admf.setAttrib(dmf.getMetaname().toLowerCase());
				admf.setName(dmf.getName());
				admf.setType(dmf.getType().toString());
				admf.setOrdem(dmf.getOrdem());
				admf.setValue("");
				if (dmf.getParams() != null) {
					Gson g = new Gson();
					Type type = new TypeToken<List<AGLDocModelComboItem>>() {
					}.getType();
					List<AGLDocModelComboItem> lst = g.fromJson(dmf.getParams(), type);
					admf.setOptions(lst);
				}
				translate.getProps().put(dmf.getMetaname().toLowerCase(), "");
				translate.getModel().add(admf);
			}
		}
		logger.debug("Quantidade de Campos Preenchidos: " + translate.getModel().size());
		for (DocumentItem di : doc.getItems()) {
			AGLDocModelItem admi = new AGLDocModelItem();
			Product p = di.getProduct();

			admi.setProduct_id(p.getId().toString());
			admi.setUnit_measure(di.getMeasureUnit());
			admi.setQty(BigDecimal.valueOf(di.getQty()));
			if (di.getProperties().containsKey("SEQ") && !di.getProperties().get("SEQ").isEmpty())
				admi.setSeq(Integer.parseInt(di.getProperties().get("SEQ")));
			if (di.getProperties().containsKey("LAYER") && !di.getProperties().get("LAYER").isEmpty())
				admi.setLayer(Integer.parseInt(di.getProperties().get("LAYER")));

			translate.getItems().add(admi);
			translate.getResources().getProducts().add(convertProdToAGL(di.getProduct()));
		}

		for (DocumentThing dt : doc.getThings()) {
			if (dt != null && dt.getThing() != null) {
				AGLAddressProps prop = convertAddressToAGLProps(dt.getThing().getAddress());
				Thing t = dt.getThing();

				for (Thing ts : t.getSiblings()) {
					AGLAddressProps sibProp = convertAddressToAGLProps(ts.getAddress());

					translate.getResources().getProducts().add(convertProdToAGL(ts.getProduct()));
					if (sibProp != null) {
						translate.getAddresses().add(sibProp);
						translate.getResources().getAddresses().add(sibProp);
					}
				}
				translate.getThings().add(convertThingToAGL(t));
				if (prop != null) {
					translate.getAddresses().add(prop);
					translate.getResources().getAddresses().add(prop);
				}
			}
		}
		for (DocumentTransport dtr : doc.getTransports()) {
			AGLAddressProps prop = convertAddressToAGLProps(dtr.getAddress());

			//			if (dtr.getThing() != null && dtr.getThing().getAddress() != null && dtr.getAddress() != null && !dtr.getThing().getAddress().getId().equals(dtr.getAddress().getId()))
			translate.getTransport().add(convertTransportToAGLTransport(dtr));
			if (prop != null) {
				translate.getAddresses().add(prop);
				translate.getResources().getAddresses().add(prop);
			}
		}
		for (Document filho : doc.getSiblings()) {
			translate.getSiblings().add(convertDocToAgl(filho, doc.getId()));
		}
		return translate;
	}

	public AGLDocTransport convertTransportToAGLTransport(DocumentTransport dTr) {
		AGLDocTransport aglTr = new AGLDocTransport();

		aglTr.setThing_id(dTr.getThing().getId().toString());
		aglTr.setAddress_id(dTr.getAddress().getId().toString());
		aglTr.setOrigin_id(dTr.getOrigin() == null ? null : dTr.getOrigin().getId().toString());
		aglTr.setSeq(dTr.getSeq());
		return aglTr;
	}

	public AGLProdModel convertProdModelToAGL(ProductModel pm) {

		AGLProdModel ret = new AGLProdModel();
		ret.setCreated_at(sdf.format(pm.getCreatedAt()));
		ret.setUpdated_at(sdf.format(pm.getUpdatedAt()));
		ret.setId(pm.getId().toString());
		ret.setMetaname(pm.getMetaname());
		ret.setName(pm.getName());
		if (pm.getParent() != null) {
			ret.setParent_id(pm.getParent().getId().toString());
		}
		ret.setStatus(pm.getStatus());
		ret.setProps(pm.getProperties());
		return ret;
	}

	public AGLProd convertProdToAGL(Product pm) {
		AGLProd ret = new AGLProd();
		ret.setCreated_at(sdf.format(pm.getCreatedAt()));
		ret.setUpdated_at(sdf.format(pm.getUpdatedAt()));
		ret.setId(pm.getId().toString());
		ret.setMetaname("PRODUCT");
		ret.setName(pm.getName());
		ret.setSku(pm.getSku());
		ret.setProductmodel_id(pm.getModel().getId().toString());
		if (pm.getParent() != null) {
			ret.setParent_id(pm.getParent().getId().toString());
		}
		for (ProductField pf : pm.getFields()) {
			if (pf.getModel() != null) {
				ret.getProps().put(pf.getModel().getMetaname().toLowerCase(), pf.getValue());
			}
		}
		ret.setStatus(pm.getStatus());
		return ret;
	}

	public AGLAddressProps convertAddressToAGLProps(Address a) {
		if (a == null || a.getId() == null) return null;
		AGLAddressProps ret = new AGLAddressProps();

		//		a = rSvc.getAddRep().findById(a.getId());
		//		Hibernate.initialize(a.getModel());
		ret.setId(a.getId() == null ? null : a.getId().toString());
		ret.setName(a.getName());
		ret.setMetaname(a.getModel().getMetaname());
		if (a.getParent() != null)
			ret.setParent_id(a.getParent().getId().toString());
		else
			ret.setParent_id("VAZIO?");
		return ret;
	}

	public AGLAddress convertAddressToAGL(Address a) {
		AGLAddress ret = new AGLAddress();

		//		a = rSvc.getAddRep().findById(a.getId());
		//		Hibernate.initialize(a.getParent());
		//		Hibernate.initialize(a.getFields());
		ret.setId(a.getId());
		ret.setMetaname(a.getModel().getMetaname());
		ret.setCreated_at(sdf.format(a.getCreatedAt()));
		ret.setUpdated_at(sdf.format(a.getUpdatedAt()));
		ret.setStatus(a.getStatus());
		ret.setParent_id(a.getParent() == null ? null : a.getParent().getId());
		ret.setName(a.getName());
		for (AddressField af : a.getFields()) {
			String amfMeta = af.getModel().getMetaname().toLowerCase();

			ret.getProperties().put(amfMeta, af.getValue());
		}
		for (Address sib : a.getSiblings()) {
			ret.getSiblings().add(convertAddressToAGL(sib));
		}
		return ret;
	}

	public AGLDocModelProps convertPropDocToAgl(Document doc) {
		AGLDocModelProps translate = new AGLDocModelProps();
		if (doc == null) return translate;
		if (doc.getId() != null) translate.setId(doc.getId().toString());
		translate.setName(doc.getName());
		// translate.setCreatedAt(d.getCreatedAt());
		translate.setCreated_at(sdf.format(doc.getCreatedAt()));
		translate.setUpdated_at(sdf.format(doc.getUpdatedAt()));
		translate.setCode(doc.getCode());
		if (doc.getModel().getMetaname().equals("APOCHEGADA")) {
			translate.setMetaname("TRANSPORT_STEP");
			translate.setStatus("CAMINHAO NA PORTARIA");
		} else if (doc.getModel().getMetaname().equals("APOCHAMADA")) {
			translate.setMetaname("TRANSPORT_STEP");
			translate.setStatus("CAMINHAO FORA DA DOCA");//???
		} else if (doc.getModel().getMetaname().equals("APOENTRADA")) {
			translate.setMetaname("TRANSPORT_STEP");
			translate.setStatus("CAMINHAO NO PATIO");
		} else if (doc.getModel().getMetaname().equals("APODOCA")) {
			translate.setMetaname("TRANSPORT_STEP");
			translate.setStatus("CAMINHAO NA DOCA");
		} else if (doc.getModel().getMetaname().equals("APOLIBERACAO")) {
			translate.setMetaname("TRANSPORT_STEP");
			translate.setStatus("CAMINHAO FORA DA DOCA");
		} else if (doc.getModel().getMetaname().equals("APOSAIDA")) {
			translate.setMetaname("TRANSPORT_STEP");
			translate.setStatus("CAMINHAO NA RUA");
		} else {
			translate.setMetaname(doc.getModel().getMetaname());
			translate.setStatus(doc.getStatus());
		}
		Document parent = regSvc.getDcSvc().quickFindParentDoc(doc.getId().toString());
		if (parent != null) translate.setParent_id(parent.getId().toString());
		for (DocumentModelField dmf : doc.getModel().getFields()) {
			AGLDocModelField admf = new AGLDocModelField();

			admf.setAttrib(dmf.getMetaname().toLowerCase());
			admf.setName(dmf.getName());
			admf.setType(dmf.getType().toString());
			admf.setOrdem(dmf.getOrdem());
			if (dmf.getParams() != null) {
				Gson g = new Gson();
				Type type = new TypeToken<List<AGLDocModelComboItem>>() {
				}.getType();
				List<AGLDocModelComboItem> lst = g.fromJson(dmf.getParams(), type);
				admf.setOptions(lst);
			}
			for (DocumentField df : doc.getFields()) {
				if (df.getField().getId().equals(dmf.getId())) {
					admf.setValue(df.getValue());
					break;
				}
			}
			translate.getProps().add(admf);
		}
		return translate;
	}

	public AGLTruck convertThingToAGLTruck(Thing t) {
		AGLTruck ret = new AGLTruck();

		ret.setId(t.getId().toString());
		ret.setCreated_at(sdf.format(t.getCreatedAt() == null ? Calendar.getInstance() : t.getCreatedAt()));
		ret.setUpdated_at(sdf.format(t.getUpdatedAt() == null ? Calendar.getInstance() : t.getUpdatedAt()));
		ret.setMetaname("TRUCK");
		ret.setName(t.getName());
		for (Property prop : t.getProperties()) {
			if (prop.getField() != null && prop.getField().getMetaname() != null && prop.getValue() != null) {
				ret.getProps().put(prop.getField().getMetaname().toLowerCase(), prop.getValue());
			}
		}
		ret.setStatus(t.getStatus());
		return ret;
	}

	public AGLCustomer convertPersonToAGLCustomer(Person ps) {
		AGLCustomer ret = new AGLCustomer();

		ret.setId(ps.getId() != null ? ps.getId().toString() : null);
		ret.setName(ps.getName());
		ret.setMetaname(ps.getModel().getMetaname());
		ret.setStatus(ps.getStatus());
		ret.setCreated_at(sdf.format(ps.getCreatedAt() == null ? Calendar.getInstance() : ps.getCreatedAt()));
		ret.setUpdated_at(sdf.format(ps.getUpdatedAt() == null ? Calendar.getInstance() : ps.getUpdatedAt()));
		for (PersonField psf : ps.getFields()) {
			if (psf.getField() != null && psf.getField().getMetaname() != null && psf.getValue() != null) {
				ret.getProps().put(psf.getField().getMetaname().toLowerCase(), psf.getValue());
			}
		}
		ret.getProps().put("cnpj", ps.getCode());
		return ret;
	}

	public AGLThing convertThingToAGL(Thing t) {
		if (t == null || t.getId() == null) return null;
		AGLThing ret = new AGLThing();
		SimpleDateFormat propParser = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat propParserT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		SimpleDateFormat propFormatter = new SimpleDateFormat("yyyy-MM-dd");

		ret.setId(t.getId().toString());
		ret.setStatus(t.getStatus());
		ret.setCreated_at(sdf.format(t.getCreatedAt() == null ? Calendar.getInstance().getTime() : t.getCreatedAt()));
		ret.setUpdated_at(sdf.format(t.getUpdatedAt() == null ? Calendar.getInstance().getTime() : t.getUpdatedAt()));
		ret.setMetaname("THINGS");
		ret.setName(t.getName());
		ret.setProduct_id(t.getProduct().getId().toString());
		ret.setAddress_id(t.getAddress() == null || t.getAddress().getId() == null ? null : t.getAddress().getId().toString());
		ret.setParent_id(t.getParent() == null || t.getParent().getId() == null ? null : t.getParent().getId().toString());
		for (UUID uId : t.getUnits()) {
			Unit u = regSvc.getUnSvc().findById(uId);

			if (u != null) ret.getUnits().add(convertUnitToAGL(u));
		}
		for (Property prop : t.getProperties()) {
			if (prop != null && prop.getField() != null && prop.getField().getMetaname() != null && ret.getProps() != null)
				try {
					Date dt = prop.getValue().contains("/") ? propParser.parse(prop.getValue()) : propParserT.parse(prop.getValue());

					ret.getProps().put(prop.getField().getMetaname().toLowerCase(), propFormatter.format(dt));
				} catch (ParseException pe) {
					ret.getProps().put(prop.getField().getMetaname().toLowerCase(), prop.getValue());
				}
			else
				logger.warn("Something is null: " + new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().create().toJson(t));
		}
		for (Thing th : t.getSiblings())
			ret.getSiblings().add(convertThingToAGL(th));
		return ret;
	}

	public AGLUnit convertUnitToAGL(Unit u) {
		AGLUnit ret = new AGLUnit();

		ret.setId(u.getId().toString());
		ret.setType(u.getType().toString());
		ret.setValue(u.getTagId());
		return ret;
	}

	public Document convertAGLDocToDoc(AGLDocModelForm translate, boolean fireEvent) {
		UUID id = UUID.fromString(translate.getId());
		Document d = new Document();
		Document tmpD = regSvc.getDcSvc().findById(id);

		if (translate.getMetaname().equals("TRANSPORT_STEP") && translate.getStatus().equals("CAMINHAO NA PORTARIA")) {
			translate.setMetaname("APOCHEGADA");
			translate.setStatus("NOVO");
		} else if (translate.getMetaname().equals("TRANSPORT_STEP") && translate.getStatus().equals("CAMINHAO NA ENTRADA")) {
			translate.setMetaname("APOCHAMADA");
			translate.setStatus("NOVO");
		} else if (translate.getMetaname().equals("TRANSPORT_STEP") && translate.getStatus().equals("CAMINHAO NO PATIO")) {
			translate.setMetaname("APOENTRADA");
			translate.setStatus("NOVO");
		} else if (translate.getMetaname().equals("TRANSPORT_STEP") && translate.getStatus().equals("CAMINHAO NA DOCA")) {
			translate.setMetaname("APODOCA");
			translate.setStatus("NOVO");
		} else if (translate.getMetaname().equals("TRANSPORT_STEP") && translate.getStatus().equals("CAMINHAO FORA DA DOCA")) {
			translate.setMetaname("APOLIBERACAO");
			translate.setStatus("NOVO");
		} else if (translate.getMetaname().equals("TRANSPORT_STEP") && translate.getStatus().equals("CAMINHAO NA RUA")) {
			translate.setMetaname("APOSAIDA");
			translate.setStatus("NOVO");
		}

		if (tmpD != null) {
			d = tmpD;
		} else {
			DocumentModel dm = regSvc.getDmSvc().findByMetaname(translate.getMetaname());

			if (translate.getParent_id() != null && !translate.getParent_id().isEmpty()) {
				Document parent = regSvc.getDcSvc().findById(UUID.fromString(translate.getParent_id()));
				d.setParent(parent);
			}

			if (translate.getPerson_id() != null && !translate.getPerson_id().isEmpty()) {
				Person person = regSvc.getPsSvc().findById(UUID.fromString(translate.getPerson_id()));
				d.setPerson(person);
			}
			if (translate.getUser_id() != null && !translate.getUser_id().isEmpty()) {
				User us = regSvc.getUsrSvc().findById(UUID.fromString(translate.getUser_id()));
				d.setUser(us);
			}
			d.setId(id);
			d.setModel(dm);
			d.setCode(translate.getCode());
			d.setName(translate.getName());
			d.setStatus(translate.getStatus());
			d.setCreatedAt(translate.getCreatedAt());
			d.setUpdatedAt(translate.getUpdatedAt());
			if (translate.getMetaname().equalsIgnoreCase("ORDCONF")) {
				d.setMetaname(null);
				d.setName("Ordem de Conferência " + d.getParent().getCode());
				d.setCode("CONF" + d.getParent().getCode());
				if (translate.getItems() == null) translate.setItems(new ArrayList<AGLDocModelItem>());
				if (translate.getItems().isEmpty()) {
					List<Document> dNFs = d.getParent().getSiblings().stream().filter(tSib -> tSib.getModel().getMetaname().equals("NFENTRADA")).collect(Collectors.toList());
					Map<UUID, DocumentItem> docItemByProduct = new HashMap<>();

					for (Document nf : dNFs) {
						for (DocumentItem nfItem : nf.getItems()) {
							Product pnf = nfItem.getProduct();

							if (docItemByProduct.containsKey(pnf.getId())) {
								double qty = docItemByProduct.get(pnf.getId()).getQty();
								docItemByProduct.get(pnf.getId()).setQty(qty + nfItem.getQty());
							} else {
								docItemByProduct.put(pnf.getId(), new DocumentItem(d, pnf, nfItem.getQty(), nfItem.getStatus()));
							}
						}
					}
					for (UUID pId : docItemByProduct.keySet()) {
						d.getItems().add(docItemByProduct.get(pId));
					}
				}
			} else if (translate.getMetaname().equals("RETORDCONF")) {
				d.setMetaname(null);
				d.setName("Retorno da Ordem de Conferência " + d.getParent().getCode().replace("CONF", ""));
			} else if (translate.getMetaname().equals("ORDMOV")) {
				d.setMetaname(null);
				d.setName("Ordem de Movimentação " + d.getCode());
			}
		}

		for (AGLThing th : translate.getThings()) {
			Thing tTmp = convertAGLThingToThing(th);
			final Thing t = regSvc.getThSvc().findById(tTmp.getId());
			Optional<DocumentThing> optDT = d.getThings().stream().filter(dtp -> dtp.getThing().getId().equals(t.getId())).findFirst();
			DocumentThing dt = optDT.isPresent() ? optDT.get() : new DocumentThing(d, t, t.getStatus());

			d.getThings().add(dt);
		}

		for (AGLDocModelItem item : translate.getItems()) {
			Product p = regSvc.getPrdSvc().findById(UUID.fromString(item.getProduct_id()));
			if (p != null) {
				List<DocumentItem> diLst = d.getItems().stream().filter(di -> di.getProduct().getId().equals(p.getId())).collect(Collectors.toList());

				if (diLst.isEmpty() || !(translate.getMetaname().equals("ORDCONF") && diLst.stream().anyMatch(di -> di.getQty() == item.getQty().doubleValue()))) {
					DocumentItem di = new DocumentItem(d, p, item.getQty().doubleValue(), "NOVO");

					di.setMeasureUnit(item.getUnit_measure());
					d.getItems().add(di);
				}
			}

		}

		for (String key : translate.getProps().keySet()) {
			String value = translate.getProps().get(key);
			DocumentModelField dmf = regSvc.getDmfSvc().findByModelAndMetaname(d.getModel(), key.toUpperCase());

			if (dmf != null) {
				Optional<DocumentField> optDF = d.getFields().stream().filter(df -> df.getField().getId().equals(dmf.getId())).findFirst();
				DocumentField df = optDF.isPresent() ? optDF.get() : new DocumentField(d, dmf, "INTWMS", value);

				d.getFields().add(df);
			} else {
				logger.warn("DocumentModelField " + key + " does not exist");
			}
		}

		for (AGLDocTransport transp : translate.getTransport()) {
			try {
				Thing pallet = regSvc.getThSvc().findById(UUID.fromString(transp.getThing_id()));
				Address destination = regSvc.getAddSvc().findById(UUID.fromString(transp.getAddress_id()));
				Optional<DocumentTransport> optTransport = d.getTransports().stream().filter(dtr -> dtr.getSeq() == transp.getSeq()).findFirst();
				DocumentTransport dtr = null;

				if (optTransport.isPresent())
					dtr = optTransport.get();
				else
					dtr = new DocumentTransport();
				dtr.setSeq(transp.getSeq());
				dtr.setThing(pallet);
				dtr.setAddress(destination);
				d.getTransports().add(dtr);
			} catch (IllegalArgumentException | NullPointerException npe) {
				logger.error("Error translating transport");
				logger.trace("Error translating transport", npe);
			}
		}

		for (AGLDocModelForm filho : translate.getSiblings()) {
			convertAGLDocToDoc(filho, false);
		}
		return regSvc.getDcSvc().refresh(regSvc.getDcSvc().dirtyFullInsert(d, fireEvent));
	}

	@Transactional(value = TxType.MANDATORY)
	public DocumentTransport convertAGLTransportToTransport(AGLDocTransport agldTr, UUID docId) {
		List<DocumentTransport> dTrList = regSvc.getDtrSvc().listByDocumentId(docId);
		Optional<DocumentTransport> optDtrTransp = dTrList.stream().filter(dTr -> dTr.getSeq() == agldTr.getSeq()).findFirst();
		DocumentTransport ret;

		if (optDtrTransp.isPresent()) {
			ret = optDtrTransp.get();
		} else {
			UUID dtrId = UUID.randomUUID();
			regSvc.getDtrSvc().dirtyInsert(dtrId, null, null, "NOVO", Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), agldTr.getSeq(), docId, UUID.fromString(agldTr.getThing_id()), UUID.fromString(agldTr.getAddress_id()), (agldTr.getOrigin_id() == null || agldTr.getOrigin_id().isEmpty()) ? null : UUID.fromString(agldTr.getOrigin_id()));
			ret = regSvc.getDtrSvc().findById(dtrId);
		}

		return ret;
	}

	@Transactional(value = TxType.MANDATORY)
	public Thing convertAGLThingToThing(AGLThing ts) {
		boolean novo = false;
		Thing ret;
		Thing tmpT = ts.getId() == null || ts.getId().isEmpty() ? null : regSvc.getThSvc().findById(UUID.fromString(ts.getId()));

		if (tmpT != null) {
			ret = tmpT;
		} else {
			Product p = regSvc.getPrdSvc().findById(UUID.fromString(ts.getProduct_id()));

			novo = true;
			if (p != null) {
				PropertyModel prm = p.getModel().getPropertymodel();

				ret = new Thing(ts.getName(), p, prm, ts.getStatus());
				if (ts.getId() == null || ts.getId().isEmpty())
					ret.setId(UUID.randomUUID());
				else
					ret.setId(UUID.fromString(ts.getId()));
			} else {
				logger.error("Product " + ts.getProduct_id() + " does not exist on this database");
				return null;
			}
		}
		if (ts.getParent_id() != null && !ts.getParent_id().isEmpty()) ret.setParent(regSvc.getThSvc().findById(UUID.fromString(ts.getParent_id())));
		ret.setName(ts.getName());
		ret.setMetaname(ts.getMetaname());
		ret.setStatus(ts.getStatus());
		ret.setCreatedAt(ts.getCreatedAt());
		ret.setUpdatedAt(ts.getUpdatedAt());
		if (ts.getAddress_id() != null) {
			Address loc = regSvc.getAddSvc().findById(UUID.fromString(ts.getAddress_id()));

			ret.setAddress(loc);
		}
		for (String key : ts.getProps().keySet()) {
			try {
				String value = ts.getProps().get(key);
				Optional<Property> optProp = ret.getProperties().stream()
								.filter(pr -> pr != null && pr.getField() != null && pr.getField().getMetaname() != null && pr.getField().getMetaname().equals(key.toUpperCase()))
								.findFirst();

				if (optProp.isPresent())
					optProp.get().setValue(value);
				else {
					PropertyModelField prmf = ret.getModel().getFields().stream().filter(mf -> mf.getMetaname().equalsIgnoreCase(key)).findAny().orElse(null);

					if (prmf != null) {
						Property pr = new Property(ret, prmf, value);

						ret.getProperties().add(pr);
					} else
						logger.error("PRMF is null! " + key);
				}
			} catch (Exception e) {
				logger.error("Error converting " + key + " from propertymodel " + ret.getModel().getName());
				logger.error(e.getLocalizedMessage(), e);
			}
		}

		for (AGLThing tsib : ts.getSiblings()) {
			tsib.setParent_id(ts.getId());
			ret.getSiblings().add(convertAGLThingToThing(tsib));
		}
		if (novo) {
			return regSvc.getThSvc().refresh(regSvc.getThSvc().dirtyInsert(ret));
		} else
			return regSvc.getThSvc().persist(ret);
	}

	public Future<IntegrationReturn> sendAddressToWMS(Address add, String verb) {
		AGLAddress addr = convertAddressToAGL(add);
		String text = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(addr);

		return sendToWMS("com.wms.comunicador.aaddress", text, verb, addr.getId().toString());
	}

	public Future<IntegrationReturn> sendDocToWMS(Document doc, String verb) {
		final Map<Document, List<DocumentItem>> origNFItems = new HashMap<>();

		if (doc.getModel().getMetaname().equals("TRANSPORT")) {
			Map<UUID, List<DocumentItem>> itemNFSMap = null;

			Optional<Document> optNFEntrada = doc.getSiblings().stream()
							.filter(ds -> ds.getModel().getMetaname().equals("NFENTRADA") && !Documents.getStringField(ds, "ZTRANS", "").equalsIgnoreCase("N"))
							.sorted((Document d1, Document d2) -> d1.getCode().compareToIgnoreCase(d2.getCode()))
							.findFirst();
			Optional<Document> optNFSaida = doc.getSiblings().stream()
							.filter(ds -> ds.getModel().getMetaname().equals("NFSAIDA") && !Documents.getStringField(ds, "ZTRANS", "").equalsIgnoreCase("N"))
							.sorted((Document d1, Document d2) -> d1.getCode().compareToIgnoreCase(d2.getCode()))
							.findFirst();

			origNFItems.putAll(doc.getSiblings().parallelStream()
							.filter(ds -> ds.getModel().getMetaname().equals("NFENTRADA") || ds.getModel().getMetaname().equals("NFSAIDA"))
							.sorted((Document d1, Document d2) -> d1.getCode().compareToIgnoreCase(d2.getCode()))
							.flatMap(ds -> ds.getItems().parallelStream())
							.collect(Collectors.groupingBy(di -> di.getDocument())));

			if (optNFSaida.isPresent()) {
				Set<DocumentItem> toReplace = new HashSet<>();
				Document nfs = optNFSaida.get();

				itemNFSMap = doc.getSiblings().stream()
								.filter(ds -> ds.getModel().getMetaname().equals("NFSAIDA") && !Documents.getStringField(ds, "ZTRANS", "").equalsIgnoreCase("N"))
								.flatMap(nf -> nf.getItems().stream().filter(di -> di.getStatus() == null || !di.getStatus().equals("CONVERTED")))
								.collect(Collectors.groupingBy(di -> di.getProduct().getId()));
				for (UUID pId : itemNFSMap.keySet()) {
					List<DocumentItem> diList = itemNFSMap.get(pId);
					Product p = diList.stream().map(di -> di.getProduct()).findFirst().get();
					Double qty = diList.stream().mapToDouble(di -> di.getQty()).sum();
					Optional<ProductField> optPFUnitBox = p.getFields().stream().filter(prdField -> prdField.getModel().getMetaname().equalsIgnoreCase("UNIT_BOX") && prdField.getValue() != null && !prdField.getValue().isEmpty()).findFirst();

					if (optPFUnitBox.isPresent()) { //FAZER A CONVERSÃO DE UNIDADES PARA CAIXAS
						Optional<ProductField> optPFPalletBox = p.getFields().stream().filter(prdField -> prdField.getModel().getMetaname().equalsIgnoreCase("PALLET_BOX") && prdField.getValue() != null && !prdField.getValue().isEmpty()).findFirst();
						ProductField unitBoxField = optPFUnitBox.get();
						int unitBox = Integer.parseInt(unitBoxField.getValue());
						Double boxQuantity = qty / unitBox;

						if (optPFPalletBox.isPresent()) { //CALCULAR SE PALETES COMPELTOS
							StringBuilder messageLog = new StringBuilder();
							ProductField palletBoxField = optPFPalletBox.get();
							int palletBox = Integer.parseInt(palletBoxField.getValue());
							Double palletQuantity = boxQuantity / palletBox;

							messageLog.append("Product ").append(p.getSku()).append(" - ").append(p.getName());
							messageLog.append(" UNITS/BOX ").append(unitBox);
							messageLog.append(" BOX/PALLET ").append(palletBox);
							messageLog.append(" UNITS: ").append(qty);
							messageLog.append(" BOXES: ").append(boxQuantity);
							messageLog.append(" PALLETS: ").append(palletQuantity);
							if (palletQuantity.doubleValue() == Math.floor(palletQuantity)) {//Pallet Incompleto
								messageLog.append(" COMPLETE: ").append(palletQuantity.intValue());
							} else {
								messageLog.append(" INCOMPLETE: ").append(palletQuantity.intValue()).append(" + ").append((palletQuantity - palletQuantity.intValue()));
							}
							logger.info(messageLog.toString());
							if (ConfigUtil.get("hunter-custom-solar", "round-pallets", "false").equalsIgnoreCase("TRUE"))
								boxQuantity = Math.floor(palletQuantity) * palletBox;

							toReplace.add(new DocumentItem(nfs, p, boxQuantity, "CONVERTED"));
						}
					}
				}
				if (toReplace.size() > 0) nfs.setItems(toReplace);
				doc.getSiblings().removeIf(ds -> ds.getModel().getMetaname().equals(nfs.getModel().getMetaname()) && !ds.getId().equals(nfs.getId()));
			}
			if (optNFEntrada.isPresent()) {
				Set<DocumentItem> toReplace = new HashSet<>();
				Document nfe = optNFEntrada.get();

				//com conversao
				itemNFSMap = doc.getSiblings().stream()
								.filter(ds -> ds.getModel().getMetaname().equals("NFENTRADA") && !Documents.getStringField(ds, "ZTRANS", "").equalsIgnoreCase("N"))
								.filter(nf -> nf.getPerson() != null && (nf.getPerson().getCode().startsWith("07196033") || nf.getPerson().getCode().startsWith("08715757") || nf.getPerson().getCode().startsWith("10557540")))
								.flatMap(nf -> nf.getItems().stream().filter(di -> di.getStatus() == null || !di.getStatus().equals("CONVERTED")))
								.collect(Collectors.groupingBy(di -> di.getProduct().getId()));
				for (UUID pId : itemNFSMap.keySet()) {
					List<DocumentItem> diList = itemNFSMap.get(pId);
					DocumentItem nfItem = diList.stream().findAny().orElse(null);
					Product p = diList.stream().map(di -> di.getProduct()).findAny().get();
					Double qty = diList.stream().mapToDouble(di -> di.getQty()).sum();
					Optional<ProductField> optPFUnitBox = p.getFields().stream().filter(prdField -> prdField.getModel().getMetaname().equalsIgnoreCase("UNIT_BOX") && prdField.getValue() != null && !prdField.getValue().isEmpty()).findFirst();
					String propMultip = nfItem.getProperties().containsKey("FATOR_MULTIPLICATIVO") ? nfItem.getProperties().get("FATOR_MULTIPLICATIVO") : "0";

					if (!propMultip.isEmpty() && !propMultip.equals("0"))
						qty *= Integer.parseInt(propMultip);
					if (optPFUnitBox.isPresent()) { //FAZER A CONVERSÃO DE UNIDADES PARA CAIXAS
						StringBuilder messageLog = new StringBuilder();
						ProductField unitBoxField = optPFUnitBox.get();
						int unitBox = Integer.parseInt(unitBoxField.getValue());
						Double boxQuantity = qty / unitBox;

						messageLog.append("Product " + p.getSku() + " - " + p.getName());
						messageLog.append(" UNITS/BOX " + unitBox);
						messageLog.append(" UNITS: " + qty);
						messageLog.append(" BOXES: " + boxQuantity);
						if (boxQuantity.doubleValue() == Math.floor(boxQuantity)) {//Pallet Incompleto
							messageLog.append(" COMPLETE: " + boxQuantity.intValue());
						} else {
							messageLog.append(" INCOMPLETE: " + boxQuantity.intValue());
						}
						logger.info(messageLog.toString());

						toReplace.add(new DocumentItem(nfe, p, boxQuantity, "CONVERTED"));
					} else if (p.getModel().getMetaname().equals("MP")) {//Materia Prima não precisa de conversão
						toReplace.add(new DocumentItem(nfe, p, qty, "CONVERTED"));
					}
				}
				//Sem conversao
				itemNFSMap = doc.getSiblings().stream()
								.filter(ds -> ds.getModel().getMetaname().equals("NFENTRADA") && !Documents.getStringField(ds, "ZTRANS", "").equalsIgnoreCase("N"))
								.filter(nf -> nf.getPerson() != null && !(nf.getPerson().getCode().startsWith("07196033") || nf.getPerson().getCode().startsWith("08715757") || nf.getPerson().getCode().startsWith("10557540")))
								.flatMap(nf -> nf.getItems().stream().filter(di -> di.getStatus() == null || !di.getStatus().equals("CONVERTED")))
								.collect(Collectors.groupingBy(di -> di.getProduct().getId()));

				for (UUID pId : itemNFSMap.keySet()) {
					List<DocumentItem> diList = itemNFSMap.get(pId);
					DocumentItem nfItem = diList.stream().findAny().orElse(null);
					Product p = diList.stream().map(di -> di.getProduct()).findAny().get();
					Double qty = diList.stream().mapToDouble(di -> di.getQty()).sum();
					String propMultip = nfItem.getProperties().containsKey("FATOR_MULTIPLICATIVO") ? nfItem.getProperties().get("FATOR_MULTIPLICATIVO") : "0";

					if (!propMultip.isEmpty() && !propMultip.equals("0"))
						qty *= Integer.parseInt(propMultip);
					logger.info("New Quantity: " + qty);
					toReplace.add(new DocumentItem(nfe, p, qty, "CONVERTED"));
				}

				nfe.setItems(toReplace);
				doc.getSiblings().removeIf(ds -> ds.getModel().getMetaname().equals(nfe.getModel().getMetaname()) && !ds.getId().equals(nfe.getId()));
			}
		}
		AGLDocModelForm aglDoc = convertDocToAgl(doc);

		if (doc.getModel().getMetaname().equals("ORDMOV") && doc.getStatus().equals("SUCESSO")) {
			for (AGLDocTransport adtr : aglDoc.getTransport()) {
				aglDoc.getThings().parallelStream()
								.filter(at -> at.getId().equals(adtr.getThing_id()))
								.forEach(at -> {
									at.setAddress_id(adtr.getAddress_id());
									at.getSiblings().forEach(ats -> ats.setAddress_id(adtr.getOrigin_id()));
								});
			}
		}

		String text = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(aglDoc);

		doc.getSiblings().removeIf(ds -> origNFItems.keySet().parallelStream().anyMatch(nf -> nf.getId().equals(ds.getId())));
		for (Entry<Document, List<DocumentItem>> en : origNFItems.entrySet()) {
			Document nf = en.getKey();

			nf.setItems(en.getValue().parallelStream().filter(di -> !di.getStatus().equals("CONVERTED")).collect(Collectors.toSet()));
			doc.getSiblings().add(nf);
		}
		return sendToWMS("com.wms.comunicador.adocument", text, verb, aglDoc.getId());
	}

	public Future<IntegrationReturn> sendTruckToWMS(Thing truck, String verb) {
		AGLTruck aglTruck = convertThingToAGLTruck(truck);
		String text = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(aglTruck);

		return sendToWMS("com.wms.comunicador.atruck", text, verb, aglTruck.getId());
	}

	public Future<IntegrationReturn> sendCustomerToWMS(Person ps, String verb) {
		AGLCustomer aglCustomer = convertPersonToAGLCustomer(ps);
		String text = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(aglCustomer);

		return sendToWMS("com.wms.comunicador.acustomer", text, verb, aglCustomer.getId());
	}

	public Future<IntegrationReturn> sendSupplierToWMS(Person ps, String verb) {
		AGLCustomer aglSupplier = convertPersonToAGLCustomer(ps);
		String text = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(aglSupplier);

		return sendToWMS("com.wms.comunicador.asupplier", text, verb, aglSupplier.getId());
	}

	public Future<IntegrationReturn> sendProductToWMS(Product prd, String verb) {
		AGLProd aglPrd = convertProdToAGL(prd);
		String text = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(aglPrd);

		return sendToWMS("com.wms.comunicador.aproduct", text, verb, aglPrd.getId());
	}

	public Future<IntegrationReturn> sendThingToWMS(Thing t, String verb) {
		AGLThing aglThing = convertThingToAGL(t);
		String text = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(aglThing);

		return sendToWMS("com.wms.comunicador.athings", text, verb, aglThing.getId());
	}

	public Future<IntegrationReturn> sendToWMS(String method, String json, String verb, String id) {
		RestUtil rest = new RestUtil(ConfigUtil.get("hunter-custom-solar", "WMS-Base", "http://10.62.132.46:8080/wms/servlet"));
		JsonObject ret = null;
		JsonReader jsonReader = Json.createReader(new StringReader(json));

		logger.info(verb + " TO WMS: " + json);
		jsonReader = Json.createReader(new StringReader(json));
		ret = jsonReader.readObject();
		jsonReader.close();
		return rest.sendAsync(ret, method, verb, id);
	}
}
