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
import com.gtp.hunter.process.jsonstubs.AGLDocModelField;
import com.gtp.hunter.process.jsonstubs.AGLDocModelProps;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;

public class AGLChecklistSaidaDocumentAction extends AGLDocumentAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public AGLChecklistSaidaDocumentAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
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
			Document d = getRegSvc().getDcSvc().findById(UUID.fromString(obj.getId()));
			Unit u = null;

			for (AGLDocModelField a : obj.getProps()) {
				if (a.getAttrib().equalsIgnoreCase("AttRastreador")) {
					u = getRegSvc().getUnSvc().findByTagId(a.getValue());
					break;
				}
			}
			if (u != null) {
				Document tmpD = getRegSvc().getDcSvc().quickFindParentDoc(obj.getId().toString());
				Document transp = getRegSvc().getDcSvc().findById(tmpD.getId());
				String truckId = Documents.getStringField(transp, "TRUCK_ID");
				Thing truck = getRegSvc().getThSvc().findById(UUID.fromString(truckId));

				if (truck.getUnits().contains(u.getId())) {
					DocumentModel dm = getRegSvc().getDmSvc().findByMetaname("APOSAIDA");
					Document apo = new Document(dm, dm.getName() + " " + transp.getCode(), "SAIDA" + transp.getCode(), "NOVO");

					getRegSvc().getWmsSvc().checkoutThings(transp);
					truck.getUnits().remove(u.getId());
					//getRegSvc().getThSvc().persist(t);
					getRegSvc().getThSvc().quickRemoveUnit(truck.getId(), u.getId());
					transp.setStatus("LIBERADO");
					apo.setUser(getUser());
					apo.setParent(transp);
					getRegSvc().getDcSvc().persist(apo);
					getRegSvc().getDcSvc().persist(transp);
					d.setStatus("PREENCHIDO");
					d.setUser(getUser());
					getRegSvc().getDcSvc().persist(d);
					getRegSvc().getAglSvc().sendDocToWMS(transp, "PUT");
					getAs().onNext(IntegrationReturn.OK);
				} else {
					getAs().onNext(new IntegrationReturn(false, "Rastreador não vinculado ao Transporte."));
				}
			} else {
				getAs().onNext(new IntegrationReturn(false, "Rastreador não encontrado"));
			}
		} catch (Exception e) {
			getAs().onNext(new IntegrationReturn(false, e.getLocalizedMessage()));
			logger.error("JSON MAL FORMADO - " + msg);
			e.printStackTrace();
		}
	}

	@OnClose
	@Override
	protected void close(Session ss, CloseReason cr) {
		logger.info("Closed: " + cr.getReasonPhrase());
	}
}
