package com.gtp.hunter.custom.solar.filter.trigger;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.worker.ZHWCheckinCheckoutPortariaWorker;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

public class WMSExitTrigger extends BaseTrigger {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private SAPSolar						solar;
	private SAPService						svc;
	private IntegrationService				is;

	public WMSExitTrigger(SAPSolar solar, SAPService svc, IntegrationService is) {
		super(new FilterTrigger());
		this.solar = solar;
		this.svc = svc;
		this.is = is;
	}

	public WMSExitTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		if (ConfigUtil.get("hunter-custom-solar", "checkout-portaria", "FALSE").equalsIgnoreCase("TRUE")) {
			Document apoSaida = (Document) mdl.getModel();
			logger.info("Saída Trigger: " + apoSaida.getCode());
			Document transp = apoSaida.getParent() != null && Hibernate.isInitialized(apoSaida.getParent()) ? apoSaida.getParent() : is.getRegSvc().getDcSvc().findParent(apoSaida);

			if (transp != null) {
				List<Document> nfent = transp.getSiblings().parallelStream()
								.filter(ds -> ds.getModel().getMetaname().equals("NFENTRADA"))
								.collect(Collectors.toList());
				List<Document> nfsai = transp.getSiblings().parallelStream()
								.filter(ds -> ds.getModel().getMetaname().equals("NFSAIDA"))
								.collect(Collectors.toList());

				boolean tkNumOk = nfent.parallelStream()
								.flatMap(ds -> ds.getFields().parallelStream())
								.filter(df -> df.getField().getMetaname().equals("TRANSPORTE_SAP"))
								.allMatch(df -> !df.getValue().isEmpty()) || nfsai.parallelStream()
												.flatMap(ds -> ds.getFields().parallelStream())
												.filter(df -> df.getField().getMetaname().equals("TICKET"))
												.allMatch(df -> !df.getValue().isEmpty());

				if (tkNumOk)
					new ZHWCheckinCheckoutPortariaWorker(svc, solar, is).external(transp);
				else
					is.getRegSvc().getAlertSvc().persist(new Alert(AlertType.WORKFLOW, AlertSeverity.INFO, transp.getCode(), "Notas Fiscais de Entrada: " + nfent.size() + " Notas Fiscais de Saída: " + nfsai.size(), "Não há notas fiscais com transporte SAP."));
			} else
				is.getRegSvc().getAlertSvc().persist(new Alert(AlertType.WORKFLOW, AlertSeverity.ERROR, apoSaida.getCode(), "Não foi possível carregar transporte.", "Documento inválido"));
		}
		return true;
	}
}
