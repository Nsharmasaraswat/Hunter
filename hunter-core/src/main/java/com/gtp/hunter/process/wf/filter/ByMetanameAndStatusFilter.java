package com.gtp.hunter.process.wf.filter;

import com.gtp.hunter.core.model.UUIDAuditModel;
import com.gtp.hunter.process.model.Filter;

public class ByMetanameAndStatusFilter extends BaseFilter {

	public ByMetanameAndStatusFilter(Filter model) {
		super(model);
	}

	@Override
	protected boolean validate(BaseModelEvent event) {
		UUIDAuditModel item = (UUIDAuditModel) event.getModel();

		if (getParams().containsKey("metaname") && getParams().containsKey("status")) {
			return getParams().get("metaname").equals(item.getMetaname()) && getParams().get("status").equals(item.getStatus());
		}
		return false;
	}
}
