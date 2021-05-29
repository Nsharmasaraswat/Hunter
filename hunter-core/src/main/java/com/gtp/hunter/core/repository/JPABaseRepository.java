package com.gtp.hunter.core.repository;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.enterprise.event.Event;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import com.gtp.hunter.core.annotation.qualifier.InsertQualifier;
import com.gtp.hunter.core.annotation.qualifier.SuccessQualifier;
import com.gtp.hunter.core.annotation.qualifier.UpdateQualifier;
import com.gtp.hunter.core.model.BaseModel;

public abstract class JPABaseRepository<T extends BaseModel<I>, I> extends BaseRepository<T, I> {

	protected abstract EntityManager getEntityManager();

	private Class<T> persistentClass;
	//	private Class<I>	keyClass;

	protected abstract Event<T> getEvent();

	public JPABaseRepository(Class<T> cls, Class<I> id) {
		this.persistentClass = cls;
		//		this.keyClass = id;
	}

	public List<T> listAll() {
		EntityManager em = getEntityManager();
		return em.createQuery("from " + persistentClass.getSimpleName(), persistentClass).getResultList();
	}

	public List<T> listNewerThan(Date updated) {
		EntityManager em = getEntityManager();
		TypedQuery<T> q = em.createQuery("from " + persistentClass.getSimpleName() + " where updatedAt > :updated  order by updatedAt", persistentClass);

		return q.setParameter("updated", updated, TemporalType.TIMESTAMP).getResultList();
	}

	@Override
	public List<T> listById(Collection<I> idList) {
		if (!idList.isEmpty()) {
			EntityManager em = getEntityManager();
			TypedQuery<T> q = em.createQuery("from " + persistentClass.getSimpleName() + " where id in :ids", persistentClass);

			return q.setParameter("ids", idList).getResultList();
		}
		return new ArrayList<>();
	}

	public T findById(I i) {
		EntityManager em = getEntityManager();

		//		try {
		//			return em.createQuery("from " + persistentClass.getName() + " where id = :fld", this.persistentClass)
		//							.setParameter("fld", i)
		//							.setMaxResults(1)
		//							.setFirstResult(0)
		//							.getSingleResult();
		//		} catch (NoResultException nre) {
		//			//return null;
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
		return em.find(persistentClass, i);
	}

	public T findByMetaname(String meta) {
		EntityManager em = getEntityManager();

		try {
			return em.createQuery("from " + persistentClass.getName() + " where metaname = :fld", this.persistentClass)
							.setParameter("fld", meta)
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

	public T findByField(String fld, Object val) {
		EntityManager em = getEntityManager();

		try {
			return em.createQuery("from " + persistentClass.getName() + " where " + fld + " = :fld", this.persistentClass)
							.setParameter("fld", val)
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

	public T findParent(T entity) {
		if (entity.getId() != null) {
			EntityManager em = getEntityManager();

			try {
				persistentClass.getDeclaredField("parent");

				return em.createQuery("select d from " + persistentClass.getName() + " d join " + persistentClass.getName() + " c on c.parent = d where c.id = :prnt", this.persistentClass)
								.setParameter("prnt", entity.getId())
								.setMaxResults(1)
								.setFirstResult(0)
								.getSingleResult();
			} catch (NoSuchFieldException nsfe) {
				return null;
			} catch (NoResultException nre) {
				//return null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public List<T> listByFieldIn(String fld, List<String> val) {
		EntityManager em = getEntityManager();

		return em.createQuery("from " + persistentClass.getName() + " where " + fld + " in :fld", this.persistentClass).setParameter("fld", val).getResultList();
	}

	public List<T> listByField(String fld, Object val) {
		return getEntityManager().createQuery("from " + persistentClass.getName() + " where " + fld + " = :fld", this.persistentClass)
						.setParameter("fld", val)
						.getResultList();
	}

	public void fireEvent(Annotation a, T t) {
		getEvent().select(new SuccessQualifier()).select(a).fireAsync(t);
	}

	public T persist(T t) {
		return persist(t, true);
	}

	public T persist(T t, boolean fireEvent) {
		EntityManager em = getEntityManager();

		if (t.getId() == null) {
			em.persist(t);
			if (fireEvent) fireEvent(new InsertQualifier(), t);
		} else {
			t = em.merge(t);
			if (fireEvent) fireEvent(new UpdateQualifier(), t);
		}
		return t;
	}

	public void detach(T t) {
		getEntityManager().detach(t);
	}

	public boolean contains(T t) {
		return getEntityManager().contains(t);
	}

	public T refresh(T t) {
		EntityManager em = getEntityManager();

		if (t != null && t.getId() != null) {
			t = em.find(persistentClass, t.getId());

			try {
				em.refresh(t);
			} catch (EntityNotFoundException enfe) {
				System.out.println("EntityNotFoundException " + enfe.getLocalizedMessage());
			}
		}
		return t;
	}

	public void removeByIds(List<I> idList) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaDelete<T> cd = cb.createCriteriaDelete(this.persistentClass);
		Root<T> r = cd.from(this.persistentClass);

		cd.where(r.get("id").in(idList));
		getEntityManager().createQuery(cd).executeUpdate();
	}

	public void removeById(I id) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaDelete<T> cd = cb.createCriteriaDelete(this.persistentClass);
		Root<T> r = cd.from(this.persistentClass);

		cd.where(cb.equal(r.get("id"), id));
		getEntityManager().createQuery(cd).executeUpdate();
	}

	public void remove(T entity) {
		if (entity != null && entity.getId() != null) {
			EntityManager em = getEntityManager();

			if (em.contains(entity))
				em.remove(entity);
			else
				em.remove(em.find(persistentClass, entity.getId()));
		}
	}

	public void flush() {
		getEntityManager().flush();
	}
}
