package com.gtp.hunter.process.wf.action.solar;

import java.util.UUID;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.BaseAction;

public class CancelMovAction extends BaseAction {

	public CancelMovAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	public String execute(Action act) throws Exception {
		UUID movId = UUID.fromString(act.getParams());
		Document d = getRegSvc().getDcSvc().findById(movId);

		getRsm().getTsm().cancelTask(getUser().getId(), d);
		getRegSvc().getWmsSvc().cancelOrdMov(d, getUser());
		getRsm().getTsm().unlockTask(d);
		return act.getRoute();
	}

}
