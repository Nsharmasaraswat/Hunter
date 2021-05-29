package com.gtp.hunter.process.service;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.repository.ActionRepository;

@Stateless
public class ActionService {

	@Inject
	private ActionRepository actRep;

	public Action findById(UUID actId) {
		return actRep.findById(actId);
	}
}
