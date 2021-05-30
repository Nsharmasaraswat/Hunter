package com.gtp.hunter.custom.eurofarma.process;

import java.io.StringReader;
import java.util.Calendar;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.custom.eurofarma.json.BaseMessage;
import com.gtp.hunter.custom.eurofarma.json.CPIIntegrationMessage;
import com.gtp.hunter.custom.eurofarma.json.TagReadMessage;
import com.gtp.hunter.process.model.Origin;
import com.gtp.hunter.process.wf.process.BaseProcess;
import com.gtp.hunter.process.wf.process.interfaces.ForwardProcessEncoder;

public class PortalForwardEncoder<T extends CPIIntegrationMessage<S>, S extends BaseMessage> implements ForwardProcessEncoder {
	private static final String _TYPE = "PORTAL";

	@Override
	public JsonObject encodeMessage(BaseProcess proc, ComplexData cd) {
		JsonObject ret = null;
		JsonReader jsonReader = Json.createReader(new StringReader(buildMessage(proc, cd).toString()));

		ret = jsonReader.readObject();
		jsonReader.close();
		return ret;
	}

	@Override
	public JsonObject encodeMessage(BaseProcess proc, String s) {
		return null;
	}

	//TODO: Return T
	private CPIIntegrationMessage<TagReadMessage> buildMessage(BaseProcess proc, ComplexData cd) {
		CPIIntegrationMessage<TagReadMessage> cm = new CPIIntegrationMessage<>();
		TagReadMessage trm = new TagReadMessage();
		Origin o = proc.getModel().getOrigin();

		trm.setCodigo(cd.getTagId());
		trm.setData(Calendar.getInstance().getTime());
		trm.setPortal(o.getId());
		cm.setCommand(o.getParams());
		cm.setData(trm);
		return cm;
	}

	@Override
	public String getType() {
		return _TYPE;
	}
}
