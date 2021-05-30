package com.gtp.hunter.custom.solar.rest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.devicedata.LabelData;
import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.devices.PrinterDevice;
import com.gtp.hunter.core.model.PrintTagOrder;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;

@RequestScoped
@Path("/print")
public class PrintRest {
	@Inject
	private IntegrationService	iSvc;

	@Inject
	private Logger				logger;

	@POST
	@Path("/sugar")
	@PermitAll
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response printSugar(PrintTagOrder payload) throws Exception {
		LabelData labelData = new LabelData();
		PrinterDevice printerDevice = (PrinterDevice) iSvc.getRegSvc().getSrcSvc().getBaseDeviceByUUID(UUID.fromString("210e54e2-55b1-11e9-a948-0266c0e70a8c"));//Impressora Almoxarifado
		Thing t = iSvc.getRegSvc().getThSvc().findById(payload.getThing());
		Unit u = iSvc.getRegSvc().getUnSvc().generateUnit(UnitType.EPC96, payload.getSku());
		Address a = iSvc.getRegSvc().getAddSvc().findById(UUID.fromString("27998254-563a-11e9-b375-005056a19775"));//Almoxarifado
		Document transp = iSvc.getRegSvc().getDcSvc().findById(iSvc.getRegSvc().getDcSvc().quickFindParentDoc(payload.getDocument()).getId());
		Document dNF = transp.getSiblings().stream().filter(sib -> sib.getModel().getMetaname().equals("NFENTRADA")).findFirst().get();

		if (t == null) {
			Product prd = iSvc.getRegSvc().getPrdSvc().findById(payload.getProduct());

			t = new Thing(prd.getName(), prd, prd.getModel().getPropertymodel(), "NOVO");
			t.setId(payload.getThing());
			iSvc.getRegSvc().getThSvc().dirtyInsert(t);
			t = iSvc.getRegSvc().getThSvc().findById(t.getId());
		}
		Optional<String> optUniMed = t.getProduct().getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("PACKING_TYPE")).map(pf -> pf.getValue()).findFirst();

		payload.getProperties().put("INFLAMABILIDADE", "");
		payload.getProperties().put("RECOMENDACOESESPECIAIS", "");
		payload.getProperties().put("REATIVIDADE", "");
		payload.getProperties().put("RISCOAVIDA", "");
		payload.getProperties().put("RODAPE", "");
		payload.getProperties().put("DESTINO", a.getName());
		payload.getProperties().put("UNIDADEMEDIDA", optUniMed.isPresent() ? optUniMed.get() : "");
		payload.getProperties().put("FORNECEDOR", dNF.getPerson().getName());
		payload.getProperties().put("NFENTRADA", String.valueOf(Integer.parseInt(dNF.getCode())));
		payload.getProperties().put("DTRECEBIMENTO", new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()));
		labelData.addField("EPC", u.getTagId());
		labelData.addField("SERIAL", u.getTagId().substring(6));
		for (Property pr : t.getProperties()) {
			logger.info("Property: " + pr.getField().getMetaname() + ": " + pr.getValue());
			labelData.addField(pr.getField().getMetaname().toUpperCase(), pr.getValue());
		}

		for (String key : payload.getProperties().keySet()) {
			String field = key.toUpperCase();
			String value = payload.getProperties().get(key);

			labelData.addField(field, value);
			for (PropertyModelField prmf : t.getModel().getFields()) {
				if (prmf.getMetaname().equals(field)) {
					Optional<Property> optProp = t.getProperties().stream().filter(pr -> pr.getField().getId().equals(prmf.getId())).findFirst();

					if (optProp.isPresent()) {
						logger.info("Ja tem: " + field);
						optProp.get().setValue(value);
					} else {
						logger.info("Added field " + field + " with value " + value);
						Property prop = new Property(t, prmf, value);
						t.getProperties().add(prop);
					}
					break;
				}
			}
		}

		if (printerDevice.getModel().getProperties().get("Resolution") == null) {
			labelData.setMaskName(ConfigUtil.get("hunter-process", "item-mask", "PM4i_almoxarifado.prn"));
		} else {
			labelData.setMaskName(ConfigUtil.get("hunter-process", "item-mask", "PM4i_almoxarifado.prn").replace(".prn", "_" + printerDevice.getModel().getProperties().get("Resolution") + ".prn"));
		}

		try {
			logger.info(labelData.toString());
			Command ret = printerDevice.print(labelData);

			if ((ret != null) && (ret.getReturnValue() != null) && (ret.getReturnValue().equals("true"))) {
				t.setStatus("ARMAZENADO");
				t.setAddress(a);
				t.getUnits().add(u.getId());
				t.getUnitModel().add(u);
				logger.info("Properties: " + t.getProperties().size() + " Units: " + t.getUnits().size());
				iSvc.getRegSvc().getThSvc().persist(t);
			} else {
				iSvc.getRegSvc().getThSvc().getThRep().removeById(t.getId());
				iSvc.getRegSvc().getUnSvc().removeById(u.getId());
				if (ret != null) {
					return Response.ok(new IntegrationReturn(false, ret.getReturnValue())).build();
				} else {
					return Response.ok(new IntegrationReturn(false, "RET NULL")).build();
				}
			}

			return Response.ok(new IntegrationReturn(true, new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(u))).build();
		} catch (NullPointerException npe) {
			return Response.ok(new IntegrationReturn(false, "Imrpessora não conectada!")).build();
		}
	}
}
