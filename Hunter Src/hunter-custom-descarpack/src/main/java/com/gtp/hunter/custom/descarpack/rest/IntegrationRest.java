package com.gtp.hunter.custom.descarpack.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.custom.descarpack.service.IntegrationService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductField;
import com.gtp.hunter.process.model.ProductModel;
import com.gtp.hunter.process.model.ProductModelField;
import com.gtp.hunter.process.model.Supplier;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.stream.TaskStreamManager;

@Path("integration")
public class IntegrationRest {

	@Inject
	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private IntegrationService	intSvc;

	@EJB(lookup = "java:global/hunter-core-2.0.0-SNAPSHOT/TaskStreamManager!com.gtp.hunter.process.stream.TaskStreamManager")
	private TaskStreamManager	tsm;

	@POST
	@Path("/purchase-order")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response postPO(JsonObject po) throws Exception {
		if (!isParameterValid(po, "po-number"))
			return Response.ok(new IntegrationReturn(false, "po-number is empty")).build();
		if (!isParameterValid(po, "supplier-id"))
			return Response.ok(new IntegrationReturn(false, "supplier-id is empty")).build();
		if (!isParameterValid(po, "items"))
			return Response.ok(new IntegrationReturn(false, "items is empty")).build();
		DocumentModel dmdl = intSvc.getDocModel("PO");
		Document d = intSvc.getDocByCodeAndType(dmdl, po.getString("po-number"));

		if (d == null || d.getStatus().equals("CANCELADO")) {
			Supplier s = intSvc.getSupplierByCode(po.getString("supplier-id"));

			d = new Document(dmdl, dmdl.getName() + " #" + po.getString("po-number"), po.getString("po-number"));
			d.setSupplier(s);
			if (s == null) {
				return Response.ok(new IntegrationReturn(false, "Supplier not found: " + po.getString("supplier-id"))).build();
			}
			if (s.isHasprinter()) {
				d.setStatus("IMPRESSAO" + s.getName().toUpperCase());
			} else {
				d.setStatus("PARAIMPRESSAO");
			}
			intSvc.persistDoc(d);
			JsonArray itens = (JsonArray) po.get("items");
			for (int cnt = 0; cnt < itens.size(); cnt++) {
				JsonObject obj = itens.getJsonObject(cnt);
				if (!isParameterValid(obj, "sku"))
					return Response.ok(new IntegrationReturn(false, "Field sku not found.")).build();
				if (!isParameterValid(obj, "packages"))
					return Response.ok(new IntegrationReturn(false, "Field packages not found.")).build();
				Product prd = intSvc.getBySku(obj.getString("sku"));
				if (prd == null) {
					return Response.ok(new IntegrationReturn(false, "Product not found: " + obj.getString("sku"))).build();
				}
				DocumentItem di = new DocumentItem();
				di.setDocument(d);
				di.setProduct(prd);
				di.setQty(obj.getInt("packages"));
				di.setStatus("NOVO");
				intSvc.persistDocItem(di);
				d.getItens().add(di);
			}
		} else {
			return Response.ok(new IntegrationReturn(false, "Document already exists")).build();
		}
		return Response.ok(IntegrationReturn.OK).build();
	}

	@PUT
	@Path("/purchase-order")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response putPO(JsonObject po) throws Exception {
		if (!isParameterValid(po, "po-number"))
			return Response.ok(new IntegrationReturn(false, "po-number is empty")).build();
		DocumentModel dmdl = intSvc.getDocModel("PO");
		Document d = intSvc.getDocByCodeAndType(dmdl, po.getString("po-number"));

		if (d != null && !d.getStatus().equals("CANCELADO")) {
			if (isParameterValid(po, "supplier-id")) {
				Supplier s = intSvc.getSupplierByCode(po.getString("supplier-id"));
				d.setSupplier(s);
			}
			if (isParameterValid(po, "items")) {
				for (DocumentItem di : d.getItens()) {
					intSvc.getrSvc().getDiRep().removeById(di.getId());
				}

				d.setItens(new HashSet<DocumentItem>());

				JsonArray itens = (JsonArray) po.get("items");
				for (int cnt = 0; cnt < itens.size(); cnt++) {
					JsonObject obj = itens.getJsonObject(cnt);
					if (!isParameterValid(obj, "sku"))
						return Response.ok(new IntegrationReturn(false, "Field sku not found.")).build();
					if (!isParameterValid(obj, "packages"))
						return Response.ok(new IntegrationReturn(false, "Field packages not found.")).build();
					Product prd = intSvc.getBySku(obj.getString("sku"));
					if (prd == null) {
						return Response.ok(new IntegrationReturn(false, "Product not found: " + obj.getString("sku"))).build();
					}
					DocumentItem di = new DocumentItem();
					di.setDocument(d);
					di.setProduct(prd);
					di.setQty(obj.getInt("packages"));
					di.setStatus("NOVO");
					intSvc.persistDocItem(di);
					d.getItens().add(di);
				}
			}
		} else {
			return Response.ok(new IntegrationReturn(false, "Document not found")).build();
		}
		return Response.ok(IntegrationReturn.OK).build();
	}

