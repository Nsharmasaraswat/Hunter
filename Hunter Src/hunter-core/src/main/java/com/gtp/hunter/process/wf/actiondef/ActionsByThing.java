package com.gtp.hunter.process.wf.actiondef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;

public class ActionsByThing extends BaseActionDef {

	public ActionsByThing(Action act, Purpose pur, RegisterService regSvc) {
		super(act, pur, regSvc);
	}

	@Override
	public List<Action> getActions() {
		List<Action> ret = new ArrayList<Action>();

		for (Thing t : this.getPur().getThings()) {
			Action tact = new Action();
			Map<String, Object> defParams = JsonUtil.jsonToMap(getAct().getDefparams());
			UnitType ut = UnitType.valueOf((String) defParams.get("unit_type"));
			Unit u = t.getUnits().stream()
							.map(uid -> getRegSvc().getUnSvc().findById(uid))
							.filter(un -> un.getType().equals(ut))
							.findAny()
							.get();

			tact.setId(UUID.randomUUID());
			tact.setClasse(getAct().getClasse());
			tact.setName(getAct().getName().replaceAll("%%thingname%%", t.getName()).replaceAll("%%thingid%%", t.getId().toString()).replaceAll("%%tagid%%", u.getTagId()));
			tact.setMetaname(getAct().getMetaname());
			tact.setRoute(getAct().getRoute().replaceAll("%%thingname%%", t.getName()).replaceAll("%%thingid%%", t.getId().toString()).replaceAll("%%tagid%%", u.getTagId()));
			tact.setParams(getAct().getParams().replaceAll("%%thingname%%", t.getName()).replaceAll("%%thingid%%", t.getId().toString()).replaceAll("%%tagid%%", u.getTagId()));
			ret.add(tact);
		}
		return ret;
	}

}
