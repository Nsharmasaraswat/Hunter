package com.gtp.hunter.process.repository;

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
import com.gtp.hunter.process.model.FilterTrigger;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class FilterTriggerRepository extends JPABaseRepository<FilterTrigger, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager			em;

	@Inject
	private Event<FilterTrigger>	fEvent;

	public FilterTriggerRepository() {
		super(FilterTrigger.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Event<FilterTrigger> getEvent() {
		return fEvent;
	}

}
