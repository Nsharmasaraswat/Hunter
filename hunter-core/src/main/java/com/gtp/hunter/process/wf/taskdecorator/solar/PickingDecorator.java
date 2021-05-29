package com.gtp.hunter.process.wf.taskdecorator.solar;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;

public class PickingDecorator extends BaseTaskDecorator {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public PickingDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document pick) {
		Profiler prof = new Profiler("TaskManager");
		StringBuilder sb = new StringBuilder();

		sb.append(pick.getSiblings()
						.parallelStream()
						.filter(ds -> ds.getModel().getMetaname().equals("OSG"))
						.flatMap(ds -> ds.getFields().parallelStream())
						.filter(df -> df.getField().getMetaname().equals("LOAD_ID"))
						.map(df -> df.getValue())
						.distinct()
						.findAny()
						.orElse(""));
		prof.done("Name Decorated", false, false).forEach(logger::debug);
		return sb.toString();
	}

	@Override
	public String decorateContent(Document pick) {
		StringBuilder sb = new StringBuilder();
		Profiler prof = new Profiler("TaskManager");

		sb.append("Entrega: ");
		sb.append(Documents.getStringField(pick, "DELIVERY_DATE"));
		prof.done("Content Decorated", false, false).forEach(logger::debug);
		return sb.toString();
	}

}
