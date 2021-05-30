package com.gtp.hunter.custom.solar.filter.trigger;

import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.worker.ZHWTransferenciaWorker;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

public class SAPTransfTrigger extends BaseTrigger {

	private SAPSolar			solar;
	private SAPService			svc;
	private IntegrationService	is;

	public SAPTransfTrigger(SAPSolar solar, SAPService svc, IntegrationService is) {
		super(new FilterTrigger());
		this.solar = solar;
		this.svc = svc;
		this.is = is;
	}

	public SAPTransfTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		Document ordTransf = (Document) mdl.getModel();

		if (ordTransf.getStatus().equals("SUCESSO")) {
			ZHWTransferenciaWorker worker = new ZHWTransferenciaWorker(svc, solar, is);
			worker.external(ordTransf);
		}
		return true;
	}
}
