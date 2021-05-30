package com.gtp.hunter.process.wf.action.solar;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.websocket.session.ActionSession;
import com.gtp.hunter.process.wf.action.DocumentAction;
import com.gtp.hunter.process.wf.process.BaseProcess;
import com.gtp.hunter.process.wf.process.ContinuousProcess;
import com.gtp.hunter.process.wf.process.interfaces.UserTransporterInterface;

public class AGLSeparacaoDocumentAction extends DocumentAction {

	private static final String				PROCESS_ID_KEY		= "PROCID";
	private static final String				PROCESS_SESSION_KEY	= "PS";

	private transient static final boolean	PROFILE				= false;
	private transient static final Logger	logger				= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static boolean					checkingResupply	= false;

	public AGLSeparacaoDocumentAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	protected void open(Action t) {
		Profiler pf = new Profiler("Separação Document Action");
		Session session = getAs().getBaseSession();
		Map<String, Object> params = JsonUtil.jsonToMap(t.getDefparams());
		UUID procId = UUID.fromString((String) params.get("process-id"));
		ActionSession as = new ActionSession(session);
		Document d = getDoc().getSiblings().stream().filter(ds -> ds.getModel().getMetaname().equals((String) params.get("sbling-meta"))).findAny().orElse(null);

		if (d != null) {
			Map<Product, Double> prdMap = new HashMap<>();

			logger.info(pf.step("Got Document", PROFILE));
			for (DocumentItem di : d.getItems()) {//Buscar locais recentes dos produtos
				List<String> pkAddList = getRegSvc().getWmsSvc().listPickingByProduct(di.getProduct().getId());
				String addId = pkAddList.isEmpty() ? "" : pkAddList.get(0);

				if (addId.isEmpty()) {
					Address tmp = getRegSvc().getAddSvc().findRandomByModelFieldValue("SKU", di.getProduct().getSku());//SHIT MODAFOCA GAMBS!

					addId = tmp == null ? "" : tmp.getId().toString();
				}
				if (!addId.isEmpty()) {
					di.getProperties().put("ADDRESS_ID", addId);
					getRegSvc().getDiSvc().persist(di);
					logger.info(pf.step("DI Persisted", PROFILE));
				}
				if (prdMap.containsKey(di.getProduct()))
					prdMap.put(di.getProduct(), prdMap.get(di.getProduct()) + di.getQty());
				else
					prdMap.put(di.getProduct(), di.getQty());
			}
			logger.info(pf.step("Updated Item Addresses", PROFILE));
			getRegSvc().getWmsSvc().checkResupply(prdMap);
			getAs().onNext(d);
			logger.info(pf.step("Sent to Device", PROFILE));
			if (getRsm().getPsm().getProcesses().containsKey(procId)) {
				logger.info(getUser().getName() + " CONNECTING ON PROCESS " + procId + " Open? " + session.isOpen());
				BaseProcess p = getRsm().getPsm().getProcesses().get(procId);
				String tagId = (String) params.get("tag-id");

				if (getUser().getProperties().containsKey("rtls-tag"))
					tagId = getUser().getProperties().get("rtls-tag");
				if (p instanceof UserTransporterInterface)
					((UserTransporterInterface) p).setUserTransporter(getUser());
				session.getUserProperties().put(PROCESS_SESSION_KEY, as);
				session.getUserProperties().put(PROCESS_ID_KEY, procId.toString());
				logger.info(getUser().getName() + " using tagId " + tagId);
				p.getFilterByTagId(tagId).subscribe(as);
			} else {
				logger.info("NAO AUTORIZADO " + procId);
				closeSession(as, new CloseReason(CloseCodes.CANNOT_ACCEPT, "Não Autorizado: " + procId));
			}
		} else
			closeSession(as, new CloseReason(CloseCodes.CANNOT_ACCEPT, "Romaneio Não Liberado!"));
		pf.done("Got Document", PROFILE, PROFILE);
	}

