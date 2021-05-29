package com.gtp.hunter.process.wf.action;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;

public class ChangeStatusAction extends BaseAction {

	private Map<String, Object> params = new HashMap<String, Object>();

	public ChangeStatusAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
		this.params = JsonUtil.jsonToMap(action.getParams());
	}

	@Override
	public String execute(Action t) throws Exception {
		String masterId = (String) params.get("master-id");
		String masterStatus = (String) params.get("master-status");
		String childModelMeta = (String) params.get("child-model-meta");
		String childStatus = (String) params.get("child-status");
		String childCodePrefix = (String) params.get("child-code-prefix");
		Document dMaster = getRegSvc().getDcSvc().findById(UUID.fromString(masterId));

		getRsm().getTsm().cancelTask(getUser().getId(), dMaster);
		getRegSvc().getDcSvc().createChild(dMaster, masterStatus, childModelMeta, childStatus, childCodePrefix, null, null, null, getUser());
		//getRegSvc().getDcSvc().createChild(dMaster, masterStatus, "MANUALSTATUSCHANGE", "NOVO", "Mudança de Status Manual ", "STCH", null, null, null, getUsr());
		getRsm().getTsm().unlockTask(dMaster);
		return t.getRoute();
	}

}
