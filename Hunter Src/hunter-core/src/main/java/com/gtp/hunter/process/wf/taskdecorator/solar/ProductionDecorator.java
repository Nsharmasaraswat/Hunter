package com.gtp.hunter.process.wf.taskdecorator.solar;

import java.util.stream.Collectors;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;

public class ProductionDecorator extends BaseTaskDecorator {

	public ProductionDecorator(String params, RegisterService rSvc) {
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
		String line = planProd.getFields().stream()
						.filter(df -> df.getField().getMetaname().equals("LINHA_PROD"))
						.map(df -> df.getValue())
						.distinct()
						.collect(Collectors.joining(","));
		String prod = planProd.getItems().stream()
						.filter(di -> di.getProperties().containsKey("PRODUCAO") && di.getProperties().get("PRODUCAO").equals("PRODUCAO"))
						.map(di -> di.getProduct().getSku() + " - " + di.getProduct().getName())
						.distinct()
						.collect(Collectors.joining(","));
		return line + ": " + prod;
	}
}
