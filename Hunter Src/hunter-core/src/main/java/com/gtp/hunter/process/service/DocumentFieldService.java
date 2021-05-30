package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.repository.DocumentFieldRepository;

@Stateless
public class DocumentFieldService {

	@Inject
	private DocumentFieldRepository dfRep;

	public void persist(DocumentField df) {
		dfRep.persist(df);
	}

	public List<DocumentField> listByDocumentId(UUID docId) {
		return dfRep.listByDocumentId(docId);
	}

	@Deprecated
	public void quickRemoveDocumentField(UUID id, String string) {
		dfRep.quickRemoveDocumentField(id, string);
	}

	public void quickInsertDocumentField(UUID docId, UUID docModelId, String value) {
		dfRep.quickInsertDocumentField(docId, docModelId, value);
	}

	public void quickChangeModel(UUID document, UUID docmodelFrom, UUID docmodelTo) {
		dfRep.quickChangeModel(document, docmodelFrom, docmodelTo);
	}

	public void removeById(UUID id) {
		dfRep.removeById(id);
	}

	public List<DocumentField> quickListByDocumentId(UUID id) {
		return dfRep.quickListByDocumentId(id);
	}

	public void quickUpdateValue(DocumentField df) {
		dfRep.quickInsert(df.getDocument().getId(), df.getField().getId(), df.getValue());
	}

	public List<DocumentField> listByValue(String code) {
		return dfRep.listByField("value", code);
	}

}
