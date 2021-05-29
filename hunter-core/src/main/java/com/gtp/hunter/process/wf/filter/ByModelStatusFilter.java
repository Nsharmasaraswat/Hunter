package com.gtp.hunter.process.wf.filter;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.UUIDAuditModel;
import com.gtp.hunter.process.model.Filter;

public class ByModelStatusFilter extends BaseFilter {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ByModelStatusFilter(Filter model) {
		super(model);
	}

	@Override
	protected boolean validate(BaseModelEvent event) {
		try {
			Class<?> c = Class.forName(getModel().getModel());

			if (getParams().containsKey("metaname")) {
				String modelMeta = (String) getParams().get("metaname");
				String status = (String) getParams().get("status");
				Optional<Method> optModelMethod = Stream.of(c.getDeclaredMethods()).filter(m -> m.getName().equalsIgnoreCase("getModel")).findAny();
				UUIDAuditModel item = (UUIDAuditModel) event.getModel();

				if (optModelMethod.isPresent() && item.getStatus() != null && item.getStatus().equals(status)) {
					String metaname = ((UUIDAuditModel) optModelMethod.get().invoke(event.getModel())).getMetaname();

					return metaname.equals(modelMeta);
				}
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
		}
		return false;
	}

}
