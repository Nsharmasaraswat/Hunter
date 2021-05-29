package com.gtp.hunter.process.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.model.TaskDef;
import com.gtp.hunter.process.repository.PurposeRepository;
import com.gtp.hunter.process.repository.TaskDefRepository;

@RequestScoped
@Path("/taskdef")
public class TaskDefRest {

	@Inject
	private TaskDefRepository	tdRep;
	@Inject
	private PurposeRepository	pRep;

	@GET
	@Path("/all")
	public List<TaskDef> getAll() {
		return tdRep.listAll();
	}

	@GET
	@Path("/{id}")
	public TaskDef getById(@PathParam("id") String id) {
		TaskDef td = null;
		try {
			td = tdRep.findById(UUID.fromString(id));
		} catch (Exception e) {
			td = new TaskDef();
		}
		return td;
	}

	@GET
	@Path("/bypurpose/{id}")
	public List<TaskDef> listByPurposeId(@PathParam("id") String id) {
		Purpose p = null;
		try {
			p = pRep.findById(UUID.fromString(id));
			if (p == null)
				return null;

			return tdRep.listByPurpose(p);
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	@POST
	@Path("/")
	public TaskDef save(TaskDef taskDef) {
		tdRep.persist(taskDef);
		// tsm.inicializa(taskDef);
		return taskDef;
	}

	@DELETE
	@Path("/{id}")
	public String deleteById(@PathParam("id") String id) {
		tdRep.removeById(UUID.fromString(id));
		return "Ok";
	}
}
