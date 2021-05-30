package com.gtp.hunter.process.wf.taskdecorator.solar;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;

public class ConfSpaDecorator extends BaseTaskDecorator {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ConfSpaDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
	}

	@Override
	public String decorateName(Document dConf) {
		return dConf.getName();
	}

	@Override
	public String decorateContent(Document dConf) {
		StringBuilder sb = new StringBuilder();
		Profiler prof = new Profiler("TaskManager");

		sb.append(Documents.getStringField(dConf, "LOAD_ID"));
		prof.done("Content Decorated", false, false).forEach(logger::debug);
		return sb.toString();
	}

}