	@DELETE
	@Path("/purchase-order/{po-number}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response deletePO(@PathParam("po-number") String poNumber) throws Exception {
		if (poNumber == null)
			return Response.ok(new IntegrationReturn(false, "po-number is empty")).build();
		DocumentModel dmdl = intSvc.getDocModel("PO");
		Document d = intSvc.getDocByCodeAndType(dmdl, poNumber);

		if (d != null) {
			return Response.ok(cancelDoc(d, "IMPRESSO", "IMPRESSO")).build();
		} else {
			return Response.ok(new IntegrationReturn(false, "Document not found")).build();
		}
	}

	@POST
	@Path("/manufacture-order")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response postMO(JsonObject po) throws Exception {
		if (!isParameterValid(po, "mo-number"))
			return Response.ok(new IntegrationReturn(false, "mo-number is empty")).build();
		if (!isParameterValid(po, "items"))
			return Response.ok(new IntegrationReturn(false, "items is empty")).build();
		DocumentModel dmdl = intSvc.getDocModel("MO");
		logger.info("Procurando Document " + po.getString("mo-number"));
		Document d = intSvc.getDocByCodeAndType(dmdl, po.getString("mo-number"));
		if (d == null || d.getStatus().equals("CANCELADO")) {
			String supId = !isParameterValid(po, "supplier-id") || po.getString("supplier-id").isEmpty() ? "DESCARPACK" : po.getString("supplier-id");
			Supplier s = intSvc.getSupplierByCode(supId);

			if (s == null || s.getStatus().equals("CANCELADO"))
				return Response.ok(new IntegrationReturn(false, "Supplier not found: " + supId)).build();
			d = new Document(dmdl, dmdl.getName() + " #" + po.getString("mo-number"), po.getString("mo-number"));
			d.setStatus("PARAIMPRESSAO");
			d.setSupplier(s);
			intSvc.persistDoc(d);
			JsonArray itens = (JsonArray) po.get("items");
			for (int cnt = 0; cnt < itens.size(); cnt++) {
				JsonObject obj = itens.getJsonObject(cnt);
				if (!isParameterValid(obj, "sku"))
					return Response.ok(new IntegrationReturn(false, "Field sku not found.")).build();
				if (!isParameterValid(obj, "packages"))
					return Response.ok(new IntegrationReturn(false, "Field packages not found.")).build();
				Product prd = intSvc.getBySku(obj.getString("sku"));
				if (prd == null) {
					return Response.ok(new IntegrationReturn(false, "Product not found: " + obj.getString("sku"))).build();
				}
				DocumentItem di = new DocumentItem();
				di.setDocument(d);
				di.setProduct(prd);
				di.setQty(obj.getInt("packages"));
				di.setStatus("NOVO");
				intSvc.persistDocItem(di);
				d.getItens().add(di);
			}
		} else {
			return Response.ok(new IntegrationReturn(false, "Document already exists")).build();
		}

		return Response.ok(IntegrationReturn.OK).build();
	}

	@PUT
	@Path("/manufacture-order")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response putMO(JsonObject po) throws Exception {
		if (!isParameterValid(po, "mo-number"))
			return Response.ok(new IntegrationReturn(false, "mo-number is empty")).build();
		DocumentModel dmdl = intSvc.getDocModel("MO");
		//logger.info("Procurando Document " + po.getString("mo-number"));
		Document d = intSvc.getDocByCodeAndType(dmdl, po.getString("mo-number"));
		if (d != null && !d.getStatus().equals("CANCELADO")) {
			if (isParameterValid(po, "supplier-id")) {
				Supplier s = intSvc.getSupplierByCode(po.getString("supplier-id"));
				d.setSupplier(s);
			}
			// TODO: Alterar itens
			if (isParameterValid(po, "items")) {
				for (DocumentItem di : d.getItens()) {
					intSvc.getrSvc().getDiRep().removeById(di.getId());
				}

				d.setItens(new HashSet<DocumentItem>());

				JsonArray itens = (JsonArray) po.get("items");
				for (int cnt = 0; cnt < itens.size(); cnt++) {
					JsonObject obj = itens.getJsonObject(cnt);
					if (!isParameterValid(obj, "sku"))
						return Response.ok(new IntegrationReturn(false, "Field sku not found.")).build();
					if (!isParameterValid(obj, "packages"))
						return Response.ok(new IntegrationReturn(false, "Field packages not found.")).build();
					Product prd = intSvc.getBySku(obj.getString("sku"));
					if (prd == null) {
						return Response.ok(new IntegrationReturn(false, "Product not found: " + obj.getString("sku"))).build();
					}
					DocumentItem di = new DocumentItem();
					di.setDocument(d);
					di.setProduct(prd);
					di.setQty(obj.getInt("packages"));
					di.setStatus("NOVO");
					intSvc.persistDocItem(di);
					d.getItens().add(di);
				}
			}
		} else {
			return Response.ok(new IntegrationReturn(false, "Document not found")).build();
		}

		return Response.ok(IntegrationReturn.OK).build();
	}

