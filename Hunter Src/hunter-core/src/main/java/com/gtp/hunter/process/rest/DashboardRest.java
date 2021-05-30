package com.gtp.hunter.process.rest;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Dashboard;
import com.gtp.hunter.process.model.DashboardWidget;
import com.gtp.hunter.process.model.Widget;
import com.gtp.hunter.process.service.RegisterService;

@Transactional
@RequestScoped
@Path("/dashboard")
public class DashboardRest {

	@Inject
	private RegisterService regSvc;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listDashboards() {
		return Response.ok(regSvc.getDshSvc().listAll()).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDashboard(@PathParam("id") UUID id) {
		return Response.ok(regSvc.getDshSvc().findById(id)).build();
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserDashboard(@Context HttpHeaders rs) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User us = regSvc.getAuthSvc().getUser(token);

		return Response.ok(regSvc.getDshSvc().findByUser(us)).build();
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteById(@PathParam("id") UUID id) {
		regSvc.getDswSvc().removeByDashboardId(id);
		regSvc.getDshSvc().removeById(id);
		return Response.ok().build();
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insert(Dashboard dashboard) {
		Set<UUID> widgets = dashboard.getWidgets()
						.parallelStream()
						.map(wdg -> {
							return wdg.getWidget().getId();
						})
						.collect(Collectors.toSet());
		List<Widget> wdgList = regSvc.getWdgSvc().listById(widgets);
		Set<DashboardWidget> dshWdgs = wdgList.parallelStream()
						.map(wdg -> new DashboardWidget(dashboard, wdg, "NOVO"))
						.collect(Collectors.toSet());

		dashboard.setWidgets(dshWdgs);
		return Response.ok(regSvc.getDshSvc().persist(dashboard)).build();
	}

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") UUID id, Dashboard dashboard) {
		if (id == null || dashboard.getId() == null) return Response.status(Status.BAD_REQUEST).entity("id cannot be null").build();
		if (!dashboard.getId().equals(id)) return Response.status(Status.BAD_REQUEST).entity("id must match model to update").build();
		Dashboard dbDash = regSvc.getDshSvc().findById(id);
		if (dbDash == null) return Response.status(Status.BAD_REQUEST).entity("Dashboard with id " + id + " not found. Use POST").build();
		Set<UUID> wdgId = dashboard.getWidgets()
						.parallelStream()
						.map(dw -> dw.getWidget().getId())
						.collect(Collectors.toSet());

		dbDash.getWidgets().stream()
						.filter(dw -> !wdgId.contains(dw.getWidget().getId()))
						.forEach(dw -> regSvc.getDswSvc().removeById(dw.getId()));
		dashboard.getWidgets().parallelStream()
						.filter(dw -> dw.getDashboard() == null)
						.forEach(dw -> dw.setDashboard(dashboard));
		return Response.ok(regSvc.getDshSvc().persist(dashboard)).build();
	}
}
