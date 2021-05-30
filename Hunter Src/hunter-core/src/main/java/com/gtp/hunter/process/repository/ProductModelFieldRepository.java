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
import javax.persistence.Query;

import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.ProductModelField;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class ProductModelFieldRepository extends JPABaseRepository<ProductModelField, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager				em;

	@Inject
	private Event<ProductModelField>	pmfEvent;

	public ProductModelFieldRepository() {
		super(ProductModelField.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@SuppressWarnings("unchecked")
	public List<ProductModelField> listByModelId(UUID prodModId) {
		Query q = em.createQuery("from ProductModelField where model.id = :prodmodid").setParameter("prodmodid",
						prodModId);
		return q.getResultList();
	}

	@Override
	protected Event<ProductModelField> getEvent() {
		return pmfEvent;
	}
}