	@DELETE
	@Path("/manufacture-order/{mo-number}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response deleteMO(@PathParam("mo-number") String moNumber) throws Exception {
		if (moNumber == null)
			return Response.ok(new IntegrationReturn(false, "mo-number is empty")).build();
		DocumentModel dmdl = intSvc.getDocModel("MO");
		//logger.debug("Procurando Document " + moNumber);
		Document d = intSvc.getDocByCodeAndType(dmdl, moNumber);
		if (d != null) {
			return Response.ok(cancelDoc(d, "IMPRESSO", "IMPRESSO")).build();
		} else {
			return Response.ok(new IntegrationReturn(false, "Document not found")).build();
		}
	}

	@POST
	@Path("/inbound-no-label")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response postInboundNoLabel(JsonObject po) throws Exception {
		if (!isParameterValid(po, "po-number"))
			return Response.ok(new IntegrationReturn(false, "po-number is empty")).build();
		if (!isParameterValid(po, "import-number"))
			return Response.ok(new IntegrationReturn(false, "import-number is empty")).build();
		if (!isParameterValid(po, "invoice-number"))
			return Response.ok(new IntegrationReturn(false, "invoice-number is empty")).build();
		if (!isParameterValid(po, "items"))
			return Response.ok(new IntegrationReturn(false, "items is empty")).build();
		DocumentModel dmpo = intSvc.getDocModel("PO");
		Document pod = intSvc.getDocByCodeAndType(dmpo, po.getString("po-number"));

		if (pod != null && !pod.getStatus().equals("CANCELADO")) {
			DocumentModel dmdl = intSvc.getDocModel("NFENT");
			Document d = intSvc.getDocByCodeAndType(dmdl, po.getString("invoice-number"));
			if (d == null || d.getStatus().equals("CANCELADO")) {
				DocumentField df = new DocumentField();
				DocumentModelField dmf = dmdl.getFields().stream().filter(dm -> dm.getMetaname().equals("DI")).findFirst().get();
				JsonArray itens = (JsonArray) po.get("items");
				Map<String, Integer> skuQty = new HashMap<>();

				for (int cnt = 0; cnt < itens.size(); cnt++) {
					JsonObject i = itens.getJsonObject(cnt);
					if (!isParameterValid(i, "sku"))
						return Response.ok(new IntegrationReturn(false, "at least one sku is empty")).build();
					if (!isParameterValid(i, "packages"))
						return Response.ok(new IntegrationReturn(false, "at least one packages is empty")).build();
					String sku = i.getString("sku");
					int qty = i.getInt("packages");

					// TODO: Check sku/quantity on PO
					skuQty.put(sku, qty);
				}
				d = new Document(dmdl, dmdl.getName() + " #" + po.getString("invoice-number"), po.getString("invoice-number"));
				d.setSupplier(pod.getSupplier());
				d.setParent(pod);
				d.setStatus("PARAIMPRESSAO");
				intSvc.persistDoc(d);
				df.setDocument(d);
				df.setField(dmf);
				df.setValue(po.getString("import-number"));
				df.setName(dmf.getName());
				df.setMetaname(dmf.getMetaname());
				intSvc.persistDocumentField(df);
				for (String s : skuQty.keySet()) {
					DocumentItem di = new DocumentItem();
					Product prd = intSvc.getBySku(s);

					if (prd == null) {
						return Response.ok(new IntegrationReturn(false, "Product not found: " + s)).build();
					}
					di.setDocument(d);
					di.setProduct(prd);
					di.setQty(skuQty.get(s));
					di.setStatus("NOVO");
					intSvc.persistDocItem(di);
					d.getItens().add(di);
				}
			} else {
				return Response.ok(new IntegrationReturn(false, "Document Already Exists")).build();
			}
		} else {
			return Response.ok(new IntegrationReturn(false, "Purchase Order not found")).build();
		}

		return Response.ok(IntegrationReturn.OK).build();
	}

	@PUT
	@Path("/inbound-no-label")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response putInboundNoLabel(JsonObject po) throws Exception {
		if (!isParameterValid(po, "invoice-number"))
			return Response.ok(new IntegrationReturn(false, "invoice-number is empty")).build();
		DocumentModel dmdl = intSvc.getDocModel("NFENT");
		// DocumentModel dmpo = intSvc.getDocModel("PO");
		Document d = intSvc.getDocByCodeAndType(dmdl, po.getString("invoice-number"));
		if (d != null && !d.getStatus().equals("CANCELADO")) {
			if (!isParameterValid(po, "po-number")) {
				// Document pod = intSvc.getDocByCodeAndType(dmpo, po.getString("po-number"));
			}
			if (!isParameterValid(po, "import-number")) {
			}
			if (!isParameterValid(po, "items")) {
			}
		} else {
			return Response.ok(new IntegrationReturn(false, "Document not found")).build();
		}

		return Response.ok(IntegrationReturn.OK).build();
	}

