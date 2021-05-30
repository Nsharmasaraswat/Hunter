package com.gtp.hunter.process.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.DocumentTransport;
import com.gtp.hunter.process.repository.DocumentTransportRepository;

@Stateless
public class DocumentTransportService {

	@Inject
	private DocumentTransportRepository dtrRep;

	public DocumentTransport findById(UUID dtrId) {
		return dtrId == null ? null : dtrRep.findById(dtrId);
	}

	public List<DocumentTransport> listByDocumentId(UUID docId) {
		return docId == null ? null : dtrRep.listByDocumentId(docId);
	}

	public void dirtyInsert(UUID dtrId, String metaname, String name, String status, Date createdAt, Date updatedAt, int seq, UUID document_id, UUID thing_id, UUID address_id, UUID origin_id) {
		dtrRep.dirtyInsert(dtrId, metaname, name, status, createdAt, updatedAt, seq, document_id, thing_id, address_id, origin_id);
	}

	public void removeById(UUID id) {
		dtrRep.removeById(id);
	}
}
