package com.gtp.hunter.process.websocket;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.stream.TaskStreamManager;
import com.gtp.hunter.process.websocket.session.ActionSession;
import com.gtp.hunter.process.wf.action.WebSocketAction;
import com.gtp.hunter.ui.json.action.BaseActionMessage;

@ApplicationScoped
@ServerEndpoint("/action/{token}/{id}/{param}")
public class ActionProcessWebSocketServer {

	private static final String				TOKEN_KEY			= "TOKEN";
	private static final String				ACTION_SESSION_KEY	= "AS";
	private static final String				ACTION_KEY			= "ACT";

	@Inject
	private transient Logger				logger;

	@Inject
	private RegisterService					regSvc;

	@Inject
	private TaskStreamManager				tsm;

	@Inject
	private RegisterStreamManager			rsm;

	private Map<String, WebSocketAction>	actions				= new HashMap<String, WebSocketAction>();

	@OnOpen
	public void open(@PathParam("token") String token, @PathParam("id") String id, @PathParam("param") String param, Session session) {
		boolean log = false;

		session.setMaxIdleTimeout(30000);
		try {
			if (token == null || token.isEmpty()) {
				logger.error("Token Inválido!");
				session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "BAD REQUEST Token NULL"));
				return;
			}
			if (id == null || id.isEmpty() || id.equals("null")) {
				logger.error("Ação Inválida! " + "Token: " + token + " id: " + (id == null ? "NULL" : id) + " param: " + (param == null ? "NULL" : param));
				session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "Ação Inválida"));
				return;
			}
			if (param == null || param.isEmpty() || param.equals("null")) {
				logger.error("Documento Inválido! " + "Token: " + token + " id: " + (id == null ? "NULL" : id) + " param: " + (param == null ? "NULL" : param));
				session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "Documento Inválido"));
				return;
			}

			if (regSvc.getAuthSvc().valid(token)) {
				Profiler p = new Profiler("Action Opened");
				User usr = regSvc.getAuthSvc().getUser(token);
				UUID actId = UUID.fromString(id);
				UUID docId = UUID.fromString(param);

				p.step("Load User " + usr == null ? " NULL " : usr.getName(), log);
				if (!tsm.isTaskLocked(docId) || tsm.isTaskBoundToUser(usr.getId(), docId)) {
					p.step("Check Task Locked And Bound", log);
					tsm.cancelTask(usr.getId(), docId);
					p.step("Open Action " + usr.getName() + " T:" + token + " (" + session.getId() + ": " + session.getOpenSessions().size() + ")", true);
					ActionSession as = new ActionSession(session);
					Action act = regSvc.getActSvc().findById(actId);

					p.step("Found Action: " + act.getMetaname(), log);
					act.setParams(param);
					Constructor wsac = Class.forName(act.getClasse()).getConstructor(User.class, Action.class, RegisterStreamManager.class, RegisterService.class);
					WebSocketAction wsa = (WebSocketAction) wsac.newInstance(usr, act, rsm, regSvc);

					p.step("Constructed Websocket Instance", log);
					wsa.onOpen(act, as);
					tsm.registerAction(usr, wsa);
					session.getUserProperties().put(TOKEN_KEY, token);
					session.getUserProperties().put(ACTION_SESSION_KEY, as);
					session.getUserProperties().put(ACTION_KEY, act);
					this.actions.put(token, wsa);
				} else {
					tsm.cancelTask(usr.getId(), docId);
					logger.info(p.step("Task is in use, resend cancel to other devices", log));
					session.close(new CloseReason(CloseCodes.PROTOCOL_ERROR, "Tarefa em execução em outro dispositivo"));
				}
				p.done("Action Opened", log, false).forEach(logger::info);
			} else {
				logger.warn("USER LOGGED OFF: " + token + " (" + session.getId() + ": " + session.getOpenSessions().size() + ")");
				session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "Token Inválido. Necessária Autenticação"));
			}
		} catch (IOException ignored) {

		} catch (Exception ex) {
			logger.error(ex.getLocalizedMessage(), ex);
		}
	}

	@OnMessage
	public void message(String message, Session session) {
		try {
			if (message.equals("PING")) {
				if (session.getUserProperties().containsKey(ACTION_SESSION_KEY)) {
					ActionSession as = (ActionSession) session.getUserProperties().get(ACTION_SESSION_KEY);
					as.onNext("PONG");
				}
			} else if (!session.getUserProperties().containsKey(TOKEN_KEY)) {
				session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "Token Inválido. Necessária Autenticação"));
			} else if (!this.actions.containsKey(session.getUserProperties().get(TOKEN_KEY))) {
				session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "Sessão não mapeada para websocket"));
			} else {
				logger.info("Mensagem Recebida: " + message);
				this.actions.get(session.getUserProperties().get(TOKEN_KEY)).onMessage(message);
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	@OnClose
	public void close(Session ss, CloseReason cr) {
		logger.info("CLOSING ACTION: Code " + cr.getCloseCode().getCode() + " Reason: " + cr.getReasonPhrase() + " Token: " + ss.getUserProperties().get(TOKEN_KEY) + " (Session ID: " + ss.getId() + " Open Sessions to same endpoint: " + ss.getOpenSessions().size() + ")");
		String token = (String) ss.getUserProperties().get(TOKEN_KEY);
		ActionSession as = (ActionSession) ss.getUserProperties().get(ACTION_SESSION_KEY);
		WebSocketAction wsAct = this.actions.get(token);
		User usr = regSvc.getAuthSvc().getUser(token);

		if (wsAct != null) {
			wsAct.completed((Action) ss.getUserProperties().get(ACTION_KEY));
			this.actions.remove(token);
			if (usr != null)
				tsm.unregisterAction(usr);
		}
		if (as != null)
			as.close(cr);
	}

	@OnError
	public void error(Session session, Throwable throwable) {
		if (throwable instanceof IOException) {
			// perdeu conexão. derrubando a sessão.
		} else {
			// erro de outro canto. melhor mostrar bra poder depurar...
			logger.error("Erro na sessão " + session.getUserProperties().get(TOKEN_KEY) + "(" + throwable.getLocalizedMessage() + ") " + session.getId() + ": " + session.getOpenSessions().size());
			logger.error(throwable.getLocalizedMessage(), throwable);
		}
		//de qualquer forma. fechar essa sessão.
		try {
			session.getUserProperties().clear();
			session.close();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	public void interact(String token, BaseActionMessage message) {
		if (this.actions.containsKey(token)) {
			this.actions.get(token).interact(message);
		}
	}
}
