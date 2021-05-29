package com.gtp.hunter.process.wf.action;

import java.io.StringReader;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.websocket.session.ActionSession;

public abstract class DocumentAction extends WebSocketAction {

	private Document	doc;
	protected Gson		gs;
	private JsonObject	params;

	public DocumentAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
		doc = regSvc.getDcSvc().findById(UUID.fromString(action.getParams()));
		gs = new GsonBuilder()
						.excludeFieldsWithoutExposeAnnotation()
						.create();
		if (action.getParams() != null && action.getParams().startsWith("{")) {
			JsonReader jsonReader = Json.createReader(new StringReader(action.getParams()));
			params = jsonReader.readObject();
			jsonReader.close();
		} else
			params = JsonObject.EMPTY_JSON_OBJECT;
	}

	@Override
	public final void onOpen(Action t, ActionSession as) {
		setAs(as);

		getRegSvc().getTskSvc().startTask(doc, getUser());
		open(t);
		getRsm().getTsm().lockTask(getUser().getId(), doc.getId());
	}

	@Override
	public void completed(Action t) {
		Document d = getRegSvc().getDcSvc().findById(UUID.fromString(t.getParams()));

		getRegSvc().getTskSvc().completeTask(d, getUser());
		getRsm().getTsm().unlockTask(d);
		getRsm().getTsm().unregisterAction(getUser());
		closeSession(getAs(), new CloseReason(CloseCodes.NORMAL_CLOSURE, "Completed!"));
	}

	@Override
	public void canceled(Action t, CloseReason cr) {
		Document d = getRegSvc().getDcSvc().findById(UUID.fromString(t.getParams()));

		getRsm().getTsm().unlockTask(d);
		closeSession(getAs(), cr);
	}

	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}

	public String getParam(String key) {
		return params.containsKey(key) ? params.getString(key) : "";
	}

	protected abstract void open(Action t);

	protected abstract void close(Session ss, CloseReason cr);
}
