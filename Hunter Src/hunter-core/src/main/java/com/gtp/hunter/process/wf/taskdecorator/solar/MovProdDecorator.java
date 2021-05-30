package com.gtp.hunter.process.wf.taskdecorator.solar;

import java.util.stream.Collectors;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.service.RegisterService;

public class MovProdDecorator extends MovDecorator {

	public MovProdDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document d) {
		StringBuilder ret = new StringBuilder(Documents.getStringField(d, "MOV_TITLE"));

		if (d.getTransports() != null) {
			String dest = d.getTransports().stream()
							.filter(dtr -> dtr.getAddress() != null)
							.map(dtr -> dtr.getAddress().getName())
							.collect(Collectors.joining(","));

			if (!dest.isEmpty())
				ret.append("\r\n(" + dest + ")");
		}
		return ret.toString();
	}
}
