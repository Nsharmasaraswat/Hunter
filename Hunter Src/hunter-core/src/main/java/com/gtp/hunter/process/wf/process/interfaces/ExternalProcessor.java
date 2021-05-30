package com.gtp.hunter.process.wf.process.interfaces;

import com.gtp.hunter.process.wf.process.BaseProcess;
import com.gtp.hunter.ui.json.process.BaseProcessMessage;

public interface ExternalProcessor {
	//TODO: Enum?
	String getType();

	BaseProcessMessage process(BaseProcess proc, Object msg);
}
