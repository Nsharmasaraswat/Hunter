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
import com.gtp.hunter.process.model.AddressModel;
import com.gtp.hunter.process.model.AddressModelField;
import com.gtp.hunter.process.service.RegisterService;

@RequestScoped
@Path("/addressmodelfield")
public class AddressModelFieldRest {

	@Inject
	private RegisterService regSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AddressModelField> listAll() {
		return regSvc.getAmfSvc().listAll();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public AddressModelField getById(@PathParam("id") UUID id) {
		return regSvc.getAmfSvc().getById(id);
	}

	@GET
	@Path("/bymodel/{modelId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listByModel(@PathParam("modelId") UUID modelId) {
		AddressModel am = regSvc.getAdmSvc().findById(modelId);
		if (am == null) return Response.status(Status.BAD_REQUEST).entity("AddressModel with id " + modelId.toString() + " not found").build();
		return Response.ok(regSvc.getAmfSvc().listByModel(am)).build();
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteById(@PathParam("id") UUID id) {
		regSvc.getAmfSvc().removeById(id);
		return Response.ok().build();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(JsonObject amfjs) {
		if (!amfjs.containsKey("model")) return Response.status(Status.BAD_REQUEST).entity("model cannot be null").build();
		UUID modelId = UUID.fromString(amfjs.getJsonObject("model").getString("id", UUID.randomUUID().toString()));
		AddressModel am = regSvc.getAdmSvc().findById(modelId);
		if (am == null) return Response.status(Status.BAD_REQUEST).entity("Invalid model: " + amfjs.getJsonObject("model").getString("id")).build();
		AddressModelField amf = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(amfjs.toString(), AddressModelField.class);

		amf.setModel(am);
		am.getFields().add(amf);
		return Response.ok(regSvc.getAmfSvc().persist(amf)).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") UUID id, AddressModelField amf) {
		if (id == null || amf.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!amf.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		if (amf.getModel() == null || amf.getModel().getId() == null) return Response.status(Status.BAD_REQUEST).entity("model.id CANNOT BE NULL").build();
		AddressModel am = regSvc.getAdmSvc().findById(amf.getModel().getId());

		amf.setModel(am);
		return Response.ok(regSvc.getAmfSvc().persist(amf)).build();
	}
}
