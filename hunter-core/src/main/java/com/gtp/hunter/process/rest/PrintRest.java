package com.gtp.hunter.process.rest;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
import com.gtp.hunter.core.service.SourceService;
import com.gtp.hunter.core.service.UnitService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.repository.DocumentRepository;
import com.gtp.hunter.process.service.AddressService;
import com.gtp.hunter.process.service.PropertyModelFieldService;
import com.gtp.hunter.process.service.ThingService;

@RequestScoped
@Path("/print")
public class PrintRest {

	@Inject
	private SourceService				sSvc;

	@Inject
	private DocumentRepository			dRep;

	@Inject
	private ThingService				tSvc;

	@Inject
	private UnitService					uSvc;

	@Inject
	private AddressService				aSvc;

	@Inject
	private PropertyModelFieldService	pmfSvc;

	@Inject
	private transient Logger			logger;

	@POST
	@Path("/single/{addressid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response printThing(@PathParam("addressid") UUID addrId, PrintTagOrder payload) throws Exception {
		LabelData ld = new LabelData();
		PrinterDevice pd = (PrinterDevice) sSvc.getBaseDeviceByUUID(payload.getDevice());
		Thing t = tSvc.findById(payload.getThing());
		Unit u = uSvc.generateUnit(UnitType.EPC96, payload.getSku());
		Address a = aSvc.findById(addrId);

		ld.addField("EPC", u.getTagId());
		ld.addField("SERIAL", u.getTagId().substring(6));

		for (Property pr : t.getProperties()) {
			ld.addField(pr.getField().getMetaname().toUpperCase(), pr.getValue());
		}

		for (String key : payload.getProperties().keySet()) {
			String field = key.toUpperCase();
			String value = payload.getProperties().get(key);

			ld.addField(field, value);
			for (PropertyModelField prmf : t.getModel().getFields()) {
				if (prmf.getMetaname().equals(field)) {
					Optional<Property> optProp = t.getProperties().stream().filter(pr -> pr.getField().getId().equals(prmf.getId())).findFirst();

					if (optProp.isPresent()) {
						logger.info("Ja tem: " + field);
						optProp.get().setValue(value);
					} else {
						logger.info("Não tem: " + field);
						Property prop = new Property(t, prmf, value);
						t.getProperties().add(prop);
					}
					break;
				}
			}
		}

		if (pd.getModel().getProperties().get("Resolution") == null) {
			ld.setMaskName(ConfigUtil.get("hunter-process", "item-mask", "PM4i_almoxarifado.prn"));
		} else {
			ld.setMaskName(ConfigUtil.get("hunter-process", "item-mask", "PM4i_almoxarifado.prn").replace(".prn", "_" + pd.getModel().getProperties().get("Resolution") + ".prn"));
		}

		try {
			logger.info(ld.toString());
			Command ret = pd.print(ld);

			if ((ret != null) && (ret.getReturnValue() != null) && (ret.getReturnValue().equals("true"))) {
				t.setStatus("ARMAZENADO");
				t.setAddress(a);
				t.getUnits().add(u.getId());
				t.getUnitModel().add(u);
				tSvc.persist(t);
			} else {
				uSvc.removeById(u.getId());
				if (ret != null) {
					return Response.ok(new IntegrationReturn(false, ret.getReturnValue())).build();
				} else {
					return Response.ok(new IntegrationReturn(false, "RET NULL")).build();
				}
			}

			return Response.ok(new IntegrationReturn(true, new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(u))).build();
		} catch (NullPointerException npe) {
			logger.info("Error: ", npe);
			return Response.ok(new IntegrationReturn(false, "Imrpessora não conectada!")).build();
		}
	}

	@POST
	@Path("/reprint/{unitid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response reprintThing(@PathParam("unitid") UUID unitId, PrintTagOrder payload) throws Exception {
		LabelData ld = new LabelData();
		PrinterDevice pd = (PrinterDevice) sSvc.getBaseDeviceByUUID(payload.getDevice());
		Thing t = tSvc.findByUnitId(unitId);
		Unit u = t.getUnitModel().stream().filter(un -> un.getType() == UnitType.EPC96).findFirst().get();

		ld.addField("EPC", u.getTagId());
		ld.addField("SERIAL", u.getTagId().substring(6));

		for (Property pr : t.getProperties()) {
			ld.addField(pr.getField().getMetaname().toUpperCase(), pr.getValue());
		}

		if (pd.getModel().getProperties().get("Resolution") == null) {
			ld.setMaskName(ConfigUtil.get("hunter-process", "item-mask", "PM4i_almoxarifado.prn"));
		} else {
			ld.setMaskName(ConfigUtil.get("hunter-process", "item-mask", "PM4i_almoxarifado.prn").replace(".prn", "_" + pd.getModel().getProperties().get("Resolution") + ".prn"));
		}

		try {
			logger.info(ld.toString());
			Command ret = pd.print(ld);

			if ((ret != null) && (ret.getReturnValue() != null) && (ret.getReturnValue().equals("true"))) {
				logger.info("Reprint Success");
			} else {
				if (ret != null) {
					return Response.ok(new IntegrationReturn(false, ret.getReturnValue())).build();
				} else {
					return Response.ok(new IntegrationReturn(false, "RET NULL")).build();
				}
			}

			return Response.ok(new IntegrationReturn(true, new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(u))).build();
		} catch (NullPointerException npe) {
			logger.info("Error: ", npe);
			return Response.ok(new IntegrationReturn(false, "Imrpessora não conectada!")).build();
		}
	}
}
