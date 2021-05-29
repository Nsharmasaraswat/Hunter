package com.gtp.hunter.process.wf.action.solar;

import java.lang.invoke.MethodHandles;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.DocumentAction;

public class DynamicDocumentAction extends DocumentAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public DynamicDocumentAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	protected void open(Action t) {
		logger.debug("Dynamic Document Action");
		getAs().onNext(getDoc());
	}

	@Override
	public void onMessage(Object msg) {
		String json = (String) msg;
		Document tmp = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(json, Document.class);
		Document d = getRegSvc().getDcSvc().findById(tmp.getId());

		for (DocumentField tdf : tmp.getFields()) {
			DocumentModelField tdmf = getRegSvc().getDmfSvc().findById(tdf.getField().getId());
			DocumentField docField = d.getFields().parallelStream()
							.filter(df -> df.getField().getId().equals(tdmf.getId()))
							.findAny()
							.orElseGet(() -> new DocumentField(d, tdmf, "PREENCHIDO", tdf.getValue()));

			docField.setValue(tdf.getValue());
			d.getFields().add(docField);
		}

		d.setStatus("PREENCHIDO");
		d.setUser(getUser());
		getRegSvc().getDcSvc().persist(d);
		getAs().onNext(IntegrationReturn.OK);
	}

	@OnClose
	@Override
	protected void close(Session ss, CloseReason cr) {
		logger.info("Closed: " + cr.getReasonPhrase());
	}
}
