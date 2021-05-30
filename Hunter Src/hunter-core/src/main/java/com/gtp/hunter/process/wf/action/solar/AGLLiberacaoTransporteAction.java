package com.gtp.hunter.process.wf.action.solar;

import java.io.StringReader;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.BaseAction;

public class AGLLiberacaoTransporteAction extends BaseAction {

	public AGLLiberacaoTransporteAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String execute(Action t) throws Exception {
		JsonReader jsonReader = Json.createReader(new StringReader(t.getParams()));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();
		UUID docId = UUID.fromString(object.getString("doc_id"));
		Document d = getRegSvc().getDcSvc().findById(docId);

		if (!d.getStatus().equals("LIBERADO") && !d.getStatus().equals(object.getString("statuspara"))) {
			getRsm().getTsm().cancelTask(getUser().getId(), d);
			d.setStatus(object.getString("statuspara"));
			getRegSvc().getDcSvc().persist(d);
			if (!d.getSiblings().parallelStream().anyMatch(ds -> ds.getModel().getMetaname().equals("APOLIBERACAO"))) {
				DocumentModel dm = getRegSvc().getDmSvc().findByMetaname("APOLIBERACAO");
				Document apo = new Document(dm, dm.getName() + " " + d.getCode(), "LIB" + d.getCode(), "NOVO");

				apo.setUser(getUser());
				apo.setParent(d);
				getRegSvc().getDcSvc().persist(apo);
			}
			if (!d.getSiblings().parallelStream().anyMatch(ds -> ds.getModel().getMetaname().equals("APOCHECKSAIDA"))) {
				DocumentModel dmChk = getRegSvc().getDmSvc().findByMetaname("APOCHECKSAIDA");
				Document apochk = new Document(dmChk, dmChk.getName() + " " + d.getCode(), "CHKEXIT" + d.getCode(), "NOVO");

				apochk.setParent(d);
				getRegSvc().getDcSvc().persist(apochk);
			}
			getRsm().getTsm().unlockTask(d);
		}
		return t.getRoute();
	}

}
