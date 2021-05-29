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

import com.gtp.hunter.process.model.AddressModel;
import com.gtp.hunter.process.model.AddressModelField;
import com.gtp.hunter.process.service.AddressModelService;

@Transactional
@RequestScoped
@Path("/addressmodel")
public class AddressModelRest {

	@Inject
	private AddressModelService addMdSrv;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listAll() {
		return Response.ok(addMdSrv.getListAllAddressModel()).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findById(@PathParam("id") UUID id) {
		return Response.ok(addMdSrv.findById(id)).build();
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteAddressModelById(@PathParam("id") UUID id) {
		addMdSrv.removeById(id);
		return Response.ok().build();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(AddressModel addressModel) {
		addMdSrv.persist(addressModel);
		for (AddressModelField amf : addressModel.getFields())
			amf.setModel(addressModel);
		return Response.ok(addressModel).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateAddressModel(@PathParam("id") UUID id, AddressModel am) {
		if (id == null || am.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!am.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		for (AddressModelField amf : am.getFields()) {
			amf.setModel(am);
		}
		return Response.ok(addMdSrv.persist(am)).build();
	}
}
