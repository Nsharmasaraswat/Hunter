package com.gtp.hunter.process.repository;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.gtp.hunter.core.repository.JPABaseRepository;
import com.gtp.hunter.process.model.Widget;

@Singleton
@ApplicationScoped
@AccessTimeout(value = 90, unit = TimeUnit.SECONDS)
public class WidgetRepository extends JPABaseRepository<Widget, UUID> {

	@Inject
	//	@Named("ProcessPersistence")
	private EntityManager	em;

	@Inject
	private Event<Widget>	widgetEvent;

	public WidgetRepository() {
		super(Widget.class, UUID.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	@Override
	protected Event<Widget> getEvent() {
		return widgetEvent;
	}

}
