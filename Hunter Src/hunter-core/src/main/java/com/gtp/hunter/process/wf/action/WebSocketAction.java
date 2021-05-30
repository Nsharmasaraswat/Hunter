package com.gtp.hunter.process.wf.action;

import javax.websocket.CloseReason;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.websocket.session.ActionSession;
import com.gtp.hunter.ui.json.action.BaseActionMessage;

public abstract class WebSocketAction extends BaseAction {
	private ActionSession as;

	public WebSocketAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	public String execute(Action t) throws Exception {
		return null;
	}

	public void setAs(ActionSession as) {
		this.as = as;
	}

	public ActionSession getAs() {
		return this.as;
	}

	public abstract void onOpen(Action t, ActionSession as);

	public abstract void onMessage(Object msg);

	public abstract void completed(Action t);

	public abstract void canceled(Action t, CloseReason cr);

	protected void closeSession(ActionSession session, CloseReason cr) {
		session.close(cr);
	}

	public void interact(BaseActionMessage msg) {
		if (as != null && as.isOnline())
			as.onNext(msg);
	}
}
