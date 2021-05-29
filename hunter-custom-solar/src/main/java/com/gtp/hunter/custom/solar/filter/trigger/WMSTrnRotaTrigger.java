package com.gtp.hunter.custom.solar.filter.trigger;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.worker.ZHWCheckoutFaturado;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

public class WMSTrnRotaTrigger extends BaseTrigger {

	private transient static final Logger	logger		= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final List<UUID>			processing	= new CopyOnWriteArrayList<>();
	private IntegrationService				iSvc;
	private ZHWCheckoutFaturado				rfc;

	public WMSTrnRotaTrigger(SAPSolar solar, SAPService svc, IntegrationService iSvc) {
		super(new FilterTrigger());
		this.iSvc = iSvc;
		this.rfc = new ZHWCheckoutFaturado(svc, solar, iSvc);
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> processing.clear(), 1, 1, TimeUnit.HOURS);
	}

	public WMSTrnRotaTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		Document transport = (Document) mdl.getModel();
		String serviceType = Documents.getStringField(transport, "SERVICE_TYPE");

		logger.info("Liberação de Rota Trigger: " + transport.getCode());
		if (!processing.contains(transport.getId())) {
			processing.add(transport.getId());
			if (serviceType.equals("ROTA")) {
				if (rfc.external(transport))
					return true;
				else
					return false;
			} else
				iSvc.getRegSvc().getAlertSvc().persist(new Alert());
		} else {
			iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.WARNING, transport.getCode(), "Transport " + transport.getCode() + " status " + transport.getStatus(), "Transporte já em Liberação de Carregamento"));
		}
		return false;
	}
}
