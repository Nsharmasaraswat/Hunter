package com.gtp.hunter.process.rest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.repository.DocumentFieldRepository;
import com.gtp.hunter.process.repository.DocumentItemRepository;
import com.gtp.hunter.process.repository.DocumentModelFieldRepository;
import com.gtp.hunter.process.repository.DocumentModelRepository;
import com.gtp.hunter.process.repository.DocumentRepository;
import com.gtp.hunter.process.service.RegisterService;

@RequestScoped
@Path("/mobile")
public class MobileProcessRest {

	@Inject
	private DocumentRepository				dRep;
	@Inject
	private DocumentItemRepository			diRep;
	@Inject
	private DocumentModelRepository			dmRep;
	@Inject
	private DocumentModelFieldRepository	dmfRep;
	@Inject
	private DocumentFieldRepository			dfRep;
	@Inject
	private RegisterService					regSvc;

	@GET
	@Path("/document/{doccode}/{type}")
	@PermitAll
	public JsonArray getDocumentMobile(@PathParam("doccode") String docCode, @PathParam("type") String type, @QueryParam("mobileId") Integer mobileId) {
		final JsonArrayBuilder items = Json.createArrayBuilder();
		final JsonObjectBuilder status = Json.createObjectBuilder();
		final Document d = dRep.quickFindByCodeAndModelMetaname(docCode, type);
		String errorMessage = "";

		if (d != null) {
			// List<DocumentItem> lstdi =
			// diRep.getQuickDocumentItemListByDocumentThingStatus(d.getId(), "EMBARCANDO",
			// "EMBARCADO");
			// FIXME: AAAAAAAAAAAAARGH QUE NOOOOJOOOOOOOOO
			List<DocumentItem> lstdi = new ArrayList<DocumentItem>();
			DocumentModelField dmf = dmfRep.findByMetaname("UNIT");

			if (type.equals("NFSAIDA")) {
				if (mobileId != null) {
					d.setFields(new HashSet<DocumentField>(dfRep.listByDocumentId(d.getId())));
					boolean locked = d.getFields().stream().anyMatch(f -> f.getField().getId().equals(dmf.getId()));

					if (!locked || d.getFields().stream().allMatch(f -> f.getValue().equalsIgnoreCase(String.valueOf(mobileId)))) {
						lstdi = diRep.getQuickDocumentItemListByDocumentThingStatus(d.getId(), "SEPARADO", "EXPEDIDO");

						if (!locked) {
							DocumentField df = new DocumentField();

							df.setDocument(d);
							df.setMetaname(dmf.getMetaname());
							df.setName(dmf.getName());
							df.setValue(String.valueOf(mobileId));
							df.setField(dmf);
							dfRep.persist(df);
						}
					} else
						errorMessage = "Documento Aberto no Coletor " + d.getFields().stream().filter(f -> f.getField().getId().equals(dmf.getId())).findFirst().get().getValue();
				} else
					errorMessage = "Versão Incorreta. Atualizar Coletor";

				status.add("status", errorMessage.isEmpty());
				status.add("message", errorMessage);
				lstdi.forEach(di -> {
					JsonObjectBuilder responseObject = Json.createObjectBuilder();

					responseObject.add("sku", di.getProduct().getSku());
					responseObject.add("description", di.getProduct().getName());
					responseObject.add("quantity", di.getQtdThings());
					responseObject.add("total", new Double(di.getQty()).intValue());
					items.add(responseObject);
				});
				items.add(status);
			} else {
				lstdi = diRep.getQuickDocumentItemListByDocumentThingStatus(d.getId(), "EMBARCANDO", "EMBARCADO");
				lstdi.forEach(di -> {
					JsonObjectBuilder responseObject = Json.createObjectBuilder();

					responseObject.add("sku", di.getProduct().getSku());
					responseObject.add("description", di.getProduct().getName());
					responseObject.add("quantity", di.getQtdThings());
					responseObject.add("total", new Double(di.getQty()).intValue());
					items.add(responseObject);
				});
			}

		}

		return items.build();
	}

	@GET
	@Path("/document/unlock/{doccode}/{type}")
	@PermitAll
	public JsonArray unlockDocumentMobile(@PathParam("doccode") String docCode, @PathParam("type") String type, @QueryParam("mobileId") Integer mobileId) {
		final JsonArrayBuilder resp = Json.createArrayBuilder();
		final JsonObjectBuilder status = Json.createObjectBuilder();
		final Document d = dRep.quickFindByCodeAndModelMetaname(docCode, type);
		String errorMessage = "";

		if (d != null) {
			DocumentModelField dmf = dmfRep.findByMetaname("UNIT");

			if (mobileId != null) {
				List<DocumentField> fields = dfRep.listByDocumentId(d.getId());

				fields.forEach(f -> {
					if (f.getField().getId().equals(dmf.getId())) {
						dfRep.removeById(f.getId());
					}
				});
			} else
				errorMessage = "Versão Incorreta. Atualizar Coletor";
		} else {
			errorMessage = "Documento Inexistente";
		}
		status.add("status", errorMessage.isEmpty());
		status.add("message", errorMessage);
		resp.add(status);
		return resp.build();
	}

