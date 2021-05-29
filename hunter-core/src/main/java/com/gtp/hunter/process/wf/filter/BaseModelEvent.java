package com.gtp.hunter.process.wf.filter;

import javax.enterprise.inject.spi.EventMetadata;

import com.gtp.hunter.core.model.BaseModel;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;

public class BaseModelEvent {

	private BaseModel				model;

	private EventMetadata			metadata;

	private RegisterService			regSvc;

	private RegisterStreamManager	rsm;

	public BaseModelEvent(BaseModel model, EventMetadata metadata, RegisterService rSvc, RegisterStreamManager rsm) {
		this.model = model;
		this.metadata = metadata;
		this.regSvc = rSvc;
		this.rsm = rsm;
	}

	public BaseModel getModel() {
		return model;
	}

	public void setModel(BaseModel model) {
		this.model = model;
	}

	public EventMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(EventMetadata metadata) {
		this.metadata = metadata;
	}

	public RegisterService getRegSvc() {
		return regSvc;
	}

	public RegisterStreamManager getRsm() {
		return rsm;
	}

}
