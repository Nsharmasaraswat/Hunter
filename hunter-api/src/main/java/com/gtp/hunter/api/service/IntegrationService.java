package com.gtp.hunter.api.service;

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
import com.gtp.hunter.process.model.Task;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;

@Stateless
public class IntegrationService {

	@EJB(lookup = "java:global/hunter-core/RegisterService!com.gtp.hunter.process.service.RegisterService")
	private RegisterService regSvc;

	public RegisterService getRegSvc() {
		return regSvc;
	}

	public Document getDocByCodeAndType(DocumentModel dm, String code) {
		return regSvc.getDcSvc().findByModelAndCode(dm, code);
	}

	public void persistDoc(Document doc) {
		regSvc.getDcSvc().persist(doc);
	}

	public DocumentModel getDocModel(String metaname) {
		return regSvc.getDmSvc().findByMetaname(metaname);
	}

	public void persistDocItem(DocumentItem items) {
		regSvc.getDiSvc().persist(items);
	}

	public Product getBySku(String sku) {
		return regSvc.getPrdSvc().findBySKU(sku);
	}

	public void persistProduct(Product p) {
		regSvc.getPrdSvc().persist(p);
	}

	public Product getProduct(UUID id) {
		return regSvc.getPrdSvc().findById(id);
	}

	public ProductModel getProductModel(String metaname) {
		return regSvc.getPmSvc().findByMetaname(metaname);
	}

	public Document getDoc(UUID id) {
		return regSvc.getDcSvc().findById(id);
	}

	public void persistThing(Thing t) {
		regSvc.getThSvc().persist(t);
	}

	public Unit generateUnit(UnitType type, String prefix) {
		return regSvc.getUnSvc().generateUnit(type, prefix);
	}

	public void persistTask(Task t) {
		regSvc.getTskSvc().persist(t);
	}

	public void persistProperty(Property prop) {
		regSvc.getPrSvc().persist(prop);
	}

	public void persistDocumentThing(DocumentThing dt) {
		regSvc.getDtSvc().persist(dt);
	}

	public BaseDevice getBaseDevByUUID(UUID uuid) {
		return regSvc.getSrcSvc().getBaseDeviceByUUID(uuid);
	}

	public List<ProductField> listProductFieldsByProduct(Product prd) {
		return regSvc.getPfSvc().listByProduct(prd);
	}

	public void persistProductField(ProductField pf) {
		regSvc.getPfSvc().persist(pf);
	}

	public void persistDocumentField(DocumentField df) {
		regSvc.getDfSvc().persist(df);
	}

	public Document getQuickDocument(UUID doc) {
		return regSvc.getDcSvc().quickFindById(doc);
	}

	public List<DocumentItem> getQuickDocumentItemListByDocument(UUID docid) {
		return regSvc.getDiSvc().quickListByDocumentId(docid);
	}

	public List<PropertyModelField> listQuickPropertyModelFieldFromProduct(UUID prod) {
		return regSvc.getPrmfSvc().listQuickPropertyModelFieldFromProduct(prod);
	}

	public int getQuickDocumentThingCountByDocAndProduct(UUID docid, UUID prodid) {
		return regSvc.getDtSvc().getQuickDocumentThingCountByDocAndProduct(docid, prodid);
	}

	public Document quickFindByMetaname(String doc) {
		return regSvc.getDcSvc().quickFindByMetaname(doc);
	}

	public DocumentItem getQuickDocumentItemByDocumentAndProductSKUAndBatch(UUID docid, String prodid, String batch) {
		return regSvc.getDiSvc().getQuickDocumentItemByDocumentAndProductSKUAndBatch(docid, prodid, batch);
	}

	public void updateDocumentItemQuantity(UUID di, int quantity) {
		regSvc.getDiSvc().updateDocumentItemQuantity(di, quantity);
	}

	public int getCountThingsBySKUAndProperty(String sku, String property, String value) {
		return regSvc.getThSvc().getThRep().getCountThingsBySKUAndProperty(sku, property, value);
	}

	public List<ProductField> listQuickProductFieldByProductId(UUID id) {
		return regSvc.getPfSvc().quickListByProductId(id);
	}
}
