package com.gtp.hunter.process.wf.actiondef;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;

import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.service.RegisterService;

public class ActionsByDocumentProcess extends BaseActionDef {
	@Inject
	private static transient Logger logger;

	public ActionsByDocumentProcess(Action act, Purpose pur, RegisterService regSvc) {
		super(act, pur, regSvc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Action> getActions() {
		Action tact = getAct();
		List<Action> ret = new ArrayList<Action>();
		JsonReader jsonReader = Json.createReader(new StringReader(tact.getDefparams()));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();

		try {
			JsonObject prodLines = object.getJsonObject("process-map");
			Document planProd = tact.getDocument();
			String line = planProd.getFields().stream().filter(df -> df.getField().getMetaname().equals(object.getString("document-field-line-meta"))).findFirst().get().getValue();
			if (prodLines.containsKey(line)) {
				UUID procId = UUID.fromString(prodLines.getString(line));
				Process proc = getRegSvc().getPrcSvc().findById(procId);

				tact.setName(tact.getName().replaceAll("%%procname%%", proc.getName()).replaceAll("%%procid%%", proc.getId().toString()).replaceAll("%%procmeta%%", proc.getMetaname()).replaceAll("%%orimeta%%", proc.getOrigin().getMetaname()));
				tact.setStatus(tact.getStatus());
				if (tact.getDefparams() != null)
					tact.setDefparams(tact.getDefparams().replaceAll("%%procname%%", proc.getName()).replaceAll("%%procid%%", proc.getId().toString()).replaceAll("%%procmeta%%", proc.getMetaname()).replaceAll("%%orimeta%%", proc.getOrigin().getMetaname()));
				if (tact.getSrvparams() != null)
					tact.setSrvparams(tact.getSrvparams().replaceAll("%%procname%%", proc.getName()).replaceAll("%%procid%%", proc.getId().toString()).replaceAll("%%procmeta%%", proc.getMetaname()).replaceAll("%%orimeta%%", proc.getOrigin().getMetaname()));
				tact.setRoute(tact.getRoute().replaceAll("%%procname%%", proc.getName()).replaceAll("%%procid%%", proc.getId().toString()).replaceAll("%%procmeta%%", proc.getMetaname()).replaceAll("%%orimeta%%", proc.getOrigin().getMetaname()));
				tact.setParams(tact.getParams().replaceAll("%%procname%%", proc.getName()).replaceAll("%%procid%%", proc.getId().toString()).replaceAll("%%procmeta%%", proc.getMetaname()).replaceAll("%%orimeta%%", proc.getOrigin().getMetaname()));
				ret.add(tact);
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
		}

		return ret;
	}

}
