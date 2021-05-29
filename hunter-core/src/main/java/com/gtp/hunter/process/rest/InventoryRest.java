package com.gtp.hunter.process.rest;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.core.repository.PrefixRepository;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.repository.DocumentModelRepository;
import com.gtp.hunter.process.repository.DocumentRepository;
import com.gtp.hunter.process.repository.ProductRepository;
import com.gtp.hunter.process.repository.ThingRepository;
import com.gtp.hunter.process.service.RegisterService;

@RequestScoped
@Path("/inventory")
public class InventoryRest {

	@Inject
	private transient Logger		logger;

	@Inject
	private ProductRepository		pdRep;

	@Inject
	private DocumentRepository		dRep;

	@Inject
	private DocumentModelRepository	dmRep;

	@Inject
	private ThingRepository			tRep;

	@Inject
	private PrefixRepository		pfxRep;

	@Inject
	private RegisterService			regSvc;

	@POST
	@Path("/volume")
	@Consumes(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response volumeInventory(JsonObject json) throws Exception {
		logger.info(json.toString());
		DocumentModel dm = dmRep.findByMetaname("INVENTORY");

		if (dm != null) {
			DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
			DecimalFormat decForm = new DecimalFormat("#0.##########", symbols);
			decForm.setRoundingMode(RoundingMode.HALF_EVEN);
			Prefix prefix = pfxRep.findByField("prefix", "INV");
			if (prefix == null) prefix = new Prefix("INV", 0L);
			String dCode = String.join("", Collections.nCopies(6 - String.valueOf(prefix.getCount() + 1).length(), "0")) + String.valueOf(prefix.getCount() + 1);
			prefix.setCount(prefix.getCount() + 1);
			String name = json.getString("name");
			String image = json.getString("local_image");
			long timestamp = json.getJsonNumber("time_created").longValue();
			JsonArray data = json.getJsonArray("data");
			Document doc = new Document(dm, "Inventur " + dCode, "INV" + dCode, "INVENTORY");
			DocumentModelField dmfImg = dm.getFields().stream().filter(dmf -> dmf.getMetaname().equals("GRIDFILE")).findFirst().get();
			DocumentField dfImg = new DocumentField(doc, dmfImg, "NOVO", image);
			doc.getFields().add(dfImg);

			for (JsonValue d : data) {
				JsonObject jo = (JsonObject) d;
				int row = jo.getInt("id");
				int t5 = jo.getInt("T5");
				int code = jo.getInt("code");
				double volume = jo.getJsonNumber("volume").doubleValue();
				String quadrants = jo.getString("quadrants");
				String sku = String.valueOf(t5) + (code == 0 ? "" : String.valueOf(code));
				Product p = pdRep.findByField("sku", sku);

				if (p != null) {
					double width = Double.parseDouble(p.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("WIDTH")).findFirst().get().getValue()) / 1000;
					double length = Double.parseDouble(p.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("LENGTH")).findFirst().get().getValue()) / 1000;
					double height = Double.parseDouble(p.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("FOLDEDHEIGHT")).findFirst().get().getValue()) / 1000;
					double vol = width * length * height;
					int cnt = new Double(Double.parseDouble(decForm.format(volume / vol))).intValue();
					Optional<DocumentItem> optDi = doc.getItems().stream().filter(di -> di.getProduct().getId().equals(p.getId())).findFirst();
					DocumentItem di = optDi.isPresent() ? optDi.get() : new DocumentItem(doc, p, 0d, "NOVO");
					PropertyModel prm = p.getModel().getPropertymodel();
					PropertyModelField prmfVol = prm.getFields().stream().filter(prmf -> prmf.getMetaname().equals("VOLUME")).findFirst().get();
					PropertyModelField prmfQty = prm.getFields().stream().filter(prmf -> prmf.getMetaname().equals("QUANTITY")).findFirst().get();
					PropertyModelField prmfSec = prm.getFields().stream().filter(prmf -> prmf.getMetaname().equals("SECTORS")).findFirst().get();
					Thing t = new Thing(p.getName(), p, prm, "NOVO");
					DocumentThing dt = new DocumentThing(doc, t, "NOVO");
					Property prVol = new Property(t, prmfVol, decForm.format(vol));
					Property prQty = new Property(t, prmfQty, String.valueOf(cnt));
					Property prSec = new Property(t, prmfSec, quadrants);

					t.getProperties().add(prVol);
					t.getProperties().add(prQty);
					t.getProperties().add(prSec);
					tRep.persist(t);
					doc.getThings().add(dt);
					di.setQty(di.getQty() + cnt);
					di.setMeasureUnit("LC");
					doc.getItems().add(di);
					logger.info("Volume " + decForm.format(volume) + " Single Volume: " + vol + " Count: " + cnt);
				} else
					logger.warn("Skipping LC with T5+CODE=" + sku + " not found on database");
			}
			dRep.persist(doc);
			return Response.ok().build();
		}
		return Response.ok("Missing Inventory Document Model").build();
	}

	@GET
	@Path("/fixcount/{invid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response fixCountInventory(@PathParam("invid") UUID invId) throws Exception {
		try {
			regSvc.getDcSvc().completeInventory(invId);
			return Response.ok().build();
		} catch (Exception e) {
			logger.info(e.getLocalizedMessage(), e);
			return Response.ok("ERRO : " + e.getLocalizedMessage()).build();
		}
	}
}
