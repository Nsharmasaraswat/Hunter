package com.gtp.hunter.custom.eurofarma.rest.app;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.gtp.hunter.process.service.RegisterService;

@Provider
public class AuthenticationFilter implements ContainerRequestFilter {

	@Context
	private ResourceInfo			resInf;

	//	@Inject
	//	private Logger					logger;

	@EJB(lookup = "java:global/hunter-core/RegisterService!com.gtp.hunter.process.service.RegisterService")
	private RegisterService			regSvc;

	private static final String		AUTHORIZATION_PROPERTY	= "Authorization";
	private static final String		AUTHENTICATION_SCHEME	= "Bearer";

	private static final Response	ACCESS_DENIED			= Response.status(Response.Status.UNAUTHORIZED).entity("You cannot access this resource").build();
	private static final Response	ACCESS_FORBIDDEN		= Response.status(Response.Status.FORBIDDEN).entity("Access blocked for all users !!").build();

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		Method method = resInf.getResourceMethod();

		if ((!requestContext.getUriInfo().getAbsolutePath().toString().contains("openapi.json")) && (method.isAnnotationPresent(DenyAll.class))) {
			requestContext.abortWith(ACCESS_FORBIDDEN);
			return;
		}

		if ((!requestContext.getUriInfo().getAbsolutePath().toString().contains("openapi.json")) && (!method.isAnnotationPresent(PermitAll.class))) {
			final MultivaluedMap<String, String> headers = requestContext.getHeaders();
			final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

			if (authorization == null || authorization.isEmpty()) {
				requestContext.abortWith(ACCESS_DENIED);
				return;
			}

			final String token = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");

			if (!regSvc.getAuthSvc().valid(token)) {
				requestContext.abortWith(ACCESS_DENIED);
				return;
			}
		}
	}
}
