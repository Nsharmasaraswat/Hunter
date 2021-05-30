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

import com.gtp.hunter.process.model.Workflow;
import com.gtp.hunter.process.repository.WorkflowRepository;

@RequestScoped
@Path("/workflow")
public class WorkflowRest {

	@Inject
	private WorkflowRepository wRep;

	@GET
	@Path("/{id}")
	public Workflow getById(@PathParam("id") String id) {
		Workflow w = null;
		try {
			w = wRep.findById(UUID.fromString(id));
		} catch (Exception e) {
			w = new Workflow();
		}
		return w;
	}

	@GET
	@Path("/all")
	public List<Workflow> getAll() {
		return wRep.listAll();
	}

	@POST
	@Path("/")
	public Workflow save(Workflow work) {
		wRep.persist(work);
		return work;
	}

	@DELETE
	@Path("/{id}")
	public String deleteById(@PathParam("id") String id) {
		wRep.removeById(UUID.fromString(id));
		return "Ok";
	}
}
