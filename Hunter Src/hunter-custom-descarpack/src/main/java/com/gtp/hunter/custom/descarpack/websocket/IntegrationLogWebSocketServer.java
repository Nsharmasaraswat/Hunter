package com.gtp.hunter.custom.descarpack.websocket;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;

import com.gtp.hunter.custom.descarpack.stream.IntegrationLogStreamManager;
import com.gtp.hunter.custom.descarpack.websocket.session.IntegrationLogSession;

@ApplicationScoped
@ServerEndpoint("/integrationlog")
public class IntegrationLogWebSocketServer {

	@Inject
	private transient Logger			logger;

	@Inject
	private IntegrationLogStreamManager	ilsm;

	@OnOpen
	public void open(Session session) {
		logger.info("CONECTANDO INTEGRATIONLOG ");
		IntegrationLogSession ils = new IntegrationLogSession(session);
		ilsm.getStream().subscribe(ils);
		session.getUserProperties().put("ILS", ils);
	}

	@OnClose
	public void close(Session ss) {
		logger.info("FECHANDO INTEGRATIONLOG");
		IntegrationLogSession ils = (IntegrationLogSession) ss.getUserProperties().get("ILS");
		ils.close();
	}

}
