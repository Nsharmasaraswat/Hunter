package com.gtp.hunter.custom.eurofarma.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.core.util.MailUtil;
import com.gtp.hunter.custom.eurofarma.json.CPIIntegrationMessage;
import com.gtp.hunter.custom.eurofarma.json.ProductCheckingMessage;
import com.gtp.hunter.custom.eurofarma.json.ProductStoreMessage;
import com.gtp.hunter.custom.eurofarma.json.TagReadMessage;
import com.gtp.hunter.custom.eurofarma.process.PortalForwardEncoder;
import com.gtp.hunter.custom.eurofarma.process.ProductCheckingExternalProcessor;
import com.gtp.hunter.custom.eurofarma.process.ProductStoreExternalProcessor;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.ProcessStreamManager;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.process.DynamicProcess;
import com.gtp.hunter.process.wf.process.ForwardProcess;

@Startup
@Singleton
public class IntegrationService {

	@EJB(lookup = "java:global/hunter-core/RegisterService!com.gtp.hunter.process.service.RegisterService")
	private RegisterService								regSvc;

	@EJB(lookup = "java:global/hunter-core/RegisterStreamManager!com.gtp.hunter.process.stream.RegisterStreamManager")
	private RegisterStreamManager						rsm;

	@EJB(lookup = "java:global/hunter-core/ProcessStreamManager!com.gtp.hunter.process.stream.ProcessStreamManager")
	private ProcessStreamManager						psm;

	@EJB(lookup = "java:global/hunter-core/MailUtil!com.gtp.hunter.core.util.MailUtil")
	private MailUtil									mail;

	@Inject
	private Logger										logger;

	private Map<UUID, PortalForwardEncoder>				encoders;
	private Map<UUID, ProductCheckingExternalProcessor>	prdCheckExtProcessors;
	private Map<UUID, ProductStoreExternalProcessor>	prdStoreExtProcessors;

	@PostConstruct
	public void init() {
		logger.info("INIT EUROFARMA INTEGRATION SERVICE");
		try {
			while (!psm.isInitialized()) {
				Thread.sleep(1000);
			}
			logger.info("Injecting processors");
			encoders = new HashMap<UUID, PortalForwardEncoder>();
			prdCheckExtProcessors = new HashMap<UUID, ProductCheckingExternalProcessor>();
			prdStoreExtProcessors = new HashMap<UUID, ProductStoreExternalProcessor>();
			psm.getProcesses().entrySet().parallelStream()
							.forEach(en -> {
								if (en.getValue() instanceof ForwardProcess) {
									ForwardProcess fp = (ForwardProcess) en.getValue();

									encoders.put(fp.getModel().getId(), new PortalForwardEncoder<CPIIntegrationMessage<TagReadMessage>, TagReadMessage>());
									fp.addEncoder(encoders.get(fp.getModel().getId()));
									logger.info("Added forwarder for " + en.getValue().getModel().getName());
								} else if (en.getValue() instanceof DynamicProcess) {
									DynamicProcess fp = (DynamicProcess) en.getValue();

									prdCheckExtProcessors.put(fp.getModel().getId(), new ProductCheckingExternalProcessor<CPIIntegrationMessage<ProductCheckingMessage>, ProductCheckingMessage>(this));
									prdStoreExtProcessors.put(fp.getModel().getId(), new ProductStoreExternalProcessor<CPIIntegrationMessage<ProductStoreMessage>, ProductStoreMessage>(this));
									fp.addExternalProcessor(prdCheckExtProcessors.get(fp.getModel().getId()));
									fp.addExternalProcessor(prdStoreExtProcessors.get(fp.getModel().getId()));
									logger.info("Added External Processor for " + en.getValue().getModel().getName());
								}
							});
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}

	}

	@PreDestroy
	public void destroy() {
		logger.info("Removing processors");
		psm.getProcesses().entrySet().parallelStream()
						.forEach(en -> {
							if (en.getValue() instanceof ForwardProcess) {
								ForwardProcess fp = (ForwardProcess) en.getValue();

								fp.removeEncoder(encoders.remove(fp.getModel().getId()));
								logger.info("Removed forwarder for " + en.getValue().getModel().getName());
							} else if (en.getValue() instanceof DynamicProcess) {
								DynamicProcess fp = (DynamicProcess) en.getValue();

								fp.removeExternalProcessor(prdCheckExtProcessors.remove(fp.getModel().getId()));
								fp.removeExternalProcessor(prdStoreExtProcessors.remove(fp.getModel().getId()));
								logger.info("Removed External Processor for " + en.getValue().getModel().getName());
							}
						});
	}

	/**
	 * @return the regSvc
	 */
	public RegisterService getRegSvc() {
		return regSvc;
	}

	/**
	 * @param regSvc the regSvc to set
	 */
	public void setRegSvc(RegisterService regSvc) {
		this.regSvc = regSvc;
	}

	/**
	 * @return the rsm
	 */
	public RegisterStreamManager getRsm() {
		return rsm;
	}

	/**
	 * @param rsm the rsm to set
	 */
	public void setRsm(RegisterStreamManager rsm) {
		this.rsm = rsm;
	}

	/**
	 * @return the psm
	 */
	public ProcessStreamManager getPsm() {
		return psm;
	}

	/**
	 * @param psm the psm to set
	 */
	public void setPsm(ProcessStreamManager psm) {
		this.psm = psm;
	}

	/**
	 * @return the mail
	 */
	public MailUtil getMail() {
		return mail;
	}

	/**
	 * @param mail the mail to set
	 */
	public void setMail(MailUtil mail) {
		this.mail = mail;
	}
}
