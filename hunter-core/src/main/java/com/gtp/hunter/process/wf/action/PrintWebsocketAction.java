package com.gtp.hunter.process.wf.action;

import java.lang.invoke.MethodHandles;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.PrintTagOrder;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.websocket.session.ActionSession;
import com.gtp.hunter.ui.json.action.BaseActionMessage;

public class PrintWebsocketAction extends WebSocketAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public PrintWebsocketAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
		logger.info("Action Params: " + action.getParams());
	}

	@Override
	public void onOpen(Action t, ActionSession ss) {
		try {
			PrintMessage msg = new PrintMessage();

			setAs(ss);
			msg.action = "OPEN";
			ss.onNext(msg);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void onMessage(Object msg) {
		logger.info((String) msg);
	}

	class PrintMessage {
		String			action;
		PrintTagOrder	order;
	}

	@Override
	public void completed(Action t) {
		if (getAs().isOnline()) closeSession(getAs(), new CloseReason(CloseCodes.NORMAL_CLOSURE, ""));
	}

	@Override
	public void canceled(Action t, CloseReason cr) {
		if (getAs().isOnline()) closeSession(getAs(), cr);
	}

	@Override
	public void interact(BaseActionMessage msg) {
		getAs().onNext(msg);
	}
}
