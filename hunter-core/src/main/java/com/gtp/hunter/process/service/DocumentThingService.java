package com.gtp.hunter.process.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.repository.DocumentThingRepository;

@Stateless
public class DocumentThingService {

	@Inject
	private DocumentThingRepository dtRep;

	public DocumentThing findByDocIdThingId(UUID docId, UUID thingId) {
		return docId == null || thingId == null ? null : dtRep.findByDocumentIdAndThingId(docId, thingId);
	}

	public List<DocumentThing> listByDocumentId(UUID docId) {
		return docId == null ? null : dtRep.listByDocumentId(docId);
	}

	public void persist(DocumentThing dt) {
		dtRep.persist(dt);
	}

	public int getQuickDocumentThingCountByDocAndProduct(UUID docid, UUID prodid) {
		return dtRep.getQuickDocumentThingCountByDocAndProduct(docid, prodid);
	}

	public DocumentThing findByDocumentAndThing(Document dSib, Thing ht) {
		return dtRep.findByDocumentAndThing(dSib, ht);
	}

	public void quickInsert(UUID doc, UUID thing, String status) {
		dtRep.quickInsert(doc, thing, status);
	}

	public void quickUpdateStatus(UUID id, String statusDocThingTo) {
		dtRep.quickUpdateStatus(id, statusDocThingTo);
	}

	public DocumentThing quickFindByThingIdAndDocModelMeta(UUID thing, String docmeta) {
		return dtRep.quickFindByThingIdAndDocModelMeta(thing, docmeta);
	}

	public void multiPersist(List<DocumentThing> dtList) {
		dtRep.multiPersist(dtList);
	}

	public void removeById(UUID id) {
		dtRep.removeById(id);
	}

	public void multiPersist(Set<DocumentThing> dtSet) {
		dtRep.multiPersist(dtSet);
	}

}
