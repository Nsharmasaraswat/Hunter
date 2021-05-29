package com.gtp.hunter.custom.descarpack.websocket.session;

import javax.websocket.Session;

import com.gtp.hunter.core.websocket.BaseSession;
import com.gtp.hunter.process.model.IntegrationLog;

public class IntegrationLogSession extends BaseSession<IntegrationLog> {

	public IntegrationLogSession(Session ss) {
		super(ss);
	}

}
