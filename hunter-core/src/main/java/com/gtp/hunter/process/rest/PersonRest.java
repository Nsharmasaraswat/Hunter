package com.gtp.hunter.process.rest;

import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.gtp.hunter.process.model.Person;
import com.gtp.hunter.process.model.PersonField;
import com.gtp.hunter.process.service.RegisterService;

@RequestScoped
@Path("/person")
public class PersonRest {

	@Inject
	private RegisterService regSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPersons() {
		return Response.ok(regSvc.getPsSvc().getListAllPerson()).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPerson(@PathParam("id") UUID id) {
		return Response.ok(regSvc.getPsSvc().findById(id)).build();
	}

	@GET
	@Path("/bytypeproperty/{type}/{prop}/{value}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPersonByProperty(@PathParam("type") String type, @PathParam("prop") String prop,
					@PathParam("value") String value) {
		return Response.ok(regSvc.getPsSvc().getByPropModelAndMetaname(type, prop, value)).build();
	}

	@GET
	@Path("/bytypecode/{model}/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getByModelAndCode(@PathParam("model") String model, @PathParam("code") String code) {
		return Response.ok(regSvc.getPsSvc().getByModelAndCode(model, code)).build();
	}

	@GET
	@Path("/bytype/{model}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getByModel(@PathParam("model") String model) {
		return Response.ok(regSvc.getPsSvc().listByModelMetaname(model)).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(Person ps) {
		regSvc.getPsSvc().persist(ps);
		for (PersonField psf : ps.getFields())
			psf.setPerson(ps);
		return Response.ok(ps).build();
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updatePerson(@PathParam("id") UUID id, Person prs) {
		if (id == null || prs.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!prs.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		prs.getFields().parallelStream().forEach(prsf -> prsf.setPerson(prs));
		return Response.ok(regSvc.getPsSvc().persist(prs)).build();
	}

}
