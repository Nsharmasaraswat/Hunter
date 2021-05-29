package com.gtp.hunter.process.rest;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.slf4j.Logger;

import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.repository.DocumentThingRepository;

@RequestScoped
@Path("/documentthing")
public class DocumentThingRest {

	@Inject
	private DocumentThingRepository	dtRep;

	@Inject
	private transient Logger		logger;

	@GET
	@Path("/all")
	public List<DocumentThing> getAll() {
		return dtRep.listAll();
	}

	@GET
	@Path("/documentid/{documentid}")
	public List<DocumentThing> getAll(@PathParam("documentid") String documentId) {
		return dtRep.listByDocumentId(UUID.fromString(documentId));
	}

	@GET
	@Path("/quickByTypeCode/{type}/{code}")
	public List<DocumentThing> getQuickByTypeCode(@PathParam("type") String type, @PathParam("type") String code) {
		return dtRep.getQuickByTypeCode(type, code);
	}

	@GET
	@Path("/{id}")
	public DocumentThing getById(@PathParam("id") String id) {
		DocumentThing dt = null;
		try {
			dt = dtRep.findById(UUID.fromString(id));
		} catch (Exception e) {
			logger.error("Creating new DocumentThing " + e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			dt = new DocumentThing();
		}
		return dt;
	}

	@DELETE
	@Path("/{id}")
	public String deleteById(@PathParam("id") String id) {
		try {
			dtRep.removeById(UUID.fromString(id));
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			return "Error";
		}
		return "Ok";
	}

}
