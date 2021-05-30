package com.gtp.hunter.process.wf.taskdecorator;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;

public class ContentSizeDecorator extends BaseTaskDecorator {

	public ContentSizeDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document d) {
		return d.getName();
	}

	@Override
	public String decorateContent(Document d) {
		return Integer.toString(d.getItems().size());
	}
}
