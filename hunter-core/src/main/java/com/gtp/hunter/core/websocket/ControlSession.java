package com.gtp.hunter.core.websocket;

import javax.websocket.Session;

import com.gtp.hunter.common.model.Command;

// CHANGE: Direct Websocket Command
public class ControlSession extends BaseSession<Command> {

	Command response;

	public ControlSession(Session ss) {
		super(ss);
	}

	public Session getRawSession() {
		return this.session;
	}

	public void setResponse(Command c) {
		response = c;
	}

	public Command getResponse() {
		return response;
	}
}
