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
import javax.persistence.NoResultException;

import com.gtp.hunter.core.model.User;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class UserRepository extends JPABaseRepository<User, UUID> {

	@Inject
	//	@Named("CorePersistence")
	private EntityManager	em;

	@Inject
	private Event<User>		userEvent;

	public UserRepository() {
		super(User.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Event<User> getEvent() {
		return userEvent;
	}

	public List<User> listByGroup(String groupMeta) {
		return em.createQuery("from User us where us.groups = :model in ELEMENTS(groups);", User.class)
						.setParameter("model", groupMeta)
						.getResultList();
	}

	public User findByProperty(String key, String value) {
		User ret = null;

		try {
			ret = em.createQuery("from User us where :key in (KEY(us.properties)) and :val in (VALUE(us.properties))", User.class)
							.setParameter("key", key)
							.setParameter("val", value)
							.setMaxResults(1)
							.setFirstResult(0)
							.getSingleResult();
		} catch (NoResultException nre) {
			//return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
}
