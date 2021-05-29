package com.gtp.hunter.process.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
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

import org.slf4j.Logger;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.repository.DocumentModelRepository;
import com.gtp.hunter.process.service.DocumentService;
import com.gtp.hunter.process.service.PersonService;

@Transactional
@RequestScoped
@Path("/document")
public class DocumentRest {

	@Inject
	private DocumentModelRepository	dmRep;

	@Inject
	private DocumentService			dSvc;

	@Inject
	private PersonService			psSvc;

	@Inject
	private transient Logger		logger;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Document> getAll() {
		return dSvc.listAll();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Document getById(@PathParam("id") UUID id) {
		return dSvc.findById(id);
	}

	@GET
	@Path("/quickByTypeAndCode/{type}/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	public Document quickByTypeAndCode(@PathParam("type") String type, @PathParam("code") String code) {
		Document doc = null;
		try {
			doc = dSvc.quickFindByCodeAndModelMetaname(code, type);
			doc = dSvc.findById(doc.getId());
			//			doc.setItens(new HashSet<DocumentItem>(diRep.getQuickDocumentItemListByDocument(doc.getId())));
			//			doc.setThings(new HashSet<DocumentThing>(dtRep.getByDocumentId(doc.getId())));
		} catch (Exception e) {
			logger.error("Creating new Document" + e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			doc = new Document();
		}
		return doc;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(Document d) {
		if (d.getPerson() != null && d.getPerson().getId() != null)
			d.setPerson(psSvc.findById(d.getPerson().getId()));
		d.getSiblings().forEach(ds -> {
			ds.setParent(d);
			if (ds.getPerson() != null && ds.getPerson().getId() != null)
				ds.setPerson(psSvc.findById(ds.getPerson().getId()));
		});
		if (d.getParent_id() != null) {
			logger.info("Setting parent id " + d.getParent_id());
			d.setParent(dSvc.findById(UUID.fromString(d.getParent_id())));
		}
		d.getFields().forEach(df -> df.setDocument(d));
		d.getSiblings().forEach(ds -> ds.getFields().forEach(df -> df.setDocument(ds)));
		return Response.ok(dSvc.findById(dSvc.persist(d).getId())).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDocument(@PathParam("id") UUID id, Document tmp) {
		if (id == null || tmp.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!tmp.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		if (tmp.getPerson() != null && tmp.getPerson().getId() != null)
			tmp.setPerson(psSvc.findById(tmp.getPerson().getId()));
		if (tmp.getParent() == null && tmp.getParent_id() != null && !tmp.getParent_id().isEmpty())
			tmp.setParent(dSvc.findById(UUID.fromString(tmp.getParent_id())));
		tmp.getSiblings().forEach(ds -> {
			ds.setParent(tmp);
			if (ds.getPerson() != null && ds.getPerson().getId() != null)
				ds.setPerson(psSvc.findById(ds.getPerson().getId()));
		});
		tmp.getFields().forEach(df -> df.setDocument(tmp));
		tmp.getSiblings().forEach(ds -> ds.getFields().forEach(df -> df.setDocument(ds)));
		return Response.ok(dSvc.findById(dSvc.persist(tmp).getId())).build();
	}

	@DELETE
	@Path("/{id}")
	public String deleteById(@PathParam("id") String id) {
		try {
			dSvc.removeById(UUID.fromString(id));
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			return "Error";
		}
		return "Ok";
	}

	@GET
	@Path("/parent/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Document findParent(@PathParam("id") String id) {
		Document parent = dSvc.quickFindParentDoc(id);
		return dSvc.findById(parent.getId());
	}

	@GET
	@Path("/bytype/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Document> getListByType(@PathParam("type") String type) {
		//		DocumentModel pm = dmRep.findByMetaname(type);
		//		return dRep.listByField("model", pm);
		return dSvc.listByModelMeta(type);
	}

	@GET
	@Path("/bytypestatus/{type}/{status}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Document> listByTypeStatus(@PathParam("type") String type, @PathParam("status") String status) {
		return dSvc.listByTypeAndStatus(type, status);
	}

	@GET
	@Path("/bytypecode/{type}/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	public Document findByTypeCode(@PathParam("type") String type, @PathParam("code") String code) {
		return dSvc.findByTypeAndCode(type, code);
	}

	@GET
	@Path("/bypersontypecode/{persontype}/{personcode}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Document> listOrphanByPersonField(@PathParam("persontype") String personType, @PathParam("personcode") String personCode) {
		Profiler pf = new Profiler("bypersontpecode/{persontype}/{personcode}");
		List<Document> dList = dSvc.listOrphanByPersonTypeAndCode(personType, personCode);

		pf.done("got list", false, true);
		//TODO: Fix Siblings JSON
		return dList.stream().map(d -> {
			d.setSiblings(null);
			return d;
		}).collect(Collectors.toList());
	}

	@GET
	@Path("/quickByType/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Document> getQuickListByType(@PathParam("type") String type) {
		return dSvc.listByModelMeta(type);
	}

	@GET
	@Path("/quickOrphanedByType/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Document> getQuickListOrphanedByType(@PathParam("type") String type) {
		return dSvc.getQuickListOrphanedByType(type);
	}

	@GET
	@Path("/quickByTypeStatus/{type}/{status}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Document> getQuickListByTypeStatus(@PathParam("type") String type, @PathParam("status") String status) {
		return dSvc.quickListByTypeStatus(type, status);
	}

	@GET
	@Path("/quickChildrenByType/{id}/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Document> quickChildrenListByType(@PathParam("id") String id, @PathParam("type") String type) {
		return dSvc.getQuickChildrenListByType(UUID.fromString(id), type);
	}

	@GET
	@Path("/quickChildrenByTypeStatus/{id}/{type}/{status}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<Document> quickChildrenListByTypeStatus(@PathParam("id") String id, @PathParam("type") String type, @PathParam("status") String status) {
		return dSvc.getQuickChildrenListByTypeStatus(UUID.fromString(id), type, status);
	}

	@GET
	@Path("/newDocument/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public Document createNewDocument(@PathParam("type") String type) {
		final Document doc = new Document();
		final DocumentModel model = dmRep.findByMetaname(type);
		final String code = String.valueOf(Integer.parseInt(ConfigUtil.get("hunter-process", "last-oo-document-code", "1")) + 1);

		doc.setName(model.getName() + " #" + code);
		doc.setCode(code);
		doc.setModel(model);
		dSvc.persist(doc);
		ConfigUtil.put("hunter-process", "last-oo-document-code", code);
		return doc;
	}

	@PUT
	@Path("/saveChildren/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Document saveChildren(@PathParam("id") String id, JsonArray json) {
		Document parent = dSvc.findById(UUID.fromString(id));

		for (int i = 0; i < json.size(); i++) {
			JsonObject o = json.getJsonObject(i);
			String chId = o.getString("id");
			Document child = dSvc.findById(UUID.fromString(chId));

			child.setParent(parent);
			dSvc.persist(child);
		}
		return parent;
	}

	@PUT
	@Path("/addchild/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Document addChild(@PathParam("id") UUID id, JsonObject childObj) {
		Document parent = dSvc.findById(id);
		Document child = dSvc.findById(UUID.fromString(childObj.getString("child-id")));

		child.setParent(parent);
		dSvc.persist(child);
		return parent;
	}

	@PUT
	@Path("/remchild/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Document remChild(@PathParam("id") UUID id, JsonObject childObj) {
		Document child = dSvc.findById(UUID.fromString(childObj.getString("child-id")));

		child.setParent(null);
		dSvc.persist(child);
		return dSvc.findById(id);
	}

	@PUT
	@Path("/quickchangestatus/{id}/{status}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response quickChangeStatus(@PathParam("id") UUID id, @PathParam("status") String status) {
		Document d = dSvc.findById(id);

		if (d != null) {
			dSvc.quickUpdateStatus(d.getId(), status);
			return Response.ok("Status Alterado com Sucesso").build();
		}
		return Response.notModified("Documento Inexistente").build();
	}

	@GET
	@Path("/byThingIdTransports/{thingId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response byThingIdTransports(@PathParam("thingId") UUID thId) {
		return Response.ok(dSvc.listByTransportThingId(thId)).build();
	}

	@GET
	@Path("/byTypeFieldValue/{model}/{field}/{value}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response byTypeFieldValue(@PathParam("model") String model, @PathParam("field") String field, @PathParam("value") String value) {
		return Response.ok(dSvc.listByTypeFieldValue(model, field, value)).build();
	}

	@GET
	@Path("/byTypeFrom/{model}/{from}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response byTypeFrom(@PathParam("model") String model, @PathParam("from") String from) {
		try {
			Date created = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).parse(from);

			return Response.ok(dSvc.listByTypeFrom(model, created)).build();
		} catch (ParseException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Date " + from).build();
		}
	}
}
