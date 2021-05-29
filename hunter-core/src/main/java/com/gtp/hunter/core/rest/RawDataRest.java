package com.gtp.hunter.core.rest;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.repository.RawDataRepository;
import com.gtp.hunter.core.service.RawDataService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;

@RequestScoped
@Path("/rawdata")
public class RawDataRest {

	@Inject
	private Logger				logger;

	@Inject
	private RawDataRepository	rdRep;

	@Inject
	private RawDataService		rds;

	@GET
	@Path("/listInterval")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ComplexData> listInterval(@QueryParam("begin") Long begin, @QueryParam("end") Long end) {
		return rdRep.listInterval(begin, end);
	}

	@POST
	@Path("/input")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void testrdsm(ComplexData ld) {
		rds.processRawData(ld);
	}

	@POST
	@Path("/toremove")
	@PermitAll
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn toRemoveLater(ComplexData ld) {
		rds.processRawData(ld);
		return IntegrationReturn.OK;
	}

	@POST
	@Path("/repeat/{period}/{freq}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void testrdsm(ComplexData ld, @PathParam("period") int period, @PathParam("freq") int frequency) {
		long begin = System.currentTimeMillis();

		do {
			try {
				rds.processRawData(ld);
				Thread.sleep(frequency);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (System.currentTimeMillis() - begin > period);
	}
}
