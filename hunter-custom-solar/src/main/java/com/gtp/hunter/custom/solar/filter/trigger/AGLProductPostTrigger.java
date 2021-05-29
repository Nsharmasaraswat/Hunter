package com.gtp.hunter.custom.solar.filter.trigger;

import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

public class AGLProductPostTrigger extends BaseTrigger {

	public AGLProductPostTrigger() {
		super(new FilterTrigger());
	}
	
	public AGLProductPostTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		return true;
	}

}
