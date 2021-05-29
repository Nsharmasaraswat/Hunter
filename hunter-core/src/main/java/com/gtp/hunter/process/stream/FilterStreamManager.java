package com.gtp.hunter.process.stream;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.ObservesAsync;
import javax.enterprise.inject.spi.EventMetadata;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.core.model.BaseModel;
import com.gtp.hunter.process.model.Filter;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.repository.FilterRepository;
import com.gtp.hunter.process.repository.FilterTriggerRepository;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.filter.BaseFilter;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class FilterStreamManager {

	@Inject
	private transient Logger				logger;

	@Inject
	private FilterRepository				fRep;

	@Inject
	private FilterTriggerRepository			ftRep;

	@Inject
	private RegisterService					regSvc;

	@Inject
	private RegisterStreamManager			rsm;

	//	private Executor						exec		= Executors.newCachedThreadPool();

	private Map<UUID, BaseFilter>			docs		= new HashMap<UUID, BaseFilter>();

	private Map<Class, List<BaseFilter>>	listeners	= new HashMap<Class, List<BaseFilter>>();

	@PostConstruct
	public void init() {
		logger.info("STARTING Filter Stream Manager (FSM)");
		List<Filter> lst = fRep.listAll();
		for (Filter f : lst) {
			addFilter(f);
		}
	}

	public void getEvent(@ObservesAsync BaseModel<UUID> event, EventMetadata metadata) {
		if (listeners.containsKey(event.getClass())) {
			List<BaseFilter> lst = listeners.get(event.getClass());

			for (BaseFilter flt : lst) {
				BaseModelEvent evt = new BaseModelEvent(event, metadata, regSvc, rsm);

				flt.sendEvent(evt);
			}
		}
	}

	public void addFilter(Filter f) {
		try {
			Constructor c1 = Class.forName(f.getBasefilter()).getConstructor(Filter.class);
			BaseFilter bf = (BaseFilter) c1.newInstance(f);
			List<FilterTrigger> lstFt = ftRep.listByField("filter", f);

			for (FilterTrigger trg : lstFt) {
				try {
					logger.info("Carregando Trigger: " + trg.getClasse());
					Constructor<BaseTrigger> c = (Constructor<BaseTrigger>) Class.forName(trg.getClasse()).getConstructor(FilterTrigger.class);
					BaseTrigger bt = c.newInstance(trg);

					bf.getTriggers().add(bt);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			registerBaseFilter(Class.forName(f.getModel()), bf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void registerBaseFilter(Class cls, BaseFilter bf) {
		if (!listeners.containsKey(cls)) {
			listeners.put(cls, new ArrayList<BaseFilter>());
		}
		listeners.get(cls).add(bf);
		logger.info("Adding " + cls.getSimpleName() + " Listener " + bf.hashCode() + " Listeners: " + listeners.get(cls).size());
	}

	public void unRegisterBaseFilter(Class cls, BaseFilter bf) {
		if (listeners.containsKey(cls)) {
			listeners.get(cls).remove(bf);
			logger.info("Removing " + cls.getSimpleName() + " Listener " + bf.hashCode() + " Listeners: " + listeners.get(cls).size());
		}
	}

}
