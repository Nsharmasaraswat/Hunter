package com.gtp.hunter.process.websocket.session;

import javax.websocket.Session;

import com.gtp.hunter.core.websocket.BaseSession;

public class ContinuousProcessSession extends BaseSession<Object> {

	public ContinuousProcessSession(Session ss) {
		super(ss);
	}


}
