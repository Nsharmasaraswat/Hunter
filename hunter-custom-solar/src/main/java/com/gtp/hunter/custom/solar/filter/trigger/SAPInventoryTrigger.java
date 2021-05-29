package com.gtp.hunter.custom.solar.filter.trigger;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.worker.ZHWInformacaoInventarioWorker;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

public class SAPInventoryTrigger extends BaseTrigger {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private SAPSolar						solar;
	private SAPService						svc;
	private IntegrationService				is;

	public SAPInventoryTrigger(SAPSolar solar, SAPService svc, IntegrationService is) {
		super(new FilterTrigger());
		this.solar = solar;
		this.svc = svc;
		this.is = is;
	}

	public SAPInventoryTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		Document inventory = (Document) mdl.getModel();
		logger.info("Inventory Trigger: " + inventory.getCode());
		ZHWInformacaoInventarioWorker worker = new ZHWInformacaoInventarioWorker(svc, solar, is);

		worker.external(inventory);
		return true;
	}
}
