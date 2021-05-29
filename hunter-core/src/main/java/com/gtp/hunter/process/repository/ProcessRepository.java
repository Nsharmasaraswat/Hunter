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
import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.model.Purpose;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class ProcessRepository extends JPABaseRepository<Process, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager	em;

	@Inject
	private Event<Process>	procEvent;

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public ProcessRepository() {
		super(Process.class, UUID.class);
	}

	public List<Process> listByPurpose(Purpose purp) {
		return em.createQuery("from Process o join fetch o.purposes p where p.id = :purpose", Process.class)
						.setParameter("purpose", purp.getId()).getResultList();
	}

	@Override
	protected Event<Process> getEvent() {
		return procEvent;
	}
}
