package com.gtp.hunter.process.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;

import com.google.gson.GsonBuilder;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.service.AuthService;
import com.gtp.hunter.core.service.UserService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Task;
import com.gtp.hunter.process.service.DocumentService;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.service.TaskService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.stream.TaskStreamManager;
import com.gtp.hunter.process.websocket.session.ActionSession;
import com.gtp.hunter.process.wf.action.InventoryDocumentAction;
import com.gtp.hunter.ui.json.TaskInProgressStub;

@RequestScoped
@Path("/task")
public class TaskRest {

	@EJB(lookup = "java:global/hunter-core/AuthService!com.gtp.hunter.core.service.AuthService")
	private AuthService			authSvc;

	@EJB(lookup = "java:global/hunter-core/UserService!com.gtp.hunter.core.service.UserService")
	private UserService			usSvc;

	@Inject
	private DocumentService		dSvc;

	@Inject
	private TaskService			tSvc;

	@Inject
	private TaskStreamManager	tsm;

	@Inject
	private transient Logger	logger;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTasksByUser(@Context HttpHeaders rs) {
		return Response.status(Status.GONE).build();
	}

	@POST
	@Path("/action")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public String runAction(@Context HttpHeaders rs, Action act) throws Exception {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User u = authSvc.getUser(token);

		String ret = tsm.runAction(u, act);
		return ret;
	}

	@POST
	@Path("/actionStateChange/{from}/{to}")
	@Produces(MediaType.TEXT_PLAIN)
	public String runAction(@Context HttpHeaders rs, Action act, @PathParam("from") String from, @PathParam("to") String to) throws Exception {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User u = authSvc.getUser(token);

		if (act.getTaskdef() != null)
			act.getTaskdef().setState(to);
		act.setTaskstatus(from + "," + to);// FIXME: ARMMMMMEEEENNNNGOOOOOOOO
		String ret = tsm.runAction(u, act);
		return ret;
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Task getTask(@PathParam("id") String id) {
		return tSvc.getFullTask(UUID.fromString(id));
	}

	@GET
	@Path("/inprogress")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TaskInProgressStub> listAll(@Context HttpHeaders rs) {
		Map<UUID, UUID> lockedTaskMap = tsm.listLockedTasks();
		List<TaskInProgressStub> tasksInProgress = new ArrayList<>();

		logger.info(lockedTaskMap.size() + " Locked Tasks");
		for (Entry<UUID, UUID> e : lockedTaskMap.entrySet()) {
			User us = usSvc.findById(e.getKey());
			Document d = dSvc.findById(e.getValue());
			TaskInProgressStub t = new TaskInProgressStub(d, us);

			if (t != null && t.getUser() != null && d != null && d.getId() != null)
				logger.info(t.getUser().getLogin() + " - " + d.getCode() + " - " + d.getId().toString());
			tasksInProgress.add(t);
		}
		return tasksInProgress;
	}

	@GET
	@Path("/interact")
	@PermitAll
	public void interact() {

	}

	@POST
	@Path("/releaseAll")
	@Consumes(MediaType.APPLICATION_JSON)
	public void unlockAll(@Context HttpHeaders rs) throws Exception {
		tsm.unlockTasks();
	}

	@POST
	@Path("/release/{docid}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void unlockAll(@Context HttpHeaders rs, @PathParam("docid") UUID docId) throws Exception {
		tsm.unlockTask(regSvc.getDcSvc().findById(docId));
	}

	@Inject
	private RegisterService			regSvc;

	@Inject
	private RegisterStreamManager	rsm;

	@POST
	@Path("/testExecute/{actionId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public IntegrationReturn execute(@Context HttpHeaders rs, @PathParam("actionId") UUID actionId, Document doc) {
		IntegrationReturn ret = IntegrationReturn.OK;
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];

		if (authSvc.valid(token)) {
			User u = authSvc.getUser(token);
			Action action = regSvc.getActSvc().findById(actionId);

			action.setParams(doc.getId().toString());

			InventoryDocumentAction docAction = new InventoryDocumentAction(u, action, rsm, regSvc);

			docAction.setAs(new ActionSession(null));
			docAction.onMessage(new GsonBuilder().create().toJson(doc));
		} else {
			ret = new IntegrationReturn(false, "User Not Authenticated");
		}
		return ret;
	}
}
