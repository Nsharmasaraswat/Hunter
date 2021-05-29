package com.gtp.hunter.process.rest;

import java.util.ArrayList;
import java.util.EnumSet;
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

import org.slf4j.Logger;

import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.enums.FieldType;
import com.gtp.hunter.process.model.ProductModel;
import com.gtp.hunter.process.model.ProductModelField;
import com.gtp.hunter.process.service.RegisterService;

@RequestScoped
@Path("/productmodelfield")
public class ProductModelFieldRest {

	@Inject
	private transient Logger	logger;

	@Inject
	private RegisterService		regSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAll() {
		return Response.ok(regSvc.getPmfSvc().listAll()).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getForProductModel(@PathParam("id") String prodModId) {
		return Response.ok(regSvc.getPmfSvc().listByModelId(UUID.fromString(prodModId))).build();
	}

	@GET
	@Path("/type")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FieldType> getFieldTypeList() {
		return new ArrayList<FieldType>(EnumSet.allOf(FieldType.class));
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(JsonObject pmfjs) {
		logger.info(pmfjs.toString());
		if (!pmfjs.containsKey("model")) return Response.status(Status.BAD_REQUEST).entity("model cannot be null").build();
		UUID modelId = UUID.fromString(pmfjs.getJsonObject("model").getString("id", UUID.randomUUID().toString()));
		ProductModel pm = regSvc.getPmSvc().findById(modelId);
		if (pm == null) return Response.status(Status.BAD_REQUEST).entity("Invalid model: " + pmfjs.getJsonObject("model").getString("id")).build();
		ProductModelField pmf = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(pmfjs.toString(), ProductModelField.class);

		pmf.setModel(pm);
		pm.getFields().add(pmf);
		return Response.ok(regSvc.getPmfSvc().persist(pmf)).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") UUID id, ProductModelField pmf) {
		if (id == null || pmf.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!pmf.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		return Response.ok(regSvc.getPmfSvc().persist(pmf)).build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteById(@PathParam("id") String id) {
		regSvc.getPmfSvc().removeById(UUID.fromString(id));
		return Response.ok().build();
	}

}
