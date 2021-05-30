package com.gtp.hunter.process.websocket;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.TaskStreamManager;
import com.gtp.hunter.process.websocket.session.TaskSession;

@ApplicationScoped
@ServerEndpoint("/tasks/{token}/{metaname}")
public class TaskWebSocketServer {

	private static final String	TOKEN_KEY			= "TOKEN";
	private static final String	TASK_SESSION_KEY	= "TS";

	@Inject
	private Logger				logger;

	@Inject
	private RegisterService		regSvc;

	@Inject
	private TaskStreamManager	tsm;

	@OnOpen
	public void open(@PathParam("token") String token, @PathParam("metaname") String metaname, Session session) {
		try {
			if (regSvc.getAuthSvc().valid(token)) {
				if (tsm.isTaskDefActive(metaname)) {
					logger.info("CONECTANDO TASKS " + token + " (" + session.getId() + ": " + session.getOpenSessions().size() + ")");
					session.getUserProperties().put(TOKEN_KEY, token);
					TaskSession ts = new TaskSession(session);
					tsm.subscribeByTask(token, ts, metaname);
					session.getUserProperties().put(TASK_SESSION_KEY, ts);
				} else {
					IntegrationReturn ir = new IntegrationReturn(false, metaname + " is not an active task.");

					session.getBasicRemote().sendText(new Gson().toJson(ir));
				}
			} else {
				session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "BAD REQUEST"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@OnMessage
	public void message(String message, Session session) {
		logger.info("Message Received: " + message);
	}

	@OnClose
	public void close(Session ss, CloseReason cr) {
		logger.info("FECHANDO TASKS: " + ss.getUserProperties().get(TOKEN_KEY) + " (" + ss.getId() + ": " + ss.getOpenSessions().size() + ")");
		TaskSession ts = (TaskSession) ss.getUserProperties().get(TASK_SESSION_KEY);

		if (ts != null)
			ts.close(cr);
	}

	@OnError
	public void error(Session session, Throwable throwable) {
		logger.error("Erro na sessão " + session.getUserProperties().get(TOKEN_KEY) + "(" + throwable.getLocalizedMessage() + ") " + session.getId() + ": " + session.getOpenSessions().size());
		logger.trace(throwable.getLocalizedMessage(), throwable);
	}

}
