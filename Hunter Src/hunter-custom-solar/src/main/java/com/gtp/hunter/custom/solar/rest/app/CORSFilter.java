package com.gtp.hunter.custom.solar.rest.app;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class CORSFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
		if (request.getHeaderString("origin") != null) {
			response.getHeaders().add("Access-Control-Allow-Origin", request.getHeaderString("origin"));
			response.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, responseType, observe");
			response.getHeaders().add("Access-Control-Allow-Credentials", "true");
			response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
		}
	}
}