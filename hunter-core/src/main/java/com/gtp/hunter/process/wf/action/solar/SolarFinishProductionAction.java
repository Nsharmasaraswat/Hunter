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
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.BaseAction;
import com.gtp.hunter.process.wf.process.solar.ProductionLineProcess;

public class SolarFinishProductionAction extends BaseAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public SolarFinishProductionAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	public String execute(Action t) throws Exception {
		logger.info("RODANDO ACTION DE FINAL DE LINHA DE PRODUÇÃO");
		logger.info(t.getParams());
		JsonReader jsonReader = Json.createReader(new StringReader(t.getParams()));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();
		UUID docId = UUID.fromString(object.getString("doc_id"));
		Document ordProd = getRegSvc().getDcSvc().findById(docId);

		if (!getRsm().getTsm().isTaskLocked(docId))
			getRsm().getTsm().cancelTask(getUser().getId(), ordProd);
		try {
			JsonObject prodLines = object.getJsonObject("process-map");
			String line = ordProd.getFields().stream().filter(df -> df.getField().getMetaname().equals(object.getString("document-field-line-meta"))).findFirst().get().getValue();

			if (prodLines.containsKey(line)) {
				UUID procId = UUID.fromString(prodLines.getString(line));
				ProductionLineProcess proc = (ProductionLineProcess) getRsm().getPsm().getProcesses().get(procId);
				String childCodePrefix = object.getString("child-code-prefix");
				String childModelMeta = object.getString("child-model-meta");
				String masterStatus = object.getString("master-status");
				String childStatus = object.getString("child-status");
				Document dMaster = proc.getProductionOrder();

				if (dMaster != null) getRsm().getTsm().unlockTask(dMaster);
				getRegSvc().getDcSvc().createChild(ordProd, masterStatus, childModelMeta, childStatus, childCodePrefix, null, null, null, getUser());
				getRegSvc().getDcSvc().flush();
				proc.stopProduction();
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		getRsm().getTsm().unlockTask(ordProd);
		return t.getRoute();
	}
}
