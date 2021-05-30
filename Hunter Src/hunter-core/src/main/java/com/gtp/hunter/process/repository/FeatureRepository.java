package com.gtp.hunter.process.repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.Feature;
import com.gtp.hunter.process.model.Origin;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class FeatureRepository extends JPABaseRepository<Feature, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager	em;

	@Inject
	private Event<Feature>	fEvent;

	public FeatureRepository() {
		super(Feature.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	@Deprecated
	public Feature findByMetaname(String meta) {
		return super.findByMetaname(meta);
	}

	public Feature findByMetaname(Origin origin, String meta) {
		Feature ret = null;
		List<Feature> lst = em.createQuery("from Feature f where f.origin = :ori and metaname = :meta", Feature.class).setParameter("ori", origin).setParameter("meta", meta).getResultList();
		if (lst.size() > 0) {
			ret = lst.get(0);
		}
		return ret;
	}

	@Override
	protected Event<Feature> getEvent() {
		return fEvent;
	}

}
