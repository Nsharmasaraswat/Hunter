package com.gtp.hunter.process.websocket.session;

import javax.websocket.Session;

import com.gtp.hunter.core.websocket.BaseSession;
import com.gtp.hunter.ui.json.ViewTaskStub;

public class TaskSession extends BaseSession<ViewTaskStub> {

	public TaskSession(Session ss) {
		super(ss);
	}

}
