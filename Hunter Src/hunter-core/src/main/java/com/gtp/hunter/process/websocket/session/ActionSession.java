package com.gtp.hunter.process.websocket.session;

import java.lang.invoke.MethodHandles;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.websocket.BaseSession;

public class ActionSession extends BaseSession<Object> {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ActionSession(Session ss) {
		super(ss);
	}

	//Log Messages
	@Override
	public void onNext(Object msg) {
		if (isOnline()) {
			if (msg instanceof String) {
				String m = (String) msg;
				if (!(m.equals("PONG") || m.equals("PING") || (m.contains("tagId") && m.contains("type") && m.contains("port") && m.contains("device") && m.contains("source") && m.contains("payload"))))
					logger.info(getGson().toJson(msg));
			}
			super.onNext(msg);
		}
	}

}
