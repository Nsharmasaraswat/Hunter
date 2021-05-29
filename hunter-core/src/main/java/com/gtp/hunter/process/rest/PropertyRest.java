package com.gtp.hunter.process.rest;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.service.RegisterService;

@Transactional
@RequestScoped
@Path("/property")
public class PropertyRest {

	@Inject
	private RegisterService regSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Property> getListContainsAllProperty() {
		return regSvc.getPrSvc().listAll();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Property getOneProperty(@PathParam("id") UUID id) {
		return regSvc.getPrSvc().findById(id);
	}

	@GET
	@Path("/bytype/{metanamemodel}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPropertyByMetaName(@PathParam("metaname") String metaName) {
		return Response.ok(regSvc.getPrSvc().findByMetaname(metaName)).build();
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deletePropertyById(@PathParam("id") UUID id) {
		regSvc.getPrSvc().deletePropertyByID(id);
		return Response.ok().build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addProperty(Property property) {
		return Response.ok(regSvc.getPrSvc().persist(property)).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateProperty(@PathParam("id") UUID id, Property property) {
		if (id == null || property.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!property.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		return Response.ok(regSvc.getPrSvc().persist(property)).build();
	}
}
