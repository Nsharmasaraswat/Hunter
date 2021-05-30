package com.gtp.hunter.custom.eurofarma.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.gtp.hunter.custom.eurofarma.json.CPIIntegrationMessage;
import com.gtp.hunter.custom.eurofarma.json.FreeTransportMessage;
import com.gtp.hunter.custom.eurofarma.json.TransportMessage;
import com.gtp.hunter.custom.eurofarma.service.IntegrationService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.DocumentTransport;
import com.gtp.hunter.process.model.Thing;

@RequestScoped
@Path("/empilhadeira")
public class EmpilhadeiraRest {

	@Inject
	private Logger				logger;

	@Inject
	private IntegrationService	iSvc;

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn ressuply(CPIIntegrationMessage<TransportMessage> message) {
		if (!message.getCommand().equals("REABASTECE")) return new IntegrationReturn(false, "Commando Inválido: " + message.getCommand());
		Address origin = iSvc.getRegSvc().getAddSvc().findByMetaname(message.getData().getEndereco());
		if (origin == null) return new IntegrationReturn(false, "Endereço não cadastrado: " + message.getData().getEndereco());
		Address destination = iSvc.getRegSvc().getAddSvc().findByMetaname("PICKING.01");
		Thing t = iSvc.getRegSvc().getThSvc().findByAddress(origin);
		if (t == null) return new IntegrationReturn(false, "Endereço Vazio: " + message.getData().getEndereco());
		logger.info("Reabastecimento em " + message.getData().getEndereco());

		createMov(message.getData().getDocumento(), message.getCommand(), t, origin, destination, 5);
		return IntegrationReturn.OK;
	}

	@POST
	@Path("/manual/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn transport(CPIIntegrationMessage<FreeTransportMessage> message) {
		if (!message.getCommand().equals("MOVIMENTAR")) return new IntegrationReturn(false, "Commando Inválido: " + message.getCommand());
		Address origin = iSvc.getRegSvc().getAddSvc().findByMetaname(message.getData().getOrigem());
		if (origin == null) return new IntegrationReturn(false, "Origem não cadastrada: " + message.getData().getOrigem());
		Address destination = iSvc.getRegSvc().getAddSvc().findByMetaname(message.getData().getOrigem());
		if (destination == null) return new IntegrationReturn(false, "Destino não cadastrado: " + message.getData().getDestino());
		Thing t = iSvc.getRegSvc().getThSvc().findByAddress(origin);
		if (t == null) return new IntegrationReturn(false, "Origem Vazia: " + message.getData().getOrigem());
		Thing tmpDest = iSvc.getRegSvc().getThSvc().findByAddress(destination);
		if (tmpDest == null) return new IntegrationReturn(false, "Destino Ocupado: " + message.getData().getDestino());
		logger.info("Movimentação de " + message.getData().getOrigem() + "para " + message.getData().getDestino());

		createMov(message.getData().getDocumento(), message.getCommand(), t, origin, destination, 5);
		return IntegrationReturn.OK;
	}

	private void createMov(String code, String status, Thing t, Address origin, Address destination, int priority) {
		DocumentModel dmOm = iSvc.getRegSvc().getDmSvc().findByMetaname("ORDMOV");
		DocumentModelField dmfType = dmOm.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TYPE")).findAny().get();
		DocumentModelField dmfPrio = dmOm.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("PRIORITY")).findAny().get();
		DocumentModelField dmfTitle = dmOm.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TITLE")).findAny().get();
		Document ordMov = new Document(dmOm, dmOm.getName() + code, code, "ATIVO");

		ordMov.getFields().add(new DocumentField(ordMov, dmfType, "NOVO", "REABASTECIMENTO"));
		ordMov.getFields().add(new DocumentField(ordMov, dmfPrio, "NOVO", String.valueOf(priority)));
		ordMov.getFields().add(new DocumentField(ordMov, dmfTitle, "NOVO", "REABASTECIMENTO"));
		ordMov.getTransports().add(new DocumentTransport(ordMov, 1, t, origin, destination));
		ordMov.getThings().add(new DocumentThing(ordMov, t, status));
		iSvc.getRegSvc().getDcSvc().persist(ordMov);
	}
}