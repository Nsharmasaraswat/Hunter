package com.gtp.hunter.process.wf.taskdecorator.solar;

import com.gtp.hunter.process.service.RegisterService;

public class MovStoreDecorator extends MovDecorator {

	public MovStoreDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}
}