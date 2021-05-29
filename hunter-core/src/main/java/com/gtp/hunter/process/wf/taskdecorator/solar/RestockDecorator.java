package com.gtp.hunter.process.wf.taskdecorator.solar;

import java.util.stream.Collectors;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;

public class RestockDecorator extends BaseTaskDecorator {

	public RestockDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document d) {
		return "FÁBRICA: REABASTECIMENTO DE LINHA";
	}

	@Override
	public String decorateContent(Document d) {
		return d.getTransports().stream()
						.flatMap(dtr -> dtr.getThing().getSiblings().stream())
						.map(t -> t.getProduct().getSku() + " - " + t.getProduct().getName())
						.distinct()
						.collect(Collectors.joining(","));
	}
}