	@DELETE
	@Path("/inbound-no-label/{invoice-number}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response deleteInboundNoLabel(@PathParam("invoice-number") String invoice) throws Exception {
		if (invoice == null)
			return Response.ok(new IntegrationReturn(false, "invoice-number is empty")).build();
		DocumentModel dmdl = intSvc.getDocModel("NFENT");
		Document d = intSvc.getDocByCodeAndType(dmdl, invoice);
		if (d != null) {
			return Response.ok(cancelDoc(d, "IMPRESSO", "ARMAZENADO")).build();
		} else {
			return Response.ok(new IntegrationReturn(false, "Document not found")).build();
		}
	}

	@POST
	@Path("/inbound-label")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response postInboundLabel(JsonObject inbound) throws Exception {
		if (!isParameterValid(inbound, "po-number"))
			return Response.ok(new IntegrationReturn(false, "po-number is empty")).build();
		if (!isParameterValid(inbound, "import-number"))
			return Response.ok(new IntegrationReturn(false, "import-number is empty")).build();
		if (!isParameterValid(inbound, "invoice-number"))
			return Response.ok(new IntegrationReturn(false, "invoice-number is empty")).build();
		if (!isParameterValid(inbound, "items"))
			return Response.ok(new IntegrationReturn(false, "items is empty")).build();
		DocumentModel dmdl = intSvc.getDocModel("NFENT");
		logger.debug("Procurando Document " + inbound.getString("invoice-number"));
		Document d = intSvc.getDocByCodeAndType(dmdl, inbound.getString("invoice-number"));
		if (d == null || d.getStatus().equals("CANCELADO")) {
			JsonArray itens = (JsonArray) inbound.get("items");
			Map<String, Integer> skuQty = new HashMap<>();

			for (int cnt = 0; cnt < itens.size(); cnt++) {
				JsonObject i = itens.getJsonObject(cnt);

				if (!isParameterValid(i, "sku"))
					return Response.ok(new IntegrationReturn(false, "at least one sku is empty")).build();
				if (!isParameterValid(i, "quantity"))
					return Response.ok(new IntegrationReturn(false, "at least one package is empty")).build();
				String sku = i.getString("sku");
				int qty = i.getInt("packages");

				// TODO: Check sku/quantity on PO
				skuQty.put(sku, qty);
			}
			d = new Document(dmdl, dmdl.getName() + " #" + inbound.getString("invoice-number"), inbound.getString("invoice-number"));
			d.setStatus("IMPRESSO");
			intSvc.persistDoc(d);
			for (String s : skuQty.keySet()) {
				Product prd = intSvc.getBySku(s);

				if (prd == null) {
					return Response.ok(new IntegrationReturn(false, "product not registered - " + s)).build();
				}
				DocumentItem di = new DocumentItem();
				di.setDocument(d);
				di.setProduct(prd);
				di.setQty(skuQty.get(s));
				di.setStatus("NOVO");
				intSvc.persistDocItem(di);
				d.getItens().add(di);
			}
		} else {
			return Response.ok(new IntegrationReturn(false, "Document already exists")).build();
		}

		return Response.ok(IntegrationReturn.OK).build();
	}

	@PUT
	@Path("/inbound-label")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response putInboundLabel(JsonObject inbound) throws Exception {
		if (!isParameterValid(inbound, "po-number"))
			return Response.ok(new IntegrationReturn(false, "po-number is empty")).build();
		DocumentModel dmdl = intSvc.getDocModel("NFENT");
		logger.debug("Procurando Document " + inbound.getString("invoice-number"));
		Document d = intSvc.getDocByCodeAndType(dmdl, inbound.getString("invoice-number"));
		if (d != null && !d.getStatus().equals("CANCELADO")) {
			if (isParameterValid(inbound, "import-number")) {
			}
			if (isParameterValid(inbound, "invoice-number")) {
			}
			if (isParameterValid(inbound, "items")) {
			}
			// TODO: Checar filhos e pais
		} else {
			return Response.ok(new IntegrationReturn(false, "Document not found")).build();
		}

		return Response.ok(IntegrationReturn.OK).build();
	}

	@DELETE
	@Path("/inbound-label/{invoice-number}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response deleteInboundLabel(@PathParam("invoice-number") String invoice) throws Exception {
		if (invoice == null)
			return Response.ok(new IntegrationReturn(false, "invoice-number is empty")).build();
		DocumentModel dmdl = intSvc.getDocModel("NFENT");
		Document d = intSvc.getDocByCodeAndType(dmdl, invoice);
		if (d != null) {
			return Response.ok(cancelDoc(d, "EMBARCADO", "ARMAZENADO")).build();
		} else {
			return Response.ok(new IntegrationReturn(false, "Document not found")).build();
		}
	}

