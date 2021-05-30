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
import com.gtp.hunter.process.model.Origin;
import com.gtp.hunter.process.model.Purpose;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class OriginRepository extends JPABaseRepository<Origin, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager	em;

	@Inject
	private Event<Origin>	oriEvent;

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public OriginRepository() {
		super(Origin.class, UUID.class);
	}

	@SuppressWarnings("unchecked")
	public List<Origin> listByPurpose(Purpose purp) {
		return em.createQuery("from Origin o join fetch o.purposes p where p.id = :purpose").setParameter("purpose", purp.getId()).getResultList();
	}

	@Override
	protected Event<Origin> getEvent() {
		return oriEvent;
	}

}
