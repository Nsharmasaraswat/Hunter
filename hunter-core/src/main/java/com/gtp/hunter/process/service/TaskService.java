package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.Task;
import com.gtp.hunter.process.model.TaskDef;
import com.gtp.hunter.process.repository.DocumentModelRepository;
import com.gtp.hunter.process.repository.DynTaskDefRepository;
import com.gtp.hunter.process.repository.TaskDefRepository;
import com.gtp.hunter.process.repository.TaskRepository;
import com.gtp.hunter.ui.json.ViewTaskStub;

@Stateless
public class TaskService {

	@Inject
	private TaskDefRepository		tdRep;

	@Inject
	private TaskRepository			tRep;

	@Inject
	private DocumentModelRepository	dmRep;

	@Inject
	private DocumentService			dcSvc;

	@Inject
	private DynTaskDefRepository	tskRep;

	@Inject
	private RegisterService			regSvc;

	public List<TaskDef> getTaskDefsByUser(User usr) {
		List<UUID> perms = regSvc.getAuthSvc().getUGEntitiesByUser(usr);
		List<TaskDef> tasks = tdRep.getTaskDefsByPermissions(perms);
		return tasks;
	}

	public List<Task> listNewTasksByTaskDef(TaskDef defs) {
		return tRep.listNewTasksByTaskDef(defs);
	}

	public Task generateTaskFromDocument(Document d, TaskDef td) {
		Task tsk = new Task(td, d);
		tsk.setStatus("NOVO");
		tRep.persist(tsk);
		return tsk;
	}

	public Task getFullTask(UUID id) {
		Task tsk = tRep.getFullTask(id);

		return tsk;
	}

	public void persist(Task t) {
		tRep.persist(t);
	}

	public List<ViewTaskStub> listStubsByModelAndStatusAndItemThingDifference(DocumentModel model, String status) {
		return tskRep.listStubsByModelAndStatusAndItemThingDifference(model, status);
	}

	public List<ViewTaskStub> listStubsByModelAndItemThingDifference(DocumentModel model) {
		return tskRep.listStubsByModelAndItemThingDifference(model);
	}

	public List<ViewTaskStub> listStubsByModelAndItemThingDifferenceAndUnit(DocumentModel model, String unit) {
		return tskRep.listStubsByModelAndItemThingDifferenceAndUnit(model, unit);
	}

	@Asynchronous
	public void startTask(Document doc, User usr) {
		if (doc != null) {
			String stCode = "INI" + doc.getCode();
			DocumentModel dmStart = dmRep.findByMetaname("APOINICIO");
			Document st = dcSvc.findByModelAndCode(dmStart, stCode);
			Document inicio = st == null ? new Document(dmStart, dmStart.getName() + " " + doc.getCode(), stCode, "NOVO") : st;

			inicio.setUser(usr);
			inicio.setParent(doc);
			dcSvc.persist(inicio);
		}
	}

	@Asynchronous
	public void completeTask(Document doc, User usr) {
		if (doc != null) {
			String endCode = "FIM" + doc.getCode();
			DocumentModel dmEnd = dmRep.findByMetaname("APOFINAL");
			Document fim = dcSvc.findByModelAndCode(dmEnd, endCode);

			if (fim == null)
				fim = new Document(dmEnd, dmEnd.getName() + " " + doc.getCode(), endCode, "NOVO");
			fim.setUser(usr);
			fim.setParent(doc);
			dcSvc.persist(fim);
		}
	}
}
