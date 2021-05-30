package com.gtp.hunter.core.rest;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

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

import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.process.service.AlertService;

@RequestScoped
@Path("/alert")
public class AlertRest {

	@Inject
	private AlertService aSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAlerts() {
		return Response.ok(aSvc.listAll()).build();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(Alert alert) {
		if (alert.getId() != null) return Response.status(Status.BAD_REQUEST).entity("id CANNOT HAVE VALUE. USE PUT INSTEAD").build();
		try {
			return Response.ok(aSvc.persist(alert).get()).build();
		} catch (InterruptedException | ExecutionException ie) {
			return Response.status(Status.EXPECTATION_FAILED).entity("Failed saving Alert: " + ie.getLocalizedMessage()).build();
		}
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") UUID id, Alert alert) {
		if (alert.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id CANNOT BE NULL. USE POSTT INSTEAD").build();
		if (!alert.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id CANNOT BE DIFFERENTE THAN alert.id").build();
		try {
			return Response.ok(aSvc.persist(alert).get()).build();
		} catch (InterruptedException | ExecutionException ie) {
			return Response.status(Status.EXPECTATION_FAILED).entity("Failed updating Alert: " + ie.getLocalizedMessage()).build();
		}
	}

	@DELETE
	@Path("/{id}")
	public Response update(@PathParam("id") UUID id) {
		aSvc.removeById(id);
		return Response.ok().build();
	}

}