	@POST
	@Path("/invoice")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response postInvoice(JsonObject invoice) throws Exception {
		if (!isParameterValid(invoice, "invoice-number"))
			return Response.ok(new IntegrationReturn(false, "invoice-number is empty")).build();
		if (!isParameterValid(invoice, "items"))
			return Response.ok(new IntegrationReturn(false, "items list empty")).build();

		DocumentModel dmdl = intSvc.getDocModel("NFSAIDA");
		Document d = intSvc.getDocByCodeAndType(dmdl, invoice.getString("invoice-number"));

		if (d == null || d.getStatus().equals("CANCELADO")) {
			JsonArray itens = (JsonArray) invoice.get("items");

			d = new Document(dmdl, dmdl.getName() + " #" + invoice.getString("invoice-number"), invoice.getString("invoice-number"));
			d.setStatus("PICKING");
			intSvc.persistDoc(d);
			for (int cnt = 0; cnt < itens.size(); cnt++) {
				JsonObject obj = itens.getJsonObject(cnt);
				Product prd = intSvc.getBySku(obj.getString("sku"));
				DocumentItem di = new DocumentItem();

				if (prd == null)
					return Response.ok(new IntegrationReturn(false, "Product not found: " + obj.getString("sku"))).build();
				di.setDocument(d);
				di.setProduct(prd);
				if (!isParameterValid(obj, "packages"))
					return Response.ok(new IntegrationReturn(false, "Packages field not found.")).build();
				di.setQty(obj.getInt("packages"));
				di.setStatus("NOVO");
				intSvc.persistDocItem(di);
				d.getItens().add(di);
			}
		} else {
			return Response.ok(new IntegrationReturn(false, "Document already exists")).build();
		}
		return Response.ok(IntegrationReturn.OK).build();
	}

	@DELETE
	@Path("/invoice/{invoice-number}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response deleteInvoice(@PathParam("invoice-number") String invoice) throws Exception {
		if (invoice == null)
			return Response.ok(new IntegrationReturn(false, "invoice-number is empty")).build();
		DocumentModel dmdl = intSvc.getDocModel("NFSAIDA");
		Document d = intSvc.getDocByCodeAndType(dmdl, invoice);

		if (d != null) {
			return Response.ok(cancelDoc(d, "ARMAZENADO", "EXPEDIDO")).build();
		} else {
			return Response.ok(new IntegrationReturn(false, "Document not found")).build();
		}
	}

	@POST
	@Path("/product-register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response postProductRegister(JsonObject prod) throws Exception {
		if (!isParameterValid(prod, "sku"))
			return Response.ok(new IntegrationReturn(false, "sku is empty")).build();
		if (!isParameterValid(prod, "description"))
			return Response.ok(new IntegrationReturn(false, "description is empty")).build();
		if (!isParameterValid(prod, "length"))
			return Response.ok(new IntegrationReturn(false, "length is empty")).build();
		if (!isParameterValid(prod, "width"))
			return Response.ok(new IntegrationReturn(false, "width is empty")).build();
		if (!isParameterValid(prod, "height"))
			return Response.ok(new IntegrationReturn(false, "height is empty")).build();
		if (!isParameterValid(prod, "max-pile") || !isNumber(prod, "max-pile"))
			return Response.ok(new IntegrationReturn(false, "max-pile is empty")).build();
		if (!isParameterValid(prod, "max-stack") || !isNumber(prod, "max-stack"))
			return Response.ok(new IntegrationReturn(false, "max-stack is empty")).build();
		if (!isParameterValid(prod, "quantity-in-pallet") || !isNumber(prod, "quantity-in-pallet"))
			return Response.ok(new IntegrationReturn(false, "quantity-in-pallet is empty")).build();
		Product extPrd = intSvc.getBySku(prod.getString("sku"));
		if (extPrd == null || extPrd.getStatus().equals("CANCELADO")) {

			if (extPrd == null) {
				ProductModel mdl = intSvc.getProductModel("WAREITEM");

				extPrd = new Product();
				extPrd.setSku(prod.getString("sku"));
				extPrd.setName(prod.getString("description"));
				extPrd.setModel(mdl);
				intSvc.persistProduct(extPrd);
				for (ProductModelField fmdl : mdl.getFields()) {
					if (prod.containsKey(fmdl.getMetaname().toLowerCase())) {
						ProductField pf = new ProductField();
						pf.setMetaname(fmdl.getMetaname());
						pf.setName(fmdl.getName());
						pf.setProduct(extPrd);
						pf.setModel(fmdl);
						if (isNumber(prod, fmdl.getMetaname().toLowerCase()))
							pf.setValue(Integer.toString(prod.getInt(fmdl.getMetaname().toLowerCase())));
						else
							pf.setValue(prod.getString(fmdl.getMetaname().toLowerCase()));
						intSvc.persistProductField(pf);
					}
				}
			} else {
				extPrd.setName(prod.getString("description"));
				intSvc.persistProduct(extPrd);
				updateProductFields(extPrd, prod);
			}

			return Response.ok(IntegrationReturn.OK).build();
		} else {
			return Response.ok(new IntegrationReturn(false, "Product already exists")).build();
		}
	}

