package com.gtp.hunter.process.service;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.repository.DocumentModelFieldRepository;

@Stateless
public class DocumentModelFieldService {

	@Inject
	private DocumentModelFieldRepository dmfRep;

	public DocumentModelField findByModelAndMetaname(DocumentModel dm, String metaname) {
		return dmfRep.findByModelAndMetaname(dm, metaname);
	}

	public DocumentModelField findByModelIdAndMetaname(UUID modelId, String metaname) {
		return dmfRep.findByModelIdAndMetaname(modelId, metaname);
	}

	public DocumentModelField findByMetaname(String metaname) {
		return dmfRep.findByMetaname(metaname);
	}

	public DocumentModelField findById(UUID id) {
		return dmfRep.findById(id);
	}
}
