package com.gtp.hunter.core.rest;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gtp.hunter.common.model.RawData.RawDataType;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Source;
import com.gtp.hunter.core.repository.SourceRepository;
import com.gtp.hunter.core.service.RawDataService;

@RequestScoped
@Path("/coremobile")
public class MobileRest {

	@Resource
	private ManagedExecutorService	mes;

	@Inject
	private RawDataService			rds;

	@Inject
	private SourceRepository		srcRep;

	@POST
	@Path("/source")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response postRawDataMobile(JsonObject payload) {
		Profiler p = new Profiler();

		p.step(payload.toString(), false);
		mes.execute(() -> {
			ComplexData cd = new ComplexData();
			Source src = srcRep.findByMetaname(payload.getString("source"));

			p.step("Source Found", false);
			for (Device d : src.getDevices()) {
				if (d.getMetaname().equals(payload.getString("device"))) {
					p.step("Device Found", false);
					cd.setDevice(d.getId());
					cd.setPayload(payload.getString("payload"));
					cd.setPort(payload.getInt("port"));
					cd.setTagId(payload.getString("tagid"));
					cd.setSource(src.getId());
					cd.setTs(payload.getJsonNumber("timestamp").longValue());
					cd.setType(RawDataType.IDENT);
					rds.processRawData(cd);
					break;
				}
			}
		});

		p.done("Processed", false, true);
		return Response.ok().build();
	}
}
