package com.gtp.hunter.custom.solar.rest;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.process.jsonstubs.AGLProd;
import com.gtp.hunter.process.model.Product;

@RequestScoped
@Path("/product")
public class ProductRest {

	@Inject
	private IntegrationService iSvc;

	@GET
	@Path("/agl")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAll() {
		List<Product> pList = iSvc.getRegSvc().getPrdSvc().listAll();

		List<AGLProd> pmList = pList.stream().map(p -> {
			p.setFields(p.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("VAR_WEIGHT") || pf.getModel().getMetaname().equals("GROSS_WEIGHT")).collect(Collectors.toSet()));
			return iSvc.getRegSvc().getAglSvc().convertProdToAGL(p);
		}).collect(Collectors.toList());

		return Response.ok(pmList).build();
	}
}
