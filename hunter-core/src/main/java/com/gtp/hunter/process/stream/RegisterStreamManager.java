package com.gtp.hunter.process.stream;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class RegisterStreamManager {

	@Inject
	private OriginStreamManager		osm;

	@Inject
	private ProcessStreamManager	psm;

	@Inject
	private RawDataConsumerManager	rdcm;

	@Inject
	private TaskStreamManager		tsm;

	@Inject
	private FilterStreamManager		fsm;

	@Inject
	private AuditStreamManager		asm;

	@Inject
	private Logger					logger;

	@PostConstruct
	public void init() {
		logger.info("STARTING Register Stream Manager (RSM)");
	}

	public OriginStreamManager getOsm() {
		return osm;
	}

	public ProcessStreamManager getPsm() {
		return psm;
	}

	public RawDataConsumerManager getRdcm() {
		return rdcm;
	}

	public TaskStreamManager getTsm() {
		return tsm;
	}

	public FilterStreamManager getFsm() {
		return fsm;
	}

	public AuditStreamManager getAsm() {
		return asm;
	}
}
