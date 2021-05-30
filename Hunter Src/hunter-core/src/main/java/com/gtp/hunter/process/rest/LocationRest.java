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

import com.gtp.hunter.process.model.Location;
import com.gtp.hunter.process.service.LocationService;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

@RequestScoped
@Path("/location")
public class LocationRest {

	@Inject
	private LocationService locSvc;

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response byId(@PathParam("id") UUID locId) {
		return Response.ok(locSvc.findById(locId)).build();
	}

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listAll() {
		return Response.ok(locSvc.listAll()).build();
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteLocationById(@PathParam("id") UUID id) {
		locSvc.removeById(id);
		return Response.ok().build();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(Location location) {
		if (location.getWkt() == null) return Response.status(Status.BAD_REQUEST).entity("wkt cannot be null.").build();
		WKTReader rdr = new WKTReader();

		try {
			Geometry g = rdr.read(location.getWkt());

			location.setCenter(g.getCentroid());
			return Response.ok(locSvc.persist(location)).build();
		} catch (ParseException e) {
			return Response.status(Status.BAD_REQUEST).entity("invalid wkt format (" + location.getWkt() + ")").build();
		}
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateAddress(@PathParam("id") UUID id, Location location) {
		if (id == null || location.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!location.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		return Response.ok(locSvc.persist(location)).build();
	}

	@GET
	@Path("/byMetaname/{metaname}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response byMetaname(@PathParam("metaname") String metaname) {
		return Response.ok(locSvc.findByMetaname(metaname)).build();
	}
}
