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
import com.gtp.hunter.process.model.Purpose;
import com.gtp.hunter.process.model.TaskDef;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class TaskDefRepository extends JPABaseRepository<TaskDef, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager	em;

	@Inject
	private Event<TaskDef>	tdEvent;

	public TaskDefRepository() {
		super(TaskDef.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@SuppressWarnings("unchecked")
	public List<TaskDef> getTaskDefsByPermissions(List<UUID> perms) {
		perms.forEach(System.out::println);
		return em.createQuery("from TaskDef t join fetch t.permissions p where p.permission in :perms")
						.setParameter("perms", perms).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<TaskDef> listByPurpose(Purpose purp) {

		List<TaskDef> list = em.createQuery("from TaskDef o join fetch o.purposes p where p.id = :purpose")
						.setParameter("purpose", purp.getId()).getResultList();
		return list;
	}

	@Override
	protected Event<TaskDef> getEvent() {
		return tdEvent;
	}

}
