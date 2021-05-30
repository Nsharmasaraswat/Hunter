package com.gtp.hunter.process.wf.taskdecorator;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;

public abstract class BaseTaskDecorator {

	private String			params;

	private RegisterService	regSvc;

	public BaseTaskDecorator(String params, RegisterService rSvc) {
		this.params = params;
		this.regSvc = rSvc;
	}

	public abstract String decorateName(Document d);

	public abstract String decorateContent(Document d);

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	protected RegisterService getRegSvc() {
		return regSvc;
	}

	protected String getFieldValue(Document d, String metaname) {
		return d == null ? "---" : d.getFields().stream()
						.filter(df -> df.getField().getMetaname().equals(metaname))
						.map(df -> df.getValue())
						.findFirst()
						.orElse("");
	}

}
