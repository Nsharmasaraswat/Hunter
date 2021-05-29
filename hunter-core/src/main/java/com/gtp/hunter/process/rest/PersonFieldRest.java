package com.gtp.hunter.process.rest;

import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
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

import com.google.gson.GsonBuilder;
import com.gtp.hunter.process.model.Person;
import com.gtp.hunter.process.model.PersonField;
import com.gtp.hunter.process.service.PersonFieldService;
import com.gtp.hunter.process.service.PersonService;

@RequestScoped
@Path("/personfield")
public class PersonFieldRest {

	@Inject
	private PersonFieldService	psfSvc;

	@Inject
	private PersonService		psSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getListContainsAllPersonField() {
		return Response.ok(psfSvc.getListAllPersonField()).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOnePersonField(@PathParam("id") UUID id) {
		return Response.ok(psfSvc.findById(id)).build();
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deletePersonFieldById(@PathParam("id") UUID id) {
		psfSvc.deleteById(id);
		return Response.ok().build();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(JsonObject psfjs) {
		if (!psfjs.containsKey("person")) return Response.status(Status.BAD_REQUEST).entity("person cannot be null").build();

		UUID personId = UUID.fromString(psfjs.getJsonObject("person").getString("id", UUID.randomUUID().toString()));
		Person ps = psSvc.findById(personId);

		if (ps == null) return Response.status(Status.BAD_REQUEST).entity("Invalid person: " + psfjs.getJsonObject("person").getString("id")).build();

		PersonField psf = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(psfjs.toString(), PersonField.class);

		psf.setPerson(ps);
		return Response.ok(psfSvc.persist(psf)).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") UUID id, PersonField psf) {
		if (id == null || psf.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!psf.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();

		if (psf.getPerson() == null) {
			PersonField ppsf = psfSvc.findById(id);

			psf.setPerson(ppsf.getPerson());
		}
		return Response.ok(psfSvc.persist(psf)).build();
	}

}
