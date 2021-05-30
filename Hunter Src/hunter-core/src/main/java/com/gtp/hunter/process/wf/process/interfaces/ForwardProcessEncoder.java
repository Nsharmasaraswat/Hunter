package com.gtp.hunter.process.wf.process.interfaces;

import javax.json.JsonObject;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.wf.process.BaseProcess;

public interface ForwardProcessEncoder {
	//TODO: Enum?
	String getType();

	JsonObject encodeMessage(BaseProcess proc, ComplexData cd);

	JsonObject encodeMessage(BaseProcess proc, String msg);
}
