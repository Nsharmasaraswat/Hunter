package com.gtp.hunter.process.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
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

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.repository.DocumentFieldRepository;
import com.gtp.hunter.process.repository.DocumentModelFieldRepository;
import com.gtp.hunter.process.repository.DocumentRepository;

@RequestScoped
@Path("/documentfield")
public class DocumentFieldRest {

	@Inject
	private DocumentRepository				dRep;
	@Inject
	private DocumentFieldRepository			dfRep;
	@Inject
	private DocumentModelFieldRepository	dmfRep;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DocumentField> getAll() {
		return dfRep.listAll();
	}

	@GET
	@Path("/{docId}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DocumentField> getByDocumentId(@PathParam("docId") String docId) {

		Document d = dRep.findById(UUID.fromString(docId));

		if (d == null)
			return null;

		List<DocumentField> dfList = dfRep.listByDocumentId(d.getId());
		List<DocumentModelField> dmfList = dmfRep.listByModelMetaname(d.getModel().getMetaname());

		if (dmfList.size() > dfList.size()) {
			// new field was created after document was generated
			// collect current fields to compare with model list
			final List<String> currentFields = new ArrayList<>();
			for (DocumentField df : dfList) {
				currentFields.add(df.getMetaname());
			}

			// compare with model list and add the missing ones
			for (DocumentModelField dmf : dmfList) {
				if (!currentFields.contains(dmf.getMetaname())) {
					final DocumentField field = new DocumentField();
					field.setDocument(d);
					field.setName(dmf.getName());
					field.setMetaname(dmf.getMetaname());
					field.setField(dmf);

					dfRep.persist(field);

					dfList.add(field);
				}
			}
		}

		return dfList;
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(DocumentField doc) {
		return Response.ok(dfRep.persist(doc)).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") UUID id, DocumentField df) {
		if (id == null || df.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!df.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		return Response.ok(dfRep.persist(df)).build();
	}

	@POST
	@Path("/list")
	public String saveList(JsonArray json) {

		final int size = json.size();

		for (int i = 0; i < size; i++) {
			JsonObject j = json.getJsonObject(i);
			final String id = j.containsKey("id") ? j.getString("id") : null;
			final String value = j.containsKey("valor") ? j.getString("valor") : null;
			DocumentField field = null;

			if (id == null)
				return "id is NULL";

			field = dfRep.findById(UUID.fromString(id));

			if (field == null)
				return "No DocumentField found in database for uuid: " + id;

			field.setValue(value);
			dfRep.persist(field);
		}

		return "Ok";
	}

	@DELETE
	@Path("/{id}")
	public Response deleteById(@PathParam("id") String id) {
		dfRep.removeById(UUID.fromString(id));
		return Response.ok("Ok").build();
	}

	@PUT
	@Path("/value/{dfid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response changeValue(@PathParam("dfid") UUID docFieldId, String value) {
		DocumentField df = dfRep.findById(docFieldId);

		df.setValue(value);
		return Response.ok(df).build();
	}
}
