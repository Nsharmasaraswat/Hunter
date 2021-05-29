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
import javax.persistence.NoResultException;

import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class DocumentModelFieldRepository extends JPABaseRepository<DocumentModelField, UUID> {

	@Inject
//	@Named("ProcessPersistence")
	private EntityManager				em;

	@Inject
	private Event<DocumentModelField>	dmfEvent;

	public DocumentModelFieldRepository() {
		super(DocumentModelField.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public List<DocumentModelField> listByModelId(UUID docModId) {
		return em.createQuery("from DocumentModelField where model.id = :docmodid", DocumentModelField.class)
						.setParameter("docmodid", docModId)
						.getResultList();
	}

	public List<DocumentModelField> listByModelMetaname(String metaname) {
		return em.createQuery("from DocumentModelField where model.metaname = :metaname", DocumentModelField.class)
						.setParameter("metaname", metaname)
						.getResultList();
	}

	public DocumentModelField findByModelAndMetaname(DocumentModel model, String metaname) {
		try {
			return em.createQuery("from DocumentModelField where model = :docmod and metaname = :metaname", DocumentModelField.class)
							.setParameter("docmod", model)
							.setParameter("metaname", metaname)
							.setMaxResults(1)
							.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public DocumentModelField findByModelIdAndMetaname(UUID id, String metaname) {
		try {
			return em.createQuery("from DocumentModelField where model.id = :docmod and metaname = :metaname", DocumentModelField.class)
							.setParameter("docmod", id.toString())
							.setParameter("metaname", metaname)
							.setMaxResults(1)
							.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	protected Event<DocumentModelField> getEvent() {
		return dmfEvent;
	}

}
