package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.Origin;
import com.gtp.hunter.process.repository.OriginRepository;

@Stateless
public class OriginService {

	@Inject
	private OriginRepository oRep;

	public List<Origin> listAll() {
		return oRep.listAll();
	}

	public Origin findByMetaname(String metaname) {
		return oRep.findByMetaname(metaname);
	}

	public Origin findById(UUID id) {
		return oRep.findById(id);
	}
}
