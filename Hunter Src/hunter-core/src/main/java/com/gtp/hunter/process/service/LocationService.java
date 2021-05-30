package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.Location;
import com.gtp.hunter.process.repository.LocationRepository;

@Stateless
public class LocationService {

	@Inject
	private LocationRepository locRep;

	public void delete(Location loc) {
		if (loc != null && loc.getStatus() != null && !loc.getStatus().equals("CANCELADO")) {
			loc.setStatus("CANCELADO");
			locRep.persist(loc);
		}
	}

	public void deleteById(UUID id) {
		if (id != null) delete(findById(id));
	}

	public Location findById(UUID id) {
		return locRep.findById(id);
	}

	public Location findByMetaname(String metaname) {
		return locRep.findByMetaname(metaname);
	}

	public List<Location> listAll() {
		return locRep.listAll();
	}

	public Location persist(Location location) {
		return locRep.persist(location);
	}

	public void removeById(UUID id) {
		locRep.removeById(id);
	}

}
