package com.gtp.hunter.process.wf.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.model.Filter;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

public abstract class BaseFilter {

	private Filter model = null;
	
	private Map<String, Object> params = new HashMap<String,Object>();
	
	private List<BaseTrigger> triggers = new ArrayList<BaseTrigger>();

	public BaseFilter(Filter model) {
		this.model = model;
		if(model.getParams() != null) {
			params = JsonUtil.jsonToMap(model.getParams());
		}
	}
	
	public List<BaseTrigger> getTriggers() {
		return triggers;
	}

	public void sendEvent(BaseModelEvent event) {
		if (validate(event)) {
			for (BaseTrigger t : triggers) {
				t.execute(event);
			}
		}
	}

	protected abstract boolean validate(BaseModelEvent event);

	public Filter getModel() {
		return model;
	}

	public void setModel(Filter model) {
		this.model = model;
	}

	public Map<String, Object> getParams() {
		return params;
	}

}
