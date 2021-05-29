package com.gtp.hunter.process.stream;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.common.util.MapUtil;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.process.model.Origin;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.origin.BaseOrigin;

@Startup
@Singleton
@DependsOn("RawDataConsumerManager")
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class OriginStreamManager {

	@Inject
	private RegisterService			regSvc;

	@Inject
	private RegisterStreamManager	rsm;

	@Inject
	private transient Logger		logger;

	private Map<UUID, BaseOrigin>	origins	= new HashMap<UUID, BaseOrigin>();
	private Map<String, String>		origmap	= new HashMap<String, String>();

	@PostConstruct
	public void init() {
		logger.info("STARTING Origin Stream Manager (OSM)");
		Profiler p = new Profiler();
		List<Origin> lst = regSvc.getOrgSvc().listAll();

		logger.info(p.step("List All Origins", false));
		lst.stream().forEach(o -> {
			try {
				Constructor<? extends BaseOrigin> c = Class.forName(o.getType()).asSubclass(BaseOrigin.class).getConstructor(RegisterService.class, RegisterStreamManager.class, Origin.class);
				BaseOrigin b = c.newInstance(regSvc, rsm, o);

				origins.put(o.getId(), b);
				origmap.put(o.getId().toString(), o.getName());
				logger.info(p.step("Adicionado Origin " + o.getMetaname(), false));
			} catch (NoSuchMethodException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
			} catch (SecurityException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
			} catch (ClassNotFoundException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
			} catch (InstantiationException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
			} catch (IllegalAccessException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
			} catch (IllegalArgumentException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
			} catch (InvocationTargetException e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
			} catch (NullPointerException e) {
				logger.error(e.getLocalizedMessage());
			}

		});
		p.done("OrginStreamManager inicializado", false, false).forEach(logger::info);
	}

	public Map<String, String> getOrigins() {
		return MapUtil.sortByValue(origmap);
	}

	public BaseOrigin getOrigin(UUID origin) {
		return origins.get(origin);
	}

	// public Map<UUID,BaseOrigin> getStreams() {
	// return origins;
	// }

	public Origin getOriginByMetaname(String metaname) {
		return regSvc.getOrgSvc().findByMetaname(metaname);
	}

	public Origin getOriginById(UUID id) {
		return regSvc.getOrgSvc().findById(id);
	}
}
