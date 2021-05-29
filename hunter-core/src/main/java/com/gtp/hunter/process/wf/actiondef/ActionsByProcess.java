package com.gtp.hunter.process.wf.actiondef;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.service.RegisterService;

public class ActionsByProcess extends BaseActionDef {
	@Inject
	private static transient Logger logger;

	public ActionsByProcess(Action act, Purpose pur, RegisterService regSvc) {
		super(act, pur, regSvc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Action> getActions() {
		List<Action> ret = new ArrayList<Action>();

		for (Process proc : this.getPur().getProcesses()) {
			Action tact = getAct();

			tact.setName(tact.getName().replaceAll("%%procname%%", proc.getName()).replaceAll("%%procid%%", proc.getId().toString()).replaceAll("%%procmeta%%", proc.getMetaname()).replaceAll("%%orimeta%%", proc.getOrigin().getMetaname()));
			tact.setStatus(tact.getStatus());
			if (tact.getDefparams() != null)
				tact.setDefparams(tact.getDefparams().replaceAll("%%procname%%", proc.getName()).replaceAll("%%procid%%", proc.getId().toString()).replaceAll("%%procmeta%%", proc.getMetaname()).replaceAll("%%orimeta%%", proc.getOrigin().getMetaname()));
			if (tact.getSrvparams() != null)
				tact.setSrvparams(tact.getSrvparams().replaceAll("%%procname%%", proc.getName()).replaceAll("%%procid%%", proc.getId().toString()).replaceAll("%%procmeta%%", proc.getMetaname()).replaceAll("%%orimeta%%", proc.getOrigin().getMetaname()));
			tact.setRoute(tact.getRoute().replaceAll("%%procname%%", proc.getName()).replaceAll("%%procid%%", proc.getId().toString()).replaceAll("%%procmeta%%", proc.getMetaname()).replaceAll("%%orimeta%%", proc.getOrigin().getMetaname()));
			tact.setParams(tact.getParams().replaceAll("%%procname%%", proc.getName()).replaceAll("%%procid%%", proc.getId().toString()).replaceAll("%%procmeta%%", proc.getMetaname()).replaceAll("%%orimeta%%", proc.getOrigin().getMetaname()));
			ret.add(tact);
		}

		return ret;
	}

}
