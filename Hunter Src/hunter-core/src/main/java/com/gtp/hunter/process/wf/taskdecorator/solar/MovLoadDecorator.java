package com.gtp.hunter.process.wf.taskdecorator.solar;

import java.util.stream.Collectors;

import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;

public class MovLoadDecorator extends MovDecorator {

	public MovLoadDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateContent(Document d) {
		String products = d.getTransports().stream()
						.map(dtr -> dtr.getThing())
						.map(t -> t.getSiblings().stream()
										.map(ts -> {
											StringBuilder name = new StringBuilder();

											if (ts.getProduct() != null) {
												name.append(ts.getProduct().getSku());
												name.append(" - ");
												name.append(ts.getProduct().getName());
											}
											if (t.getAddress() != null) {
												Address quickp = getRegSvc().getAddSvc().quickFindParent(t.getAddress().getId());

												if (quickp != null) {
													name.append(" (");
													name.append(quickp.getName());
													name.append(")");
												}
											}
											return name;
										})
										.distinct()
										.collect(Collectors.joining(" ")))
						.distinct()
						.sorted()
						.collect(Collectors.joining("\r\n"));

		return products;
	}
}
