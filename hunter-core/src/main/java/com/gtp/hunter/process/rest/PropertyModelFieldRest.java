package com.gtp.hunter.process.rest;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
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

import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.enums.FieldType;
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.service.RegisterService;

@Transactional
@RequestScoped
@Path("/propertymodelfield")
public class PropertyModelFieldRest {

	@Inject
	private RegisterService regSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PropertyModelField> getAll() {
		return regSvc.getPrmfSvc().listAll();
	}

	@GET
	@Path("/model/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PropertyModelField> listByPropertyModelId(@PathParam("id") String propModId) {
		return regSvc.getPrmfSvc().listByModelId(UUID.fromString(propModId));
	}

	@GET
	@Path("/model/{metaname}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PropertyModelField> listByModelMetaname(@PathParam("metaname") String metaname) {
		final PropertyModel pm = regSvc.getPrmSvc().findByMetaname(metaname);

		if (pm != null) {
			return regSvc.getPrmfSvc().listByModelId(pm.getId());
		}
		return new ArrayList<>();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findById(@PathParam("id") UUID id) {
		return Response.ok(regSvc.getPrmfSvc().findById(id)).build();
	}

	@GET
	@Path("/types")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FieldType> getFieldTypeList() {
		return new ArrayList<FieldType>(EnumSet.allOf(FieldType.class));
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(JsonObject prmfjs) {
		if (!prmfjs.containsKey("model")) return Response.status(Status.BAD_REQUEST).entity("model cannot be null").build();
		UUID modelId = UUID.fromString(prmfjs.getJsonObject("model").getString("id", UUID.randomUUID().toString()));
		PropertyModel prm = regSvc.getPrmSvc().findById(modelId);
		if (prm == null) return Response.status(Status.BAD_REQUEST).entity("Invalid model: " + prmfjs.getJsonObject("model").getString("id")).build();
		PropertyModelField prmf = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(prmfjs.toString(), PropertyModelField.class);

		prmf.setModel(prm);
		prm.getFields().add(prmf);
		return Response.ok(regSvc.getPrmfSvc().persist(prmf)).build();
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") UUID id, PropertyModelField prmf) {
		if (id == null || prmf.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!prmf.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		return Response.ok(regSvc.getPrmfSvc().persist(prmf)).build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteById(@PathParam("id") String id) {
		regSvc.getPrmfSvc().removeById(UUID.fromString(id));
		return Response.ok().build();
	}

}
