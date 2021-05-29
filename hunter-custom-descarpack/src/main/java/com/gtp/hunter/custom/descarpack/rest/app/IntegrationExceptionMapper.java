package com.gtp.hunter.custom.descarpack.rest.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.google.gson.Gson;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;

@Provider
public class IntegrationExceptionMapper implements ExceptionMapper<Throwable> {

	@Inject
	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());// = Logger.getLogger(IntegrationExceptionMapper.class.getSimpleName());

	@Inject
	private HttpServletRequest	req;

	@Override
	public Response toResponse(Throwable e) {
		if ("OPTIONS".equals(req.getMethod())) {
			ResponseBuilder ret = Response.ok("HEAD, DELETE, POST, PUT, GET, OPTIONS");
			ret.type(MediaType.TEXT_PLAIN);
			return ret.build();
		} else {
			Gson g = new Gson();
			String ret = g.toJson(new IntegrationReturn(false, e.getLocalizedMessage()));

			logger.log(Level.SEVERE, "Exception Caught: " + e.getLocalizedMessage(), e);
			return Response.ok(ret).build();
		}

	}
}
