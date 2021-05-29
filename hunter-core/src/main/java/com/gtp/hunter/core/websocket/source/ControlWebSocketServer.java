package com.gtp.hunter.core.websocket.source;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.common.util.WebsocketKeepalive;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Source;
import com.gtp.hunter.core.service.RawDataService;
import com.gtp.hunter.core.service.SourceService;
import com.gtp.hunter.core.websocket.ControlSession;
import com.gtp.hunter.dms.config.device.DeviceConfig;

@ApplicationScoped
@ServerEndpoint("/control/{sourceid}")
public class ControlWebSocketServer {

	@Inject
	private SourceService		sSvc;

	@Inject
	private RawDataService		rds;

	@Inject
	private transient Logger	logger;

	private Gson				gson;

	private WebsocketKeepalive	keepAlive;

	public ControlWebSocketServer() {
		gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		keepAlive = new WebsocketKeepalive("ISO-8859-1");
	}

	@OnOpen
	public void open(@PathParam("sourceid") String srcId, Session ss) {
		ss.setMaxIdleTimeout(0);
		keepAlive.start(ss);
		sSvc.removeFailureNotification(UUID.fromString(srcId));
		logger.debug(srcId + " connected!");
	}

	@OnError
	public void error(@PathParam("sourceid") String srcId, Session session, Throwable error) {
		String token = (String) session.getUserProperties().get("TOKEN");

		if (srcId != null && !srcId.isEmpty())
			sSvc.addFailureNotification(token, UUID.fromString(srcId));
		logger.error(error.getLocalizedMessage(), error);
		sSvc.remove(token, new CloseReason(CloseCodes.UNEXPECTED_CONDITION, error.getLocalizedMessage()));
		keepAlive.stop();
	}

	@OnClose
	public void close(@PathParam("sourceid") String srcId, Session ss, CloseReason cr) {
		String token = ss.getUserProperties().containsKey("TOKEN") ? (String) ss.getUserProperties().remove("TOKEN") : "";

		sSvc.remove(token, cr);
		try {
			ss.close(cr);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
		}
		keepAlive.stop();
		logger.info(srcId + " disconnected. Reason: " + cr.getReasonPhrase());
	}

	@OnMessage
	public void handleMessage(@PathParam("sourceid") String srcId, String message, Session session) {
		try {
			try {
				UUID sourceId = UUID.fromString(srcId);
				if (sSvc.verifySource(sourceId)) { // Check if token is valid
					session.setMaxIdleTimeout(0);
					if (!sSvc.isAttached(sourceId)) { // Check if source is Attached
						attachSession(session, sourceId);
					} else { // if its already attached, process message
						if (message.equals("PING")) {
							session.getAsyncRemote().sendText("PONG");
						} else if (message.equals("PONG")) {
							// logger.info("Client is alive!");
						} else if (message.startsWith("[") || message.startsWith("{")) {
							Command cmd = gson.fromJson(message, Command.class);

							// TODO: UGH!!! QUE FEIO
							if (cmd.getPayload().equals("CONFIGURE")) {
								Source source = sSvc.findById(sourceId);
								List<Device> devs = sSvc.listBySource(source);
								List<DeviceConfig> config = new ArrayList<>();

								for (Device d : devs) {
									DeviceConfig c = new DeviceConfig();

									c.setMetaname(d.getMetaname());
									c.setId(d.getId());
									config.add(c);
								}
								session.getBasicRemote().sendText(this.gson.toJson(config));
								//CHANGE: Direct Websocket Command
							} else if (cmd.getMethod().equalsIgnoreCase("print")) {
								ControlSession s = sSvc.getSources().get(cmd.getSource()).getControlSession();
								synchronized (s) {
									s.setResponse(cmd);
									s.notifyAll();
								}
							} else {
								rds.processCommand(cmd);
							}
						} else {
							sSvc.detachSource(sourceId);
							attachSession(session, sourceId);
						}
					}
				} else if (sSvc.getSources().isEmpty()) {
					session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "EMPTY SOURCES"));
				} else {
					session.getBasicRemote().sendText("UNAUTHORIZED");
					session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "UNAUTHORIZED"));
				}
			} catch (IllegalArgumentException e) {
				logger.warn("Invalid source " + srcId);
				session.getBasicRemote().sendText("UNAUTHORIZED");
				session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "UNAUTHORIZED"));
			}
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
		}
	}

	private void attachSession(Session session, UUID srcId) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S Z");
			String timestamp = sdf.format(Calendar.getInstance().getTime());
			String token = sSvc.attach(new ControlSession(session), srcId);
			session.getUserProperties().put("TOKEN", token);
			logger.info("Authorizing source " + srcId.toString());
			session.getBasicRemote().sendText(token + " " + timestamp);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
		}
	}

	@OnMessage
	public void onPong(@PathParam("sourceid") String src, PongMessage pongMessage) {
		logger.trace(pongMessage.getApplicationData().toString());
	}
}
