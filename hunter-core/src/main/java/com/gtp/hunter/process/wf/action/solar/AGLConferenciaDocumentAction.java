package com.gtp.hunter.process.wf.action.solar;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.jsonstubs.AGLDocModelForm;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;

public class AGLConferenciaDocumentAction extends AGLDocumentAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public AGLConferenciaDocumentAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	protected void open(Action t) {
		logger.debug("Conferencia Document Action");

		getAs().onNext(getTranslate());
	}

	@Override
	public void onMessage(Object msg) {
		String json = (String) msg;

		try {
			final List<Thing> toRemove = new ArrayList<>();
			AGLDocModelForm aglDoc = new Gson().fromJson(json, AGLDocModelForm.class);
			//			getRegSvc().getAglSvc().sendToWMS("com.wms.comunicador.adocument", json, "PUT", aglDoc.getId());
			getAs().onNext(IntegrationReturn.OK);
			final Document ordconf = getRegSvc().getDcSvc().findById(this.salvadoc(json).getId());
			final Document transp = getRegSvc().getDcSvc().findById(getRegSvc().getDcSvc().quickFindParentDoc(aglDoc.getId()).getId());
			boolean nfentrada = transp.getSiblings().stream().anyMatch(ds -> ds.getModel().getMetaname().equals("NFENTRADA"));
			boolean nfsaida = transp.getSiblings().stream().anyMatch(ds -> ds.getModel().getMetaname().equals("NFSAIDA"));

			if (ordconf.getModel().getMetaname().equalsIgnoreCase("ORDCONF")) {
				//TODO: Consertar fluxo  da informação de Unidade de medida
				if (nfentrada) {
					List<DocumentItem> diList = transp.getSiblings().stream().filter(sib -> sib.getModel().getMetaname().equals("NFENTRADA")).flatMap(nf -> nf.getItems().stream()).collect(Collectors.toList());

					for (DocumentItem di : ordconf.getItems()) {
						Optional<DocumentItem> optDi = diList.stream().filter(nfdi -> nfdi.getProduct().getId().equals(di.getProduct().getId())).findFirst();

						if (optDi.isPresent()) {
							DocumentItem diNf = optDi.get();
							di.setMeasureUnit(diNf.getMeasureUnit());
						}
					}
				}

				if (aglDoc.getProps().containsKey("CTE") || aglDoc.getProps().containsKey("cte")) {
					DocumentModelField dmfCTE = getRegSvc().getDmfSvc().findByModelAndMetaname(transp.getModel(), "CTE");
					DocumentField df = new DocumentField();

					df.setField(dmfCTE);
					df.setValue(aglDoc.getProps().get("CTE"));
					df.setCreatedAt(Calendar.getInstance().getTime());
					df.setUpdatedAt(Calendar.getInstance().getTime());
					df.setStatus("NOVO");
					df.setDocument(transp);
					transp.getFields().add(df);
				}
				boolean paConf = ordconf.getFields().parallelStream()
								.filter(df -> df.getField().getMetaname().equals("CONF_TYPE"))
								.allMatch(df -> df.getValue().equals("EPAPD"));

				if (paConf && nfentrada) {
					List<Document> rocs = ordconf.getSiblings().parallelStream()
									.filter(ds -> ds.getModel().getMetaname().equals("RETORDCONF") && ds.getStatus().equals("SUCESSO"))
									.collect(Collectors.toList());

					for (final Document retOrdConf : rocs) {
						Iterator<DocumentThing> it = retOrdConf.getThings().iterator();

						while (it.hasNext()) {
							Thing t = it.next().getThing();

							if (t.getSiblings().parallelStream().anyMatch(ts -> ts.getProduct().getModel().getMetaname().equals("VAS") && ts.getProduct().getName().startsWith("EMB"))) {
								it.remove();
								ordconf.getThings().removeIf(tdt -> tdt.getThing().getId().equals(t.getId()));
								transp.getThings().removeIf(tdt -> tdt.getThing().getId().equals(t.getId()));
								logger.warn("Deleting EMB Thing " + t.getId());
								toRemove.add(t);
								t.getSiblings().clear();
							}
							while (t.getSiblings().size() > 1) {//TODO: FUCKING REMOVE THIS SHIT
								Thing toDel = t.getSiblings().stream()
												.sorted((t1, t2) -> {
													if (t1.getCreatedAt() == null && t2.getCreatedAt() == null) return 0;
													if (t1.getCreatedAt() == null) return 1;
													if (t2.getCreatedAt() == null) return -1;
													return t1.getCreatedAt().compareTo(t2.getCreatedAt());
												})
												.findFirst().get();

								logger.warn("Deleting Duplicate Thing " + toDel.getId());
								toRemove.add(toDel);
								t.getSiblings().removeIf(td -> td.getId().equals(toDel.getId()));
							}
						}
					}
				}
			}

			ordconf.setUser(getUser());
			ordconf.setParent(transp);
			ordconf.setStatus(aglDoc.getStatus());
			if (ordconf.getStatus().equals("TEMPSUCESSO")) ordconf.setStatus("SUCESSO");
			Executors.newSingleThreadScheduledExecutor().schedule(() -> {
				getRegSvc().getDcSvc().persist(ordconf);
			}, 5, TimeUnit.SECONDS);
			if (!transp.getSiblings().stream().anyMatch(ds -> ds.getModel().getMetaname().equals("APODESCARGA")) && !nfsaida)
				getRegSvc().getDcSvc().createChild(transp, "CAMINHAO DESCARREGADO", "APODESCARGA", "NOVO", "DES", null, null, null, getUser());
			else
				getRegSvc().getDcSvc().persist(transp);
			toRemove.addAll(transp.getThings().stream()
							.filter(dt -> !dt.getThing().getProduct().getModel().getMetaname().equals("TRUCK") && !dt.getThing().getProduct().getModel().getMetaname().equals("FORKLIFT") && dt.getThing().getSiblings().size() == 0)
							.map(dt -> dt.getThing())
							.collect(Collectors.toSet()));
			Executors.newSingleThreadScheduledExecutor().schedule(() -> {
				toRemove.forEach(t -> {
					for (Thing ts : t.getSiblings()) {
						getRegSvc().getWmsSvc().removePallet(ts);
						getRegSvc().getThSvc().remove(ts);
					}
					getRegSvc().getWmsSvc().removePallet(t);
					getRegSvc().getThSvc().remove(t);
				});
			}, 200, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			getAs().onNext(new IntegrationReturn(false, "JSON MAL FORMADO - " + msg));
			e.printStackTrace();
		}
	}

	@OnClose
	@Override
	protected void close(Session ss, CloseReason cr) {
		logger.info("Closed: " + cr.getReasonPhrase());
	}
}