	@Override
	@Transactional(value = TxType.REQUIRES_NEW)
	public void onMessage(Object msg) {
		Profiler prof = new Profiler("Action Message");
		String json = (String) msg;

		try {
			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
			Document tmp = gson.fromJson(json, Document.class);

			logger.info(prof.step("Document Converted", PROFILE));
			Document picking = getRegSvc().getDcSvc().findParent(tmp.getId());
			Document d = picking.getSiblings().parallelStream()
							.filter(ds -> ds.getId().equals(tmp.getId()))
							.findAny()
							.get();
			logger.info(prof.step("Document Loaded, Status: " + d.getStatus(), PROFILE));

			getAs().onNext(IntegrationReturn.OK);
			d.setStatus(tmp.getStatus());
			d.setUser(getUser());
			if (tmp.getStatus().equals("SUCESSO")) {
				//SEPARAÇAO COMPELTA
				logger.info(prof.step("Find Picking, Status: " + picking.getStatus(), PROFILE));
				List<DocumentThing> dtList = new ArrayList<>();
				for (DocumentThing dtmp : tmp.getThings()) {
					Thing t = saveThing(dtmp.getThing());
					DocumentThing dt = new DocumentThing(d, t, "SEPARADO");

					for (Thing tmps : dtmp.getThing().getSiblings()) {
						Thing ts = saveThing(tmps);

						ts.setParent(t);
						t.getSiblings().add(ts);
					}
					d.getThings().add(dt);
					dtList.add(new DocumentThing(picking, t, "SEPARADO"));
				}
				getRegSvc().getDtSvc().multiPersist(dtList);
				logger.info(prof.step("Update Things", PROFILE));
				getRegSvc().getWmsSvc().consumePickingQuantity(d);
				logger.info(prof.step("Update Items", PROFILE));
				d.getThings().stream()
								.map(dt -> dt.getThing())
								.forEach(t -> {
									try {
										IntegrationReturn iRet = getRegSvc().getAglSvc().sendThingToWMS(t, "POST").get();

										if (iRet.isResult()) {
											t.getSiblings().forEach(ts -> {
												ts.setStatus("SEPARADO");
												getRegSvc().getWmsSvc().updateThingStatus(ts.getId(), "SEPARADO");
											});
											t.setStatus("SEPARADO");
											getRegSvc().getWmsSvc().updateThingStatus(t.getId(), "SEPARADO");
										}
									} catch (ExecutionException | InterruptedException ioe) {
										ioe.printStackTrace();
									}
									getRegSvc().getThSvc().persist(t);
								});
				logger.info(prof.step("Post Things to WMS", PROFILE));
				getRegSvc().getDtSvc().multiPersist(d.getThings());
				logger.info(prof.step("Persist OSG Thing", PROFILE));

				picking.setStatus("SEPARADO");
				picking.setUser(getUser());
			}
			getRegSvc().getDcSvc().persist(picking);
			logger.info(prof.step("Picking Persisted", PROFILE));
			if (!checkingResupply) {
				checkingResupply = true;
				Executors.newSingleThreadExecutor().submit(() -> {
					getRegSvc().getWmsSvc().checkResupplyMin();
					checkingResupply = false;
				});
			}
		} catch (Exception e) {
			getAs().onNext(new IntegrationReturn(false, "JSON MAL FORMADO - " + msg));
			e.printStackTrace();
		}
		prof.done("Action Message Processed", PROFILE, PROFILE);
	}

	@Transactional(value = TxType.REQUIRED)
	private Thing saveThing(Thing ttmp) {
		Address add = getRegSvc().getAddSvc().findById(ttmp.getAddress().getId());
		Product prd = getRegSvc().getPrdSvc().findById(ttmp.getProduct().getId());
		Thing ret = new Thing(prd.getName(), prd, prd.getModel().getPropertymodel(), "INVENTARIO");

		ret.setAddress(add);
		for (Property prtmp : ttmp.getProperties()) {
			PropertyModelField pmf = getRegSvc().getPrmfSvc().findById(UUID.fromString(prtmp.getModelfield_id()));
			Property pr = new Property(ret, pmf, prtmp.getValue(), "NOVO");

			ret.getProperties().add(pr);
		}
		return getRegSvc().getThSvc().persist(ret);
	}

	@OnClose
	@Override
	public void close(Session ss, CloseReason cr) {
		logger.info("FECHANDO PROCESS: " + cr.getReasonPhrase());

		if (ss.getUserProperties().containsKey(PROCESS_SESSION_KEY)) {
			ActionSession as = (ActionSession) ss.getUserProperties().get(PROCESS_SESSION_KEY);

			as.onComplete();
		}
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
}
