package com.gtp.hunter.process.wf.action;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.model.RawData.RawDataType;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.websocket.session.ProcessSession;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.BaseProcess;
import com.gtp.hunter.process.wf.process.ContinuousProcess;

public class AttachProcessWSAction extends DocumentAction {

	private static final String	PROCESS_ID_KEY		= "PROCID";
	private static final String	PROCESS_SESSION_KEY	= "PS";

	@Inject
	private static Logger		logger				= LoggerFactory.getLogger(AttachProcessWSAction.class);

	public AttachProcessWSAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	protected void open(Action t) {
		logger.debug("AttachProcessWS Document Action " + t.getName());
		Map<String, Object> params = JsonUtil.jsonToMap(t.getDefparams());
		UUID procId = UUID.fromString((String) params.get("process-id"));
		Session session = getAs().getBaseSession();
		getAs().onNext(getDoc());

		if (getRsm().getPsm().getProcesses().containsKey(procId)) {
			logger.info(getUser().getName() + " CONNECTING ON PROCESS " + procId + " Open? " + session.isOpen());
			ProcessSession ps = new ProcessSession(session);

			session.getUserProperties().put(PROCESS_SESSION_KEY, ps);
			session.getUserProperties().put(PROCESS_ID_KEY, procId.toString());
			getRsm().getPsm().getProcesses().get(procId).getFilterByDocument(getDoc().getId()).distinct().subscribe(ps);
		} else {
			logger.info("NAO AUTORIZADO " + procId);
			closeSession(getAs(), new CloseReason(CloseCodes.CANNOT_ACCEPT, "Não Autorizado: " + procId));
		}
	}

	@Override
	public void onMessage(Object msg) {
		UUID procId = UUID.fromString((String) getAs().getBaseSession().getUserProperties().get(PROCESS_ID_KEY));
		BaseProcess bp = getRsm().getPsm().getProcesses().get(procId);
		Thing t = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson((String) msg, Thing.class);

		//Fucking Modafoca time of the infernos
		ComplexData cd = new ComplexData();
		BaseOrigin org = getRsm().getOsm().getOrigin(bp.getModel().getOrigin().getId());
		String sourceMeta = bp.getModel().getOrigin().getFeatures().stream().findFirst().get().getSource();
		String deviceMeta = bp.getModel().getOrigin().getFeatures().stream().findFirst().get().getDevice();
		Unit u = t.getUnitModel().stream().findAny().get();

		cd.setTagId(u.getTagId());
		cd.setSource(getRegSvc().getSrcSvc().findByMetaname(sourceMeta).getId());
		cd.setDevice(getRegSvc().getDevSvc().findByMetaname(cd.getSource(), deviceMeta).getId());
		cd.setPayload("");
		cd.setPort(0);
		cd.setType(RawDataType.IDENT);
		cd.setUnit(u);
		org.getOrigin().onNext(cd);
		getAs().onNext(IntegrationReturn.OK);
	}

	@Override
	protected void close(Session ss, CloseReason cr) {
		logger.info("FECHANDO PROCESS: " + cr.getReasonPhrase());
		ProcessSession ps = (ProcessSession) ss.getUserProperties().get(PROCESS_SESSION_KEY);

		if (ss.getUserProperties().containsKey(PROCESS_ID_KEY)) {
			BaseProcess p = getRsm().getPsm().getProcesses().get(UUID.fromString((String) ss.getUserProperties().get(PROCESS_ID_KEY)));

			if (p instanceof ContinuousProcess) {
				logger.debug("Process is continuous, dont stop it");
			} else if (!p.isComplete()) {
				logger.debug("MANDANDO O FAILURE DO PROCESSO");
				p.setFailure("CONEXÃO WEBSOCKET ENCERRADA");
				p.finish();
			}
		}
		ps.close(cr);
	}
}
