package com.gtp.hunter.custom.solar.filter.trigger;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.worker.ZHWConferenciaCegaTranspWorker;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

public class SAPResendNFTrigger extends BaseTrigger {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final AtomicLong				count	= new AtomicLong();
	private SAPSolar						solar;
	private SAPService						svc;
	private IntegrationService				is;

	public SAPResendNFTrigger(SAPSolar solar, SAPService svc, IntegrationService is) {
		super(new FilterTrigger());
		this.solar = solar;
		this.svc = svc;
		this.is = is;
	}

	public SAPResendNFTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		Document nf = (Document) mdl.getModel();

		try {
			logger.info("Resend NF Trigger-" + count.getAndIncrement() + ": " + nf.getCode());
			Document parent = Hibernate.isInitialized(nf.getParent()) ? nf.getParent() : is.getRegSvc().getDcSvc().findParent(nf);

			new ZHWConferenciaCegaTranspWorker(svc, solar, is).external(parent);
			return true;
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return false;
	}
}
