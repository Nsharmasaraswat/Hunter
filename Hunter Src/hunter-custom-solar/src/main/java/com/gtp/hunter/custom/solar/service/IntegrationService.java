package com.gtp.hunter.custom.solar.service;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.core.util.MailUtil;
import com.gtp.hunter.custom.solar.filter.trigger.ApoProducaoTrigger;
import com.gtp.hunter.custom.solar.filter.trigger.CheckingFailedMailTrigger;
import com.gtp.hunter.custom.solar.filter.trigger.SAPCheckingTrigger;
import com.gtp.hunter.custom.solar.filter.trigger.SAPInventoryTrigger;
import com.gtp.hunter.custom.solar.filter.trigger.SAPResendNFTrigger;
import com.gtp.hunter.custom.solar.filter.trigger.SAPTransfTrigger;
import com.gtp.hunter.custom.solar.filter.trigger.SAPUnloadTrigger;
import com.gtp.hunter.custom.solar.filter.trigger.WMSCheckinCheckoutTrigger;
import com.gtp.hunter.custom.solar.filter.trigger.WMSExitTrigger;
import com.gtp.hunter.custom.solar.filter.trigger.WMSRepackTrigger;
import com.gtp.hunter.custom.solar.filter.trigger.WMSResupplyTrigger;
import com.gtp.hunter.custom.solar.filter.trigger.WMSTrnRotaTrigger;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Filter;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.filter.BaseFilter;
import com.gtp.hunter.process.wf.filter.ByMetanameAndStatusFilter;

@Startup
@Singleton
public class IntegrationService {

	@EJB(lookup = "java:global/hunter-core/RegisterService!com.gtp.hunter.process.service.RegisterService")
	private RegisterService			regSvc;

	@EJB(lookup = "java:global/hunter-core/RegisterStreamManager!com.gtp.hunter.process.stream.RegisterStreamManager")
	private RegisterStreamManager	rsm;

	@EJB(lookup = "java:global/hunter-core/MailUtil!com.gtp.hunter.core.util.MailUtil")
	private MailUtil				mail;

	@Inject
	private SAPSolar				solar;

	@Inject
	private SAPService				svc;

	@Inject
	private RealpickingService		rpSvc;

	@Inject
	private Logger					logger;

	@PostConstruct
	public void init() {
		logger.info("INIT INTEGRATION SERVICE");
		initFilters();
	}

	public MailUtil getMail() {
		return mail;
	}

	public RegisterService getRegSvc() {
		return regSvc;
	}

	public RegisterStreamManager getRsm() {
		return rsm;
	}

	public RealpickingService getRpSvc() {
		return rpSvc;
	}

	public SAPService getSap() {
		return svc;
	}

	public SAPSolar getSolar() {
		return solar;
	}

