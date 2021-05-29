package com.gtp.hunter.api.websocket;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;

@ApplicationScoped
@ServerEndpoint("/echo")
public class EchoWebSocketServer {

	@Inject
	private transient Logger logger;

	private Timer regtimer;
	private TimerTask timer;

	@OnOpen
	public void open(Session session) {
		logger.info("Conectado: " + session.getId());
		for (Session s : session.getOpenSessions()){
			try {
				if (s.isOpen())
					s.getBasicRemote().sendText(session.getId() + " ENTROU ");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		timer = new EchoWebSocketTimer(session);
		regtimer = new Timer();
		regtimer.schedule(timer, 0, 2000);
	}

	@OnMessage
	public void handleMessage(String message, Session session) {
		logger.info("Você disse: " + message);
		// session.getBasicRemote().sendText("Você disse: " + message );
		for (Session s : session.getOpenSessions()){
			try {
				if (s.isOpen())
					s.getBasicRemote().sendText(session.getId() + ": " + message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@OnClose
	public void close(Session ss) {
		logger.info("Desconectado: " + ss.getId());
		for (Session s : ss.getOpenSessions()){
			try {
				if (s.isOpen())
					s.getBasicRemote().sendText(ss.getId() + " SAIU ");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		regtimer.cancel();
		timer = null;
		regtimer = null;
	}

}
