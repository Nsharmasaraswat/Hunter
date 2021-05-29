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

//TODO: Check
public class DocByModelThingNoUnitStatus extends BaseTaskDef {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public DocByModelThingNoUnitStatus(TaskDef def, RegisterService regSvc, RegisterStreamManager rsm, User usr) {
		super(def, regSvc, rsm, usr);
	}

	@Override
	protected List<Document> listQuickDocuments(User usr) {
		List<Document> lst = getRegSvc().getDcSvc().getQuickListByTypeThingNoUnitStatus(getDef().getModel().getMetaname(), getDef().getState());

		logger.info("Listagem: " + lst.size());
		lst.forEach(d -> logger.debug(d.getName()));
		return lst;
	}

	@Override
	protected List<ViewTaskStub> validateTask(Document d) {
		List<Document> ret = new ArrayList<Document>();
		if (d.getModel().getMetaname().equals(getDef().getModel().getMetaname())) {
			if (d.getStatus().equals(getDef().getState())) {
				ret.add(d);
			}
		}
		return generateTasks(ret);
	}

}
