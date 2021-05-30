package com.gtp.hunter.api.rest.app;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.MDC;

@Provider
public class CoreProfilerFilter implements ContainerRequestFilter, ContainerResponseFilter {

	@Context
	private HttpServletRequest	sr;

	private static final Logger	LOGGER	= Logger.getLogger(CoreProfilerFilter.class.getSimpleName());

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
		LOGGER.info("[Profiler: " + sr.getRequestURI() + " took " + executionTime + "ms]");
		MDC.clear();
	}
}