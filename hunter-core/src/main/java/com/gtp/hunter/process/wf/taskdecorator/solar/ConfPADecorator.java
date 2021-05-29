package com.gtp.hunter.process.wf.taskdecorator.solar;

import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;

public class ConfPADecorator extends BaseTaskDecorator {

	public ConfPADecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document dConf) {
		StringBuilder sb = new StringBuilder();
		String motora = getRegSvc().getCustomSvc().findParentDriver(dConf);

		sb.append(dConf.getCode().replaceAll("[A-Z]", ""));
		sb.append(" - ");
		sb.append(motora);
		return sb.toString();
	}

	@Override
	public String decorateContent(Document dConf) {
		StringBuilder sb = new StringBuilder();

		if (dConf.getParent() != null && Hibernate.isInitialized(dConf.getParent())) {
			Document parent = dConf.getParent();

			sb.append(parent.getFields().stream()
							.filter(df -> df.getField().getMetaname().equals("DOCK") && !df.getValue().isEmpty())
							.map(df -> getRegSvc().getAddSvc().findById(UUID.fromString(df.getValue())).getName())
							.findAny()
							.orElse(""));
		} else
			sb.append(dConf.getItems().stream().map(di -> di.getProduct().getSku() + " - " + di.getProduct().getName()).distinct().collect(Collectors.joining(",")));
		return sb.toString();
	}

}
