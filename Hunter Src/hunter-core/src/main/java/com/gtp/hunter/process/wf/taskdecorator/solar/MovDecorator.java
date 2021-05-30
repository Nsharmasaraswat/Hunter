package com.gtp.hunter.process.wf.taskdecorator.solar;

import java.lang.invoke.MethodHandles;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;

public class MovDecorator extends BaseTaskDecorator {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public MovDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document d) {
		return Documents.getStringField(d, "MOV_TITLE");
		//		Document trn = Hibernate.isInitialized(d.getParent()) ? d.getParent() : getRegSvc().getDcSvc().findParent(d);
		//
		//		if (trn != null) {
		//			String cam = trn.getThings().stream()
		//							.map(dt -> dt.getThing())
		//							.filter(t -> t.getUnits().size() > 0)
		//							.flatMap(t -> {
		//								t.setUnitModel(getRegSvc().getUnSvc().getAllUnitById(t.getUnits()));
		//								return t.getUnitModel().stream();
		//							}).filter(u -> u.getType() == UnitType.LICENSEPLATES)
		//							.map(u -> u.getTagId())
		//							.distinct()
		//							.collect(Collectors.joining(","));
		//
		//			String motora = trn.getFields().stream()
		//							.filter(df -> df.getField().getMetaname().equals("DRIVER_ID") && !df.getValue().isEmpty())
		//							.map(df -> getRegSvc().getPsSvc().findById(UUID.fromString(df.getValue())))
		//							.map(p -> p.getName())
		//							.collect(Collectors.joining(","));
		//
		//			if (!cam.isEmpty() && !motora.isEmpty())
		//				return "\r\n" + cam + " (" + motora + ")";
		//			else if (!cam.isEmpty())
		//				return "\r\n" + cam;
		//			else if (!motora.isEmpty())
		//				return "\r\n" + motora;
		//		}
		//		return "MOVIMENTACAO " + d.getCode();
	}

	//TOFIX: VONTADE DE MORRER
	@Override
	public String decorateContent(Document d) {
		String dest = d.getTransports().stream()
						.flatMap(dtr -> dtr.getThing().getSiblings().stream()
										.map(t -> {
											StringBuilder desc = new StringBuilder();

											desc.append(t.getProduct().getSku());
											desc.append(" - ");
											desc.append(t.getProduct().getName());
											desc.append(" (");
											if (dtr.getAddress().getName().contains("."))
												desc.append(dtr.getAddress().getName().substring(0, dtr.getAddress().getName().lastIndexOf(".")));
											else
												desc.append(dtr.getAddress().getName());
											desc.append(")");
											return desc.toString();
										}))
						.distinct()
						.collect(Collectors.joining(","));
		return dest.isEmpty() ? d.getTransports().size() + " Movimentações" : dest;
	}
}
