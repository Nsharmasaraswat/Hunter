package com.gtp.hunter.ui.json.process.drone;

import com.gtp.hunter.ui.json.process.BaseProcessMessage;

public class ProcessMessage extends BaseProcessMessage {
	public ProcessMessage() {
		super("NO-OP");
	}

	public ProcessMessage(String cmd) {
		super(cmd);
	}
}
