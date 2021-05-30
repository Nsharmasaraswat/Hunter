package com.gtp.hunter.custom.descarpack.rest.app;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import com.gtp.hunter.core.service.AuthService;

@Provider
public class AuthenticationFilter implements ContainerRequestFilter {

	@Inject
	private static transient Logger	logger;

	@Context
	private ResourceInfo			resInf;

	@EJB(lookup = "java:global/hunter-core-2.0.0-SNAPSHOT/AuthService!com.gtp.hunter.core.service.AuthService")
	private AuthService				authSvc;

	private static final String		AUTHORIZATION_PROPERTY	= "Authorization";
	private static final String		AUTHENTICATION_SCHEME	= "Bearer";

	private static final Response	ACCESS_DENIED			= Response.status(Response.Status.UNAUTHORIZED).entity("You cannot access this resource").build();
	private static final Response	ACCESS_FORBIDDEN		= Response.status(Response.Status.FORBIDDEN).entity("Access blocked for all users !!").build();

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		logger.debug("INICIO DO FILTRO");
		Method method = resInf.getResourceMethod();

		if (method.isAnnotationPresent(DenyAll.class)) {
			requestContext.abortWith(ACCESS_FORBIDDEN);
			return;
		}

		if (!method.isAnnotationPresent(PermitAll.class)) {
			final MultivaluedMap<String, String> headers = requestContext.getHeaders();
			final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

			if (authorization == null || authorization.isEmpty()) {
				requestContext.abortWith(ACCESS_DENIED);
				return;
			}

			final String token = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");

			if (!authSvc.valid(token)) {
				requestContext.abortWith(ACCESS_DENIED);
				return;
			}

		}

	}

}
