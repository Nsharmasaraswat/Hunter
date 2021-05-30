package com.gtp.hunter.process.websocket;

import java.io.IOException;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;

import com.google.gson.GsonBuilder;
import com.gtp.hunter.process.stream.ProcessStreamManager;
import com.gtp.hunter.process.websocket.session.ProcessSession;
import com.gtp.hunter.process.wf.process.BaseProcess;
import com.gtp.hunter.process.wf.process.BaseSingleProcess;
import com.gtp.hunter.ui.json.process.drone.ProcessMessage;

@ApplicationScoped
@ServerEndpoint("/process/{id}")
public class ProcessWebSocketServer {

	private static final String		PROCESS_SESSION_KEY	= "PS";
	private static final String		TOKEN_KEY			= "TOKEN";

	@Inject
	private ProcessStreamManager	psm;

	@Inject
	private transient Logger		logger;

	@OnOpen
	public void open(@PathParam("id") String id, Session session) {
		UUID token = UUID.randomUUID();
		logger.debug("Procurando process " + id);
		try {
			// token = psm.getProcessFromMetaname(id).getId();
			token = UUID.fromString(id);
			logger.info("Tentando conectar em: " + token.toString());
		} catch (Exception e1) {
			try {
				session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "BAD REQUEST PROCESS_ID"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (psm.getProcesses().containsKey(token)) {
			logger.info("CONECTANDO PROCESS " + id + " Open? " + session.isOpen());
			ProcessSession ps = new ProcessSession(session);
			psm.getProcesses().get(token).subscribe(ps);
			session.getUserProperties().put(PROCESS_SESSION_KEY, ps);
			session.getUserProperties().put(TOKEN_KEY, id);
		} else {
			try {
				session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "BAD REQUEST NO PROCESS"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@OnClose
	public void close(Session ss, CloseReason cr) {
		logger.info("FECHANDO PROCESS: " + cr.getReasonPhrase());
		ProcessSession ps = (ProcessSession) ss.getUserProperties().get(PROCESS_SESSION_KEY);
		if (ss.getUserProperties().containsKey(TOKEN_KEY)) {
			UUID procId = UUID.fromString((String) ss.getUserProperties().get(TOKEN_KEY));
			BaseProcess p = psm.getProcesses().get(procId);

			if (p instanceof BaseSingleProcess) {
				if (!p.isComplete()) {
					logger.info("MANDANDO O FAILURE DO PROCESSO");
					p.setFailure("CONEXÃO WEBSOCKET ENCERRADA");
				}
				psm.getProcesses().remove(procId);
				p.finish();
			} else {
				logger.info("Process is continuous, dont stop it");
			}
		}
		if (ps != null)
			ps.close(cr);
	}

	@OnMessage
	public void Message(String sMsg, Session ss) {
		if (sMsg.equals("PING") || sMsg.equals("PONG")) return;
		ProcessMessage message = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().enableComplexMapKeySerialization().create().fromJson(sMsg, ProcessMessage.class);

		if (ss.getUserProperties().containsKey(TOKEN_KEY)) {
			UUID procId = UUID.fromString((String) ss.getUserProperties().get(TOKEN_KEY));
			BaseProcess p = psm.getProcesses().get(procId);

			if (p != null)
				p.message(message);
			else
				close(ss, new CloseReason(CloseCodes.CANNOT_ACCEPT, "Processo não inicializado"));
		}
	}
}
