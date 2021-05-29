package com.gtp.hunter.process.wf.actiondef;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.service.RegisterService;

public class ActionsByAddress extends BaseActionDef {

	public ActionsByAddress(Action act, Purpose pur, RegisterService regSvc) {
		super(act, pur, regSvc);
	}

	@Override
	public List<Action> getActions() {
		List<Action> ret = new ArrayList<Action>();
		for(Address addr : this.getPur().getAddresses()) {
			Action tact = new Action();
			tact.setId(UUID.randomUUID());
			tact.setClasse(getAct().getClasse());
			tact.setName(getAct().getName().replaceAll("%%addrname%%", addr.getName()).replaceAll("%%addrid%%", addr.getId().toString()).replaceAll("%%addrmeta%%", addr.getMetaname()));
			tact.setMetaname(getAct().getMetaname());
			tact.setRoute(getAct().getRoute().replaceAll("%%addrname%%", addr.getName()).replaceAll("%%addrid%%", addr.getId().toString()).replaceAll("%%addrmeta%%", addr.getMetaname()));
			tact.setParams(getAct().getParams().replaceAll("%%addrname%%", addr.getName()).replaceAll("%%addrid%%", addr.getId().toString()).replaceAll("%%addrmeta%%", addr.getMetaname()));
			ret.add(tact);
		}
		
		return ret;
	}

}
