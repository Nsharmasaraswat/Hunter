package com.gtp.hunter.process.wf.filter.trigger;

import java.util.HashMap;
import java.util.Map;

import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;

public abstract class BaseTrigger {

	private FilterTrigger		model;

	private Map<String, Object>	params	= new HashMap<String, Object>();

	public BaseTrigger(FilterTrigger model) {
		this.model = model;
		if (model.getParams() != null) {
			params = JsonUtil.jsonToMap(model.getParams());
		}
	}

	public abstract boolean execute(BaseModelEvent mdl);

	public FilterTrigger getModel() {
		return this.model;
	}

	public Map<String, Object> getParams() {
		return params;
	}
}
