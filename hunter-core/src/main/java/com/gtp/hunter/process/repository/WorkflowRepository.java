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
import com.gtp.hunter.process.model.Workflow;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class WorkflowRepository extends JPABaseRepository<Workflow, UUID> {

	@Inject
	//	@Named("ProcessPersistence")
	private EntityManager	em;

	@Inject
	private Event<Workflow>	wfEvent;

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public WorkflowRepository() {
		super(Workflow.class, UUID.class);
	}

	@Override
	protected Event getEvent() {
		return wfEvent;
	}
}
