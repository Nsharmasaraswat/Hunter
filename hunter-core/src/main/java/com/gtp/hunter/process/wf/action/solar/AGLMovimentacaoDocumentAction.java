package com.gtp.hunter.process.wf.action.solar;

import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.jsonstubs.AGLDocModelForm;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.AddressField;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentTransport;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.websocket.session.ActionSession;
import com.gtp.hunter.process.wf.process.BaseProcess;
import com.gtp.hunter.process.wf.process.ContinuousProcess;
import com.gtp.hunter.process.wf.process.interfaces.UserTransporterInterface;

public class AGLMovimentacaoDocumentAction extends AGLDocumentAction {

	private static final String				PROCESS_ID_KEY		= "PROCID";
	private static final String				PROCESS_SESSION_KEY	= "PS";

	private transient static final Logger	logger				= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public AGLMovimentacaoDocumentAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	protected void open(Action t) {
		Profiler prof = new Profiler("Movimentacao Profiler");
		logger.debug("Movimentação Document Action");
		Session session = getAs().getBaseSession();
		Map<String, Object> params = JsonUtil.jsonToMap(t.getDefparams());
		UUID token = UUID.fromString((String) params.get("process-id"));
		Document d = getDoc();
		AGLDocModelForm doc = getTranslate();
		ActionSession as = new ActionSession(session);

		prof.step("Convert to JSON and get Doc", false);
		for (DocumentTransport dtr : d.getTransports()) {
			//			UUID parentId = doc.getAddresses().parallelStream().filter(ada -> ada.getId().equals(adtr.getOrigin_id())).map(ada -> UUID.fromString(ada.getParent_id())).findAny().orElse(UUID.randomUUID());
			//			Map<Integer, List<UUID>> allocMap = getRegSvc().getWmsSvc().listAddresStock(parentId);
			//			List<UUID> thIds = allocMap.entrySet().parallelStream()
			//							.filter(en -> en.getKey() != 1)
			//							.flatMap(en -> en.getValue()
			//											.parallelStream())
			//							.collect(Collectors.toList());
			//
			//			if (thIds.size() > 0) {
			//				List<Thing> ret = getRegSvc().getThSvc().listById(thIds);
			//				Set<String> expSet = ret.parallelStream()
			//								.flatMap(th -> th.getSiblings().parallelStream())
			//								.flatMap(ts -> ts.getProperties().parallelStream())
			//								.filter(pr -> pr.getField().getMetaname().equals("LOT_EXPIRE"))
			//								.map(pr -> new SimpleDateFormat("dd/MM/yyyy").format(parseDate(pr.getValue())))
			//								.collect(Collectors.toSet());
			//				boolean multiExpiry = expSet.size() > 1;
			boolean multiExpiry = getRegSvc().getWmsSvc().isMultiExpiry(dtr.getAddress().getParent().getId());

			if (multiExpiry) {
				doc.getThings().parallelStream()
								//									.filter(adt -> adt.getId().equals(adtr.getThing_id()))
								.filter(adt -> adt.getId().equals(dtr.getThing().getId().toString()))
								.findAny()
								.get()
								.getProps()
								.put("payload", "ALERT");
			}
			//			}
		}
		;
		prof.step("Check Expire", false);
		if (d.getTransports().isEmpty()) {
			d.setStatus("CANCELADO");
			getRegSvc().getDcSvc().persist(d);
		} else if (d.getTransports().parallelStream()
						.allMatch(dtr -> dtr.getThing().getAddress() == null || dtr.getThing().getAddress().getId().equals(dtr.getAddress().getId()))) {
			d.setStatus("SUCESSO");
			createComplete(d);
			getRegSvc().getWmsSvc().completeOrdMov(d);
		}
		getAs().onNext(doc);
		prof.step("Send Action Network", false);
		if (getRsm().getPsm().getProcesses().containsKey(token)) {
			logger.info(getUser().getName() + " CONNECTING ON PROCESS " + token + " Open? " + session.isOpen());
			BaseProcess p = getRsm().getPsm().getProcesses().get(token);
			String tagId = (String) params.get("forklift-tag-id");

			if (getUser().getProperties().containsKey("forklift-tag"))
				tagId = getUser().getProperties().get("forklift-tag");
			else if (getUser().getProperties().containsKey("rtls-tag"))
				tagId = getUser().getProperties().get("rtls-tag");
			if (p instanceof UserTransporterInterface)
				((UserTransporterInterface) p).setUserTransporter(getUser());
			session.getUserProperties().put(PROCESS_SESSION_KEY, as);
			session.getUserProperties().put(PROCESS_ID_KEY, token.toString());
			logger.info(getUser().getName() + " using tagId " + tagId);
			p.getFilterByTagId(tagId).subscribe(as);
			prof.step("Subscribe Process", false);
		} else {
			logger.info("NAO AUTORIZADO " + token);
			closeSession(as, new CloseReason(CloseCodes.CANNOT_ACCEPT, "Não Autorizado: " + token));
		}
		prof.done("Action Opened", false, true);
	}

