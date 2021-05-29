package com.gtp.hunter.process.rest;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.repository.DocumentModelRepository;

@Transactional
@RequestScoped
@Path("/documentmodel")
public class DocumentModelRest {

	@Inject
	private DocumentModelRepository dmRep;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DocumentModel> getAll() {
		return dmRep.listAll();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public DocumentModel getById(@PathParam("id") UUID id) {
		return dmRep.findById(id);
	}

	@GET
	@Path("/byMetaname/{metaname}")
	@Produces(MediaType.APPLICATION_JSON)
	public DocumentModel getByMetaname(@PathParam("metaname") String metaname) {
		return dmRep.findByMetaname(metaname);
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(DocumentModel docMod) {
		dmRep.persist(docMod);
		for (DocumentModelField dmf : docMod.getFields())
			dmf.setModel(docMod);
		return Response.ok(docMod).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") UUID id, DocumentModel df) {
		if (id == null || df.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!df.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		return Response.ok(dmRep.persist(df)).build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteById(@PathParam("id") UUID id) {
		dmRep.removeById(id);
		return Response.ok().build();
	}
}
