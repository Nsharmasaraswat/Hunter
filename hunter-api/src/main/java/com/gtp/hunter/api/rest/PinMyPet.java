package com.gtp.hunter.api.rest;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.gtp.hunter.api.temp.PinMyPetMessage;
import com.gtp.hunter.common.util.Profiler;

@Path("/")
public class PinMyPet {

	private static final Executor	executor	= Executors.newCachedThreadPool();

	@Inject
	private transient Logger		logger;

	@POST
	@GET
	@Path("/rtls")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	@PermitAll
	public String deviceMessage(@QueryParam("data") String data) {
		Profiler p = new Profiler();

		executor.execute(() -> {
			logger.debug(data);
			final PinMyPetMessage pmpMsg = new PinMyPetMessage(data);

			logger.debug(pmpMsg.toString());
		});

		p.done("Processed", true, false);
		return "Message Received: " + data;
	}
}