	@GET
	@Path("/documentthings/{doccode}/{type}")
	@PermitAll
	public JsonArray getDocumentThingsMobile(@PathParam("doccode") String docCode, @PathParam("type") String type) {
		final JsonArrayBuilder items = Json.createArrayBuilder();
		DocumentModel dm = dmRep.findByMetaname(type);
		Document d = dRep.findByModelAndCode(dm, docCode);

		if (d != null) {
			d.getThings().forEach(dt -> {
				JsonObjectBuilder responseObject = Json.createObjectBuilder();
				Unit u = regSvc.getUnSvc().getUnitById(dt.getThing().getUnits().stream().findFirst().get());

				responseObject.add("unit.uid", u.getTagId());
				responseObject.add("unit.idtecnologia", "2");
				responseObject.add("unit.idtipoetiqueta", "1");
				responseObject.add("unit.idregravalidacaoparms", "1");
				responseObject.add("coisa.codigo", 0);
				responseObject.add("coisa.descricao", dt.getThing().getProduct().getName());
				responseObject.add("coisa.urlicone", "");
				responseObject.add("coisa.urlphoto", "");
				responseObject.add("coisaextra.id", "");
				responseObject.add("coisaextra.idcoisa", "");
				responseObject.add("coisaextra.sexo", "");
				responseObject.add("coisaextra.nascimento", dt.getThing().getProperties().stream().filter(p -> p.getField().getMetaname().equals("MANUFACTURE")).findFirst().get().getValue());
				responseObject.add("coisaextra.obs", dt.getThing().getProperties().stream().filter(p -> p.getField().getMetaname().equals("BATCH")).findFirst().get().getValue());
				responseObject.add("coisaextra.reserved1", dt.getThing().getProperties().stream().filter(p -> p.getField().getMetaname().equals("EXPIRY")).findFirst().get().getValue());
				responseObject.add("coisaextra.reserved2", dt.getThing().getProduct().getSku());
				items.add(responseObject);
			});
		}
		return items.build();
	}
	//TODO: Consrtar
	//	@GET
	//	@Path("/documentthings/{doccode}/{type}")
	//	@PermitAll
	//	public JsonArray getDocumentThingsMobile(@PathParam("doccode") String docCode, @PathParam("type") String type) {
	//		final JsonArrayBuilder items = Json.createArrayBuilder();
	//		List<DocumentThing> dtList = dtRep.listNoParentsWithThingPropertyAndProductByTypeCode(type, docCode);
	//
	//		for (DocumentThing dt : dtList) {
	//			JsonObjectBuilder responseObject = Json.createObjectBuilder();
	//			Optional<Property> manufacture = dt.getThing().getProperties().stream().filter(p -> p.getField().getMetaname().equals("MANUFACTURE")).findFirst();
	//			Optional<Property> batch = dt.getThing().getProperties().stream().filter(p -> p.getField().getMetaname().equals("BATCH")).findFirst();
	//			Optional<Property> expiry = dt.getThing().getProperties().stream().filter(p -> p.getField().getMetaname().equals("EXPIRY")).findFirst();
	//			Product prd = dt.getThing().getProduct();
	//
	//			responseObject.add("unit.uid", dt.getUnit());
	//			responseObject.add("unit.idtecnologia", "2");
	//			responseObject.add("unit.idtipoetiqueta", "1");
	//			responseObject.add("unit.idregravalidacaoparms", "1");
	//			responseObject.add("coisa.codigo", 0);
	//			responseObject.add("coisa.descricao", prd.getName());
	//			responseObject.add("coisa.urlicone", "");
	//			responseObject.add("coisa.urlphoto", "");
	//			responseObject.add("coisaextra.id", "");
	//			responseObject.add("coisaextra.idcoisa", "");
	//			responseObject.add("coisaextra.sexo", "");
	//			responseObject.add("coisaextra.nascimento", (manufacture.isPresent() && manufacture.get().getValue() != null) ? manufacture.get().getValue() : "INVALID");
	//			responseObject.add("coisaextra.obs", (batch.isPresent() && batch.get().getValue() != null) ? batch.get().getValue() : "INVALID");
	//			responseObject.add("coisaextra.reserved1", (expiry.isPresent() && expiry.get().getValue() != null) ? expiry.get().getValue() : "INVALID");
	//			responseObject.add("coisaextra.reserved2", prd.getSku());
	//			items.add(responseObject);
	//		}
	//		return items.build();
	//	}
}
