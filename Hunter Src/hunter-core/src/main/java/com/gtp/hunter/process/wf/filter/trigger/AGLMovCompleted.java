package com.gtp.hunter.process.wf.filter.trigger;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;

public class AGLMovCompleted extends BaseTrigger {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public AGLMovCompleted(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		Document d = (Document) mdl.getModel();

		if (d == null || d.getModel() == null || d.getModel().getMetaname() == null || d.getStatus() == null)
			return false;
		if (d.getModel().getMetaname().equals("ORDMOV") && d.getStatus().equals("SUCESSO")) {
			logger.info(String.format("Document %s Completed with %d transports and %d things", d.getName(), d.getTransports().size(), d.getThings().size()));
		}
		return true;
	}

}