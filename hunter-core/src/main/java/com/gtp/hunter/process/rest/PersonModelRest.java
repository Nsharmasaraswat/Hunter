package com.gtp.hunter.process.rest;

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

import com.gtp.hunter.process.model.PersonModel;
import com.gtp.hunter.process.model.PersonModelField;
import com.gtp.hunter.process.service.PersonModelService;

@Transactional
@RequestScoped
@Path("/personmodel")
public class PersonModelRest {

	@Inject
	private PersonModelService psmSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPersonModels() {
		return Response.ok(psmSvc.getListAllPersonModel()).build();
	}

	@GET
	@Path("/bymetaname/{metaname}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPersonModelByMetaname(@PathParam("metaname") String metaname) {
		return Response.ok(psmSvc.findByMetaname(metaname)).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOnePerson(@PathParam("id") UUID id) {
		return Response.ok(psmSvc.findById(id)).build();
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deletePersonModelById(@PathParam("id") UUID id) {
		psmSvc.delete(id);
		return Response.ok().build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(PersonModel personModel) {
		psmSvc.persist(personModel);
		for (PersonModelField psmf : personModel.getFields())
			psmf.setModel(personModel);
		return Response.ok(personModel).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updatePersonModel(@PathParam("id")UUID id, PersonModel personModel) {
		if (id == null || personModel.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!personModel.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		return Response.ok(psmSvc.persist(personModel)).build();
	}

}
