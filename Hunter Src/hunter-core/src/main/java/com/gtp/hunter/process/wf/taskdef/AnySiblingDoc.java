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

public class AnySiblingDoc extends BaseTaskDef {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public AnySiblingDoc(TaskDef def, RegisterService regSvc, RegisterStreamManager rsm, User usr) {
		super(def, regSvc, rsm, usr);
	}

	@Override
	protected List<Document> listQuickDocuments(User usr) {
		List<Document> all = getRegSvc().getDcSvc().listAll();
		List<Document> lst = new ArrayList<>();

		lst.addAll(all.stream().filter(d -> d.getParent() == null).collect(Collectors.toList()));
		all.stream().filter(d -> d.getParent() != null).forEach(d -> {
			if (lst.contains(d.getParent())) {
				lst.remove(d.getParent());
				lst.add(d);
			}
		});
		lst.forEach(d -> logger.debug(d.getName()));
		return lst;
	}

	@Override
	protected List<ViewTaskStub> validateTask(Document d) {
		// TODO Auto-generated method stub
		return null;
	}

}
