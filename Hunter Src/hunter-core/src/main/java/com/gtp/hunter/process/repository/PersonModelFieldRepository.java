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
import com.gtp.hunter.process.model.PersonModel;
import com.gtp.hunter.process.model.PersonModelField;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class PersonModelFieldRepository extends JPABaseRepository<PersonModelField, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager			em;

	@Inject
	private Event<PersonModelField>	pEvent;

	public PersonModelFieldRepository() {
		super(PersonModelField.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Event<PersonModelField> getEvent() {
		return pEvent;
	}

	public PersonModelField getModelFieldByModelAndMetaname(PersonModel perm, String meta) {
		PersonModelField ret = null;
		List<PersonModelField> lstret = em.createQuery("from PersonModelField where model = :permod and metaname = :metaname", PersonModelField.class)
						.setParameter("permod", perm)
						.setParameter("metaname", meta)
						.getResultList();

		if (lstret.size() > 0)
			ret = lstret.get(0);
		return ret;
	}

}
