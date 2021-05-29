package com.gtp.hunter.custom.eurofarma.rest.app;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.MDC;
import org.slf4j.Logger;

@Provider
public class CoreProfilerFilter implements ContainerRequestFilter, ContainerResponseFilter {

	@Context
	private HttpServletRequest	sr;
	
	@Inject
	private Logger logger;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// Note down the start request time...we will use to calculate the total
		// execution time
		MDC.put("start-time", String.valueOf(System.currentTimeMillis()));
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		String stTime = (String) MDC.get("start-time");
		if (null == stTime || stTime.length() == 0) {
			return;
		}
		long startTime = Long.parseLong(stTime);
		long executionTime = System.currentTimeMillis() - startTime;
		logger.info("[Profiler: " + sr.getRequestURI() + " took " + executionTime + "ms]");
		MDC.clear();
	}
}