package com.gtp.hunter.process.service;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.DashboardWidget;
import com.gtp.hunter.process.repository.DashboardWidgetRepository;

@Stateless
public class DashboardWidgetService {

	@Inject
	private DashboardWidgetRepository dswRep;

	public void remove(DashboardWidget dsw) {
		dswRep.remove(dsw);
	}

	public void removeById(UUID dswId) {
		dswRep.removeById(dswId);
	}

	public void removeByDashboardId(UUID id) {
		dswRep.removeByDashboardId(id);
	}
}
