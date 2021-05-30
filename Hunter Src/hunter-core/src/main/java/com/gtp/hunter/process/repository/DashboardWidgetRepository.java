package com.gtp.hunter.process.repository;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.DashboardWidget;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class DashboardWidgetRepository extends JPABaseRepository<DashboardWidget, UUID> {

	@Inject
	//	@Named("ProcessPersistence")
	private EntityManager			em;

	@Inject
	private Event<DashboardWidget>	dashboardEvent;

	public DashboardWidgetRepository() {
		super(DashboardWidget.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Event<DashboardWidget> getEvent() {
		return dashboardEvent;
	}

	public void removeByDashboardId(UUID id) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaDelete<DashboardWidget> cd = cb.createCriteriaDelete(DashboardWidget.class);
		Root<DashboardWidget> r = cd.from(DashboardWidget.class);

		cd.where(cb.equal(r.get("dashboard").get("id"), id));
		getEntityManager().createQuery(cd).executeUpdate();
	}

}
