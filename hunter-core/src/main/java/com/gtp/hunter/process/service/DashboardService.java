package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Dashboard;
import com.gtp.hunter.process.repository.DashboardRepository;

@Stateless
public class DashboardService {

	@Inject
	private DashboardRepository dshRep;

	public Dashboard findById(UUID dshId) {
		return dshRep.findById(dshId);
	}

	public Dashboard persist(Dashboard dsh) {
		return dshRep.persist(dsh);
	}

	public List<Dashboard> listAll() {
		return dshRep.listAll();
	}

	public void remove(Dashboard dsh) {
		dshRep.remove(dsh);
	}

	public void removeById(UUID dshId) {
		dshRep.removeById(dshId);
	}

	public Dashboard findByUser(User us) {
		return dshRep.findByField("user", us);
	}
}
