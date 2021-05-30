package com.gtp.hunter.common.util;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Timer;
import java.util.TimerTask;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketKeepalive {
	private transient static final Logger	logger				= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final long				KEEPALIVE_DELAY		= 35000;
	private static final long				KEEPALIVE_INTERVAL	= 60000;

	private Timer							keepAliveTimer;

	public WebsocketKeepalive(String charsetName) {
	}

	public void start(Session userSession) {
		userSession.setMaxIdleTimeout(KEEPALIVE_DELAY + 2 * KEEPALIVE_INTERVAL);
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				if (userSession.isOpen()) {
					// logger.debug("Connection running for " + (System.currentTimeMillis() - startTime) + " ms");
					try {
						userSession.getAsyncRemote().sendText("PING");
					} catch (IllegalArgumentException e) {
						CloseReason cr = new CloseReason(CloseCodes.GOING_AWAY, "Service shutting down");

						e.printStackTrace();
						try {
							userSession.close(cr);
						} catch (IOException e1) {
							logger.error("Error Closing Closed Session " + e.getLocalizedMessage());
						}
					}
				} else {
					try {
						logger.warn("Session is not opened");
						userSession.close(new CloseReason(CloseCodes.CLOSED_ABNORMALLY, "TIMED-OUT"));
						stop();
					} catch (IOException e) {
						logger.warn("Error Closing Closed Session " + e.getLocalizedMessage());
					}
				}
			}
		};
		keepAliveTimer = new Timer("WS-KEEPALIVE-THREAD");
		keepAliveTimer.scheduleAtFixedRate(task, KEEPALIVE_DELAY, KEEPALIVE_INTERVAL);
		logger.debug("Websocket KeepAlive started");
	}

	public void stop() {
		if (keepAliveTimer != null)
			keepAliveTimer.cancel();
		logger.debug("Websocket KeepAlive stoped");
	}
}
