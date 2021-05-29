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

import com.gtp.hunter.core.model.Group;
import com.gtp.hunter.core.model.Permission;
import com.gtp.hunter.core.service.GroupService;
import com.gtp.hunter.core.service.PermissionService;

@RequestScoped
@Path("/group")
@Transactional
public class GroupRest {

	@Inject
	private GroupService		grSvc;

	@Inject
	private PermissionService	pSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Group> getAll() {
		return grSvc.listAll();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Group getById(@PathParam("id") String id) {
		Group g = null;
		try {
			g = grSvc.findById(UUID.fromString(id));
		} catch (Exception e) {
			g = new Group();
		}
		return g;
	}

	@GET
	@Path("/user/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getById(@PathParam("id") UUID userid) {
		return Response.ok(grSvc.listByUserId(userid)).build();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Group save(Group group) {
		grSvc.persist(group);
		return group;
	}

	@DELETE
	@Path("/{id}")
	public Response deleteById(@PathParam("id") String id) {
		grSvc.removeById(UUID.fromString(id));
		return Response.ok().build();
	}

	@PUT
	@Path("/permission/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addPermission(@PathParam("id") UUID id, Permission p) {
		Group g = grSvc.findById(id);
		if (g == null) return Response.status(Status.BAD_REQUEST).entity("No group found with ID " + id).build();
		Permission perm = pSvc.findById(p.getId());
		if (perm == null) {
			perm = pSvc.persist(p);
		}

		g.getPermissions().add(perm);
		return Response.ok(g).build();
	}

	@DELETE
	@Path("/permission/{id}/{permId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removePermission(@PathParam("id") UUID id, @PathParam("permId") UUID permId) {
		Group g = grSvc.findById(id);
		if (g == null) return Response.status(Status.BAD_REQUEST).entity("No group found with ID " + id).build();
		g.getPermissions().removeIf(perm -> perm.getId().equals(permId));
		return Response.ok().build();
	}
}
