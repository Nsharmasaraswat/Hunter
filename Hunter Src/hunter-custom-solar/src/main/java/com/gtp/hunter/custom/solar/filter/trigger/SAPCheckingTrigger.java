package com.gtp.hunter.custom.solar.filter.trigger;

import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.worker.ZHWCheckinCheckoutTranspWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWCheckinCheckoutWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWConferenciaCegaTranspWorker;
import com.gtp.hunter.custom.solar.sap.worker.ZHWConferenciaCegaWorker;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

public class SAPCheckingTrigger extends BaseTrigger {

	private transient static final Logger	logger		= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final List<UUID>			processing	= new CopyOnWriteArrayList<>();

	private final AtomicLong				count		= new AtomicLong();
	private SAPSolar						solar;
	private SAPService						svc;
	private IntegrationService				is;

	public SAPCheckingTrigger(SAPSolar solar, SAPService svc, IntegrationService is) {
		super(new FilterTrigger());
		this.solar = solar;
		this.svc = svc;
		this.is = is;
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> processing.clear(), 1, 1, TimeUnit.HOURS);//TODO: Check why its duplicating trigger
	}

	public SAPCheckingTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		Document ordcnf = (Document) mdl.getModel();

		try {
			logger.info("Checking Trigger-" + count.getAndIncrement() + ": " + ordcnf.getCode() + " Processing: " + processing.contains(ordcnf.getId()));
			if (!processing.contains(ordcnf.getId())) {
				processing.add(ordcnf.getId());
				Document parent = is.getRegSvc().getDcSvc().isParentInitialized(ordcnf) ? ordcnf.getParent() : is.getRegSvc().getDcSvc().findParent(ordcnf);
				String strConfType = Documents.getStringField(ordcnf, "CONF_TYPE");
				boolean sendToSap = parent != null && ordcnf.getSiblings().stream().anyMatch(ds -> ds.getModel().getMetaname().equals("RETORDCONF") && ds.getStatus().equals("SUCESSO"));
				boolean genMov = false;
				boolean checkSeal = false;

				switch (strConfType) {
					case "SPA"://ROTA (parent = PICKING)
						parent.setStatus("CONFERIDO");
						is.getRegSvc().getDcSvc().persist(parent);
						sendToSap = false;
						checkSeal = true;
						genMov = ConfigUtil.get("hunter-custom-solar", "create-single-route-load", "TRUE").equalsIgnoreCase("FALSE");
						break;
					case "SPAPD":
						sendToSap = false;
						checkSeal = true;
						break;
					case "ENV"://TODO: REMOVE
						sendToSap = false;
						break;
					case "RPAPD"://RETORNO DE ROTA, (parent = TRANSPORT)
						if (ordcnf.getStatus().equals("SUCESSO")) {
							String rcode = new SimpleDateFormat("'RR'yyyyMMdd").format(ordcnf.getCreatedAt());
							Document tmpRet = is.getRegSvc().getDcSvc().quickFindByCodeAndModelMetaname(rcode, "RROTA");
							Document ret = is.getRegSvc().getDcSvc().findById(tmpRet.getId());
							Document roc = ordcnf.getSiblings().parallelStream()
											.filter(ds -> ds.getModel().getMetaname().equals("RETORDCONF") && ds.getStatus().equals("SUCESSO"))
											.findAny()
											.orElse(null);

							if (roc != null) {
								roc.getItems().parallelStream()
												.forEach(diRoc -> {
													DocumentItem di = new DocumentItem(ret, diRoc.getProduct(), diRoc.getQty(), "RETORNO", diRoc.getMeasureUnit());

													di.getProperties().put("TRANSPORTE", parent.getCode());
													ret.getItems().add(di);
												});
								is.getRegSvc().getDcSvc().persist(ret);
							} else
								logger.warn("Ordconf status " + ordcnf.getStatus() + " with no RETORDCONF SUCESSO");
						}
						sendToSap = true;
						break;
					case "EPAPD"://Transferências/Revenda/DA
						Optional<Document> optRoc = ordcnf.getSiblings().parallelStream().filter(roc -> roc.getStatus().equals("SUCESSO")).findAny();

						sendToSap = ConfigUtil.get("hunter-custom-solar", "generate-outbound-checking", "FALSE").equalsIgnoreCase("TRUE");
						if (optRoc.isPresent()) {
							final Document retOrdConf = optRoc.get();

							if (retOrdConf.getThings().size() > 0 && retOrdConf.getThings().stream().allMatch(dt -> dt.getThing().getSiblings().size() > 0)) {
								Executors.newSingleThreadScheduledExecutor().schedule(() -> {
									final Document d = is.getRegSvc().getDcSvc().findById(retOrdConf.getId());
									final DocumentModel dm = is.getRegSvc().getDmSvc().findByMetaname("ORDCRIACAO");
									Prefix prefix = is.getRegSvc().getPfxSvc().findNext("OCR", 9);
									String code = prefix.getCode();
									Document ordcri = new Document(dm, dm.getName() + " " + code, prefix.getPrefix() + code, "NOVO");

									d.getThings().forEach(dtord -> {
										Thing t = dtord.getThing();
										DocumentThing dt = new DocumentThing(ordcri, t, "NOVO");

										if (t.getSiblings().size() > 1) {//TODO: FUCKING REMOVE THIS SHIT
											Thing toDel = t.getSiblings().parallelStream()
															.sorted((Thing o1, Thing o2) -> {
																if (o1 == null && o2 == null) return 0;
																if (o2 == null) return 1;
																if (o1 == null) return -1;
																if (o1.getUpdatedAt() == null && o2.getUpdatedAt() == null) return 0;
																if (o2.getUpdatedAt() == null) return 1;
																if (o1.getUpdatedAt() == null) return -1;
																return o1.getUpdatedAt().compareTo(o2.getUpdatedAt()) * -1;
															})
															.findFirst()
															.get();

											is.getRegSvc().getThSvc().remove(toDel);
											t.getSiblings().removeIf(td -> td.getId().equals(toDel.getId()));
										}
										dt.setCreatedAt(Calendar.getInstance().getTime());
										dt.setUpdatedAt(Calendar.getInstance().getTime());
										ordcri.getThings().add(dt);
									});
									ordcri.setCreatedAt(Calendar.getInstance().getTime());
									ordcri.setUpdatedAt(Calendar.getInstance().getTime());
									ordcri.setParent(parent);
									is.getRegSvc().getDcSvc().persist(ordcri);
									is.getRegSvc().getAglSvc().sendDocToWMS(ordcri, "POST");
								}, 3000, TimeUnit.MILLISECONDS);
							}
						} else {
							logger.error("Ordconf " + ordcnf.getCode() + " status " + ordcnf.getStatus() + " with no ROC SUCESSO");
							is.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.WARNING, parent.getCode(), "Ordconf " + ordcnf.getCode() + " status " + ordcnf.getStatus(), "Nenhuma conferência preenchida no transporte"));
							processing.remove(ordcnf.getId());
						}
						break;

				}
				final boolean sts = sendToSap;
				final boolean cs = checkSeal;
				final boolean gm = genMov;

				Executors.newSingleThreadScheduledExecutor().schedule(() -> {
					if (sts) {
						boolean trnIc = parent.getSiblings().parallelStream().anyMatch(ds -> ds.getPerson() != null && (ds.getPerson().getCode().startsWith("07196033") || ds.getPerson().getCode().startsWith("08715757") || ds.getPerson().getCode().startsWith("10557540")));
						Document transp = is.getRegSvc().getDcSvc().findById(parent.getId());

						switch (strConfType) {
							case "EPAPD":
								if (trnIc) {
									ZHWCheckinCheckoutTranspWorker wrk = new ZHWCheckinCheckoutTranspWorker(svc, solar, is);

									wrk.setCheckin(true);
									wrk.external(transp);
								}
								new ZHWConferenciaCegaTranspWorker(svc, solar, is).external(transp);
								break;
							case "EMP":
								new ZHWConferenciaCegaWorker(svc, solar, is).external(transp);
								break;
							case "RPAPD":
								new ZHWCheckinCheckoutWorker(svc, solar, is).external(transp);
						}

					}
					if (cs) {
						Document transp = is.getRegSvc().getDcSvc().findParent(parent);
						boolean genLacreRota = strConfType.equals("SPA") && transp.getSiblings().parallelStream()
										.filter(ds -> ds.getModel().getMetaname().equals("PICKING") && !ds.getStatus().equals("CANCELADO"))
										.allMatch(ds -> ds.getStatus().equals("CONFERIDO"));
						boolean genLacreOutros = strConfType.equals("SPAPD") && !transp.getSiblings().parallelStream()
										.filter(ds -> {
											if (ds.getModel().getMetaname().equals("ORDCONF") && Documents.getStringField(ds, "CONF_TYPE").equals("SPAPD")) return true;
											if (ds.getModel().getMetaname().equals("ORDMOV") && Documents.getStringField(ds, "MOV_TYPE").equals("LOAD")) return true;
											return false;
										})
										.anyMatch(ds -> ds.getStatus().equals("ATIVO") || ds.getStatus().equals("LOAD"));

						logger.info("Transport: " + transp.getCode() + " - ConfType: " + strConfType + " - GenLacreOutros: " + genLacreOutros + " - GenLacreRota " + genLacreRota);
						if (genLacreOutros || genLacreRota) {
							is.getRegSvc().getWmsSvc().createLacre(transp);
							if (genLacreRota && !gm) {//SingleRouteLoad
								is.getRegSvc().getWmsSvc().createRouteLoadSingle(transp);
							}
						}
					}
					if (gm) {
						is.getRegSvc().getWmsSvc().createRouteLoad(parent);
					}
				}, 3, TimeUnit.SECONDS);
				return true;
			} else {
				is.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.WARNING, (Hibernate.isInitialized(ordcnf.getParent()) ? ordcnf.getParent().getCode() : ordcnf.getCode()), "Ordconf " + ordcnf.getCode() + " status " + ordcnf.getStatus(), "Conferência já em processamento"));
			}
		} catch (Exception e) {
			processing.remove(ordcnf.getId());
		}
		return false;
	}
}
