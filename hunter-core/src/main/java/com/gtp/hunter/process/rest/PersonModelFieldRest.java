package com.gtp.hunter.process.rest;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
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

import com.google.gson.GsonBuilder;
import com.gtp.hunter.process.model.PersonModel;
import com.gtp.hunter.process.model.PersonModelField;
import com.gtp.hunter.process.service.PersonModelFieldService;
import com.gtp.hunter.process.service.PersonModelService;

@RequestScoped
@Path("/personmodelfield")
public class PersonModelFieldRest {

	@Inject
	private PersonModelFieldService	psmfSvc;

	@Inject
	private PersonModelService		psmSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PersonModelField> getListContainsAllPersonModelField() {
		return psmfSvc.listAll();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public PersonModelField getOnePersonModelField(@PathParam("id") UUID id) {
		return psmfSvc.findById(id);
	}

	@DELETE
	@Path("/{id}")
	public Response deletePersonModelFieldById(@PathParam("id") UUID id) {
		psmfSvc.deleteByID(id);
		return Response.ok().build();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(JsonObject psmfjs) {
		if (!psmfjs.containsKey("model")) return Response.status(Status.BAD_REQUEST).entity("model cannot be null").build();
		UUID modelId = UUID.fromString(psmfjs.getJsonObject("model").getString("id", UUID.randomUUID().toString()));
		PersonModel psm = psmSvc.findById(modelId);
		if (psm == null) return Response.status(Status.BAD_REQUEST).entity("Invalid model: " + psmfjs.getJsonObject("model").getString("id")).build();
		PersonModelField psmf = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(psmfjs.toString(), PersonModelField.class);

		psmf.setModel(psm);
		psm.getFields().add(psmf);
		return Response.ok(psmfSvc.persist(psmf)).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updatePersonModelField(@PathParam("id") UUID id, PersonModelField psmf) {
		if (id == null || psmf.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!psmf.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();

		if (psmf.getModel() == null) {
			PersonModelField pmf = psmfSvc.findById(id);

			psmf.setModel(pmf.getModel());
		}
		return Response.ok(psmfSvc.persist(psmf)).build();
	}

}
