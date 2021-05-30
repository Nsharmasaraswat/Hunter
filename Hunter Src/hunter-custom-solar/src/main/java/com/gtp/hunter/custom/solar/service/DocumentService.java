package com.gtp.hunter.custom.solar.service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.DocumentTransport;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.ui.json.SolarPickingResupply;

@Stateless
public class DocumentService {

	@Inject
	private IntegrationService iSvc;

	public void addDocument(Document document) {
		iSvc.getRegSvc().getDcSvc().persist(document);
	}

	public void deleteDocument(UUID id) {
		Document doc = iSvc.getRegSvc().getDcSvc().findById(id);
		doc.setStatus("CANCELADO");
		iSvc.getRegSvc().getDcSvc().persist(doc);

	}

	public Document getOneDocumentByCodeAndMetaname(String code, String metaname) {
		return iSvc.getRegSvc().getDcSvc().quickFindByCodeAndModelMetaname(code, metaname);
	}

	public Document getOneDocument(UUID id) {
		return iSvc.getRegSvc().getDcSvc().findById(id);
	}

	public Document dirtyInsertFullDocument(Document d, boolean fireEvent) {
		return iSvc.getRegSvc().getDcSvc().dirtyFullInsert(d, fireEvent);
	}

	public List<Document> getCustomYMSNF(String personModel, String personCode) {
		List<Document> dList = iSvc.getRegSvc().getDcSvc().customIncompleteQuickListOrphanByPersonTypeAndCode(personModel, personCode);

		dList.forEach(d -> d.setFields(new HashSet<>(iSvc.getRegSvc().getDfSvc().quickListByDocumentId(d.getId()))));
		return dList;
	}
}
