package com.gtp.hunter.process.service;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.repository.DocumentModelRepository;

@Stateless
public class DocumentModelService {

	@Inject
	private DocumentModelRepository dmRep;

	public DocumentModel findByMetaname(String metaname) {
		return dmRep.findByMetaname(metaname);
	}

	public DocumentModel findById(UUID id) {
		return dmRep.findById(id);
	}
}
