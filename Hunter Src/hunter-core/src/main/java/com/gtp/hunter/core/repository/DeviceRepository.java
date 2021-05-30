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

import com.gtp.hunter.core.model.Device;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class DeviceRepository extends JPABaseRepository<Device, UUID> {

	@Inject
	//	@Named("CorePersistence")
	private EntityManager	em;

	@Inject
	private Event<Device>	deviceEvent;

	public DeviceRepository() {
		super(Device.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	public Device getFullDevice(UUID id) {
		Device ret = null;

		List<Device> lst = em.createQuery("from Device d join fetch d.source where id = :id", Device.class).setParameter("id", id).getResultList();
		if (lst.size() > 0) {
			ret = lst.get(0);
		}

		return ret;
	}

	public List<Device> listFull() {
		return em.createQuery("from Device d left join fetch d.source", Device.class).getResultList();
	}

	@Override
	@Deprecated
	public Device findByMetaname(String meta) {
		return null;
	}

	public Device findByMetaname(UUID src, String meta) {
		Device ret = null;

		List<Device> lst = em.createQuery("from Device d where d.source.id = :id and d.metaname = :meta", Device.class).setParameter("id", src).setParameter("meta", meta).getResultList();
		if (lst.size() > 0) {
			ret = lst.get(0);
		}

		return ret;
	}

	@Override
	protected Event<Device> getEvent() {
		return deviceEvent;
	}

}
