package com.gtp.hunter.process.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ejb.AccessTimeout;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.core.service.UnitService;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Location;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.repository.AddressRepository;
import com.gtp.hunter.process.repository.DocumentRepository;
import com.gtp.hunter.process.repository.DocumentThingRepository;
import com.gtp.hunter.process.repository.DocumentTransportRepository;
import com.gtp.hunter.process.repository.PropertyRepository;
import com.gtp.hunter.process.repository.ThingRepository;

@Stateless
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class ThingService {

	@Inject
	private ThingRepository				thRep;

	@Inject
	private PropertyRepository			ptyRep;

	@Inject
	private DocumentRepository			dcRep;

	@Inject
	private DocumentThingRepository		dtRep;

	@Inject
	private DocumentTransportRepository	dtrRep;

	@Inject
	private AddressRepository			addrRep;

	@EJB(lookup = "java:global/hunter-core/UnitService!com.gtp.hunter.core.service.UnitService")
	private UnitService					unSvc;

	public Thing transform(ComplexData cd) {
		Profiler p = new Profiler();
		Thing t = null;

		if (cd.getUnit() != null) {
			t = thRep.findByUnitId(cd.getUnit().getId());
			p.step("findByUnitId", false);
			if (t != null) {
				if (cd.getPayload() != null)
					t.setPayload(cd.getPayload());
			}
		}
		p.done("Transformation Done", false, false);
		return t;
	}

	public Thing findById(UUID id) {
		return thRep.findById(id);
	}

	public Thing findByUnitId(UUID id) {
		return thRep.findByUnitId(id);
	}

	public Thing findByTagId(String tagId) {
		return thRep.findByTagId(tagId);
	}

	public Thing quickFindByUnitTagId(String tagId) {
		return thRep.quickFindByUnitTagId(tagId);
	}

	public Thing findByMetaName(String metaName) {
		return thRep.findByMetaname(metaName);
	}

	public List<Thing> quickListByProductId(UUID pid) {
		return thRep.quickListByProductId(pid);
	}

	public List<Thing> quickListByProductIdAndStatus(UUID pid, String status) {
		return thRep.quickListByProductIdAndStatus(pid, status);
	}

	public List<Thing> listByProduct(Product p) {
		return thRep.listByProduct(p);
	}

	public List<Thing> listByModel(PropertyModel ppm) {
		return thRep.listByModel(ppm);
	}

	public List<Thing> listByModelAndStatus(PropertyModel ppm, String status) {
		return thRep.listByModelAndStatus(ppm, status);
	}

	public List<Thing> listByModelAndPropertyValue(PropertyModel ppm, String fieldMeta, String value) {
		List<Thing> ret = new ArrayList<>();
		if (ppm != null) {
			List<Thing> tmpList = thRep.quickListByModelAndPropertyValue(ppm.getMetaname(), fieldMeta, value);

			ret.addAll(thRep.listById(tmpList.stream().map(d -> d.getId()).collect(Collectors.toList())));
		}
		return ret;
	}

	public List<Thing> listByModelAndStatusAndPropertyValue(PropertyModel ppm, String status, String fieldMeta, String value) {
		List<Thing> ret = new ArrayList<>();
		if (ppm != null) {
			List<Thing> tmpList = thRep.quickListByModelAndStatusAndPropertyValue(ppm.getMetaname(), status, fieldMeta, value);

			ret.addAll(thRep.listById(tmpList.stream().map(d -> d.getId()).collect(Collectors.toList())));
		}
		return ret;
	}

	public List<Thing> listByPropertyValue(String fieldMeta, String value) {
		List<Thing> ret = new ArrayList<>();
		List<Thing> tmpList = thRep.quickListByPropertyValue(fieldMeta, value);

		ret.addAll(thRep.listById(tmpList.stream().map(d -> d.getId()).collect(Collectors.toList())));
		return ret;
	}

	public List<Thing> listByModelMeta(String propModelMeta) {
		return thRep.listByModelMeta(propModelMeta);
	}

	public List<Thing> listAll() {
		return thRep.listAll();
	}

	public List<Thing> listByStatusLimit(String status, int from, int to) {
		return thRep.listByStatusLimit(status, from, to);
	}

	public List<Thing> listByDocument(Document d) {
		return thRep.listByDocument(d);
	}

	public List<Thing> listByParent(Thing t) {
		return thRep.listByParent(t);
	}

	public void deleteById(UUID id) {
		Thing thing = new Thing();
		thing = findById(id);
		if (!"CANCELADO".equals(thing.getStatus())) {
			thing.setStatus("CANCELADO");
			thRep.persist(thing);
		}

	}

	public void multiPersist(Thing... things) {
		thRep.multiPersist(things);
	}

	public void multiPersist(List<Thing> tList) {
		thRep.multiPersist(tList);
	}

	public void multiPersist(Set<Thing> tSet) {
		thRep.multiPersist(tSet);
	}

	public Thing persist(Thing thing) {
		thRep.persist(thing);
		return thing;
	}

	public ThingRepository getThRep() {
		return thRep;
	}

	public void storeThingsOnFirstDocAddress(List<DocumentThing> dtList, DocumentModelField addrModelField) {

		for (DocumentThing dt : dtList) {
			Thing t = thRep.findById(dt.getThing().getId());
			Document d = dt.getDocument();

			while (d != null && !d.getFields().stream().filter(df -> df.getField().getId().equals(addrModelField.getId())).findFirst().isPresent()) {
				Document quickParent = dcRep.quickFindParentDoc(d.getId().toString());

				if (quickParent != null)
					d = dcRep.findById(quickParent.getId());
				else
					d = null;
			}
			if (d != null) {
				DocumentField addrField = d.getFields().stream().filter(df -> df.getField().getId().equals(addrModelField.getId())).findFirst().get();
				Address a = addrRep.findById(UUID.fromString(addrField.getValue()));
				t.setAddress(a);
				t.setUpdatedAt(Calendar.getInstance().getTime());
				thRep.persist(t);
			}
		}
	}

	public Thing dirtyInsert(Thing t) {
		if (t.getId() == null) t.setId(UUID.randomUUID());
		thRep.dirtyInsert(t.getId(), t.getMetaname(), t.getName(), t.getStatus(), t.getCreatedAt(), t.getAddress() != null ? t.getAddress().getId() : null, t.getModel() != null ? t.getModel().getId() : null, t.getParent() != null ? t.getParent().getId() : null, t.getProduct() != null ? t.getProduct().getId() : null);

		for (Property pty : t.getProperties())
			if (pty != null && pty.getField() != null && pty.getField().getId() != null)
				ptyRep.quickInsert(t.getId(), pty.getField().getId(), pty.getValue());
		return t;
	}

	public List<Thing> listByLocationAndProduct(Location loc, Product prd) {
		return thRep.listByLocationAndProduct(loc, prd);
	}

	public List<Thing> listByLocationId(UUID locId) {
		return thRep.listByLocationId(locId);
	}

	public List<Thing> listByAddressId(UUID addrId) {
		return thRep.listByAddressId(addrId);
	}

	public List<Thing> listByAddressIdNoOrphan(UUID addrId) {
		return thRep.listByAddressIdNoOrphan(addrId);
	}

	public List<Thing> listParentByChildAddressId(UUID addrId) {
		return thRep.listParentByChildAddressId(addrId);
	}

	public List<Thing> listByChildAddressId(UUID addrId) {
		return thRep.listByChildAddressId(addrId);
	}

	public List<Thing> listByProductIdAndStatus(UUID prdId, String status) {
		return thRep.listByProductIdAndStatus(prdId, status);
	}

	public List<Thing> listByProductIdAndNotStatus(UUID prdId, String status) {
		return thRep.listByProductIdAndNotStatus(prdId, status);
	}

	public List<Thing> listByProductId(UUID prdId) {
		return thRep.listByProductId(prdId);
	}

	public void quickUpdateParentId(UUID thingId, UUID parentId) {
		thRep.quickUpdateParentId(thingId, parentId);
	}

	public List<Thing> listByStatus(String status) {
		return thRep.listByStatus(status);
	}

	public void remove(Thing toDel) {
		if (toDel.getId() != null) {
			for (Property pr : toDel.getProperties())
				ptyRep.removeById(pr.getId());
			dtrRep.dirtyRemoveThing(toDel);
			dtRep.dirtyRemoveThing(toDel);
			thRep.removeById(toDel.getId());
		}
	}

	public void removeById(UUID id) {
		ptyRep.removeByThingId(id);
		thRep.removeById(id);
	}

	public Thing refresh(Thing t) {
		return thRep.refresh(t);
	}

	public List<Thing> listById(Collection<UUID> idList) {
		return thRep.listById(idList);
	}

	public void quickRemoveUnit(UUID thingId, UUID unitId) {
		thRep.quickRemoveUnit(thingId, unitId);
	}

	public Thing findByUnit(Unit plates) {
		return thRep.findByUnit(plates);
	}

	public Thing fillUnits(Thing th) {
		th.getUnitModel().clear();
		th.getUnitModel().addAll(unSvc.listById(th.getUnits()));
		return th;
	}

	public void flush() {
		thRep.flush();
	}

	public Thing findByAddress(Address add) {
		return thRep.findByAddress(add);
	}
}
