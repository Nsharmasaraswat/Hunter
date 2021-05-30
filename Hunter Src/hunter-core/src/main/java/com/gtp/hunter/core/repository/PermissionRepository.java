package com.gtp.hunter.core.repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.gtp.hunter.core.model.Permission;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class PermissionRepository extends JPABaseRepository<Permission, UUID> {

	@Inject
	//	@Named("CorePersistence")
	private EntityManager		em;

	@Inject
	private Event<Permission>	permissionEvent;

	public PermissionRepository() {
		super(Permission.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Event<Permission> getEvent() {
		return permissionEvent;
	}

	public List<Permission> listByGroup(String groupMeta) {
		return em.createQuery("from Permission where groups.metaname = :model in ELEMENTS(groups);", Permission.class)
						.setParameter("model", groupMeta)
						.getResultList();
	}

	public List<Permission> listByUserId(UUID usId) {
		return em.createQuery("from Permission where users.id = :model in ELEMENTS(groups);", Permission.class)
						.setParameter("model", usId.toString())
						.getResultList();
	}
}
