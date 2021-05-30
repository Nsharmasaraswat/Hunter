package com.gtp.hunter.custom.solar.rest;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.jsonstubs.AGLAddressProps;
import com.gtp.hunter.process.model.Address;

@RequestScoped
@Path("/address")
public class AddressRest {

	@Inject
	private Logger		logger;

	@Inject
	IntegrationService	iSvc;

	@GET
	@Path("/all")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAll() {
		//Only WMS
		List<Address> pList = iSvc.getRegSvc().getAddSvc().listByLocation(UUID.fromString("4cc18967-92f3-11e9-815b-005056a19775"));

		logger.info("List Size: " + pList.size());
		if (ConfigUtil.get("hunter-custom-solar", "full-wms-address", "").equalsIgnoreCase("true")) {
			List<AGLAddressProps> pmList = pList.stream()
							.map(p -> {
								return iSvc.getRegSvc().getAglSvc().convertAddressToAGLProps(p);
							}).collect(Collectors.toList());

			logger.info("List Size: " + pmList.size());
			return Response.ok(pmList).build();
		} else {
			//Only ARMLOC
			List<AGLAddressProps> pmList = pList.stream()
							.filter(p -> p.getModel().getMetaname().equalsIgnoreCase("RACK") || p.getModel().getMetaname().equalsIgnoreCase("DRIVE-IN") || p.getModel().getMetaname().equalsIgnoreCase("BLOCK") || p.getModel().getMetaname().equalsIgnoreCase("ROAD"))
							.map(p -> {
								return iSvc.getRegSvc().getAglSvc().convertAddressToAGLProps(p);
							}).collect(Collectors.toList());

			logger.info("List Size: " + pmList.size());
			return Response.ok(pmList).build();
		}
	}

	@PUT
	@Path("/changeStatus/{id}/{status}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn changeStatus(@PathParam("id") UUID id, @PathParam("status") String status) {
		Address a = iSvc.getRegSvc().getAddSvc().findById(id);

		if (a != null) {
			a.setStatus(status);
			a.setUpdatedAt(Calendar.getInstance().getTime());
			for (Address sib : a.getSiblings()) {
				sib.setStatus(status);
				sib.setUpdatedAt(Calendar.getInstance().getTime());
			}
			iSvc.getRegSvc().getAglSvc().sendAddressToWMS(a, "PUT");
		}
		iSvc.getRegSvc().getAddSvc().persist(a);
		return IntegrationReturn.OK;
	}
}
