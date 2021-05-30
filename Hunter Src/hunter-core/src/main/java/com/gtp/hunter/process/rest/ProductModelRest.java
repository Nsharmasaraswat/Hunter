package com.gtp.hunter.process.rest;

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

import com.gtp.hunter.process.model.ProductModel;
import com.gtp.hunter.process.model.ProductModelField;
import com.gtp.hunter.process.service.RegisterService;

@Transactional
@RequestScoped
@Path("/productmodel")
public class ProductModelRest {

	@Inject
	private RegisterService regSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAll() {
		return Response.ok(regSvc.getPmSvc().listAll()).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getById(@PathParam("id") String id) {
		return Response.ok(regSvc.getPmSvc().findById(UUID.fromString(id))).build();
	}

	@GET
	@Path("/bymetaname/{metaname}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getByMetaname(@PathParam("metaname") String metaname) {
		return Response.ok(regSvc.getPmSvc().findByMetaname(metaname)).build();
	}

	@GET
	@Path("/listsiblings/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getByMetaname(@PathParam("id") UUID parent_id) {
		return Response.ok(regSvc.getPmSvc().listByParentId(parent_id)).build();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(ProductModel prodMod) {
		regSvc.getPmSvc().persist(prodMod);
		for (ProductModelField pmf : prodMod.getFields())
			pmf.setModel(prodMod);
		return Response.ok(prodMod).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") UUID id, ProductModel pm) {
		if (id == null || pm.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!pm.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		return Response.ok(regSvc.getPmSvc().persist(pm)).build();
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteById(@PathParam("id") String id) {
		regSvc.getPmSvc().deleteById(UUID.fromString(id));
		return Response.ok().build();
	}
}
