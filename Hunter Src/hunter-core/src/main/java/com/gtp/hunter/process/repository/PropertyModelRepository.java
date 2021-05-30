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
import com.gtp.hunter.process.model.PropertyModel;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class PropertyModelRepository extends JPABaseRepository<PropertyModel, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager			em;

	@Inject
	private Event<PropertyModel>	pmEvent;

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public PropertyModelRepository() {
		super(PropertyModel.class, UUID.class);
	}

	@Override
	protected Event<PropertyModel> getEvent() {
		return pmEvent;
	}

}
