package com.gtp.hunter.process.wf.filter;

import com.gtp.hunter.core.model.UUIDAuditModel;
import com.gtp.hunter.process.model.Filter;

public class ByStatusFilter extends BaseFilter {
	
	public ByStatusFilter(Filter model) {
		super(model);
	}

	@Override
	protected boolean validate(BaseModelEvent event) {
		UUIDAuditModel item = (UUIDAuditModel) event.getModel();
		if(getParams().containsKey("status")) {
			return getParams().get("status").equals(item.getStatus());
		}
		return false;
	}

}
