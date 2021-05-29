package com.gtp.hunter.core.rest;

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

import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.core.repository.UnitRepository;
import com.gtp.hunter.core.service.UnitService;

@RequestScoped
@Path("/unit")
@Transactional
public class UnitRest {

	@Inject
	private UnitRepository	uRep;

	@Inject
	private UnitService		uSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Unit> getAll() {
		return uRep.listAll();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getById(@PathParam("id") UUID id) {
		return Response.ok(uSvc.getUnitById(id)).build();
	}

	@GET
	@Path("/bytypeandtagid/{type}/{tagid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getByTagId(@PathParam("type") UnitType type, @PathParam("tagid") String tagid) {
		return Response.ok(uSvc.findByTypeAndTagId(type, tagid)).build();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(Unit unit) {
		if (unit != null && unit.getId() != null) return Response.status(Status.BAD_REQUEST).entity("Cannot save entity with non null id").build();
		return Response.ok(uRep.persist(unit)).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(@PathParam("id") UUID id, Unit unit) {
		if (unit == null || unit.getId() != null) return Response.status(Status.BAD_REQUEST).entity("Cannot update entity with null id").build();
		if (!unit.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("ID must match the body entity's ID").build();
		Unit dbu = uRep.findById(id);

		dbu.setMetaname(unit.getMetaname());
		dbu.setName(unit.getName());
		dbu.setStatus(unit.getStatus());
		dbu.setTagId(unit.getTagId());
		dbu.setType(unit.getType());
		return Response.ok(dbu).build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteById(@PathParam("id") String id) {
		uRep.removeById(UUID.fromString(id));
		return Response.ok().build();
	}
}
