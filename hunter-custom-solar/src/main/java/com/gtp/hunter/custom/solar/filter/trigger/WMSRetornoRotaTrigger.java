package com.gtp.hunter.custom.solar.filter.trigger;

import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.sap.worker.ZHWProntaEntregaWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWRecusaDocWorker;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

public class WMSRetornoRotaTrigger extends BaseTrigger {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private SAPSolar						solar;
	private SAPService						svc;
	private IntegrationService				is;

	public WMSRetornoRotaTrigger(SAPSolar solar, SAPService svc, IntegrationService is) {
		super(new FilterTrigger());
		this.solar = solar;
		this.svc = svc;
		this.is = is;
	}

	public WMSRetornoRotaTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		ZHWProntaEntregaWorker peWorker = new ZHWProntaEntregaWorker(svc, solar, is);
		ZHWRecusaDocWorker rdWorker = new ZHWRecusaDocWorker(svc, solar, is);
		Document apoCheckinPortaria = (Document) mdl.getModel();
		Document transp = is.getRegSvc().getDcSvc().findParent(apoCheckinPortaria);
		SAPReadStartDTO rStart = new SAPReadStartDTO();
		String code = new SimpleDateFormat("'RR'yyyyMMdd").format(Calendar.getInstance().getTime());
		Document tmpRet = is.getRegSvc().getDcSvc().quickFindByCodeAndModelMetaname(code, "RROTA");

		logger.info("RetornoDeRota Trigger: " + transp.getCode());
		is.getRegSvc().getAlertSvc().persist(new Alert(AlertType.PROCESS, AlertSeverity.INFO, transp.getCode(), "RETORNO DE ROTA", "Gerar Notas Recusadas e Venda Pronta-Entrega"));
		rStart.setControle(transp.getCode());
		peWorker.external(transp);
		is.getRegSvc().getDcSvc().flush();
		rdWorker.work(rStart);
		if (tmpRet == null) {
			DocumentModel dmRet = is.getRegSvc().getDmSvc().findByMetaname("RROTA");
			Document ret = new Document(dmRet, dmRet.getName() + new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()), code, "ATIVO");
			Thing truck = transp.getThings().parallelStream()
							.filter(dt -> dt.getThing().getProduct().getModel().getMetaname().equals("TRUCK"))
							.map(dt -> dt.getThing())
							.findAny()
							.orElse(null);

			ret.getThings().add(new DocumentThing(ret, truck, "RETORNO"));
			is.getRegSvc().getDcSvc().persist(ret);
		}
		return true;
	}
}
