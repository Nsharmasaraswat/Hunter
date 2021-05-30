package com.gtp.hunter.process.wf.actiondef;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Feature;
import com.gtp.hunter.process.model.Origin;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.service.RegisterService;

public class ActionsByDevice extends BaseActionDef {

	@Inject
	private static transient Logger logger;

	public ActionsByDevice(Action act, Purpose pur, RegisterService regSvc) {
		super(act, pur, regSvc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Action> getActions() {
		List<Action> ret = new ArrayList<Action>();
		for (Origin ori : this.getPur().getOrigins()) {
			for (Feature fea : ori.getFeatures()) {
				UUID srcId = this.getRegSvc().getSrcSvc().findByMetaname(fea.getSource()).getId();
				Device dev = this.getRegSvc().getSrcSvc().findDevByMetaname(srcId, fea.getDevice());
				if (dev != null) {
					Action tact = new Action();
					tact.setId(UUID.randomUUID());
					tact.setClasse(getAct().getClasse());
					tact.setName(getAct().getName().replaceAll("%%devname%%", dev.getName()).replaceAll("%%devid%%", dev.getId().toString()));
					tact.setMetaname(getAct().getMetaname());
					tact.setRoute(getAct().getRoute().replaceAll("%%devname%%", dev.getName()).replaceAll("%%devid%%", dev.getId().toString()));
					tact.setParams(getAct().getParams().replaceAll("%%devname%%", dev.getName()).replaceAll("%%devid%%", dev.getId().toString()));
					ret.add(tact);
				} else {
					logger.warn("FAIOOOOOO");
				}
			}
		}
		return ret;
	}

}
