package com.gtp.hunter.core.websocket.source;

import javax.inject.Inject;
import javax.websocket.Session;

import org.slf4j.Logger;

import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.websocket.BaseSession;

public class ConfigurationSession extends BaseSession<Command> {
	@Inject
	private static transient Logger logger;

	public ConfigurationSession(Session ss) {
		super(ss);
	}

	@Override
	public void onNext(Command msg) {
		logger.debug("CONFIGURATIONNNNNN!!!!");
	}
}