	@PUT
	@Path("/product-register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response putProductRegister(JsonObject prod) throws Exception {
		if (!isParameterValid(prod, "sku"))
			return Response.ok(new IntegrationReturn(false, "sku is empty")).build();
		Product extPrd = intSvc.getBySku(prod.getString("sku"));
		if (extPrd != null && !extPrd.getStatus().equals("CANCELADO")) {
			if (!isParameterValid(prod, "description"))
				return Response.ok(new IntegrationReturn(false, "description is empty")).build();
			if (!isParameterValid(prod, "length"))
				return Response.ok(new IntegrationReturn(false, "length is empty")).build();
			if (!isParameterValid(prod, "width"))
				return Response.ok(new IntegrationReturn(false, "width is empty")).build();
			if (!isParameterValid(prod, "height"))
				return Response.ok(new IntegrationReturn(false, "height is empty")).build();
			if (!isParameterValid(prod, "max-pile") || !isNumber(prod, "max-pile"))
				return Response.ok(new IntegrationReturn(false, "max-pile is empty")).build();
			if (!isParameterValid(prod, "max-stack") || !isNumber(prod, "max-stack"))
				return Response.ok(new IntegrationReturn(false, "max-stack is empty")).build();
			if (!isParameterValid(prod, "quantity-in-pallet") || !isNumber(prod, "quantity-in-pallet"))
				return Response.ok(new IntegrationReturn(false, "quantity-in-pallet is empty")).build();
			extPrd.setName(prod.getString("description"));
			intSvc.persistProduct(extPrd);
			updateProductFields(extPrd, prod);

			return Response.ok(IntegrationReturn.OK).build();
		} else {
			return Response.ok(new IntegrationReturn(false, "Product not found")).build();
		}
	}

	private void updateProductFields(Product prd, JsonObject json) {
		List<ProductField> pfs = intSvc.listProductFieldsByProduct(prd);

		for (ProductField pf : pfs) {
			if (json.containsKey(pf.getMetaname().toLowerCase())) {
				if (isNumber(json, pf.getMetaname().toLowerCase()))
					pf.setValue(Integer.toString(json.getInt(pf.getMetaname().toLowerCase())));
				else
					pf.setValue(json.getString(pf.getMetaname().toLowerCase()));
				intSvc.persistProductField(pf);
			}
		}
	}

	@DELETE
	@Path("/product-register/{sku}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response deleteProduct(@PathParam("sku") String sku) throws Exception {
		if (sku == null)
			return Response.ok(new IntegrationReturn(false, "sku is empty")).build();
		Product extPrd = intSvc.getBySku(sku);

		if (extPrd != null) {
			extPrd.setStatus("CANCELADO");
			// TODO: Checar filhos e pais
			intSvc.persistProduct(extPrd);
			List<ProductField> pfs = intSvc.listProductFieldsByProduct(extPrd);
			for (ProductField pf : pfs) {
				pf.setStatus("CANCELADO");
				intSvc.persistProductField(pf);
			}
		} else {
			return Response.ok(new IntegrationReturn(false, "Product not found")).build();
		}
		return Response.ok(IntegrationReturn.OK).build();
	}

	@POST
	@Path("/supplier-register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response postSupplierRegister(JsonObject supplierJson) throws Exception {
		if (!isParameterValid(supplierJson, "supplier-id"))
			return Response.ok(new IntegrationReturn(false, "supplier-id is empty")).build();
		if (!isParameterValid(supplierJson, "description"))
			return Response.ok(new IntegrationReturn(false, "description is empty")).build();
		Supplier extSup = (Supplier) intSvc.getSupplierByCode(supplierJson.getString("supplier-id"));
		if (extSup == null || extSup.getStatus().equals("CANCELADO")) {
			if (extSup == null)
				extSup = new Supplier();
			extSup.setName(supplierJson.getString("description"));
			extSup.setExtid(supplierJson.getString("supplier-id"));
			intSvc.persistSupplier(extSup);
			return Response.ok(IntegrationReturn.OK).build();
		} else {
			return Response.ok(new IntegrationReturn(false, "Supplier already exists")).build();
		}
	}

