package com.gtp.hunter.process.wf.action.solar;

import java.lang.invoke.MethodHandles;
import java.util.UUID;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.jsonstubs.AGLDocModelProps;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;

public class AGLChecklistSegurancaDocumentAction extends AGLDocumentAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public AGLChecklistSegurancaDocumentAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	protected void open(Action t) {
		getAs().onNext(getTranspop());
	}

	@Override
	public void onMessage(Object msg) {
		try {
			this.salvapop((String) msg);
			AGLDocModelProps obj = new Gson().fromJson((String) msg, AGLDocModelProps.class);
			String attTrck = obj.getProps().stream()
							.filter(a -> a.getAttrib().equalsIgnoreCase("AttRastreador"))
							.map(a -> a.getValue())
							.findAny()
							.orElse("");
			final Unit u = getRegSvc().getUnSvc().findByTagId(attTrck);

			if (u != null) {
				Thing chkt = getRegSvc().getThSvc().findByUnitId(u.getId());
				Document transp = getRegSvc().getDcSvc().findParent(UUID.fromString(obj.getId()));
				Document d = transp.getSiblings().parallelStream()
								.filter(ds -> ds.getId().equals(UUID.fromString(obj.getId())))
								.findAny()
								.get();

				if (chkt != null) {
					final UUID chktId = chkt.getId();

					if (transp.getThings().parallelStream()
									.anyMatch(dt -> dt.getThing().getId().equals(chktId))) {
						//same truck from document
						chkt = null;
					}
				}

				if (chkt == null) {
					d.setStatus("PREENCHIDO");
					d.setUser(getUser());
					getRegSvc().getDcSvc().persist(d);
					Thing t = null;

					for (DocumentField df : transp.getFields()) {
						if (df.getField().getMetaname().equals("TRUCK_ID")) {
							t = getRegSvc().getThSvc().findById(UUID.fromString(df.getValue()));
							t.getUnits().add(u.getId());
							getRegSvc().getThSvc().persist(t);
							break;
						}
					}
					if (transp.getSiblings().parallelStream().noneMatch(ds -> ds.getModel().getMetaname().equals("APOENTRADA"))) {
						DocumentModel dm = getRegSvc().getDmSvc().findByMetaname("APOENTRADA");
						Document apo = new Document(dm, "Entrada do Transporte " + transp.getCode(), "ENT" + transp.getCode(), "NOVO");

						apo.setUser(getUser());
						apo.setParent(transp);
						getRegSvc().getDcSvc().persist(apo);
					}
					transp.setStatus("CAMINHAO NO PATIO");
					getRegSvc().getDcSvc().persist(transp);
					getAs().onNext(IntegrationReturn.OK);
					//					doMarreta(transp, u);
				} else if (d != null && d.getStatus().equals("PREENCHIDO")) {
					final UUID chktId = chkt.getId();

					if (transp != null && transp.getThings().parallelStream().anyMatch(dt -> dt.getThing().getId().equals(chktId))) {
						logger.info("Rastreador já associado ao transporte");
						getAs().onNext(IntegrationReturn.OK);
						if (transp.getStatus().equals("CAMINHAO NA ENTRADA")) {
							transp.setStatus("CAMINHAO NO PATIO");
							getRegSvc().getDcSvc().persist(transp);
						}
						//						doMarreta(transp, u);
					} else {
						logger.info("Rastreador associado a " + chkt.getId().toString() + " - " + chkt.getName());
						getAs().onNext(new IntegrationReturn(false, "Rastreador já associado a outro transporte"));
					}
				} else {
					logger.info("Rastreador associado a " + chkt.getId().toString() + " - " + chkt.getName());
					getAs().onNext(new IntegrationReturn(false, "Rastreador já associado a outro transporte"));
				}
			} else {
				getAs().onNext(new IntegrationReturn(false, "Rastreador não encontrado"));
			}
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
	//	private void doMarreta(Document transp, Unit u) {
	//		//MARRETAAA
	//		if (ConfigUtil.get("hunter-custom-solar", "bypass-gps-event", "true").equalsIgnoreCase("TRUE")) {
	//			Executors.newSingleThreadScheduledExecutor().schedule(() -> sendTagEvent(transp, u), 5, TimeUnit.SECONDS);
	//			Executors.newSingleThreadScheduledExecutor().schedule(() -> sendTagEvent(transp, u), 30, TimeUnit.SECONDS);
	//			Executors.newSingleThreadScheduledExecutor().schedule(() -> sendTagEvent(transp, u), 60, TimeUnit.SECONDS);
	//		}
	//	}
	//
	//	private void sendTagEvent(Document transp, Unit u) {
	//		String dock = transp.getFields().stream()
	//						.filter(df -> df.getField().getMetaname().equals("DOCK"))
	//						.map(df -> df.getValue())
	//						.findAny()
	//						.orElse("");
	//		if (!dock.isEmpty()) {
	//			BaseProcess prc = getRsm().getPsm().getProcesses().get(UUID.fromString("3eab4f2d-4b4b-11e9-9427-005056a19775"));
	//			Address arrival = getRegSvc().getAddSvc().findById(UUID.fromString(dock));
	//			ComplexData cd = new ComplexData();
	//			LocatePayload lp = new LocatePayload();
	//
	//			lp.setLatitude(arrival.getRegion().getCentroid().getY());
	//			lp.setLongitude(arrival.getRegion().getCentroid().getX());
	//			cd.setTagId(u.getTagId());
	//			cd.setPayload(lp.toString());
	//			cd.setUnit(u);
	//			logger.info("Sending " + cd.toString() + " to process " + prc.getModel().getName());
	//			prc.onNext(cd);
	//		}
	//	}

}
