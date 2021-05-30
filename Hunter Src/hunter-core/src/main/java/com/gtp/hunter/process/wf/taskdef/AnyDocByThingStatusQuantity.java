package com.gtp.hunter.process.wf.taskdef;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.TaskDef;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.ui.json.ViewTaskStub;

public class AnyDocByThingStatusQuantity extends BaseTaskDef {

	@Inject
	private static transient Logger logger;

	public AnyDocByThingStatusQuantity(TaskDef def, RegisterService regSvc, RegisterStreamManager rsm, User usr) {
		super(def, regSvc, rsm, usr);
	}

	@Override
	protected List<Document> listQuickDocuments(User usr) {
		logger.debug("Procurando Tasks");
		List<Document> lst = getRegSvc().getDcSvc().listByThingStatus(getDef().getState());
		List<Document> lstFiltered = new ArrayList<Document>();

		lst.forEach(d -> {
			if (!lstFiltered.contains(d))
				lstFiltered.add(d);
		});
		logger.debug("Documentos: " + lstFiltered.size());
		return lst;
	}

	@Override
	protected List<ViewTaskStub> validateTask(Document d) {
		// TODO Auto-generated method stub
		return null;
	}

}