	@PUT
	@Path("/supplier-register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response putSupplierRegister(JsonObject supplierJson) throws Exception {
		if (!isParameterValid(supplierJson, "supplier-id"))
			return Response.ok(new IntegrationReturn(false, "supplier-id is empty")).build();
		if (!isParameterValid(supplierJson, "description"))
			return Response.ok(new IntegrationReturn(false, "description is empty")).build();
		Supplier extSup = (Supplier) intSvc.getSupplierByCode(supplierJson.getString("supplier-id"));
		if (extSup != null) {
			extSup.setName(supplierJson.getString("description"));
			intSvc.persistSupplier(extSup);
			return Response.ok(IntegrationReturn.OK).build();
		} else {
			return Response.ok(new IntegrationReturn(false, "Supplier not found")).build();
		}
	}

	@DELETE
	@Path("/supplier-register/{supplier-id}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response deleteSupplier(@PathParam("supplier-id") String supplierId) throws Exception {
		if (supplierId == null)
			return Response.ok(new IntegrationReturn(false, "supplier-id is empty")).build();
		Supplier extSup = intSvc.getSupplierByCode(supplierId);

		if (extSup != null) {
			extSup.setStatus("CANCELADO");
			// TODO: Checar filhos e pais
			intSvc.persistSupplier(extSup);
		} else {
			return Response.ok(new IntegrationReturn(false, "Supplier not found")).build();
		}
		return Response.ok(IntegrationReturn.OK).build();
	}

	@POST
	@Path("/inventory-outbound")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response inventoryOutbound(JsonObject invout) {

		//		if (!isParameterValid(invout, "sku"))
		//			return Response.ok(new IntegrationReturn(false, "sku is empty")).build();
		//		if (!isParameterValid(invout, "batch"))
		//			return Response.ok(new IntegrationReturn(false, "batch is empty")).build();
		//		if (!isParameterValid(invout, "packages"))
		//			return Response.ok(new IntegrationReturn(false, "packages is empty")).build();
		//
		//		Document d = intSvc.quickFindByMetaname("INVENTORY");
		//		DocumentItem di = intSvc.getQuickDocumentItemByDocumentAndProductSKUAndBatch(d.getId(), invout.getString("sku"),
		//				invout.getString("batch"));
		//
		//		if (di == null)
		//			return Response.ok(new IntegrationReturn(false, "no product batch found")).build();
		//
		//		int qtd = new Double(di.getQty()).intValue();
		//		int redux = invout.getInt("packages");
		//
		//		if (qtd < redux)
		//			return Response.ok(new IntegrationReturn(false, "negative inventory forbidden")).build();
		//
		//		intSvc.updateDocumentItemQuantity(di.getId(), qtd - redux);
		//
		//		return Response.ok(IntegrationReturn.OK).build();
		return Response.ok(new IntegrationReturn(true, "Deprecated Method")).build();
	}

	@POST
	@Path("/outbound-return")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response outboundReceive(JsonObject invout) {
		JsonArray itens = isParameterValid(invout, "items") ? (JsonArray) invout.get("items") : null;
		logger.info("Executing invoice-return");
		if (!isParameterValid(invout, "invoice-number"))
			return Response.ok(new IntegrationReturn(false, "invoice-number is empty")).build();
		DocumentModel dmdl = intSvc.getDocModel("NFSAIDA");
		Document d = intSvc.quickDocByCodeAndType(dmdl, invout.getString("invoice-number"));

		if (d == null)
			return Response.ok(new IntegrationReturn(false, "Document does not exists")).build();
		else if (d.getStatus().equals("CANCELADO"))
			return Response.ok(new IntegrationReturn(false, "Document is canceled")).build();
		return Response.ok(returnDoc(d, "CANCELADO", "ARMAZENADO", itens)).build();
		/*
		if (!isParameterValid(invout, "return-number"))
			return Response.ok(new IntegrationReturn(false, "return-number is empty")).build();
		if (!isParameterValid(invout, "items"))
			return Response.ok(new IntegrationReturn(false, "items is empty")).build();
		DocumentModel dmdl = intSvc.getDocModel("NFDEV");
		Document d = intSvc.getDocByCodeAndType(dmdl, invout.getString("return-number"));
		
		if (d == null || d.getStatus().equals("CANCELADO")) {
			JsonArray itens = (JsonArray) invout.get("items");
		
			d = new Document(dmdl, dmdl.getName() + " #" + invout.getString("return-number"), invout.getString("return-number"));
			d.setStatus("PARAIMPRESSAO");
			intSvc.persistDoc(d);
			for (int cnt = 0; cnt < itens.size(); cnt++) {
				JsonObject obj = itens.getJsonObject(cnt);
				Product prd = intSvc.getBySku(obj.getString("sku"));
				DocumentItem di = new DocumentItem();
		
				if (prd == null) {
					return Response.ok(new IntegrationReturn(false, "Product not found: " + obj.getString("sku"))).build();
				}
				di.setDocument(d);
				di.setProduct(prd);
				di.setQty(obj.getInt("packages"));
				di.setStatus("NOVO");
				intSvc.persistDocItem(di);
				d.getItens().add(di);
			}
		} else {
			return Response.ok(new IntegrationReturn(false, "Document already exists")).build();
		}
		return Response.ok(IntegrationReturn.OK).build();*/
	}

