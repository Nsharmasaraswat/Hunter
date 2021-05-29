package com.gtp.hunter.process.wf.actiondef;

import java.util.ArrayList;
import java.util.List;

import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.service.RegisterService;

public class SimpleActionDef extends BaseActionDef {

	public SimpleActionDef(Action act, Purpose pur, RegisterService regSvc) {
		super(act, pur, regSvc);
	}

	@Override
	public List<Action> getActions() {
		List<Action> ret = new ArrayList<Action>();
		Action r = new Action();

		r.setId(getAct().getId());
		r.setClasse(getAct().getClasse());
		r.setName(getAct().getName());
		r.setMetaname(getAct().getMetaname());
		r.setActionDef(getAct().getActionDef());
		r.setCreatedAt(getAct().getCreatedAt());
		r.setDefparams(getAct().getDefparams());
		r.setIcon(getAct().getIcon());
		r.setParams(getAct().getParams());
		r.setRoute(getAct().getRoute());
		r.setSrvparams(getAct().getSrvparams());
		r.setStatus(getAct().getStatus());
		r.setTaskdef(getAct().getTaskdef());
		r.setTaskstatus(getAct().getTaskstatus());
		r.setUpdatedAt(getAct().getUpdatedAt());
		ret.add(r);
		return ret;
	}

}
