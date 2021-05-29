package com.gtp.hunter.core.websocket.user;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;

import com.gtp.hunter.core.stream.RawDataStreamManager;

@ApplicationScoped
@ServerEndpoint("/user")
public class UserWebSocketServer {
	private static final String		USER_SESSION_KEY	= "US";

	@Inject
	private RawDataStreamManager	rdsm;

	@Inject
	private transient Logger		logger;

	@OnOpen
	public void open(Session session) {
		logger.debug("CONECTANDO USUARIO");
		UserSession us = new UserSession(session);
		rdsm.getRealTime().subscribe(us);
		session.getUserProperties().put(USER_SESSION_KEY, us);
	}

	@OnClose
	public void close(Session ss, CloseReason cr) {
		logger.debug("FECHANDO USUARIO");
		UserSession us = (UserSession) ss.getUserProperties().get(USER_SESSION_KEY);

		us.onComplete();
		us.close(cr);
	}

}
