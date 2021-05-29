package com.gtp.hunter.core.rest;

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

import com.gtp.hunter.core.model.PermissionCategory;
import com.gtp.hunter.core.service.PermissionCategoryService;

@RequestScoped
@Path("/permissioncategory")
public class PermissionCategoryRest {

	@Inject
	private PermissionCategoryService pcSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAll() {
		return Response.ok(pcSvc.listAll()).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getById(@PathParam("id") UUID id) {
		return Response.ok(pcSvc.findById(id)).build();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response save(PermissionCategory pc) {
		return Response.ok(pcSvc.persist(pc)).build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteById(@PathParam("id") UUID id) {
		pcSvc.removeById(id);
		return Response.ok().build();
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") UUID id, PermissionCategory p) {
		if (id == null || p.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!p.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		return Response.ok(pcSvc.persist(p)).build();
	}
}
