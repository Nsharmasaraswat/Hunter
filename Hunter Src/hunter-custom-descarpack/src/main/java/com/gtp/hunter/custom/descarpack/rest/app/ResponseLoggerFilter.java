package com.gtp.hunter.custom.descarpack.rest.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.MDC;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.util.StreamUtil;
import com.gtp.hunter.process.model.IntegrationLog;
import com.gtp.hunter.process.service.RegisterService;

@Provider
public class ResponseLoggerFilter implements ContainerRequestFilter, ContainerResponseFilter {

	@EJB(lookup = "java:global/hunter-core-2.0.0-SNAPSHOT/RegisterService!com.gtp.hunter.process.service.RegisterService")
	private RegisterService		rSvc;

	@Context
	private ResourceInfo		resourceInfo;

	@Context
	private HttpServletRequest	sr;

	@Inject
	private transient Logger	log;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		String urlRequest = requestContext.getHeaderString("HOST") + requestContext.getUriInfo().getPath();
		// Note down the start request time...we will use to calculate the total
		// execution time
		MDC.put("start-time", String.valueOf(System.currentTimeMillis()));
		MDC.put("resource", urlRequest);
		MDC.put("method", requestContext.getMethod());
		MDC.put("address", sr.getRemoteAddr());
		MDC.put("body", readEntityStream(requestContext));
		//log.info("Request {} {} media type {}", requestContext.getMethod(), urlRequest, requestContext.getMediaType());
	}

	private String readEntityStream(ContainerRequestContext requestContext) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		final InputStream inputStream = requestContext.getEntityStream();
		final StringBuilder builder = new StringBuilder();
		try {
			StreamUtil.copy(inputStream, outStream);
			byte[] requestEntity = outStream.toByteArray();
			if (requestEntity.length == 0) {
				builder.append("");
			} else {
				builder.append(new String(requestEntity));
			}
			requestContext.setEntityStream(new ByteArrayInputStream(requestEntity));
		} catch (Exception ex) {
			log.error(ex.getLocalizedMessage());
			log.trace(ex.getLocalizedMessage(), ex);
		}
		return builder.toString();
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		String stTime = (String) MDC.get("start-time");
		if (null == stTime || stTime.length() == 0) {
			return;
		}
		long startTime = Long.parseLong(stTime);
		long executionTime = System.currentTimeMillis() - startTime;
		String response = "";

		if (responseContext.hasEntity()) {
			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().setPrettyPrinting().create();

			response = gson.toJson(responseContext.getEntityClass().cast(responseContext.getEntity()));
			//log.info("Response: {}", response);
		} else {
			log.info("Não tem entity. Media Type = {}", responseContext.getMediaType());
		}
		IntegrationLog il = new IntegrationLog((String) MDC.get("resource"), (String) MDC.get("method"), (String) MDC.get("address"), (String) MDC.get("body"), responseContext.getStatus(), response, executionTime);

		rSvc.getIlRep().persist(il);
		MDC.clear();
	}
}