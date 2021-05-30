package com.gtp.hunter.process.wf.taskdecorator.solar;

import java.util.stream.Collectors;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;

public class ResupplyDecorator extends BaseTaskDecorator {

	public ResupplyDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document d) {
		return "REABASTECIMENTO DE PICKING";
	}

	@Override
	public String decorateContent(Document d) {
		return d.getTransports().stream()
						.map(dtr -> {
							StringBuilder sb = new StringBuilder();
							sb.append(dtr.getThing().getSiblings().stream()
											.map(t -> t.getProduct().getSku() + " - " + t.getProduct().getName())
											.distinct()
											.collect(Collectors.joining(",")));
							sb.append("(");
							if (d.getStatus().equals("RESUPPLY"))
								sb.append(dtr.getAddress().getName());
							else if (d.getStatus().equals("PICKING"))
								sb.append(dtr.getOrigin().getName());
							sb.append(")");
							return sb.toString();
						})
						.distinct()
						.collect(Collectors.joining(","));
	}
}
