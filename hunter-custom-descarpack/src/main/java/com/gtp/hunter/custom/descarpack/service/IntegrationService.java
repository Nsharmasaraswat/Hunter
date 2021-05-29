package com.gtp.hunter.custom.descarpack.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.core.devices.BaseDevice;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductField;
import com.gtp.hunter.process.model.ProductModel;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Supplier;
import com.gtp.hunter.process.model.Task;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;

@Stateless
public class IntegrationService {

	@EJB(lookup = "java:global/hunter-core-2.0.0-SNAPSHOT/RegisterService!com.gtp.hunter.process.service.RegisterService")
	private RegisterService rSvc;

	public RegisterService getrSvc() {
		return rSvc;
	}

	public Document quickDocByCodeAndType(DocumentModel dm, String code) {
		return rSvc.getDcrep().quickFindByCodeAndModelMetaname(code, dm.getMetaname());
	}
	
	public List<DocumentThing> quickDocThingListByCodeAndType(DocumentModel dm, String code) {
		return rSvc.getDtRep().getQuickByTypeCode(dm.getMetaname(), code);
	}

	public Document getDocByCodeAndType(DocumentModel dm, String code) {
		return rSvc.getDcrep().findDocumentByTypeAndCode(dm, code);
	}

	public void persistDoc(Document doc) {
		rSvc.getDcrep().persist(doc);
	}

	public DocumentModel getDocModel(String metaname) {
		return rSvc.getDmrep().findByMetaname(metaname);
	}

	public void persistDocItem(DocumentItem items) {
		rSvc.getDiRep().multiPersist(items);
	}

	public Product getBySku(String sku) {
		return rSvc.getPdRep().findByField("sku", sku);
	}

	public void persistProduct(Product p) {
		rSvc.getPdRep().persist(p);
	}

	public Product getProduct(UUID id) {
		return rSvc.getPdRep().findById(id);
	}

	public ProductModel getProductModel(String metaname) {
		return rSvc.getPmRep().findByMetaname(metaname);
	}

	public Document getDoc(UUID id) {
		return rSvc.getDcrep().findById(id);
	}

	public void persistThing(Thing t) {
		rSvc.getThRep().persist(t);
	}

	public Unit generateUnit(UnitType type, String prefix) {
		return rSvc.getuSvc().generateUnit(type, prefix);
	}

	public void persistTask(Task t) {
		rSvc.gettRep().persist(t);
	}

	public void persistProperty(Property prop) {
		rSvc.getPropRep().persist(prop);
	}

	public void persistDocumentThing(DocumentThing dt) {
		rSvc.getDtRep().persist(dt);
	}

	public BaseDevice getBaseDevByUUID(UUID uuid) {
		return rSvc.getSrcSvc().getBaseDeviceByUUID(uuid);
	}

	public List<ProductField> listProductFieldsByProduct(Product prd) {
		return rSvc.getPfRep().listByField("product", prd);
	}

	public void persistProductField(ProductField pf) {
		rSvc.getPfRep().persist(pf);
	}

	public Supplier getSupplierByCode(String code) {
		return rSvc.getSpRep().findByField("extid", code);
	}

	public void persistSupplier(Supplier s) {
		rSvc.getSpRep().persist(s);
	}

	public void persistDocumentField(DocumentField df) {
		rSvc.getDfRep().persist(df);
	}

	public Document getQuickDocument(UUID doc) {
		return rSvc.getDcrep().quickFindById(doc);
	}

	public List<DocumentItem> getQuickDocumentItemListByDocument(UUID docid) {
		return rSvc.getDiRep().getQuickDocumentItemListByDocument(docid);
	}

	public List<PropertyModelField> listQuickPropertyModelFieldFromProduct(UUID prod) {
		return rSvc.getPpmfRep().listQuickPropertyModelFieldFromProduct(prod);
	}

	public int getQuickDocumentThingCountByDocAndProduct(UUID docid, UUID prodid) {
		return rSvc.getDtRep().getQuickDocumentThingCountByDocAndProduct(docid, prodid);
	}

	public Document quickFindByMetaname(String doc) {
		return rSvc.getDcrep().quickFindByMetaname(doc);
	}

	public DocumentItem getQuickDocumentItemByDocumentAndProductSKUAndBatch(UUID docid, String prodid, String batch) {
		return rSvc.getDiRep().getQuickDocumentItemByDocumentAndProductSKUAndBatch(docid, prodid, batch);
	}

	public void updateDocumentItemQuantity(UUID di, int quantity) {
		rSvc.getDiRep().updateDocumentItemQuantity(di, quantity);
	}

	public int getCountThingsBySKUAndProperty(String sku, String property, String value) {
		return rSvc.getThRep().getCountThingsBySKUAndProperty(sku, property, value);
	}

	public List<ProductField> listQuickProductFieldByProductId(UUID id) {
		return rSvc.getPfRep().quickListByProductId(id);
	}

	public Unit getUnitByTagId(String tagId) {
		return rSvc.getuRep().findByField("tagId", tagId);
	}
}
