package com.gtp.hunter.core.repository;

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

import com.gtp.hunter.core.model.Port;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class PortRepository extends JPABaseRepository<Port, UUID> {

	@Inject
//	@Named("CorePersistence")
	private EntityManager	em;

	@Inject
	private Event<Port>		portEvent;

	public PortRepository() {
		super(Port.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	@Deprecated
	public Port findByMetaname(String meta) {
		// TODO Auto-generated method stub
		return null;
	}

	public Port findByMetaname(UUID src, UUID dev, String metaname) {
		Port ret = null;
		List<Port> lstRet = em.createQuery("from Port p join fetch p.device d join fetch d.source s where s.id = :src and d.id = :dev and p.metaname = :meta", Port.class).setParameter("src", src).setParameter("dev", dev).setParameter("meta", metaname).getResultList();
		if (lstRet.size() > 0)
			ret = lstRet.get(0);
		return ret;
	}

	@Override
	protected Event<Port> getEvent() {
		return portEvent;
	}

}
