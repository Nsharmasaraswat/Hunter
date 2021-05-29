package com.gtp.hunter.process.wf.actiondef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.service.RegisterService;

public class ActionFixedProcess extends BaseActionDef {

	public ActionFixedProcess(Action act, Purpose pur, RegisterService regSvc) {
		super(act, pur, regSvc);
	}

	@Override
	public List<Action> getActions() {
		List<Action> ret = new ArrayList<Action>();
		Map<String, Object> params = JsonUtil.jsonToMap(this.getAct().getDefparams());
		Optional<Process> optProc = getPur().getProcesses().stream().filter(o -> o.getId().equals(UUID.fromString((String) params.get("process-id")))).findFirst();

		if (optProc.isPresent()) {
			Process proc = optProc.get();
			Action tact = new Action();

			tact.setId(getAct().getId());
			tact.setClasse(getAct().getClasse());
			tact.setName(getAct().getName().replaceAll("%%procname%%", proc.getName()).replaceAll("%%procid%%", proc.getId().toString()).replaceAll("%%procmeta%%", proc.getMetaname()));
			tact.setMetaname(getAct().getMetaname());
			tact.setRoute(getAct().getRoute().replaceAll("%%procname%%", proc.getName()).replaceAll("%%procid%%", proc.getId().toString()).replaceAll("%%procmeta%%", proc.getMetaname()));
			tact.setParams(getAct().getParams().replaceAll("%%procname%%", proc.getMetaname()).replaceAll("%%procid%%", proc.getId().toString()).replaceAll("%%procmeta%%", proc.getMetaname()));
			tact.setActionDef(getAct().getActionDef());
			tact.setCreatedAt(getAct().getCreatedAt());
			tact.setUpdatedAt(getAct().getUpdatedAt());
			if (getAct().getDefparams() != null)
				tact.setDefparams(getAct().getDefparams().replaceAll("%%procname%%", proc.getMetaname()).replaceAll("%%procid%%", proc.getId().toString()).replaceAll("%%procmeta%%", proc.getMetaname()));
			tact.setRoute(getAct().getRoute());
			if (getAct().getSrvparams() != null)
				tact.setSrvparams(getAct().getSrvparams().replaceAll("%%procname%%", proc.getMetaname()).replaceAll("%%procid%%", proc.getId().toString()).replaceAll("%%procmeta%%", proc.getMetaname()));
			tact.setStatus(getAct().getStatus());
			tact.setTaskdef(getAct().getTaskdef());
			tact.setTaskstatus(getAct().getTaskstatus());
			ret.add(tact);
		}
		return ret;
	}

}
