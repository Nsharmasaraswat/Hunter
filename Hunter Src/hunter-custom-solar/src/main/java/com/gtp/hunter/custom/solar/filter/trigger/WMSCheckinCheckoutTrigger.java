package com.gtp.hunter.custom.solar.filter.trigger;

import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.sap.worker.ZHWCheckinCheckoutPortariaWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWCheckinCheckoutTranspWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWCheckinCheckoutWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWProntaEntregaWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWRecusaDocWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWVasilhameGeralWorker;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

public class WMSCheckinCheckoutTrigger extends BaseTrigger {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private SAPSolar						solar;
	private SAPService						svc;
	private IntegrationService				is;

	public WMSCheckinCheckoutTrigger(SAPSolar solar, SAPService svc, IntegrationService is) {
		super(new FilterTrigger());
		this.solar = solar;
		this.svc = svc;
		this.is = is;
	}

	public WMSCheckinCheckoutTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		Document apoCheckinCheckout = (Document) mdl.getModel();
		logger.info("CheckinCheckout Trigger: " + apoCheckinCheckout.getCode());
		Executors.newSingleThreadScheduledExecutor().schedule(() -> {
			Document transp = is.getRegSvc().getDcSvc().findParent(apoCheckinCheckout);

			switch (apoCheckinCheckout.getModel().getMetaname()) {
				case "APOLACRE":
					if (Documents.getStringField(transp, "SERVICE_TYPE", "").equals("ROTA")) {
						new ZHWCheckinCheckoutWorker(svc, solar, is).external(transp);
						freeTruck(transp);
					} else
						new ZHWCheckinCheckoutTranspWorker(svc, solar, is).external(transp);
					if (!transp.getStatus().equals("CAMINHAO NA SAIDA") && !transp.getStatus().equals("LIBERADO"))
						transp.setStatus("CAMINHAO LACRADO");
					break;
				case "CHECKOUTPORTARIA":
					is.getRegSvc().getWmsSvc().checkoutThings(transp);
					new ZHWCheckinCheckoutPortariaWorker(svc, solar, is).external(transp);
					transp.setStatus("CAMINHAO EM ROTA");
					is.getRegSvc().getWmsSvc().finishTransport(transp.getId());
					break;
				case "CHECKINPORTARIA":
					ZHWProntaEntregaWorker peWorker = new ZHWProntaEntregaWorker(svc, solar, is);
					ZHWRecusaDocWorker rdWorker = new ZHWRecusaDocWorker(svc, solar, is);
					SAPReadStartDTO rStart = new SAPReadStartDTO();
					String code = new SimpleDateFormat("'RR'yyyyMMdd").format(Calendar.getInstance().getTime());
					Document tmpRet = is.getRegSvc().getDcSvc().quickFindByCodeAndModelMetaname(code, "RROTA");
					Map<String, String> vasMap = new ZHWVasilhameGeralWorker(svc, solar, is).getVasilhameGeralMap();

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

					is.getRegSvc().getWmsSvc().createReturnChecking(transp.getId(), vasMap);
					new ZHWCheckinCheckoutPortariaWorker(svc, solar, is).external(transp);
					transp.setStatus("CAMINHAO NO PATIO");
					break;
			}
			is.getRegSvc().getDcSvc().persist(transp, false);
		}, 1, TimeUnit.SECONDS);
		return true;
	}

	private void freeTruck(Document transp) {
		is.getRsm().getTsm().cancelTask(null, transp);
		if (!transp.getSiblings().parallelStream().anyMatch(ds -> ds.getModel().getMetaname().equals("APOLIBERACAO"))) {
			DocumentModel dm = is.getRegSvc().getDmSvc().findByMetaname("APOLIBERACAO");
			Document apo = new Document(dm, dm.getName() + " " + transp.getCode(), "LIB" + transp.getCode(), "NOVO");

			apo.setUser(null);
			apo.setParent(transp);
			is.getRegSvc().getDcSvc().persist(apo);
		}
		if (ConfigUtil.get("hunter-custom-solar", "checkout-portaria", "FALSE").equalsIgnoreCase("TRUE") && !transp.getSiblings().parallelStream().anyMatch(ds -> ds.getModel().getMetaname().equals("CHECKOUTPORTARIA"))) {
			DocumentModel dmChk = is.getRegSvc().getDmSvc().findByMetaname("CHECKOUTPORTARIA");
			Document apochk = new Document(dmChk, dmChk.getName() + " " + transp.getCode(), "CHKPORT" + transp.getCode(), "NOVO");

			apochk.setParent(transp);
			is.getRegSvc().getDcSvc().persist(apochk);
			transp.getSiblings().add(apochk);
		}
		transp.setStatus("CAMINHAO NA SAIDA");
		is.getRegSvc().getDcSvc().persist(transp);
		is.getRsm().getTsm().unlockTask(transp);
	}
}
