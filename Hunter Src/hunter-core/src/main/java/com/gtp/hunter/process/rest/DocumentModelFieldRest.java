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
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.repository.DocumentModelFieldRepository;
import com.gtp.hunter.process.repository.DocumentModelRepository;

@RequestScoped
@Path("/documentmodelfield")
public class DocumentModelFieldRest {

	@Inject
	private DocumentModelFieldRepository	dmfRep;
	@Inject
	private DocumentModelRepository			dmRep;
	@Inject
	private transient Logger				logger;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DocumentModelField> getAll() {
		return dmfRep.listAll();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public DocumentModelField getById(@PathParam("id") String id) {
		DocumentModelField dmf = null;
		try {
			dmf = dmfRep.findById(UUID.fromString(id));
		} catch (Exception e) {
			logger.error("Creating new DocumentModelField " + e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			dmf = new DocumentModelField();
		}
		return dmf;
	}

	@GET
	@Path("/metaname/{metaname}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DocumentModelField> getForDocumentModelMetaname(@PathParam("metaname") String metaname) {
		List<DocumentModelField> list = dmfRep.listByModelMetaname(metaname);
		return list;
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
	public Response save(JsonObject dmfjs) {
		if (!dmfjs.containsKey("model")) return Response.status(Status.BAD_REQUEST).entity("model cannot be null").build();
		UUID modelId = UUID.fromString(dmfjs.getJsonObject("model").getString("id", UUID.randomUUID().toString()));
		DocumentModel dm = dmRep.findById(modelId);
		if (dm == null) return Response.status(Status.BAD_REQUEST).entity("Invalid model: " + dmfjs.getJsonObject("model").getString("id")).build();
		DocumentModelField dmf = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(dmfjs.toString(), DocumentModelField.class);

		dmf.setModel(dm);
		dm.getFields().add(dmf);
		return Response.ok(dmfRep.persist(dmf)).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") UUID id, DocumentModelField dmf) {
		if (id == null || dmf.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!dmf.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		return Response.ok(dmfRep.persist(dmf)).build();
	}

	@DELETE
	@Path("/{id}")
	public String deleteById(@PathParam("id") String id) {
		dmfRep.removeById(UUID.fromString(id));
		return "Ok";
	}

}
