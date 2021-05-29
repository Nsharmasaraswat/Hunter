package com.gtp.hunter.process.wf.taskdef;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.TaskDef;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.ui.json.ViewTaskStub;

public class DocByParentThingStatus extends BaseTaskDef {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public DocByParentThingStatus(TaskDef def, RegisterService regSvc, RegisterStreamManager rsm, User usr) {
		super(def, regSvc, rsm, usr);
	}

	@Override
	protected List<Document> listQuickDocuments(User usr) {
		logger.debug("Procurando Tasks");
		List<Document> lst = getRegSvc().getDcSvc().listByModelAndThingStatus(getDef().getModel(), getDef().getState());
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
