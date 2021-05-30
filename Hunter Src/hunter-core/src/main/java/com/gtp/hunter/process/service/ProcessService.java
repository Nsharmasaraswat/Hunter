package com.gtp.hunter.process.service;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.Process;
import com.gtp.hunter.process.repository.ProcessRepository;

@Stateless
public class ProcessService {

	@Inject
	private ProcessRepository prcRep;

	public Process findById(UUID id) {
		return prcRep.findById(id);
	}
}
