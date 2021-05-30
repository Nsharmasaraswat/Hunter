package com.gtp.hunter.process.wf.action.solar;

import java.util.UUID;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.BaseAction;

public class CompleteMovAction extends BaseAction {

	public CompleteMovAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	public String execute(Action act) throws Exception {
		UUID movId = UUID.fromString(act.getParams());
		Document d = getRegSvc().getDcSvc().findById(movId);

		try {
			DocumentModel apoComplete = getRegSvc().getDmSvc().findByMetaname("APOCOMPLETEMOV");
			String prntCode = d.getCode().substring(2);
			Document acomp = new Document(apoComplete, apoComplete.getName() + " " + prntCode, "CMP" + prntCode, "MANUAL");

			acomp.setParent(d);
			acomp.setUser(getUser());
			getRsm().getTsm().cancelTask(getUser().getId(), d);
			d.setStatus("SUCESSO");
			getRegSvc().getDcSvc().persist(acomp);
			d.getSiblings().add(acomp);
			getRegSvc().getWmsSvc().completeOrdMov(d);
		} catch (Exception e) {
			e.printStackTrace();
		}
		getRsm().getTsm().unlockTask(d);
		return act.getRoute();
	}

}
