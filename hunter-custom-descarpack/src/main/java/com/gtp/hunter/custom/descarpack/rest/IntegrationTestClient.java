package com.gtp.hunter.custom.descarpack.rest;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.RestUtil;

@Path("testClient")
public class IntegrationTestClient {

	@Inject
	private static transient Logger logger;

	@GET
	@Path("/testPost/{ip}/{port}/{method}")
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public IntegrationReturn testPost(@PathParam("ip") String ip, @PathParam("port") int port, @PathParam("method") String method) {
		String url = "http://" + ip + ":" + port;
		RestUtil ru = new RestUtil(url);
		JsonObject jo = Json.createObjectBuilder().build();
		logger.debug("BEGIN");
		IntegrationReturn ir = ru.sendSync(jo, method);
		logger.debug("END");
		return ir;
	}

}
