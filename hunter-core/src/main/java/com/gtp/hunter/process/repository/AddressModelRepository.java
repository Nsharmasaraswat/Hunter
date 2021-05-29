package com.gtp.hunter.process.repository;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.AddressModel;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class AddressModelRepository extends JPABaseRepository<AddressModel, UUID> {

	@Inject
	//	@Named("ProcessPersistence")
	private EntityManager		em;

	@Inject
	private Event<AddressModel>	fEvent;

	public AddressModelRepository() {
		super(AddressModel.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Event<AddressModel> getEvent() {
		return fEvent;
	}

}
