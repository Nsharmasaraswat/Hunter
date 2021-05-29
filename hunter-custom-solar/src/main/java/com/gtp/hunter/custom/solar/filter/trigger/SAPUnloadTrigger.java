package com.gtp.hunter.custom.solar.filter.trigger;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

public class SAPUnloadTrigger extends BaseTrigger {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final AtomicLong				count	= new AtomicLong();
	private IntegrationService				is;

	public SAPUnloadTrigger(SAPSolar solar, SAPService svc, IntegrationService is) {
		super(new FilterTrigger());
		this.is = is;
	}

	public SAPUnloadTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		Document apo = (Document) mdl.getModel();

		if (apo != null) {
			logger.info("Unlaod Trigger-" + count.getAndIncrement() + ": " + apo.getCode());
			Document qparent = is.getRegSvc().getDcSvc().quickFindParentDoc(apo.getId());

			if (qparent != null) {
				Document transp = is.getRegSvc().getDcSvc().findById(qparent.getId());

				if (transp != null)
					is.getRegSvc().getAglSvc().sendDocToWMS(transp, "PUT");
				//		apo.setStatus(transp.getStatus());
				//		is.getRegSvc().getDcSvc().persist(apo);
			}
		} else
			logger.info("Should not be null: " + mdl.getClass().getCanonicalName());
		return true;
	}
}
