package com.gtp.hunter.process.wf.taskdecorator.solar;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;

public class TranspDecorator extends BaseTaskDecorator {

	public TranspDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String decorateName(Document d1) {
		return getRegSvc().getCustomSvc().findParentLoad(d1) + " - " + getRegSvc().getCustomSvc().findParentDelivery(d1);
	}

	@Override
	public String decorateContent(Document d1) {
		return getRegSvc().getCustomSvc().findParentPlates(d1);
	}
}
