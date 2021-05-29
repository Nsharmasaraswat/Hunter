package com.gtp.hunter.process.wf.action.solar;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.BaseAction;

public class SolarCancelTransportAction extends BaseAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public SolarCancelTransportAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	public String execute(Action t) throws Exception {
		logger.info("RODANDO ACTION DE CANCELAMENTO DE TRANSPORTE");
		logger.info(t.getParams());
		JsonReader jsonReader = Json.createReader(new StringReader(t.getParams()));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();
		UUID docId = UUID.fromString(object.getString("document-id"));
		String statusTo = object.getString("status-to");
		Document transp = getRegSvc().getDcSvc().findById(docId);

		getRsm().getTsm().cancelTask(getUser().getId(), transp);
		try {
			transp.getSiblings().stream()
							.filter(ds -> ds.getModel().getMetaname().equals("NFSAIDA") || ds.getModel().getMetaname().equals("NFENTRADA"))
							.forEach(nf -> nf.setParent(null));
			switch (transp.getStatus()) {
				case "CAMINHAO NA PORTARIA":
					//Só muda o status
					break;
				case "CAMINHAO NA ENTRADA":
					transp.getSiblings().stream()
									.filter(ds -> ds.getModel().getMetaname().equals("APOCHECKLIST"))
									.forEach(chk -> {
										getRsm().getTsm().cancelTask(getUser().getId(), chk);
										chk.setStatus(statusTo);
										Executors.newSingleThreadScheduledExecutor().schedule(() -> getRsm().getTsm().unlockTask(chk), 30, TimeUnit.SECONDS);
									});
					break;
				case "CAMINHAO NO PATIO":
					Document chk = transp.getSiblings().parallelStream()
									.filter(ds -> ds.getModel().getMetaname().equals("APOCHECKLIST"))
									.findAny()
									.get();
					Thing tr = transp.getThings().parallelStream()
									.filter(dt -> dt.getThing().getProduct().getModel().getMetaname().equals("TRUCK"))
									.map(dt -> dt.getThing())
									.findAny()
									.get();
					String tagId = Documents.getStringField(chk, "ATTRASTREADOR");

					getRegSvc().getThSvc().fillUnits(tr);
					Unit tag = tr.getUnitModel().parallelStream()
									.filter(u -> u.getTagId().equals(tagId))
									.findAny()
									.orElse(null);
					tr.getUnits().removeIf(u -> u.equals(tag.getId()));
					break;
			}
			transp.setStatus(statusTo);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
		}
		getRsm().getTsm().unlockTask(transp);
		return t.getRoute();
	}
}
