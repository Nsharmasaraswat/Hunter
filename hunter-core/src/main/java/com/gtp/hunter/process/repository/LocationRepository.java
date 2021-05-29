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
import com.gtp.hunter.process.model.Location;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class LocationRepository extends JPABaseRepository<Location, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager	em;

	@Inject
	private Event<Location>	lEvent;

	public LocationRepository() {
		super(Location.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Event<Location> getEvent() {
		return lEvent;
	}

	public List<?> getAllteste() {

		return null;

	}

}