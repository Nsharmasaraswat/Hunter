package com.gtp.hunter.process.wf.taskdef;

import java.lang.invoke.MethodHandles;
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

public class DocByItemQuantityAndStatus extends BaseTaskDef {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public DocByItemQuantityAndStatus(TaskDef def, RegisterService regSvc, RegisterStreamManager rsm, User usr) {
		super(def, regSvc, rsm, usr);
	}

	@Override
	protected List<Document> listQuickDocuments(User usr) {
		return getRegSvc().getDcSvc().listById(getRegSvc()
						.getTskSvc()
						.listStubsByModelAndStatusAndItemThingDifference(getDef().getModel(), getDef().getState())
						.stream()
						.map(vts -> vts.getId())
						.collect(Collectors.toList()));
	}

	@Override
	protected List<ViewTaskStub> validateTask(Document d) {
		// TODO Auto-generated method stub
		return null;
	}

}
