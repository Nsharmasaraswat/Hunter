package com.gtp.hunter.custom.solar.filter.trigger;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.DocumentTransport;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

public class WMSResupplyTrigger extends BaseTrigger {

	private transient static final Logger	logger		= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final List<UUID>			processing	= new CopyOnWriteArrayList<>();
	private IntegrationService				iSvc;

	public WMSResupplyTrigger(IntegrationService iSvc) {
		super(new FilterTrigger());
		this.iSvc = iSvc;
	}

	public WMSResupplyTrigger(FilterTrigger model) {
		super(model);
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> processing.clear(), 1, 1, TimeUnit.HOURS);//TODO: Check why its duplicating trigger
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		Document ordmov = (Document) mdl.getModel();
		if (!processing.contains(ordmov.getId())) {
			processing.add(ordmov.getId());
			final List<Address> resupplied = new ArrayList<>();
			final List<String> destIds = new ArrayList<>();
			AtomicInteger ai = new AtomicInteger(0);

			logger.info("Resupply Trigger: " + ordmov.getCode());

			ordmov.getTransports().parallelStream()
							.forEach(dtr -> {
								Executors.newSingleThreadScheduledExecutor().schedule(() -> {
									Prefix pfx = iSvc.getRegSvc().getPfxSvc().findNext("RSP", 9);
									Document resupply = new Document(ordmov.getModel(), ordmov.getModel().getName() + " " + pfx.getCode(), pfx.getPrefix() + pfx.getCode(), "RESUPPLY");
									DocumentModelField dmfPriority = ordmov.getModel().getFields().stream().filter(dmf -> dmf.getMetaname().equals("PRIORITY")).findAny().get();
									DocumentModelField dmfTitle = ordmov.getModel().getFields().stream().filter(dmf -> dmf.getMetaname().equals("MOV_TITLE")).findAny().get();
									DocumentModelField dmfType = ordmov.getModel().getFields().stream().filter(dmf -> dmf.getMetaname().equals("MOV_TYPE")).findAny().get();
									DocumentField priority = new DocumentField(resupply, dmfPriority, "NOVO", "1");
									DocumentField dfTitle = new DocumentField(resupply, dmfTitle, "NOVO", "RESSUPRIMENTO");
									DocumentField dfType = new DocumentField(resupply, dmfType, "NOVO", "RESTOCK");
									AtomicInteger cnt = new AtomicInteger(0);
									Address orig = dtr.getAddress();
									Thing t = dtr.getThing();

									resupply.setParent(ordmov.getParent());
									resupply.getFields().add(priority);
									resupply.getFields().add(dfTitle);
									resupply.getFields().add(dfType);
									for (Thing ts : t.getSiblings()) {
										List<String> destList = iSvc
														.getRegSvc()
														.getWmsSvc()
														.findDestResupply(ts.getProduct().getId());

										destIds.addAll(destList
														.stream()
														.filter(s -> !resupplied
																		.stream()
																		.anyMatch(a -> a.getId().toString().equals(s)))
														.collect(Collectors.toList()));
										if (!destIds.isEmpty()) {
											String destId = destIds.remove(0);
											Address dest = iSvc.getRegSvc().getAddSvc().findById(UUID.fromString(destId));
											DocumentTransport dtrRes = new DocumentTransport(resupply, cnt.incrementAndGet(), t, dest, orig);

											if (!dfTitle.getValue().equals(dest.getName()))
												dfTitle.setValue(dest.getName());
											resupply.getTransports().add(dtrRes);
											if (resupply.getThings().isEmpty()) {
												DocumentThing dtRes = new DocumentThing(resupply, t, t.getStatus());

												resupply.getThings().add(dtRes);
											}
											if (ordmov.getThings().size() == 1)//Mais de um palete na mesma mov pode ir pro mesmo endereço (Produtos de alto giro)
												resupplied.add(dest);
											orig = dest;
											destIds.clear();
										} else {
											iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.PROCESS, AlertSeverity.ERROR, ordmov.getCode(), "Endereço para ressuprimento não encontrado", ts.getProduct().getSku() + " - " + ts.getProduct().getName()));
											ordmov.setStatus("FALHA RESSUPRIMENTO");
										}
									}
									ordmov.getSiblings().add(resupply);
									ordmov.setStatus("SUCESSO");
									iSvc.getRegSvc().getDcSvc().persist(resupply);
									iSvc.getRegSvc().getDcSvc().persist(ordmov);
									iSvc.getRegSvc().getAglSvc().sendDocToWMS(resupply, "POST");
								}, ai.incrementAndGet() * 200, TimeUnit.MILLISECONDS);
							});
		}
		return true;
	}
}
