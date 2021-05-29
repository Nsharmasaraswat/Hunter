package com.gtp.hunter.custom.eurofarma.rest;

import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.gtp.hunter.common.devicedata.GPIOData;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.devices.GPIODevice;
import com.gtp.hunter.core.model.Port;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.custom.eurofarma.json.CPIIntegrationMessage;
import com.gtp.hunter.custom.eurofarma.json.ClosePortalMessage;
import com.gtp.hunter.custom.eurofarma.json.OpenPortalMessage;
import com.gtp.hunter.custom.eurofarma.json.PortalMessage;
import com.gtp.hunter.custom.eurofarma.service.IntegrationService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Origin;
import com.gtp.hunter.process.wf.origin.DeviceOrigin;
import com.gtp.hunter.process.wf.process.ForwardProcess;

@RequestScoped
@Path("/portal")
public class PortalRest {

	@Inject
	private Logger				logger;

	@Inject
	private IntegrationService	is;

	@PUT
	@Path("/open")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn openPortal(@Context HttpHeaders rs, CPIIntegrationMessage<PortalMessage> message) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = is.getRegSvc().getAuthSvc().getUser(token);
		UUID origId = ((PortalMessage) message.getData()).getPortal();
		Origin ori = is.getRegSvc().getOrgSvc().findById(origId);
		Date reqDate = ((PortalMessage) message.getData()).getData();
		Calendar calReq = Calendar.getInstance();

		calReq.add(Calendar.MINUTE, -5);
		if (ori == null) return new IntegrationReturn(false, "Portal " + origId + " Não Cadastrado");
		if (reqDate.before(calReq.getTime())) return new IntegrationReturn(false, "Requisição Antiga (+5min)");

		logger.info("Portal " + origId.toString() + " opened by user " + usr.getName());
		return IntegrationReturn.OK;
	}

	@PUT
	@Path("/close")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn closePortal(@Context HttpHeaders rs, CPIIntegrationMessage<PortalMessage> message) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = is.getRegSvc().getAuthSvc().getUser(token);
		UUID origId = ((PortalMessage) message.getData()).getPortal();
		Origin ori = is.getRegSvc().getOrgSvc().findById(origId);
		Date reqDate = ((PortalMessage) message.getData()).getData();
		Calendar calReq = Calendar.getInstance();

		calReq.add(Calendar.MINUTE, -5);
		if (ori == null) return new IntegrationReturn(false, "Portal " + origId + " Não Cadastrado");
		if (reqDate.before(calReq.getTime())) return new IntegrationReturn(false, "Requisição Antiga (+5min)");

		logger.info("Portal " + origId.toString() + " closed by user " + usr.getName());
		return IntegrationReturn.OK;
	}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn controlPortal(@Context HttpHeaders rs, CPIIntegrationMessage<PortalMessage> message) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = is.getRegSvc().getAuthSvc().getUser(token);
		UUID origId = ((PortalMessage) message.getData()).getPortal();
		if (origId == null) return new IntegrationReturn(false, message.getCommand() + " - Portal Não Especificado");
		Origin ori = is.getRegSvc().getOrgSvc().findById(origId);
		Date reqDate = ((PortalMessage) message.getData()).getData();
		Calendar calReq = Calendar.getInstance();
		ForwardProcess bp = is.getPsm().getProcesses().values().parallelStream()
						.filter(p -> p.getModel().getOrigin().getId().equals(origId))
						.map(p -> (ForwardProcess) p)
						.findAny()
						.orElse(null);

		calReq.add(Calendar.MINUTE, -5);
		if (ori == null) return new IntegrationReturn(false, message.getCommand() + " - Portal " + origId + " Não Cadastrado");
		if (reqDate.before(calReq.getTime())) return new IntegrationReturn(false, "Requisição Antiga (+5min)");
		int stacklightState = 0;

		switch (message.getCommand()) {
			case "ABRIR":
				OpenPortalMessage omsg = new OpenPortalMessage();

				omsg.setData("{\"user\":\"" + usr.getName() + "\"}");
				logger.info("Portal " + origId.toString() + " opened by user " + usr.getName());
				stacklightState = 0;//LIGA
				bp.message(omsg);
				break;
			case "FECHAR":
				ClosePortalMessage cmsg = new ClosePortalMessage();

				cmsg.setData("{\"user\":\"" + usr.getName() + "\"}");
				logger.info("Portal " + origId.toString() + " closed by user " + usr.getName());
				stacklightState = 1;//DESLIGA
				bp.message(cmsg);
				break;
		}
		stackLight((DeviceOrigin) bp.getBaseOrigin(), stacklightState);
		return IntegrationReturn.OK;
	}

	private void stackLight(DeviceOrigin origin, int state) {
		Port prt = origin.getPorts().entrySet().parallelStream()
						.filter(en -> en.getKey().contains("SLY"))
						.map(en -> en.getValue())
						.findAny()
						.orElse(null);

		if (prt != null) {
			GPIODevice dev = (GPIODevice) origin.getDevices().get(prt.getDevice().getMetaname());
			GPIOData data = new GPIOData();
			data.setPin(prt.getPortId());
			data.setState(state);
			Command ret = dev.setState(data);
			logger.info("Port " + prt.getMetaname() + " Return: " + ret.getReturnValue());
		} else
			logger.error("Yellow Stacklight Not Configured For Origin");

		prt = origin.getPorts().entrySet().parallelStream()
						.filter(en -> en.getKey().contains("SLR"))
						.map(en -> en.getValue())
						.findAny()
						.orElse(null);

		if (prt != null) {
			GPIODevice dev = (GPIODevice) origin.getDevices().get(prt.getDevice().getMetaname());
			GPIOData data = new GPIOData();
			data.setPin(prt.getPortId());
			data.setState(1);
			Command ret = dev.setState(data);
			logger.info("Port " + prt.getMetaname() + " Return: " + ret.getReturnValue());
		} else
			logger.error("Red Stacklight Not Configured For Origin");
	}

	@GET
	@Path("/msgtst/{method}")
	public Response echo(@PathParam("method") int method) {
		CPIIntegrationMessage<PortalMessage> cm = new CPIIntegrationMessage<PortalMessage>();
		PortalMessage pm = new PortalMessage();

		pm.setData(new Date());
		pm.setPortal(UUID.randomUUID());
		cm.setData(pm);

		switch (method) {
			case 1:
				return Response.ok(cm).build();
			case 2:
				JsonObject ret = null;
				JsonReader jsonReader = Json.createReader(new StringReader(cm.toString()));

				ret = jsonReader.readObject();
				jsonReader.close();
				return Response.ok(ret.toString()).build();
		}
		return Response.ok().build();
	}
}