	private void initFilters() {
		logger.info("INITFILTER");
		Filter fConf = new Filter();
		fConf.setParams("{\"metaname\":\"ORDCONF\", \"status\":\"SUCESSO\"}");
		BaseFilter bfConf = new ByMetanameAndStatusFilter(fConf);
		bfConf.getTriggers().add(new SAPCheckingTrigger(solar, svc, this));
		rsm.getFsm().registerBaseFilter(Document.class, bfConf);

		Filter fTransf = new Filter();
		fTransf.setParams("{\"metaname\":\"ORDTRANSF\", \"status\":\"SUCESSO\"}");
		BaseFilter bfTransf = new ByMetanameAndStatusFilter(fTransf);
		bfTransf.getTriggers().add(new SAPTransfTrigger(solar, svc, this));
		rsm.getFsm().registerBaseFilter(Document.class, bfTransf);

		Filter fUnload = new Filter();
		fUnload.setParams("{\"metaname\":\"APODESCARGA\", \"status\":\"NOVO\"}");
		BaseFilter bfUnload = new ByMetanameAndStatusFilter(fUnload);
		bfUnload.getTriggers().add(new SAPUnloadTrigger(solar, svc, this));
		rsm.getFsm().registerBaseFilter(Document.class, bfUnload);

		Filter fInventory = new Filter();
		fInventory.setParams("{\"metaname\":\"SAPINVENTORY\", \"status\":\"SAP\"}");
		BaseFilter bfInventory = new ByMetanameAndStatusFilter(fInventory);
		bfInventory.getTriggers().add(new SAPInventoryTrigger(solar, svc, this));
		rsm.getFsm().registerBaseFilter(Document.class, bfInventory);

		Filter fResupply = new Filter();
		fResupply.setParams("{\"metaname\":\"ORDMOV\", \"status\":\"RESSUPRIMENTO\"}");
		BaseFilter bfResupply = new ByMetanameAndStatusFilter(fResupply);
		bfResupply.getTriggers().add(new WMSResupplyTrigger(this));
		rsm.getFsm().registerBaseFilter(Document.class, bfResupply);

		Filter fRepack = new Filter();
		fRepack.setParams("{\"metaname\":\"REPACK\", \"status\":\"APROVADO\"}");
		BaseFilter bfRepack = new ByMetanameAndStatusFilter(fRepack);
		bfRepack.getTriggers().add(new WMSRepackTrigger(this));
		rsm.getFsm().registerBaseFilter(Document.class, bfRepack);

		Filter fRota = new Filter();
		fRota.setParams("{\"metaname\":\"TRANSPORT\", \"status\":\"ROMANEIO\"}");
		BaseFilter bfRota = new ByMetanameAndStatusFilter(fRota);
		bfRota.getTriggers().add(new WMSTrnRotaTrigger(solar, svc, this));
		rsm.getFsm().registerBaseFilter(Document.class, bfRota);

		Filter fLacre = new Filter();
		fLacre.setParams("{\"metaname\":\"APOLACRE\", \"status\":\"PREENCHIDO\"}");
		BaseFilter bfLacre = new ByMetanameAndStatusFilter(fLacre);
		bfLacre.getTriggers().add(new WMSCheckinCheckoutTrigger(solar, svc, this));
		rsm.getFsm().registerBaseFilter(Document.class, bfLacre);

		Filter fSaida = new Filter();
		fSaida.setParams("{\"metaname\":\"APOSAIDA\", \"status\":\"NOVO\"}");
		BaseFilter bfSaida = new ByMetanameAndStatusFilter(fSaida);
		bfSaida.getTriggers().add(new WMSExitTrigger(solar, svc, this));
		rsm.getFsm().registerBaseFilter(Document.class, bfSaida);

		Filter fCheckout = new Filter();
		fCheckout.setParams("{\"metaname\":\"CHECKOUTPORTARIA\", \"status\":\"PREENCHIDO\"}");
		BaseFilter bfCheckout = new ByMetanameAndStatusFilter(fCheckout);
		bfCheckout.getTriggers().add(new WMSCheckinCheckoutTrigger(solar, svc, this));
		rsm.getFsm().registerBaseFilter(Document.class, bfCheckout);

		//		Filter fRetornoRota = new Filter();
		//		fRetornoRota.setParams("{\"metaname\":\"CHECKINPORTARIA\", \"status\":\"NOVO\"}");
		//		BaseFilter bfRetornoRota = new ByMetanameAndStatusFilter(fRetornoRota);
		//		bfRetornoRota.getTriggers().add(new WMSRetornoRotaTrigger(solar, svc, this));
		//		rsm.getFsm().registerBaseFilter(Document.class, bfRetornoRota);

		Filter fCheckin = new Filter();
		fCheckin.setParams("{\"metaname\":\"CHECKINPORTARIA\", \"status\":\"PREENCHIDO\"}");
		BaseFilter bfCheckin = new ByMetanameAndStatusFilter(fCheckin);
		bfCheckin.getTriggers().add(new WMSCheckinCheckoutTrigger(solar, svc, this));
		rsm.getFsm().registerBaseFilter(Document.class, bfCheckin);

		Filter ffConf = new Filter();
		ffConf.setParams("{\"metaname\":\"ORDCONF\", \"status\":\"FALHA\"}");
		BaseFilter bffConf = new ByMetanameAndStatusFilter(ffConf);
		bffConf.getTriggers().add(new CheckingFailedMailTrigger(solar, svc, this));
		rsm.getFsm().registerBaseFilter(Document.class, bffConf);

		Filter ffsConf = new Filter();
		ffsConf.setParams("{\"metaname\":\"ORDCONF\", \"status\":\"FALHA SAP\"}");
		BaseFilter bffsConf = new ByMetanameAndStatusFilter(ffsConf);
		bffsConf.getTriggers().add(new CheckingFailedMailTrigger(solar, svc, this));
		rsm.getFsm().registerBaseFilter(Document.class, bffsConf);

		Filter ffailnf = new Filter();
		ffailnf.setParams("{\"metaname\":\"NFENTRADA\", \"status\":\"REENVIO SAP\"}");
		BaseFilter bffailnf = new ByMetanameAndStatusFilter(ffailnf);
		bffailnf.getTriggers().add(new SAPResendNFTrigger(solar, svc, this));
		rsm.getFsm().registerBaseFilter(Document.class, bffailnf);

		Filter fPls = new Filter();
		fPls.setParams("{\"metaname\":\"APOPRODUCAO\", \"status\":\"NOVO\"}");
		BaseFilter bfPls = new ByMetanameAndStatusFilter(fPls);
		bfPls.getTriggers().add(new ApoProducaoTrigger(solar, svc, this));
		rsm.getFsm().registerBaseFilter(Document.class, bfPls);
	}
}
