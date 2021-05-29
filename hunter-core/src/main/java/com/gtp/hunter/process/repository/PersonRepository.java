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
import com.gtp.hunter.process.model.Person;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class PersonRepository extends JPABaseRepository<Person, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager	em;

	@Inject
	private Event<Person>	pEvent;

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Event<Person> getEvent() {
		return pEvent;
	}

	public PersonRepository() {
		super(Person.class, UUID.class);
	}

	public List<Person> getByPropModelAndMetaname(String model, String metaname, String value) {
		return em.createQuery("from Person p where p.model.metaname = :model and p.fields.model.metaname = :metaname and p.fields.value = :value", Person.class)
						.setParameter("model", model)
						.setParameter("metaname", metaname)
						.setParameter("value", model)
						.getResultList();
	}

	public Person getByModelAndCode(String model, String code) {
		List<Person> pList = em.createQuery("from Person p where p.model.metaname = :model and p.code = :code", Person.class)
						.setParameter("model", model)
						.setParameter("code", code)
						.getResultList();
		return pList.isEmpty() ? null : pList.get(0);
	}

	public List<Person> listByModelMetaname(String modelMeta) {
		return em.createQuery("from Person p where p.model.metaname = :model", Person.class)
						.setParameter("model", modelMeta)
						.getResultList();
	}
}
