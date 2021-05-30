package com.gtp.hunter.process.wf.filter;

import com.gtp.hunter.core.model.UUIDAuditModel;
import com.gtp.hunter.process.model.Filter;

//TODO: Check
public class ByMetanameFilter extends BaseFilter {

	//	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ByMetanameFilter(Filter model) {
		super(model);
	}

	@Override
	protected boolean validate(BaseModelEvent event) {
		UUIDAuditModel item = (UUIDAuditModel) event.getModel();

		if (getParams().containsKey("metaname")) {
			return getParams().get("metaname").equals(item.getMetaname());
		}
		return false;
	}

}
