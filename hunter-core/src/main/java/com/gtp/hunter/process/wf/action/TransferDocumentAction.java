package com.gtp.hunter.process.wf.action;

import java.lang.invoke.MethodHandles;
import java.util.UUID;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;

public class TransferDocumentAction extends DocumentAction {
	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public TransferDocumentAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	protected void open(Action t) {
		logger.info("Transfer Document Action");
		getAs().onNext(getDoc());
	}

	@Override
	public void onMessage(Object msg) {
		logger.info(msg.toString());
		try {
			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
			Document tmpOrdTransf = gson.fromJson((String) msg, Document.class);
			Document ordTransf = getRegSvc().getDcSvc().findById(tmpOrdTransf.getId());
			Address a = getRegSvc().getAddSvc().findById(UUID.fromString("e8943531-7b46-11e9-a9ec-005056a19775"));

			tmpOrdTransf.getThings().forEach(dt -> {
				Thing t = getRegSvc().getThSvc().findById(dt.getThing().getId());

				t.setStatus("ARMAZENADO");
				t.setAddress(a);
				dt.setDocument(ordTransf);
				dt.setThing(t);
				getRegSvc().getThSvc().persist(t);
				getRegSvc().getDtSvc().persist(dt);
				ordTransf.getThings().add(dt);
			});
			ordTransf.setStatus("SUCESSO");
			getRegSvc().getDcSvc().persist(ordTransf);
			getAs().onNext(IntegrationReturn.OK);
		} catch (Exception e) {
			getAs().onNext(new IntegrationReturn(false, "JSON MAL FORMADO - " + msg));
			e.printStackTrace();
		}
	}

	@OnClose
	@Override
	protected void close(Session ss, CloseReason cr) {
		logger.info("Closed: " + cr.getReasonPhrase());
	}
}
