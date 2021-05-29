package com.gtp.hunter.process.rest;

import java.util.ArrayList;
import java.util.EnumSet;
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

import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.repository.ProcessActivityRepository;
import com.gtp.hunter.process.wf.process.activity.ProcessActivityPhase;

@RequestScoped
@Path("/processfilter")
public class ProcessFilterRest {

	@Inject
	private ProcessActivityRepository pfRep;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ProcessActivity> getAll() {
		return pfRep.listAll();
	}

	@GET
	@Path("/phase")
	public List<ProcessActivityPhase> getPhaseList() {
		return new ArrayList<ProcessActivityPhase>(EnumSet.allOf(ProcessActivityPhase.class));
	}

	@GET
	@Path("/{id}")
	public ProcessActivity getById(@PathParam("id") String id) {
		ProcessActivity pa = null;
		try {
			pa = pfRep.findById(UUID.fromString(id));
		} catch (Exception e) {
			pa = new ProcessActivity();
		}
		return pa;
	}

	@POST
	@Path("/")
	public ProcessActivity save(ProcessActivity filter) {
		pfRep.persist(filter);
		return filter;
	}

	@DELETE
	@Path("/{id}")
	public String deleteById(@PathParam("id") String id) {
		pfRep.removeById(UUID.fromString(id));
		return "Ok";
	}

}
