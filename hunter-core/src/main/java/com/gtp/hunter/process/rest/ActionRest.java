package com.gtp.hunter.process.rest;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.repository.ActionRepository;

@RequestScoped
@Path("/action")
public class ActionRest {

	@Inject
	private ActionRepository aRep;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Action> getAll() {
		return aRep.listAll();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Action getById(@PathParam("id") String id) {
		Action a = null;
		try {
			a = aRep.findById(UUID.fromString(id));
		} catch (Exception e) {
			a = new Action();
		}
		return a;
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Action save(Action action) {
		aRep.persist(action);
		return action;
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn deleteById(@PathParam("id") String id) {
		aRep.removeById(UUID.fromString(id));
		return IntegrationReturn.OK;
	}

	@POST
	@Path("/runsimpleaction")
	public IntegrationReturn runSimpleAction(Action act) {
		return IntegrationReturn.OK;
	}
}
