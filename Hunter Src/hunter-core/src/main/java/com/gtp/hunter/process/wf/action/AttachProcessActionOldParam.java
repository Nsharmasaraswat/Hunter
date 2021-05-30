package com.gtp.hunter.process.wf.action;

import java.lang.invoke.MethodHandles;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;

public class AttachProcessActionOldParam extends BaseAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public AttachProcessActionOldParam(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	public String execute(Action t) {

		String ret = "";

		String[] params = getAction().getParams().split(",");
		if (params.length == 3) {

			//params[0] = docId, params[1] = processId, params[2] == originMeta
			logger.info("PARAMS: " + params[0] + " : " + params[1] + " : " + params[2]);

			Process pbase = this.getRsm().getPsm().getProcessFromDatabase(UUID.fromString(params[1]));
			Process p = new Process();
			String[] fromTo = t.getTaskstatus() == null ? null : t.getTaskstatus().split(",");

			logger.info("Criando processo " + pbase.getMetaname() + " para o documento " + params[0]);

			p.setId(UUID.randomUUID());
			p.setOrigin(this.getRsm().getOsm().getOriginByMetaname(params[2]));
			p.setClasse(pbase.getClasse());
			p.setParam(pbase.getParam().replaceAll("%%docid%%", params[0]));
			p.setEstadoDe(fromTo == null || fromTo.length < 2 ? pbase.getEstadoDe() : fromTo[0]);
			p.setEstadoPara(fromTo == null || fromTo.length < 2 ? pbase.getEstadoPara() : fromTo[1]);
			p.setActivities(pbase.getActivities());
			p.setMetaname(getAction().getMetaname());
			p.setName(pbase.getName());
			p.setStatus(pbase.getStatus());
			p.setUrlRetorno(pbase.getUrlRetorno());
			this.getRsm().getPsm().activateProcess(p);
			ret = t.getRoute().replaceAll("%%procid%%", p.getId().toString());
		} else {
			logger.warn("Parâmetros Inválidos. Qtd: " + params.length);
		}

		return ret;
	}

}
