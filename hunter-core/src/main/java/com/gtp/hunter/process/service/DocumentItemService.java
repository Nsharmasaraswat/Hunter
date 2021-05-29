package com.gtp.hunter.process.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.repository.DocumentItemRepository;

@Stateless
public class DocumentItemService {

	@Inject
	private DocumentItemRepository diRep;

	public DocumentItem findByMetaname(String metaname) {
		return diRep.findByMetaname(metaname);
	}

	public DocumentItem findById(UUID id) {
		return diRep.findById(id);
	}

	public void persist(DocumentItem di) {
		diRep.persist(di);
	}

	public List<DocumentItem> quickListByDocumentId(UUID docid) {
		return diRep.getQuickDocumentItemListByDocument(docid);
	}

	public DocumentItem getQuickDocumentItemByDocumentAndProductSKUAndBatch(UUID docid, String prodid, String batch) {
		return diRep.getQuickDocumentItemByDocumentAndProductSKUAndBatch(docid, prodid, batch);
	}

	public void updateDocumentItemQuantity(UUID di, int quantity) {
		diRep.updateDocumentItemQuantity(di, quantity);
	}

	public void remove(DocumentItem item) {
		diRep.remove(item);
	}

	public void removeById(UUID id) {
		diRep.removeById(id);
	}

	public void quickRemoveByIds(Collection<UUID> idList) {
		diRep.quickRemoveByIds(idList);
	}

	public void dirtyInsertProperty(UUID id, String key, String value) {
		diRep.dirtyInsertProperty(id, key, value);
	}

	public void multiPersist(List<DocumentItem> diList) {
		diRep.multiPersist(diList);
	}

	public void multiPersist(Set<DocumentItem> diList) {
		diRep.multiPersist(diList);
	}

	public void flush() {
		diRep.flush();
	}
}
