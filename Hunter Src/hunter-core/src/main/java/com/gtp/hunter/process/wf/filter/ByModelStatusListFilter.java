package com.gtp.hunter.process.wf.filter;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.JsonString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.UUIDAuditModel;
import com.gtp.hunter.process.model.Filter;

public class ByModelStatusListFilter extends BaseFilter {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ByModelStatusListFilter(Filter model) {
		super(model);
	}

	@Override
	protected boolean validate(BaseModelEvent event) {
		try {
			Class<?> c = Class.forName(getModel().getModel());

			if (getParams().containsKey("metaname")) {
				String modelMeta = (String) getParams().get("metaname");
				List<String> statusList = ((List<JsonString>) getParams().get("status-list")).parallelStream().map(js -> js.getString()).collect(Collectors.toList());
				Optional<Method> optModelMethod = Stream.of(c.getDeclaredMethods()).filter(m -> m.getName().equalsIgnoreCase("getModel")).findAny();
				UUIDAuditModel item = (UUIDAuditModel) event.getModel();

				if (optModelMethod.isPresent() && item.getStatus() != null && statusList.contains(item.getStatus())) {
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
