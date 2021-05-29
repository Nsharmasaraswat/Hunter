package com.gtp.hunter.process.wf.action;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;

public class AttachProcessAction extends BaseAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public AttachProcessAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	public String execute(Action t) {
		Map<String, Object> params = JsonUtil.jsonToMap(getAction().getParams());
		String ret = "";
		String thingId = params.containsKey("thing_id") ? (String) params.get("thing_id") : "%%thingid%%";
		String docId = params.containsKey("document_id") ? (String) params.get("document_id") : "%%docid%%";
		Process pbase = this.getRsm().getPsm().getProcessFromDatabase(UUID.fromString((String) params.get("process_id")));
		Process p = new Process();
		String[] fromTo = t.getTaskstatus() == null ? null : t.getTaskstatus().split(",");

		logger.info("Criando processo " + pbase.getMetaname() + " para o documento " + params.get("document_id"));

		p.setId(UUID.randomUUID());
		p.setOrigin(this.getRsm().getOsm().getOriginById(UUID.fromString((String) params.get("origin_id"))));
		p.setClasse(pbase.getClasse());
		p.setParam(pbase.getParam().replaceAll("%%docid%%", docId).replaceAll("%%thingid%%", thingId));
		p.setEstadoDe(fromTo == null || fromTo.length < 2 ? pbase.getEstadoDe() : fromTo[0]);
		p.setEstadoPara(fromTo == null || fromTo.length < 2 ? pbase.getEstadoPara() : fromTo[1]);
		p.setActivities(pbase.getActivities());
		p.setMetaname(getAction().getMetaname());
		p.setName(pbase.getName());
		p.setStatus(pbase.getStatus());
		p.setUrlRetorno(pbase.getUrlRetorno());
		this.getRsm().getPsm().activateProcess(p);
		ret = t.getRoute().replaceAll("%%procid%%", p.getId().toString());
		return ret;
	}

}
