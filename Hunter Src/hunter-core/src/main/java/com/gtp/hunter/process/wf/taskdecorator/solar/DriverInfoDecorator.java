package com.gtp.hunter.process.wf.taskdecorator.solar;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;

public class DriverInfoDecorator extends BaseTaskDecorator {

	public DriverInfoDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String decorateName(Document d1) {
		StringBuilder sb = new StringBuilder(getRegSvc().getCustomSvc().findParentPlates(d1));
		String carrier = getRegSvc().getCustomSvc().findParentCarrier(d1);

		if (!carrier.isEmpty()) {
			sb.append(" (");
			sb.append(carrier);
			sb.append(")");
		}
		return sb.toString();
	}

	@Override
	public String decorateContent(Document d1) {
		return getRegSvc().getCustomSvc().findParentDriver(d1);
	}
}
