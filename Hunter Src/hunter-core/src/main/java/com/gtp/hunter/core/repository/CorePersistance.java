package com.gtp.hunter.core.repository;

import javax.ejb.Singleton;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
public class CorePersistance {

	@Produces
	@PersistenceContext(unitName = "hunter-core", name = "hunter")
	private static EntityManager em;
}
