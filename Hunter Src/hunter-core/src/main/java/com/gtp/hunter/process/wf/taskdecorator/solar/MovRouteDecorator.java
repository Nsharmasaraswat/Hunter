package com.gtp.hunter.process.wf.taskdecorator.solar;

import java.util.stream.Collectors;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;

public class MovRouteDecorator extends MovDecorator {

	public MovRouteDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateContent(Document d) {
		String products = d.getTransports().parallelStream()
						.map(dtr -> dtr.getThing())
						.map(t -> {
							String ret = t.getSiblings().stream()
											.map(ts -> ts.getProduct().getSku() + " - " + ts.getProduct().getName())
											.distinct()
											.collect(Collectors.joining(" "));

							ret += " (" + t.getAddress().getName() + ")";
							return ret;
						}).distinct()
						.sorted()
						.collect(Collectors.joining("\r\n"));
		return products;
	}
}