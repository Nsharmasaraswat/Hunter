package com.gtp.hunter.process.wf.actiondef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Origin;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.service.RegisterService;

public class ActionFixedOrigin extends BaseActionDef {

	public ActionFixedOrigin(Action act, Purpose pur, RegisterService regSvc) {
		super(act, pur, regSvc);
	}

	@Override
	public List<Action> getActions() {
		List<Action> ret = new ArrayList<Action>();
		Map<String, Object> params = JsonUtil.jsonToMap(this.getAct().getParams());
		Origin ori = getPur().getOrigins().stream().filter(o -> o.getMetaname().equals((String) params.get("fixed-origin"))).findFirst().get();
		Action tact = new Action();

		tact.setId(UUID.randomUUID());
		tact.setClasse(getAct().getClasse());
		tact.setName(getAct().getName().replaceAll("%%oriname%%", ori.getName()).replaceAll("%%oriid%%", ori.getId().toString()).replaceAll("%%orimeta%%", ori.getMetaname()));
		tact.setMetaname(getAct().getMetaname());
		tact.setRoute(getAct().getRoute().replaceAll("%%oriname%%", ori.getName()).replaceAll("%%oriid%%", ori.getId().toString()).replaceAll("%%orimeta%%", ori.getMetaname()));
		tact.setParams(getAct().getParams().replaceAll("%%oriname%%", ori.getMetaname()).replaceAll("%%oriid%%", ori.getId().toString()).replaceAll("%%orimeta%%", ori.getMetaname()));
		tact.setActionDef(getAct().getActionDef());
		tact.setCreatedAt(getAct().getCreatedAt());
		tact.setUpdatedAt(getAct().getUpdatedAt());
		tact.setDefparams(getAct().getDefparams());
		tact.setRoute(getAct().getRoute());
		tact.setSrvparams(getAct().getSrvparams());
		tact.setStatus(getAct().getStatus());
		tact.setTaskdef(getAct().getTaskdef());
		tact.setTaskstatus(getAct().getTaskstatus());
		ret.add(tact);
		return ret;
	}

}
