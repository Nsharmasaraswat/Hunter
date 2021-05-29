package com.gtp.hunter.custom.descarpack.rest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.gtp.hunter.common.devicedata.LabelData;
import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.devices.PrinterDevice;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.custom.descarpack.model.DocumentQuantitySummary;
import com.gtp.hunter.custom.descarpack.model.LotPosition;
import com.gtp.hunter.custom.descarpack.model.PrintTagOrder;
import com.gtp.hunter.custom.descarpack.model.ThingQuantitySummary;
import com.gtp.hunter.custom.descarpack.repository.CustomRepository;
import com.gtp.hunter.custom.descarpack.service.IntegrationService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.ejbcommon.util.RestUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.IntegrationLog;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductField;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.stream.TaskStreamManager;

@Path("/task")
public class TaskRest {

	private static final String		BASE_URL	= ConfigUtil.get("hunter-custom-descarpack", "rest-url", "http://187.94.62.199:37015/rest/");
	private static final String		WS_METHOD	= "PRINT";
	private static final RestUtil	rest		= new RestUtil(BASE_URL);

	@Inject
	private static transient Logger	logger;

	@Inject
	private IntegrationService		iSvc;

	@Inject
	private CustomRepository		rep;

	@EJB(lookup = "java:global/hunter-core-2.0.0-SNAPSHOT/TaskStreamManager!com.gtp.hunter.process.stream.TaskStreamManager")
	private TaskStreamManager		tsm;

	@GET
	@Path("/tagsbydoc/{doc}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<PrintTagOrder> getTagsByDoc(@PathParam("doc") UUID id) {

		List<PrintTagOrder> ret = new ArrayList<PrintTagOrder>();

		Document doc = iSvc.getQuickDocument(id);
		String doccode = "";
		if (doc.getParent() == null) {
			doccode = doc.getCode();
		} else {
			Document docp = iSvc.getQuickDocument(doc.getParent().getId());
			doccode = docp.getCode();
		}
		List<DocumentItem> lstdi = iSvc.getQuickDocumentItemListByDocument(id);
		for (DocumentItem di : lstdi) {
			List<PropertyModelField> ppmf = iSvc.listQuickPropertyModelFieldFromProduct(di.getProduct().getId());
			int qtd = iSvc.getQuickDocumentThingCountByDocAndProduct(id, di.getProduct().getId());
			logger.debug("Quantidade impressa: " + qtd);
			Map<String, String> md = new HashMap<String, String>();
			for (PropertyModelField pmf : ppmf) {
				if (pmf.getVisible())
					md.put(pmf.getMetaname(), pmf.getType().toString());
			}

			PrintTagOrder pto = new PrintTagOrder();
			pto.setDocument(doc.getId());
			pto.setDocname(doc.getName());
			pto.setProduct(di.getProduct().getId());
			pto.setProdname(di.getProduct().getName());
			pto.setQty(new Double(di.getQty()).intValue());
			pto.setPrinted(qtd);
			pto.setMetadata(md);
			pto.setStatus(di.getStatus());
			pto.setSku(di.getProduct().getSku());
			Map<String, String> props = new HashMap<String, String>();
			props.put("BATCH", doccode);
			pto.setProperties(props);
			ret.add(pto);
		}

		return ret;

	}

