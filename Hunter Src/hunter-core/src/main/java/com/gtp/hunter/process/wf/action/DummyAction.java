package com.gtp.hunter.process.wf.action;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;

public class DummyAction extends BaseAction {

	public DummyAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	public String execute(Action t) {
		return t.getRoute();
	}

}
