package com.gtp.hunter.core.repository;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.gtp.hunter.core.model.Alert;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class AlertRepository extends JPABaseRepository<Alert, UUID> {

	@Inject
	//	@Named("CorePersistence")
	private EntityManager	em;

	@Inject
	private Event<Alert>	alertEvent;

	public AlertRepository() {
		super(Alert.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Event<Alert> getEvent() {
		return alertEvent;
	}

}
