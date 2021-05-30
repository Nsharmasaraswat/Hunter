package com.gtp.hunter.process.rest;

import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.service.RegisterService;

@Transactional
@RequestScoped
@Path("/propertymodel")
public class PropertyModelRest {

	@Inject
	private RegisterService regSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAll() {
		return Response.ok(regSvc.getPrmSvc().listAll()).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getById(@PathParam("id") String id) {
		return Response.ok(regSvc.getPrmSvc()).build();
	}

	@GET
	@Path("/metaname/{metaname}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getByMetaname(@PathParam("metaname") String metaname) {
		return Response.ok(regSvc.getPrmSvc().findByMetaname(metaname)).build();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(PropertyModel propMod) {
		regSvc.getPrmSvc().persist(propMod);//Pesist to get id
		for (PropertyModelField prmf : propMod.getFields())
			prmf.setModel(propMod);
		return Response.ok(propMod).build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteById(@PathParam("id") String id) {
		regSvc.getPrmSvc().removeById(UUID.fromString(id));
		return Response.ok().build();
	}
}