	@POST
	@Path("/printTags")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response persistThingsFromDocument(PrintTagOrder payload) throws Exception {
		Document doc = iSvc.getDoc(payload.getDocument());
		Product prod = iSvc.getProduct(payload.getProduct());
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		DateFormat dfv = new SimpleDateFormat("ddMMyy");
		Date manufacture = df.parse(payload.getProperties().get("MANUFACTURE"));
		String expiry = "";
		if (payload.getProperties().containsKey("EXPIRY")) {
			expiry = payload.getProperties().get("EXPIRY");
		} else {
			int expdays = -1;
			for (ProductField pfexp : prod.getFields()) {
				if (pfexp.getModel().getMetaname().equals("EXPIRY-DAYS")) {
					expdays = Integer.parseInt(pfexp.getValue());
					break;
				}
			}
			if (expdays >= 0) {
				Calendar c = Calendar.getInstance();
				c.setTime(manufacture);
				c.add(Calendar.DATE, expdays);
				expiry = df.format(c.getTime());
			} else {
				throw new Exception("PRODUTO " + prod.getSku() + " SEM EXPIRY-DATE");
			}
		}
		String expiryBarcode = expiry;
		int qtd = iSvc.getCountThingsBySKUAndProperty(prod.getSku(), "BATCH", payload.getProperties().get("BATCH"));
		int qtdpv = 0;

		try {
			// expiry = df.format(df.parse(expiry));
			expiryBarcode = dfv.format(df.parse(expiry));
		} catch (ParseException pe) {
			expiry = "INDETERMINADO";
			expiryBarcode = "000000";
		}
		logger.debug("Quantidade de itens: " + qtd);
		for (ProductField pf : prod.getFields()) {
			if (pf.getModel().getMetaname().equals("QUANTITY-PER-VOLUME")) {
				qtdpv = Integer.parseInt(pf.getValue());
				break;
			}
		}
		final DocumentItem docItem = doc.getItens().stream().filter(di -> di.getProduct().getId().equals(payload.getProduct())).findFirst().get();
		List<DocumentItem> di = doc.getItens().stream().filter(tdi -> tdi.getProduct().getId().equals(payload.getProduct())).collect(Collectors.toList());
		if (di.size() == 1) {
			di.get(0).getProduct();
		} else {
			// ERRO: erro de quantidade de produtos.
		}
		UUID devId = payload.getDevice();
		PrinterDevice pd = (PrinterDevice) iSvc.getBaseDevByUUID(devId);
		for (int cnt = 0; cnt < Integer.parseInt(payload.getProperties().get("qty")); cnt++) {
			// JsonObject props = arrObj.getJsonObject(cnt);
			List<Property> propertiesToPersist = new ArrayList<>();
			Unit u = iSvc.generateUnit(UnitType.EPC96, prod.getSku());
			LabelData ld = new LabelData();
			String barcode = "01789828381" + prod.getBarcode() + "10" + payload.getProperties().get("BATCH") + "15" + expiryBarcode + "21" + String.format("%04d", ++qtd) + "30" + String.format("%04d", qtdpv);

			ld.addField("SKU", prod.getSku());
			ld.addField("DESCRIPTION", prod.getName());
			ld.addField("BATCH", payload.getProperties().get("BATCH"));
			ld.addField("MANUFACTURE", df.format(manufacture));
			ld.addField("EXPIRY", expiry);
			ld.addField("EPC", u.getTagId());
			ld.addField("BARCODE", barcode);

			Thing t = new Thing(prod.getName(), prod, prod.getModel().getPropertymodel(), "NOVO");
			DocumentThing dt = new DocumentThing(doc, t);

			for (PropertyModelField pmf : prod.getModel().getPropertymodel().getFields()) {
				Property prop = new Property(t, pmf, payload.getProperties().get(pmf.getMetaname()));

				if (pmf.getMetaname().equals("BARCODE"))
					prop.setValue(barcode);
				propertiesToPersist.add(prop);
				ld.addField(pmf.getMetaname(), payload.getProperties().get(pmf.getMetaname()));
			}
			if (pd.getModel().getProperties().get("Resolution") == null) {
				ld.setMaskName(ConfigUtil.get("hunter-custom-descarpack", "item-mask", "R110Xi4-Descarpack.prn"));
			} else {
				ld.setMaskName(ConfigUtil.get("hunter-custom-descarpack", "item-mask", "R110Xi4-Descarpack.prn").replace(".prn", "_" + pd.getModel().getProperties().get("Resolution") + ".prn"));
			}
			Command ret = pd.print(ld);
			// TODO: Retomar com a consolidação do PrintDevice no DMS
			if ((ret != null) && (ret.getReturnValue() != null) && (ret.getReturnValue().equals("true"))) {

				final JsonObjectBuilder jo = Json.createObjectBuilder();
				final JsonArrayBuilder items = Json.createArrayBuilder();
				final JsonObjectBuilder item = Json.createObjectBuilder();

				t.getUnits().add(u.getId());
				iSvc.persistThing(t);
				propertiesToPersist.forEach(prop -> iSvc.persistProperty(prop));

				iSvc.persistDocumentThing(dt);
				// Só pra manter o controle da quantidade;
				doc.getThings().add(dt);

				if (doc.getModel().getMetaname().equals("INVENTORY")) {
					t.setStatus("ARMAZENADO");
				} else {
					t.setStatus("IMPRESSO");
				}
				iSvc.persistThing(t);

				if (doc.getThings().stream().filter(ddt -> ddt.getThing().getProduct().getId().equals(prod.getId())).count() == new Double(docItem.getQty()).intValue()) {
					if (doc.getModel().getMetaname().equals("INVENTORY")) {
						docItem.setStatus("ARMAZENADO");
					} else {
						docItem.setStatus("IMPRESSO");
					}

					iSvc.persistDocItem(docItem);
				}

				jo.add("document-number", doc.getCode());
				jo.add("document-type", doc.getModel().getMetaname().equalsIgnoreCase("NFENT") ? doc.getParent().getModel().getMetaname() : doc.getModel().getMetaname());
				item.add("sku", prod.getSku());
				item.add("epc", u.getTagId());
				item.add("desc", prod.getName());
				item.add("batch", ld.getLabelFields().get("BATCH"));
				item.add("manufacture", ld.getLabelFields().get("MANUFACTURE"));
				item.add("expiry", ld.getLabelFields().get("EXPIRY"));
				items.add(item);
				jo.add("items", items);
				JsonObject snd = jo.build();
				long pre = System.currentTimeMillis();
				IntegrationReturn retprint = rest.sendSync(snd, WS_METHOD);
				JsonObjectBuilder resp = Json.createObjectBuilder();
				long executionTime = System.currentTimeMillis() - pre;
				logger.debug(snd.toString());
				resp.add("message", retprint.getMessage() == null ? "" : retprint.getMessage());
				resp.add("result", retprint.isResult());
				try {
					IntegrationLog il = new IntegrationLog(WS_METHOD, "POST", rest.getBaseURL(), snd.toString(), retprint.isResult() ? 200 : 500, resp.build().toString(), executionTime);
					iSvc.getrSvc().getIlRep().persist(il);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (!retprint.isResult())
					return Response.ok(new IntegrationReturn(false, "Falha no envio ao Protheus")).build();

			} else {
				// logger.log(Level.INFO, "Retorno Inexperado: " + ret.getReturnValue());
				if (ret != null) {
					return Response.ok(new IntegrationReturn(false, ret.getReturnValue())).build();
				} else {
					return Response.ok(new IntegrationReturn(false, "RET NULL")).build();
				}
			}
		}

		return Response.ok(IntegrationReturn.OK).build();
	}

	@GET
	@Path("/docSummary")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<DocumentQuantitySummary> getDocSummary() {
		return rep.getDocumentQuantitySummary().stream().collect(Collectors.toList());
	}

	@GET
	@Path("/thingSummary")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<ThingQuantitySummary> getThingSummary() {
		return rep.getThingQuantitySummary().stream().collect(Collectors.toList());
	}

	@GET
	@Path("/lotposition/{lot}")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<LotPosition> getLotPosition(@PathParam("lot") String lot) {
		return rep.getLotPosition(lot);
	}

	@GET
	@Path("/receivelot/{lot}")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<LotPosition> receiveLot(@PathParam("lot") String lot) {
		rep.receiveLot(lot);
		return rep.getLotPosition(lot);
	}
	
	@GET
	@Path("/storelot/{lot}")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<LotPosition> storeLot(@PathParam("lot") String lot) {
		rep.storeLot(lot);
		return rep.getLotPosition(lot);
	}

	@GET
	@Path("/blocklot/{lot}")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<LotPosition> blockLot(@PathParam("lot") String lot) {
		rep.blockLot(lot);
		return rep.getLotPosition(lot);
	}

	@GET
	@Path("/unblocklot/{lot}")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<LotPosition> unblockLot(@PathParam("lot") String lot) {
		rep.unblockLot(lot);
		return rep.getLotPosition(lot);
	}

	@GET
	@Path("/unlockdoc/{doc}")
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn unlockDoc(@PathParam("doc") String doc) {
		rep.unlockDocument(doc);
		return IntegrationReturn.OK;
	}
}
