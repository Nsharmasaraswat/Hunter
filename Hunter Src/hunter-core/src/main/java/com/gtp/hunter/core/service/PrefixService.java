package com.gtp.hunter.core.service;

import java.util.Collections;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.core.repository.PrefixRepository;

@Stateless
public class PrefixService {

	@Inject
	private PrefixRepository pfxRep;

	@Transactional(value = TxType.REQUIRES_NEW)
	public Prefix findNext(String prefix, int padCount) {
		Prefix pfx = pfxRep.findByField("prefix", prefix);

		if (pfx == null) pfx = new Prefix(prefix, 0L);
		pfx.setCount(pfx.getCount() + 1);
		pfx.setCode(String.join("", Collections.nCopies(Math.max(padCount, String.valueOf(pfx.getCount()).length()) - String.valueOf(pfx.getCount()).length(), "0")) + String.valueOf(pfx.getCount()));
		pfxRep.persist(pfx);

		return pfx;
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public Prefix rollBack(Prefix pfx) {
		pfx.setCount(pfx.getCount() - 1);
		pfxRep.persist(pfx);
		return pfx;
	}

	@Transactional(value = TxType.REQUIRED)
	public Prefix persist(Prefix prefix) {
		pfxRep.persist(prefix);
		return prefix;
	}
}
