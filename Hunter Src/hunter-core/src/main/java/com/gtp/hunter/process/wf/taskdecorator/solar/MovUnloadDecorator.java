package com.gtp.hunter.process.wf.taskdecorator.solar;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;

public class MovUnloadDecorator extends MovDecorator {

	public MovUnloadDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateContent(Document d) {
		return d.getTransports().size() + " Movimentações";
	}
}
