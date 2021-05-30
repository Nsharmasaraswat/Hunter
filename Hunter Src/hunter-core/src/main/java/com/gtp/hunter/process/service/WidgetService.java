package com.gtp.hunter.process.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.Widget;
import com.gtp.hunter.process.repository.WidgetRepository;

@Stateless
public class WidgetService {

	@Inject
	private WidgetRepository wdgRep;

	public Widget findById(UUID wdgId) {
		return wdgRep.findById(wdgId);
	}

	public Widget persist(Widget wdg) {
		return wdgRep.persist(wdg);
	}

	public List<Widget> listAll() {
		return wdgRep.listAll();
	}

	public void remove(Widget wdg) {
		wdgRep.remove(wdg);
	}

	public void removeById(UUID wdgId) {
		wdgRep.removeById(wdgId);
	}

	public List<Widget> listById(Set<UUID> ids) {
		return wdgRep.listById(ids);
	}
}
