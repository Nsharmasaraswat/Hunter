package com.gtp.hunter.process.websocket.session;

import javax.websocket.Session;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.websocket.BaseSession;

public class OriginSession extends BaseSession<ComplexData> {
	public OriginSession(Session ss) {
		super(ss);
	}
}
