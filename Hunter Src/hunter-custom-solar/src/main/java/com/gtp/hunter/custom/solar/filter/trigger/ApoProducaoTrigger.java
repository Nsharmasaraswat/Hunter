package com.gtp.hunter.custom.solar.filter.trigger;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.worker.ZHWApontamentoProducao;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

public class ApoProducaoTrigger extends BaseTrigger {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private SAPSolar						solar;
	private SAPService						svc;
	private IntegrationService				is;

	public ApoProducaoTrigger(SAPSolar solar, SAPService svc, IntegrationService is) {
		super(new FilterTrigger());
		this.solar = solar;
		this.svc = svc;
		this.is = is;
	}

	public ApoProducaoTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		if (ConfigUtil.get("hunter-custom-solar", "apontamento-producao", "FALSE").equalsIgnoreCase("TRUE")) {
			Document apoprd = (Document) mdl.getModel();
			ZHWApontamentoProducao apoPrdWrk = new ZHWApontamentoProducao(svc, solar, is);

			if (apoPrdWrk.external(apoprd))
				logger.info("Apontamento Lançado");
			else
				logger.info("Apontamento Não Lançado");
		}
		return true;
	}
}
