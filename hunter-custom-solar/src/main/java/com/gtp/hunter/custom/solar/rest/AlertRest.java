package com.gtp.hunter.custom.solar.rest;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.gson.GsonBuilder;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;

@RequestScoped
@Path("/alert")
public class AlertRest {

	@Inject
	private IntegrationService iSvc;

	@POST
	@PermitAll
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public IntegrationReturn addAlert(Alert a) {
		System.out.println("CHEGOU POST DA NAVE MAE");
		System.out.println(new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create().toJson(a, Alert.class));
		iSvc.getRegSvc().getAlertSvc().persist(a);
		return IntegrationReturn.OK;
	}

}
