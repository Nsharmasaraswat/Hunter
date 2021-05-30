package com.gtp.hunter.process.wf.filter;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.process.model.Filter;

public class AllFilter extends BaseFilter {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public AllFilter(Filter model) {
		super(model);
	}

	@Override
	protected boolean validate(BaseModelEvent event) {
		event.getMetadata().getQualifiers().forEach(a -> logger.info(a.toString()));
		return true;
	}

}
