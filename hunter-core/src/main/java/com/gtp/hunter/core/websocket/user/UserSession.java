package com.gtp.hunter.core.websocket.user;

import javax.websocket.Session;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.websocket.BaseSession;

public class UserSession extends BaseSession<ComplexData> {

	public UserSession(Session ss) {
		super(ss);
	}

}
