package com.gtp.hunter.process.wf.action.solar;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.BaseAction;

public class SolarForceTriggerAction extends BaseAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public SolarForceTriggerAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	//TROCA STATUS DO DOCUMENTO E FILHOS COM STATUS IGUAIS AO PAI
	@Override
	public String execute(Action t) {
		JsonReader jsonReader = Json.createReader(new StringReader(t.getParams()));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();
		String statusPara = object.getString("statuspara");
		UUID docId = UUID.fromString(object.getString("doc_id"));
		Document d = getRegSvc().getDcSvc().findById(docId);
		List<Document> sibs = d.getSiblings().stream().filter(sib -> sib.getStatus().equals(d.getStatus())).collect(Collectors.toList());

		getRsm().getTsm().cancelTask(getUser().getId(), d);
		sibs.forEach(s -> {
			logger.info(s.getClass() + " De: " + s.getStatus() + " Para: " + statusPara);
			s.setStatus(statusPara);
		});
		d.setStatus(statusPara);
		getRegSvc().getDcSvc().multiPersist(sibs);
		getRegSvc().getDcSvc().persist(d);
		getRsm().getTsm().unlockTask(d);
		return t.getRoute();
	}
}
