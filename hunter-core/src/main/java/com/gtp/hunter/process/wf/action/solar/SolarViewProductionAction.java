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

public class SolarViewProductionAction extends BaseAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public SolarViewProductionAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	public String execute(Action t) throws Exception {
		logger.info("RODANDO ACTION DE VISUALIZAÇÃO DE LINHA DE PRODUÇÃO");
		logger.info(t.getParams());
		JsonReader jsonReader = Json.createReader(new StringReader(t.getDefparams()));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();
		try {
			JsonObject prodLines = object.getJsonObject("process-map");
			Document planProd = getRegSvc().getDcSvc().findById(UUID.fromString(t.getParams()));
			String line = planProd.getFields().stream().filter(df -> df.getField().getMetaname().equals(object.getString("document-field-line-meta"))).findFirst().get().getValue();
			UUID procId = UUID.fromString(prodLines.getString(line));
			ProductionLineProcess proc = (ProductionLineProcess) getRsm().getPsm().getProcesses().get(procId);

			t.setRoute(t.getRoute().replace("%%procid%%", proc.getModel().getId().toString()));
			t.setName(t.getName().replace("%%procname%%", proc.getModel().getName()));
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
		}
		return t.getRoute();
	}
}
