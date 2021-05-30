package com.gtp.hunter.core.rest;

import java.util.List;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.gtp.hunter.common.devicedata.GPIOData;
import com.gtp.hunter.common.devicedata.LabelData;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.devices.GPIODevice;
import com.gtp.hunter.core.devices.PrinterDevice;
import com.gtp.hunter.core.devices.RFIDDevice;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.repository.DeviceRepository;
import com.gtp.hunter.core.service.SourceService;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;

@RequestScoped
@Path("/device")
public class DeviceRest {

	@Inject
	private Logger				logger;

	@Inject
	private DeviceRepository	dRep;

	@Inject
	SourceService				sSvc;

	@GET
	@Path("/all")
	public List<Device> getAll() {
		return dRep.listFull();
	}

	@GET
	@Path("/{id}")
	public Device getById(@PathParam("id") String id) {
		Device d = null;
		try {
			d = dRep.findById(UUID.fromString(id));
		} catch (Exception e) {
			logger.error("Creating new Device", e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			d = new Device();
		}

		return d;
	}

	@GET
	@Path("/bysource/{id}")
	public List<Device> getBySource(@PathParam("id") UUID id) {
		return sSvc.listBySource(sSvc.findById(id));
	}

	@POST
	@Path("/")
	public Device save(Device device) {
		dRep.persist(device);
		sSvc.notifySource(device);
		return device;
	}

	@DELETE
	@Path("/{id}")
	public String deleteById(@PathParam("id") String id) {
		dRep.removeById(UUID.fromString(id));
		return "Ok";
	}

	@POST
	@Path("/print/{deviceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String print(@PathParam("deviceId") String deviceId, JsonObject param) {
		logger.debug(param.toString());
		PrinterDevice d = (PrinterDevice) sSvc.getBaseDeviceByUUID(UUID.fromString(deviceId));
		LabelData ld = new LabelData();

		for (String key : param.keySet()) {
			ld.addField(key.toUpperCase(), param.getString(key));
		}
		//		 ld.addField("SKU", param.getString("sku"));
		//		 ld.addField("DESCRIPTION", param.getString("description"));
		//		 ld.addField("BATCH", param.getString("batch"));
		//		 ld.addField("MANUFACTURE", param.getString("manufacture"));
		//		 ld.addField("EXPIRY", param.getString("expiry"));
		//		 ld.addField("EPC", param.getString("epc"));
		//		 ld.addField("BARCODE", param.getString("barcode"));
		if (d.getModel().getProperties().get("Resolution") == null) {
			ld.setMaskName(ConfigUtil.get("hunter-custom-descarpack", "item-mask", "R110Xi4-Descarpack.prn"));
		} else {
			logger.debug("Printer Resolution: " + d.getModel().getProperties().get("Resolution"));
			ld.setMaskName(ConfigUtil.get("hunter-custom-descarpack", "item-mask", "R110Xi4-Descarpack.prn").replace(".prn", "_" + d.getModel().getProperties().get("Resolution") + ".prn"));
		}
		Command cmd = d.print(ld);
		return (cmd.getReturnValue() != null && cmd.getReturnValue().equals("true")) ? "Ok" : "FAILED - " + cmd.getReturnValue();
	}

	@GET
	@Path("/teste/{maskname}/{id}")
	@PermitAll
	public String sendCmd(@PathParam("id") String id, @PathParam("maskname") String mask) {
		PrinterDevice d = (PrinterDevice) sSvc.getBaseDeviceByUUID(UUID.fromString(id));
		LabelData dt = new LabelData();

		dt.addField("ALMOXARIFADO", "ALMOXARIFADO");
		dt.addField("CIMA", "0");
		dt.addField("ESQUERDA", "-");
		dt.addField("BAIXO", "9");
		dt.addField("DIREITA", "5");
		dt.addField("PRODUTO", "PRODUTO TESTE 01");
		dt.addField("FORNECEDOR", "FORNECEDOR TESTE 01");
		dt.addField("QTDRECEBIMENTO", "999");
		dt.addField("QTDVOLUME", "666");
		dt.addField("NOTAFISCAL", "1403250");
		dt.addField("CODIGO", "CODIGO");
		dt.addField("LOTEFORNECEDOR", "LOTE FORNECEDOR");
		dt.addField("DATARECEBIMENTO", "03/05/2019");
		dt.addField("DATAFABRICACAO", "03/05/2019");
		dt.addField("DATAVALIDADE", "03/05/2019");
		dt.addField("APROVADO", "APAVORADO");
		dt.addField("LOTEINTERNO", "01/18");
		dt.setMaskName(mask);
		Command cmd = d.print(dt);
		return (cmd.getReturnValue() != null && cmd.getReturnValue().equals("Success")) ? cmd.getPayload() : "FAILED";
	}

	@GET
	@Path("/rfid/enable/{id}")
	@PermitAll
	public String enable(@PathParam("id") String id) {
		RFIDDevice d = (RFIDDevice) sSvc.getBaseDeviceByUUID(UUID.fromString(id));
		Command cmd = d.enable();

		return cmd.getReturnValue() != null ? cmd.getReturnValue() : "FAILED";
	}

	@GET
	@Path("/rfid/disable/{id}")
	@PermitAll
	public String disable(@PathParam("id") String id) {
		RFIDDevice d = (RFIDDevice) sSvc.getBaseDeviceByUUID(UUID.fromString(id));
		Command cmd = d.disable();

		return cmd.getReturnValue() != null ? cmd.getReturnValue() : "FAILED";
	}

	@GET
	@Path("/gpio/setState/{id}/{pin}/{state}")
	@PermitAll
	public String setState(@PathParam("id") String id, @PathParam("pin") int pin, @PathParam("state") int state) {
		GPIODevice d = (GPIODevice) sSvc.getBaseDeviceByUUID(UUID.fromString(id));
		GPIOData dt = new GPIOData();

		dt.setPin(pin);
		dt.setState(state);
		Command cmd = d.setState(dt);

		return cmd.getReturnValue() != null ? cmd.getReturnValue() : "FAILED";
	}
}
