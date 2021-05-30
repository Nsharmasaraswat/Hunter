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
import com.gtp.hunter.process.model.Task;
import com.gtp.hunter.process.model.TaskDef;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class TaskRepository extends JPABaseRepository<Task, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager	em;

	@Inject
	private Event<Task>		taskEvent;

	public TaskRepository() {
		super(Task.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public List<Task> listNewTasksByTaskDef(TaskDef defs) {
		return em.createQuery("from Task t join fetch t.taskdef join fetch t.document where t.status = 'NOVO' and t.taskdef = :defs", Task.class).setParameter("defs", defs).getResultList();
	}

	public Task getFullTask(UUID id) {
		return em.createQuery("from Task t join fetch t.taskdef join fetch t.taskdef.actions join fetch t.document join fetch t.document.itens where t.id = :id", Task.class).setParameter("id", id).getSingleResult();
	}

	@Override
	protected Event<Task> getEvent() {
		return taskEvent;
	}

}
