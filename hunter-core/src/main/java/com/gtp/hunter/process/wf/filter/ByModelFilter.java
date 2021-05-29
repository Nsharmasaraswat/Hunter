package com.gtp.hunter.process.wf.filter;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.process.model.Filter;

public class ByModelFilter extends BaseFilter {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ByModelFilter(Filter model) {
		super(model);
	}

	@Override
	protected boolean validate(BaseModelEvent event) {
		try {
			Class<?> c = Class.forName(getModel().getModel());

			if (getParams().containsKey("metaname")) {
				String modelMeta = (String) getParams().get("metaname");
				Optional<Method> optMethod = Stream.of(c.getDeclaredMethods()).filter(m -> m.getName().equalsIgnoreCase("getModel")).findAny();

				if (optMethod.isPresent()) {
					String metaname = (String) optMethod.get().invoke(event.getModel());

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
