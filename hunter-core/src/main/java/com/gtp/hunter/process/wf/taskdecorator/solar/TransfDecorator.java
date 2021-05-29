package com.gtp.hunter.process.wf.taskdecorator.solar;

import java.util.stream.Collectors;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;

public class TransfDecorator extends BaseTaskDecorator {

	public TransfDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document planProd) {
		StringBuilder sb = new StringBuilder();
		sb.append(planProd.getName());
		return sb.toString();
	}

	@Override
	public String decorateContent(Document planProd) {
		return planProd.getFields().stream().filter(df -> df.getField().getMetaname().equals("LINHA_PROD")).map(df -> df.getValue()).distinct().collect(Collectors.joining(","));
	}
}
