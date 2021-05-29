package com.gtp.hunter.process.wf.taskdecorator.solar;

import org.hibernate.Hibernate;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;

public class ConfDecorator extends BaseTaskDecorator {

	public ConfDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document dConf) {
		StringBuilder sb = new StringBuilder(dConf.getParent() != null && Hibernate.isInitialized(dConf.getParent()) ? dConf.getParent().getCode() : dConf.getCode());
		String plates = getRegSvc().getCustomSvc().findParentPlates(dConf);

		if (plates != null && !plates.isEmpty()) {
			sb.append(" - ");
			sb.append(plates);
		}
		return sb.toString();
	}

	@Override
	public String decorateContent(Document dConf) {
		return getRegSvc().getCustomSvc().findParentSupplierCustomer(dConf);
	}

}