	@POST
	@Path("/invoice-return")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response invoiceReturn(JsonObject invout) {
		JsonArray itens = isParameterValid(invout, "items") ? (JsonArray) invout.get("items") : null;
		logger.info("Executing invoice-return");
		if (!isParameterValid(invout, "invoice-number"))
			return Response.ok(new IntegrationReturn(false, "invoice-number is empty")).build();
		DocumentModel dmdl = intSvc.getDocModel("NFSAIDA");
		Document d = intSvc.quickDocByCodeAndType(dmdl, invout.getString("invoice-number"));

		if (d == null)
			return Response.ok(new IntegrationReturn(false, "Document does not exists")).build();
		else if (d.getStatus().equals("CANCELADO"))
			return Response.ok(new IntegrationReturn(false, "Document is canceled")).build();
		return Response.ok(returnDoc(d, "PICKING", "ARMAZENADO", itens)).build();
	}

	private boolean isParameterValid(JsonObject jo, String param) {
		return jo != null && !jo.isEmpty() && jo.containsKey(param) && !jo.isNull(param);
	}

	private boolean isNumber(JsonObject jo, String param) {
		try {
			jo.getJsonNumber(param);
			return true;
		} catch (ClassCastException cce) {
			return false;
		}
	}

	private IntegrationReturn cancelDoc(Document d, String statusThingTo, String... exceptions) {
		if (exceptions != null) {
			if (d.getThings().stream().map(dt -> dt.getThing()).anyMatch(t -> Arrays.asList().contains(t.getStatus())))
				return new IntegrationReturn(false, "There are items on this document in an invalid state to cancel.");
		}
		d.getThings().forEach(dt -> {
			Thing t = dt.getThing();

			t.setStatus(statusThingTo);
			dt.setStatus("CANCELADO");
			intSvc.persistDocumentThing(dt);
			intSvc.persistThing(t);
		});
		d.setStatus("CANCELADO");
		intSvc.persistDoc(d);
		return IntegrationReturn.OK;
	}

	private IntegrationReturn returnDoc(Document d, String docStatusTo, String thingStatusTo, JsonArray itens) {
		List<DocumentThing> toReturn = new ArrayList<DocumentThing>();
		String docThingStatusTo = "CANCELADO";
		Profiler p = new Profiler();

		d.getThings().addAll(intSvc.quickDocThingListByCodeAndType(d.getModel(), d.getCode()));
		logger.info(p.step("Filled " + d.getThings().size() + " DocumentThings", false));
		if (itens == null) {
			p.step("Return entire document", true);
			d.getThings().forEach(dt -> {
				dt.setThing(intSvc.getrSvc().getThRep().getThingByUnitTagId(dt.getUnit()));
				dt.setDocument(d);
			});
			toReturn.addAll(d.getThings());
		} else {
			p.step("Specific Units", true);
			for (int cnt = 0; cnt < itens.size(); cnt++) {
				JsonObject obj = itens.getJsonObject(cnt);
				Optional<DocumentThing> oDt = d.getThings().stream().filter(dtg -> dtg.getUnit().equalsIgnoreCase(obj.getString("epc"))).findFirst();

				if (oDt.isPresent()) {
					DocumentThing dt = oDt.get();
					Thing t = intSvc.getrSvc().getThRep().getThingByUnitTagId(obj.getString("epc"));

					dt.setThing(t);
					logger.info(p.step("Found DocumentThing " + dt.getId() + " Thing " + dt.getThing().getId(), false));
					toReturn.add(dt);
				} else
					logger.info(p.step("DocumentThing not found", false));
			}
		}
		returnItems(toReturn, docThingStatusTo, thingStatusTo);
		if (d.getThings().stream().anyMatch(dt -> !dt.getStatus().equalsIgnoreCase(docThingStatusTo))) {
			d.setStatus(docStatusTo);
			intSvc.getrSvc().getDcrep().quickUpdateStatus(d.getId(), docStatusTo);
		}

		logger.info(p.done("Document Returned", false, false));
		return IntegrationReturn.OK;
	}

	private void returnItems(List<DocumentThing> toReturn, String docThingStatusTo, String thingStatusTo) {
		Profiler p = new Profiler();

		for (DocumentThing dt : toReturn) {
			Thing t = dt.getThing();

			intSvc.getrSvc().getDtRep().quickUpdateStatus(dt.getId(), docThingStatusTo);
			intSvc.getrSvc().getThRep().quickUpdateThingStatus(t.getId(), thingStatusTo);
			p.step("Thing returned " + dt.getThing().getId() + " from document " + dt.getDocument().getId() + " with documentthing " + dt.getId(), true);
		}
	}
}
