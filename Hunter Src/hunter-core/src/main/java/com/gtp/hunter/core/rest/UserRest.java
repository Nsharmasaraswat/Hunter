package com.gtp.hunter.core.rest;

import java.util.ArrayList;
import java.util.HashSet;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;

import com.gtp.hunter.core.model.CredentialPassword;
import com.gtp.hunter.core.model.Group;
import com.gtp.hunter.core.model.Permission;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.service.AuthService;
import com.gtp.hunter.core.service.CredentialService;
import com.gtp.hunter.core.service.GroupService;
import com.gtp.hunter.core.service.PermissionService;
import com.gtp.hunter.core.service.UserService;

@Path("/user")
@RequestScoped
@Transactional
public class UserRest {

	@Inject
	private Logger				logger;

	@Inject
	private AuthService			authSvc;

	@Inject
	private UserService			uSvc;

	@Inject
	private GroupService		gSvc;

	@Inject
	private CredentialService	crSvc;

	@Inject
	private PermissionService	pSvc;

	@GET
	@Path("/permission")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Permission> getPermissions(@Context HttpHeaders rs) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];

		List<Permission> lst = authSvc.getPerms(token);

		return lst;
	}

	@GET
	@Path("/properties")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProperties(@Context HttpHeaders rs) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];

		return Response.ok(authSvc.getUser(token).getProperties()).build();
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@Context HttpHeaders rs) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];

		return Response.ok(authSvc.getUser(token)).build();
	}

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> listAll() {
		return uSvc.listAll();
	}

	@GET
	@Path("/bygroup/{grpId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listByGroupId(@PathParam("grpId") UUID grpId) {
		Group grp = gSvc.findById(grpId);

		return Response.ok(new ArrayList<>(grp == null ? null : grp.getUsers())).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User getById(@PathParam("id") String id) {
		User u = null;
		try {
			u = uSvc.findById(UUID.fromString(id));
			if (u.getProperties().containsKey("UNIT"))
				u.setUnit(u.getProperties().get("UNIT"));
			for (Group g : u.getGroups()) {
				u.setGrpId(g.getId().toString());
			}

		} catch (Exception e) {
			u = new User();
		}
		return u;
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public User save(User tmpusr) {
		User usr = tmpusr.getId() == null ? new User() : uSvc.findById(tmpusr.getId());
		HashSet<Group> tmpgrp = new HashSet<Group>();
		logger.info("Name: " + tmpusr.getName());
		logger.info("Login: " + tmpusr.getLogin());
		logger.info("Pwd: " + tmpusr.getPwd());
		logger.info("Grp: " + tmpusr.getGrpId());
		logger.info("Badge: " + tmpusr.getUnit());

		if (tmpusr.getUnit() != null && !tmpusr.getUnit().isEmpty())
			usr.getProperties().put("UNIT", tmpusr.getUnit());

		if (tmpusr.getGrpId() != null && !tmpusr.getGrpId().isEmpty())
			tmpgrp.add(gSvc.findById(UUID.fromString(tmpusr.getGrpId())));

		usr.setName(tmpusr.getName());
		usr.setStatus(tmpusr.getStatus());
		usr.setGroups(tmpgrp);
		uSvc.persist(usr);

		if (tmpusr.getLogin() != null && tmpusr.getPwd() != null) {
			CredentialPassword crd = new CredentialPassword(usr, tmpusr.getLogin(), tmpusr.getPwd());
			usr.getCredentials().add(crd);
			crSvc.persist(crd);
		}

		return usr;
	}

	@DELETE
	@Path("/{id}")
	public Response deleteById(@PathParam("id") UUID id) {
		User us = uSvc.findById(id);

		us.getCredentials().forEach(cr -> crSvc.removeById(cr.getId()));
		uSvc.removeById(id);
		return Response.ok().build();
	}

	@GET
	@Path("/property/{key}/{value}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findByPropertyValue(@PathParam("key") String key, @PathParam("value") String value) {
		return Response.ok(uSvc.findByProperty(key, value)).build();
	}

	@POST
	@Path("/property/{key}/{value}")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response setProperty(@Context HttpHeaders rs, @PathParam("key") String key, @PathParam("value") String value) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User us = uSvc.findById(authSvc.getUser(token).getId());

		if (us == null) return Response.status(Status.BAD_REQUEST).entity("No user found with current token").build();
		if (us.getProperties().containsKey(key)) return Response.status(Status.BAD_REQUEST).entity("Property " + key + " already on user. Use PUT instead.").build();
		authSvc.getUser(token).getProperties().put(key, value);
		us.getProperties().put(key, value);
		return Response.ok().build();
	}

	@PUT
	@Path("/property/{key}/{value}")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response updateProperty(@Context HttpHeaders rs, @PathParam("key") String key, @PathParam("value") String value) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User us = uSvc.findById(authSvc.getUser(token).getId());

		if (us == null) return Response.status(Status.BAD_REQUEST).entity("No user found with current token").build();
		if (!us.getProperties().containsKey(key)) if (us.getProperties().containsKey(key)) return Response.status(Status.BAD_REQUEST).entity("Property " + key + " not present on user. Use POST instead.").build();
		authSvc.getUser(token).getProperties().put(key, value);
		us.getProperties().put(key, value);
		return Response.ok().build();
	}

	@PUT
	@Path("/permission/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addPermission(@PathParam("id") UUID id, Permission p) {
		User u = uSvc.findById(id);
		Permission perm = pSvc.findById(p.getId());

		if (perm == null) {
			perm = pSvc.persist(p);
		}
		u.getPermissions().add(perm);
		return Response.ok(u).build();
	}

	@PUT
	@Path("/group/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addGroup(@PathParam("id") UUID id, Group gr) {
		User u = uSvc.findById(id);
		Group g = gSvc.findById(gr.getId());

		if (g == null) {
			g = gSvc.persist(g);
		}
		u.getGroups().add(g);
		return Response.ok(u).build();
	}

	@DELETE
	@Path("/permission/{userId}/{id}")
	public Response removePermissionById(@PathParam("userId") UUID userId, @PathParam("id") UUID id) {
		User us = uSvc.findById(userId);
		if (us == null) return Response.status(Status.BAD_REQUEST).entity("No user found with ID " + id).build();
		us.getPermissions().removeIf(p -> p.getId().equals(id));
		return Response.ok().build();
	}

	@DELETE
	@Path("/group/{userId}/{id}")
	public Response removeGroupById(@PathParam("userId") UUID userId, @PathParam("id") UUID id) {
		User us = uSvc.findById(userId);
		if (us == null) return Response.status(Status.BAD_REQUEST).entity("No user found with ID " + id).build();
		us.getGroups().removeIf(g -> g.getId().equals(id));
		return Response.ok().build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateUser(@PathParam("id") UUID id, User user) {
		if (user.getId() == null) return Response.status(Status.BAD_REQUEST).entity("Model ID is NULL. Use POST").build();
		User us = uSvc.findById(id);
		if (us == null) return Response.status(Status.BAD_REQUEST).entity("No user found with ID " + id).build();
		us.setName(user.getName());
		us.setMetaname(user.getMetaname());
		us.setStatus(user.getStatus());
		us.getProperties().entrySet().removeIf(en -> !user.getProperties().keySet().contains(en.getKey()));
		us.getProperties().putAll(user.getProperties());
		return Response.ok().build();
	}

	@PUT
	@Path("/property/{userid}/{key}/{value}")
	public Response updateUserProperty(@PathParam("userid") UUID id, @PathParam("key") String key, @PathParam("value") String value) {
		User us = uSvc.findById(id);
		if (us.getProperties().containsKey(key)) return Response.status(Status.BAD_REQUEST).entity("Cannot find user with given ID " + id).build();
		us.getProperties().put(key, value);
		return Response.ok("Property " + key + " updated on user " + us.getId() + " with value " + value).build();
	}
}