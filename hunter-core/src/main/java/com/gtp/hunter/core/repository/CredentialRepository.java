package com.gtp.hunter.core.repository;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.gtp.hunter.core.model.Credential;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class CredentialRepository extends JPABaseRepository<Credential, UUID> {

	@Inject
	//	@Named("CorePersistence")
	private EntityManager		em;

	@Inject
	private Event<Credential>	credentialEvent;

	public CredentialRepository() {
		super(Credential.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Event<Credential> getEvent() {
		return credentialEvent;
	}

	public Credential findByLogin(String login) {
		EntityManager em = getEntityManager();

		try {
			return em.createQuery("from Credential where login = :login", Credential.class)
							.setParameter("login", login)
							.setMaxResults(1)
							.setFirstResult(0)
							.getSingleResult();
		} catch (NoResultException nre) {
			//return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
