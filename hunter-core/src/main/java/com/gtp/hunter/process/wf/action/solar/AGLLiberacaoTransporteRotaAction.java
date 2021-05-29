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
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.BaseAction;

public class AGLLiberacaoTransporteRotaAction extends BaseAction {
	private static final String				THING_STATUS	= "EXPEDIDO";
	private transient static final Logger	logger			= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public AGLLiberacaoTransporteRotaAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String execute(Action action) throws Exception {
		JsonReader jsonReader = Json.createReader(new StringReader(action.getParams()));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();
		UUID docId = UUID.fromString(object.getString("doc_id"));
		Document transp = getRegSvc().getDcSvc().findById(docId);

		logger.info("RODANDO ACTION DE LIBERAÇÃO DE CAMINHAO DE ROTA " + transp.getCode());
		if (!transp.getStatus().equals(object.getString("statuspara"))) {
			getRsm().getTsm().cancelTask(getUser().getId(), transp);
			for (Document d : transp.getSiblings()) {
				if (d.getStatus().equals("ROMANEIO") || d.getStatus().equals("SEPARACAO") || d.getStatus().equals("MOVIMENTACAO")) {
					getRsm().getTsm().cancelTask(getUser().getId(), d);
					d.setStatus("CANCELADO");
					d.getSiblings().forEach(ds -> {
						switch (ds.getModel().getMetaname()) {
							case "ORDMOV":
								getRegSvc().getWmsSvc().cancelOrdMov(ds, getUser());
							case "OSG":
								ds.setStatus("CANCELADO");
								break;
							default:
								getRegSvc().getDcSvc().removeById(ds.getId());
						}
					});
				}
			}
			transp.setStatus(object.getString("statuspara"));
			getRegSvc().getDcSvc().persist(transp);
			if (!transp.getSiblings().parallelStream().anyMatch(ds -> ds.getModel().getMetaname().equals("APOLIBERACAO"))) {
				DocumentModel dm = getRegSvc().getDmSvc().findByMetaname("APOLIBERACAO");
				Document apo = new Document(dm, dm.getName() + " " + transp.getCode(), "LIB" + transp.getCode(), "NOVO");

				apo.setUser(getUser());
				apo.setParent(transp);
				getRegSvc().getDcSvc().persist(apo);
			}
			if (ConfigUtil.get("hunter-custom-solar", "checkout-portaria", "FALSE").equalsIgnoreCase("TRUE") && !transp.getSiblings().parallelStream().anyMatch(ds -> ds.getModel().getMetaname().equals("CHECKOUTPORTARIA"))) {
				DocumentModel dmChk = getRegSvc().getDmSvc().findByMetaname("CHECKOUTPORTARIA");
				Document apochk = new Document(dmChk, dmChk.getName() + " " + transp.getCode(), "CHKPORT" + transp.getCode(), "NOVO");

				apochk.setParent(transp);
				getRegSvc().getDcSvc().persist(apochk);
				transp.getSiblings().add(apochk);
			}
			getRsm().getTsm().unlockTask(transp);
		}
		return action.getRoute();
	}

}
