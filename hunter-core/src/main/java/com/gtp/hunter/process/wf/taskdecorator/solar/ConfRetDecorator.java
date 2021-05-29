package com.gtp.hunter.process.wf.taskdecorator.solar;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;

public class ConfRetDecorator extends BaseTaskDecorator {

	public ConfRetDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document dConf) {
		return dConf.getName();
	}

	@Override
	public String decorateContent(Document dConf) {
		StringBuilder sb = new StringBuilder();

		sb.append(Documents.getStringField(dConf, "LOAD_ID"));
		return sb.toString();
	}
}
