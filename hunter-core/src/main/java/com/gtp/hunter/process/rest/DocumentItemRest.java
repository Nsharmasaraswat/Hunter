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

import org.slf4j.Logger;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.repository.DocumentItemRepository;
import com.gtp.hunter.process.repository.DocumentRepository;
import com.gtp.hunter.process.repository.ProductRepository;

@RequestScoped
@Path("/documentitem")
public class DocumentItemRest {

	@Inject
	private DocumentItemRepository	diRep;
	@Inject
	private DocumentRepository		dRep;
	@Inject
	private ProductRepository		pRep;
	@Inject
	private transient Logger		logger;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DocumentItem> getAll() {
		return diRep.listAll();
	}

	@GET
	@Path("/{docId}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DocumentItem> getByDocumentId(@PathParam("docId") String docId) {
		return diRep.getByDocumentId(UUID.fromString(docId));
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(DocumentItem doc) {
		return Response.ok(diRep.persist(doc)).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") UUID id, DocumentItem di) {
		if (id == null || di.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!di.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		return Response.ok(diRep.persist(di)).build();
	}

	@POST
	@Path("/list")
	public String saveList(JsonArray json) {

		final int size = json.size();
		final List<DocumentItem> itemList = new ArrayList<>();

		for (int i = 0; i < size; i++) {
			JsonObject j = json.getJsonObject(i);
			final String id = checkField(j, "id");
			final String docId = checkField(j, "docId");
			final String productId = checkField(j, "prodId");
			final int qty = j.containsKey("qty") ? j.getInt("qty") : 0;

			try {
				DocumentItem item = null;
				try {
					UUID uuid = UUID.fromString(id);
					item = diRep.findById(uuid);
				} catch (Exception exception) {
					item = new DocumentItem();
				}

				Document doc = dRep.findById(UUID.fromString(docId));
				Product prod = pRep.findById(UUID.fromString(productId));

				item.setProduct(prod);
				item.setQty(qty);
				item.setDocument(doc);

				itemList.add(item);
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				return "Data not saved. Error: " + e.getLocalizedMessage();
			}
		}

		for (DocumentItem item : itemList) {
			diRep.persist(item);
		}

		return "Ok";
	}

	@DELETE
	@Path("/{id}")
	public String deleteById(@PathParam("id") String id) {
		diRep.removeById(UUID.fromString(id));
		return "Ok";
	}

	private String checkField(JsonObject j, String field) {
		if (j.containsKey(field) && !j.isNull(field)) {
			return j.getString(field);
		}
		return null;
	}

}
