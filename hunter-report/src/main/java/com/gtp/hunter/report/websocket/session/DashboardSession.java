package com.gtp.hunter.report.websocket.session;

import javax.websocket.Session;

import com.gtp.hunter.core.websocket.BaseSession;
import com.gtp.hunter.process.model.Document;

public class DashboardSession extends BaseSession<Document> {

	public DashboardSession(Session ss) {
		super(ss);
	}

}
