package com.gtp.hunter.custom.solar.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;

//@Startup
//@Singleton
//@Lock(LockType.READ)
//@DependsOn("IntegrationService")
//@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class SAPInvTmp {

	@Inject
	private Logger				logger;

	@Inject
	private IntegrationService	iSvc;

	private final boolean		logimmediately	= false;

	private IntegrationService getISvc() {
		return this.iSvc;
	}

	//	@PostConstruct
	public void init() {
		Executors.newSingleThreadScheduledExecutor().schedule(() -> {
			
		}, 30, TimeUnit.MILLISECONDS);
	}

}