	@Override
	public void onMessage(Object msg) {
		Profiler prof = new Profiler("Action Message");
		String json = (String) msg;

		try {
			Document tmp = this.salvadoc(json);
			logger.info(prof.step("Document Converted", false));
			Document d = getRegSvc().getDcSvc().findById(tmp.getId());
			logger.info(prof.step("Document Loaded, Status: " + d.getStatus(), false));

			getAs().onNext(IntegrationReturn.OK);
			for (DocumentTransport dtr : d.getTransports()) {
				Thing t = getRegSvc().getThSvc().findById(dtr.getThing().getId());
				Address src = t.getAddress();
				Address dst = dtr.getAddress();

				logger.info("Origin: " + src.getName() + " Destination: " + dst.getName() + " Equal: " + (src.getId().equals(dst.getId())));
				if (!src.getId().equals(dst.getId()) && !src.getSiblings().isEmpty()) {
					Address srcAdd = src.getSiblings().stream().sorted((a1, a2) -> {
						Optional<AddressField> optOrder1 = a1.getFields().stream().filter(af -> af.getModel().getMetaname().equalsIgnoreCase("ROAD_SEQ")).findFirst();
						Optional<AddressField> optOrder2 = a2.getFields().stream().filter(af -> af.getModel().getMetaname().equalsIgnoreCase("ROAD_SEQ")).findFirst();
						if (optOrder1.isPresent() && optOrder2.isPresent()) {
							String sOrder1 = optOrder1.get().getValue();
							String sOrder2 = optOrder2.get().getValue();
							int order1 = Integer.parseInt(sOrder1);
							int order2 = Integer.parseInt(sOrder2);

							return order2 - order1;
						} else if (optOrder1.isPresent()) {
							return -1;
						} else if (optOrder2.isPresent()) {
							return 1;
						} else {
							return 0;
						}
					}).findFirst().get();
					logger.info("Setting thing " + dtr.getThing().getId() + " to last sibling address " + srcAdd.getId());
					dtr.getThing().setAddress(srcAdd);
				}
			}
			if (d.getStatus().equals("SUCESSO")) {
				createComplete(d);
				logger.info(prof.step("Document Sent to WMS", false));
			}
			getRegSvc().getWmsSvc().completeOrdMov(d);
		} catch (Exception e) {
			getAs().onNext(new IntegrationReturn(false, "JSON MAL FORMADO - " + msg));
			e.printStackTrace();
		}
		prof.done("Action Message Processed", false, false).forEach(logger::info);
	}

	@OnClose
	@Override
	public void close(Session ss, CloseReason cr) {
		logger.info("FECHANDO PROCESS: " + cr.getReasonPhrase());

		if (ss.getUserProperties().containsKey(PROCESS_ID_KEY)) {
			BaseProcess p = getRsm().getPsm().getProcesses().get(UUID.fromString((String) ss.getUserProperties().get(PROCESS_ID_KEY)));

			if (p instanceof UserTransporterInterface)
				((UserTransporterInterface) p).removeUserTransporter(getUser());
			if (p instanceof ContinuousProcess) {
				logger.debug("Process is continuous, dont stop it");
			} else if (!p.isComplete()) {
				logger.debug("MANDANDO O FAILURE DO PROCESSO");
				p.setFailure("CONEXÃO WEBSOCKET ENCERRADA");
				p.finish();
			}
		}
	}

	private void createComplete(Document d) {
		if (d.getSiblings().parallelStream().noneMatch(ds -> ds.getModel().getMetaname().equals("APOCOMPLETEMOV"))) {
			Executors.newFixedThreadPool(1).execute(() -> {
				DocumentModel apoComplete = getRegSvc().getDmSvc().findByMetaname("APOCOMPLETEMOV");
				String prntCode = d.getCode().replaceAll("[A-Z]", "");
				Document acomp = new Document(apoComplete, apoComplete.getName() + " " + prntCode, "CMP" + prntCode, "DEVICE");

				acomp.setParent(d);
				acomp.setUser(getUser());
				getRegSvc().getDcSvc().persist(acomp);
				d.getSiblings().add(acomp);
			});
		}
	}

	public static Date parseDate(String sdt) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US).parse(sdt);
		} catch (ParseException pe0) {
			try {
				return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'", Locale.US).parse(sdt);
			} catch (ParseException pe1) {
				try {
					return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.US).parse(sdt);
				} catch (ParseException pe2) {
					try {
						return new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(sdt);
					} catch (ParseException pe3) {
						try {
							return new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.US).parse(sdt);
						} catch (ParseException pe4) {
							try {
								return new SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(sdt);
							} catch (ParseException pe5) {
								return new Date();
							}
						}
					}
				}
			}
		}
	}
}
