package com.gtp.hunter.process.wf.actiondef;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Origin;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.service.RegisterService;

public class ActionsByOrigin extends BaseActionDef {

	public ActionsByOrigin(Action act, Purpose pur, RegisterService regSvc) {
		super(act, pur, regSvc);
	}

	@Override
	public List<Action> getActions() {
		List<Action> ret = new ArrayList<Action>();
		for(Origin ori : this.getPur().getOrigins()) {
			Action tact = new Action();
			tact.setId(UUID.randomUUID());
			tact.setClasse(getAct().getClasse());
			tact.setName(getAct().getName().replaceAll("%%oriname%%", ori.getName()).replaceAll("%%oriid%%", ori.getId().toString()).replaceAll("%%orimeta%%", ori.getMetaname()));
			tact.setMetaname(getAct().getMetaname());
			tact.setRoute(getAct().getRoute().replaceAll("%%oriname%%", ori.getName()).replaceAll("%%oriid%%", ori.getId().toString()).replaceAll("%%orimeta%%", ori.getMetaname()));
			tact.setParams(getAct().getParams().replaceAll("%%oriname%%", ori.getName()).replaceAll("%%oriid%%", ori.getId().toString()).replaceAll("%%orimeta%%", ori.getMetaname()));
			ret.add(tact);
		}
		
		return ret;
	}

}
