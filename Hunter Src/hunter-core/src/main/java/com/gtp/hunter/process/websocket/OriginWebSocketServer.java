package com.gtp.hunter.process.websocket;

import java.io.IOException;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;

import com.gtp.hunter.process.stream.OriginStreamManager;
import com.gtp.hunter.process.websocket.session.OriginSession;

@ApplicationScoped
@ServerEndpoint("/origin/{id}")
public class OriginWebSocketServer {

	private static final String	ORIGIN_SESSION_KEY	= "OS";

	@Inject
	private OriginStreamManager	osm;

	@Inject
	private Logger				logger;

	@OnOpen
	public void open(@PathParam("id") String id, Session session) {
		UUID token = UUID.randomUUID();
		String closeReason = null;

		try {
			token = UUID.fromString(id);

			if (osm.getOrigin(token) != null) {
				logger.info("CONECTANDO ORIGIN " + id);
				OriginSession os = new OriginSession(session);
				osm.getOrigin(token).getOrigin().subscribe(os);
				session.getUserProperties().put(ORIGIN_SESSION_KEY, os);
			} else {
				logger.info("NAO AUTORIZADO " + id);
				closeReason = "Não Autorizado: " + id;
			}
		} catch (IllegalArgumentException iae) {
			closeReason = "Token Inválido: " + id;
		} catch (Exception e1) {
			closeReason = e1.getLocalizedMessage();
			e1.printStackTrace();
		}

		if (closeReason != null)
			closeSession(session, closeReason);
	}

	@OnClose
	public void close(Session ss, CloseReason cr) {
		logger.info("FECHANDO ORIGIN");
		OriginSession os = (OriginSession) ss.getUserProperties().get(ORIGIN_SESSION_KEY);
		os.close(cr);
	}

	private void closeSession(Session session, String reason) {
		try {
			session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, reason));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
