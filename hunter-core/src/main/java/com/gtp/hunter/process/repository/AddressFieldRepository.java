package com.gtp.hunter.process.repository;

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

import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.AddressField;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class AddressFieldRepository extends JPABaseRepository<AddressField, UUID> {

	@Inject
	//	@Named("ProcessPersistence")
	private EntityManager		em;

	@Inject
	private Event<AddressField>	fEvent;

	public AddressFieldRepository() {
		super(AddressField.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Event<AddressField> getEvent() {
		return fEvent;
	}

	public AddressField findRandomByModelMetaValue(String modelFieldMeta, String value) {
		EntityManager em = getEntityManager();

		try {
			return em.createQuery("from AddressField where model.metaname = :fld and value = :val order by rand()", AddressField.class)
							.setParameter("fld", modelFieldMeta)
							.setParameter("val", value)
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

	public List<AddressField> listByModelMetaValue(String modelFieldMeta, String value) {
		EntityManager em = getEntityManager();

		try {
			return em.createQuery("from AddressField where model.metaname = :fld and UPPER(value) = UPPER(:val)", AddressField.class)
							.setParameter("fld", modelFieldMeta)
							.setParameter("val", value)
							.getResultList();
		} catch (NoResultException nre) {
			//return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}