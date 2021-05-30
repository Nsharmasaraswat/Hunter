package com.gtp.hunter.process.stream;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.core.annotation.AuditEvent;

@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class AuditStreamManager {

	@Inject
	private transient Logger logger;

	@PostConstruct
	public void init() {
		logger.info("STARTING Audit Stream Manager (ASM)");
	}

	public void observeAuditEvent(@ObservesAsync @AuditEvent Object obj) {
		logger.info("AUDITSTREAMMANAGER: " + obj.getClass());
	}

}
