package com.gtp.hunter.process.rest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.gtp.hunter.process.model.Origin;
import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.model.TaskDef;
import com.gtp.hunter.process.repository.OriginRepository;
import com.gtp.hunter.process.repository.ProcessRepository;
import com.gtp.hunter.process.repository.PurposeRepository;
import com.gtp.hunter.process.repository.TaskDefRepository;

@RequestScoped
@Path("/purpose")
public class PurposeRest {

	@Inject
	private PurposeRepository	pRep;

	@Inject
	private OriginRepository	oRep;

	@Inject
	private ProcessRepository	procRep;

	@Inject
	private TaskDefRepository	tdRep;

	@Inject
	private Logger				logger;

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Purpose> getAll() {
		return pRep.listAll();
	}

	@GET
	@Path("/{id}")
	public Purpose getById(@PathParam("id") String id) {
		Purpose o = null;
		try {
			o = pRep.findById(UUID.fromString(id));
		} catch (Exception e) {
			logger.error("Creating new Purpose " + e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			o = new Purpose();
		}
		return o;
	}

	@DELETE
	@Path("/{id}")
	public String deleteById(@PathParam("id") String id) {
		try {
			pRep.removeById(UUID.fromString(id));
			return "Ok";
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			return e.getLocalizedMessage();
		}
	}

	@POST
	@Path("/")
	public Purpose persist(JsonObject json) {
		Purpose purp = null;
		final String id = json.containsKey("id") ? json.getString("id") : null;

		if (id == null)
			return null;
		try {
			purp = pRep.findById(UUID.fromString(id));
		} catch (Exception e) {
			purp = new Purpose();
			final String name = json.containsKey("name") ? json.getString("name") : null;
			final String metaname = json.containsKey("metaname") ? json.getString("metaname") : null;

			purp.setName(name);
			purp.setMetaname(metaname);
		}

		if (purp == null)
			return null;

		final JsonArray originsJson = json.containsKey("origins") ? json.getJsonArray("origins") : Json.createArrayBuilder().build();
		final JsonArray tasksJson = json.containsKey("tasks") ? json.getJsonArray("tasks") : Json.createArrayBuilder().build();
		final JsonArray processesJson = json.containsKey("processes") ? json.getJsonArray("processes") : Json.createArrayBuilder().build();
		final List<Origin> origins = new ArrayList<>();
		final List<TaskDef> tasks = new ArrayList<>();
		final List<Process> processes = new ArrayList<>();

		// convert JsonArray in List of Origin
		for (Object obj : originsJson) {
			JsonObject jo = (JsonObject) obj;
			final String oId = jo.containsKey("id") ? jo.getString("id") : null;
			if (oId == null)
				break;
			final Origin o = oRep.findById(UUID.fromString(oId));
			if (o == null)
				break;

			if (origins.contains(o))
				continue;
			origins.add(o);
		}

		// convert JsonArray in List of TaskDef
		for (Object obj : tasksJson) {
			JsonObject jo = (JsonObject) obj;
			final String tdId = jo.containsKey("id") ? jo.getString("id") : null;
			if (tdId == null)
				break;
			final TaskDef td = tdRep.findById(UUID.fromString(tdId));
			if (td == null)
				break;

			if (tasks.contains(td))
				continue;
			tasks.add(td);
		}

		// convert JsonArray in List of Process
		for (Object obj : processesJson) {
			JsonObject jo = (JsonObject) obj;
			final String pId = jo.containsKey("id") ? jo.getString("id") : null;
			if (pId == null)
				break;
			final Process p = procRep.findById(UUID.fromString(pId));
			if (p == null)
				break;

			if (processes.contains(p))
				continue;
			processes.add(p);
		}

		purp.setOrigins(new HashSet<>(origins));
		purp.setProcesses(new HashSet<>(processes));
		purp.setTasks(new HashSet<>(tasks));

		pRep.persist(purp);

		return purp;
	}

}
