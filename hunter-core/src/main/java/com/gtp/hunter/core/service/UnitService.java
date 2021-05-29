package com.gtp.hunter.core.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.core.repository.UnitRepository;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;

@Stateless
public class UnitService {

	@Inject
	private UnitRepository	uRep;

	@Inject
	private PrefixService	pfxSvc;

	public Unit findByTagId(String tagId) {
		return uRep.getUnitByTagId(tagId);
	}

	public Unit findByTypeAndTagId(UnitType type, String tagId) {
		return uRep.findByTypeAndTagId(type, tagId);
	}

	public Unit getUnitById(UUID uId) {
		return uRep.findById(uId);
	}

	@Transactional(value = TxType.REQUIRED)
	public synchronized Unit generateUnit(UnitType type, String strPrefix) {
		String general = ConfigUtil.get("hunter-core", "unit_general_prefix", "000000");
		String fullPrefix = general + strPrefix;
		Prefix prefix = pfxSvc.findNext(fullPrefix, 0);
		String strCount = Long.toHexString(prefix.getCount()).toUpperCase();
		String padding = String.join("", Collections.nCopies(type.getDataSize() - fullPrefix.length() - strCount.length(), "0"));
		String ret = fullPrefix + padding + strCount;
		Unit u = new Unit(ret, type);

		uRep.persist(u);
		return u;
	}

	public Set<Unit> getAllUnitById(Set<UUID> ids) {
		Set<Unit> ret = new HashSet<Unit>();

		ids.forEach(i -> ret.add(uRep.findById(i)));
		return ret;
	}

	public void removeById(UUID id) {
		uRep.removeById(id);
	}

	public Unit findById(UUID uId) {
		return uRep.findById(uId);
	}

	public Unit persist(Unit u) {
		uRep.persist(u);
		return u;
	}

	public List<Unit> listById(Collection<UUID> units) {
		return uRep.listById(units);
	}

}
