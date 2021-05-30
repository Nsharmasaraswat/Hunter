package com.gtp.hunter.process.wf.taskdef;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.TaskDef;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.ui.json.ViewTaskStub;

public class DocByModelAndStatus extends BaseTaskDef {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public DocByModelAndStatus(TaskDef def, RegisterService regSvc, RegisterStreamManager rsm, User usr) {
		super(def, regSvc, rsm, usr);
	}

	@Override
	protected List<Document> listQuickDocuments(User usr) {
		List<Document> lst = getRegSvc().getDcSvc().quickListByTypeStatus(getDef().getModel().getMetaname(), getDef().getState());
		logger.info(getDef().getMetaname() + ": " + lst.size() + " -> " + lst.stream().map(d -> d.getCode()).collect(Collectors.joining(", ")));
		return lst;
	}

	@Override
	protected List<ViewTaskStub> validateTask(Document d) {
		List<Document> ret = new ArrayList<>();

		if (d != null && d.getModel().getMetaname().equals(getDef().getModel().getMetaname())) {
			if (d.getStatus().equals(getDef().getState())) {
				//				logger.info("TRACE-> " + getDef().getMetaname() + " Adding Document " + d.getName());
				ret.add(d);
			}
			//			else
			//				logger.info("TRACE-> Status: " + getDef().getMetaname() + " - " + getDef().getState() + " <-> " + d.getStatus());
		}
		//		else
		//			logger.info("TRACE-> Model: " + getDef().getMetaname() + " - " + getDef().getModel().getMetaname() + " <-> " + d.getModel().getMetaname());
		return generateTasks(ret);
	}

}
