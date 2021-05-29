package com.gtp.hunter.process.rest;

import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
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

import com.gtp.hunter.process.model.ProductField;
import com.gtp.hunter.process.service.RegisterService;

@RequestScoped
@Path("/productfield")
public class ProductFieldRest {

	@Inject
	private RegisterService regSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listAll() {
		return Response.ok(regSvc.getPfSvc().listAll()).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOneRegister(@PathParam("id") UUID id) {
		return Response.ok(regSvc.getPfSvc().findById(id)).build();

	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteRegisterByID(@PathParam("id") UUID id) {
		regSvc.getPfSvc().deleteProductFieldByID(id);
		return Response.ok().build();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(ProductField productField) {
		return Response.ok(regSvc.getPfSvc().persist(productField)).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") UUID id, ProductField productField) {
		if (id == null || productField.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!productField.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		return Response.ok(regSvc.getPfSvc().persist(productField)).build();
	}

}
