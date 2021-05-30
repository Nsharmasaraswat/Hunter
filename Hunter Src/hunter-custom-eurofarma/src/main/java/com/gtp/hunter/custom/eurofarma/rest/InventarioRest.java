package com.gtp.hunter.custom.eurofarma.rest;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.gtp.hunter.custom.eurofarma.json.CPIIntegrationMessage;
import com.gtp.hunter.custom.eurofarma.json.InventoryRequestMessage;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.service.RegisterService;

@RequestScoped
@Path("/inventario")
public class InventarioRest {

	@Inject
	private Logger			logger;

	@EJB(lookup = "java:global/hunter-core/RegisterService!com.gtp.hunter.process.service.RegisterService")
	private RegisterService	regSvc;

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn createInventory(CPIIntegrationMessage<InventoryRequestMessage> message) {
		if (!message.getCommand().equals("INVENTARIO")) return new IntegrationReturn(false, "Commando Inválido: " + message.getCommand());
		logger.info("Requisição de Inventário " + message.getData().getDocumento());
		return IntegrationReturn.OK;
	}
}
