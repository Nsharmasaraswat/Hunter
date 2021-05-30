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
import com.gtp.hunter.ui.json.process.drone.ProcessMessage;

@ApplicationScoped
@ServerEndpoint("/process/{id}/{document}")
public class ContinuousProcessWebSocketServer {

	private static final String		PROCESS_SESSION_KEY	= "PS";
	private static final String		TOKEN_KEY			= "TOKEN";

	@Inject
	private ProcessStreamManager	psm;

	@Inject
	private transient Logger		logger;

	@OnOpen
	public void open(@PathParam("id") String id, @PathParam("document") String doc, Session session) {
		UUID token = UUID.randomUUID();
		UUID document = UUID.randomUUID();
		logger.debug("Procurando process " + id);
		try {
			// token = psm.getProcessFromMetaname(id).getId();
			token = UUID.fromString(id);
			document = UUID.fromString(doc);
			logger.debug("Tentando conectar em: " + token.toString());
		} catch (Exception e1) {
			try {
				session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "INVALID TOKEN"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (psm.getProcesses().containsKey(token)) {
			logger.info("CONECTANDO PROCESS " + id + " Open? " + session.isOpen());
			ProcessSession ps = new ProcessSession(session);
			psm.getProcesses().get(token).getFilterByDocument(document).subscribe(ps);
			session.getUserProperties().put(PROCESS_SESSION_KEY, ps);
			session.getUserProperties().put(TOKEN_KEY, id);
		} else {
			try {
				session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "PROCESS NOT FOUND"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@OnClose
	public void close(Session ss, CloseReason cr) {
		logger.info("FECHANDO PROCESS: " + ss.getUserProperties().get(TOKEN_KEY) + " (" + cr.getReasonPhrase() + ")");
		ProcessSession ps = (ProcessSession) ss.getUserProperties().get(PROCESS_SESSION_KEY);
		if (ps != null)
			ps.close(cr);
	}

	@OnMessage
	public void Message(String sMsg, Session ss) {
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
