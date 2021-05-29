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
import com.gtp.hunter.process.model.Action;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class ActionRepository extends JPABaseRepository<Action, UUID> {

	@Inject
	//	@Named("ProcessPersistence")
	private EntityManager	em;

	@Inject
	private Event<Action>	actionEvent;

	public ActionRepository() {
		super(Action.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public Action getFullAction(UUID id) {
		return (Action) em.createQuery("from Action a join fetch a.taskdef where a.id = :id").setParameter("id", id).getSingleResult();
	}

	@Override
	protected Event<Action> getEvent() {
		return actionEvent;
	}

}
