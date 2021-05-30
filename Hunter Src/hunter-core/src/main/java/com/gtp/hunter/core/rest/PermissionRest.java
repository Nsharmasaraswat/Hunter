package com.gtp.hunter.core.rest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.repository.GroupRepository;
import com.gtp.hunter.core.repository.UserRepository;
import com.gtp.hunter.core.service.PermissionCategoryService;
import com.gtp.hunter.core.service.PermissionService;

@RequestScoped
@Path("/permission")
@Transactional
public class PermissionRest {

	@Inject
	private PermissionService			pRep;
	
	@Inject
	private UserRepository				uRep;
	
	@Inject
	private GroupRepository				gRep;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Permission> getAll() {
		return pRep.listAll();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Permission getById(@PathParam("id") UUID id) {
		Permission p = null;
		try {
			p = pRep.findById(id);
		} catch (Exception e) {
			p = new Permission();
		}
		return p;
	}

	@GET
	@Path("/byApp/{app}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listByApp(@PathParam("app") String app) {
		return Response.ok(pRep.listByApp(app)).build();
	}

	@GET
	@Path("/byCategory/{category_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listByCategory(@PathParam("category_id") UUID catId) {
		return Response.ok(pRep.listByCategoryId(catId)).build();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response save(Permission perm) {
		if (perm.getCategory() == null || perm.getCategory().getId() == null) return Response.status(Status.BAD_REQUEST).entity("PermissionCategory must be a valid existing entiry").build();

		return Response.ok(pRep.persist(perm)).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") UUID id, Permission p) {
		if (id == null || p.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!p.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		Permission perm = pRep.findById(id);
		if (perm == null) return Response.status(Status.BAD_REQUEST).entity("No permission with id found in database").build();
		if (p.getCreatedAt() == null) p.setCreatedAt(perm.getCreatedAt());
		return Response.ok(pRep.persist(p)).build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteById(@PathParam("id") UUID id) {
		pRep.removeById(id);
		return Response.ok().build();
	}

	@GET
	@Path("/user/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Permission> getPermissionsByUser(@PathParam("id") UUID userId) {
		User u = uRep.findById(userId);

		if (u != null) {
			return u.getPermissions();
		}
		return new HashSet<>();
	}

	@GET
	@Path("/group/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Permission> getPermissionsByGroup(@PathParam("id") UUID groupId) {
		Group g = gRep.findById(groupId);

		if (g != null) {
			return g.getPermissions();
		}
		return new HashSet<>();
	}

	@PUT
	@Path("/group/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response replaceGroupPermissions(List<Permission> permissions, @PathParam("id") UUID groupId) {
		Group g = gRep.findById(groupId);

		g.getPermissions().removeIf(p -> permissions.parallelStream()
						.noneMatch(pr -> pr.getId().equals(p.getId())));

		// adding new
		for (Permission p : permissions) {
			if (g.getPermissions().parallelStream()
							.noneMatch(pr -> pr.getId().equals(p.getId()))) {
				Permission perm = pRep.findById(p.getId());
				if (perm != null)
					g.getPermissions().add(perm);
			}
		}

		gRep.persist(g);
		return Response.ok(g).build();
	}

	@PUT
	@Path("/user/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveUserPermissions(List<Permission> permissions, @PathParam("id") UUID userId) {
		User u = uRep.findById(userId);

		u.getPermissions().removeIf(p -> permissions.parallelStream()
						.noneMatch(pr -> pr.getId().equals(p.getId())));

		// adding new
		for (Permission p : permissions) {
			if (u.getPermissions().parallelStream().noneMatch(pr -> pr.getId().equals(p.getId()))) {
				Permission perm = pRep.findById(p.getId());
				if (perm != null)
					u.getPermissions().add(perm);
			}
		}

		uRep.persist(u);
		return Response.ok(u).build();
	}
}
