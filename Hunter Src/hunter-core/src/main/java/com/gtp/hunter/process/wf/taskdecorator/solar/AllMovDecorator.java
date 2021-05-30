package com.gtp.hunter.process.wf.taskdecorator.solar;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;

public class AllMovDecorator extends MovDecorator {

	public AllMovDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document d) {
		String superName = super.decorateName(d);
		String baseName = "MOVIMENTACAO " + d.getCode();

		if (superName.equals(baseName))
			return baseName;

		return baseName + " " + superName;
	}
}
