package com.gtp.hunter.report.websocket;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Filter;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.filter.AllFilter;
import com.gtp.hunter.process.wf.filter.BaseFilter;
import com.gtp.hunter.report.filter.trigger.WebsocketTrigger;
import com.gtp.hunter.report.websocket.session.DashboardSession;

@ApplicationScoped
@ServerEndpoint("/events/{token}")
public class EventWebsocket {

	private static final String		TOKEN_KEY				= "TOKEN";
	private static final String		DASHBOARD_SESSION_KEY	= "DSK";
	private static final String		FILTER_SESSION_KEY		= "FSK";

	@Inject
	private Logger					logger;

	@EJB(lookup = "java:global/hunter-core/RegisterStreamManager!com.gtp.hunter.process.stream.RegisterStreamManager")
	private RegisterStreamManager	rsm;

	@OnOpen
	public void open(@PathParam("token") String token, Session ss) {
		logger.info("CONECTANDO EVENTS" + token + " (" + ss.getId() + ": " + ss.getOpenSessions().size() + ")");
		ss.getUserProperties().put(TOKEN_KEY, token);
		DashboardSession ts = new DashboardSession(ss);
		ss.getUserProperties().put(DASHBOARD_SESSION_KEY, ts);
		Filter ffailnf = new Filter();
		BaseFilter dshbf = new AllFilter(ffailnf);
		dshbf.getTriggers().add(new WebsocketTrigger(ts));
		ss.getUserProperties().put(FILTER_SESSION_KEY, dshbf);
		rsm.getFsm().registerBaseFilter(Document.class, dshbf);
	}

	@OnMessage
	public void message(String message, Session ss) {
		logger.info("Message Received: " + message);
	}

	@OnClose
	public void close(Session ss, CloseReason cr) {
		logger.info("FECHANDO EVENTS: " + ss.getUserProperties().get(TOKEN_KEY) + " (" + ss.getId() + ": " + ss.getOpenSessions().size() + ")");
		DashboardSession ts = (DashboardSession) ss.getUserProperties().get(DASHBOARD_SESSION_KEY);

		if (ss.getUserProperties().containsKey(FILTER_SESSION_KEY))
			rsm.getFsm().unRegisterBaseFilter(Document.class, (BaseFilter) ss.getUserProperties().get(FILTER_SESSION_KEY));
		if (ts != null)
			ts.close(cr);
	}

	@OnError
	public void error(Session ss, Throwable throwable) {
		logger.error("Erro na sessão " + ss.getUserProperties().get(TOKEN_KEY) + "(" + throwable.getLocalizedMessage() + ") " + ss.getId() + ": " + ss.getOpenSessions().size());
		logger.trace(throwable.getLocalizedMessage(), throwable);
	}
}
