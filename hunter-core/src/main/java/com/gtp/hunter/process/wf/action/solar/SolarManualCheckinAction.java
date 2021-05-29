package com.gtp.hunter.process.wf.action.solar;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.BaseAction;

public class SolarManualCheckinAction extends BaseAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public SolarManualCheckinAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	public String execute(Action t) throws Exception {
		logger.info("RODANDO ACTION DE CHECKIN MANUAL DE CAMINHAO DE ROTA");
		logger.info(t.getParams());
		JsonReader jsonReader = Json.createReader(new StringReader(t.getParams()));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();
		UUID docId = UUID.fromString(object.getString("doc_id"));
		Document transport = getRegSvc().getDcSvc().findById(docId);

		getRsm().getTsm().cancelTask(getUser().getId(), transport);
		//TODO: Adicionar Recusa e Pronta Entrega
		if (!transport.getSiblings().parallelStream()
						.anyMatch(ds -> ds.getModel().getMetaname().equals("CHECKINPORTARIA"))) {
			DocumentModel dmChkin = getRegSvc().getDmSvc().findByMetaname("CHECKINPORTARIA");
			Document dCheckinPortaria = new Document(dmChkin, dmChkin.getName() + " " + transport.getCode(), "CHKIN" + transport.getCode(), "NOVO");

			dCheckinPortaria.setParent(transport);
			transport.getSiblings().add(getRegSvc().getDcSvc().persist(dCheckinPortaria));
			transport.setStatus(object.getString("statuspara"));
			getRegSvc().getDcSvc().persist(transport);
		}
		getRsm().getTsm().unlockTask(transport);
		return t.getRoute();
	}
}
