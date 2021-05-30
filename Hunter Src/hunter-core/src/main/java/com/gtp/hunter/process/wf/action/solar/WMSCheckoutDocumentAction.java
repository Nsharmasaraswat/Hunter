package com.gtp.hunter.process.wf.action.solar;

import java.lang.invoke.MethodHandles;
import java.util.stream.Collectors;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.DocumentAction;

public class WMSCheckoutDocumentAction extends DocumentAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public WMSCheckoutDocumentAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	protected void open(Action t) {
		logger.debug("WMS Checkout Document Action");
		getAs().onNext(getDoc());
	}

	@Override
	public void onMessage(Object msg) {
		Profiler p = new Profiler("WMSCheckoutDocumentAction");
		String json = (String) msg;
		Document tmp = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(json, Document.class);
		p.step("Serialize", false);
		Document dtmp = getRegSvc().getDcSvc().quickFindParentDoc(tmp.getId());
		String lcrCode = "LCR" + dtmp.getCode();
		p.step("Transport", false);
		Document d = getRegSvc().getDcSvc().findById(tmp.getId());
		p.step("CheckoutPortaria", false);
		Document lcr = getRegSvc().getDcSvc().findByTypeAndCode("APOLACRE", lcrCode);
		p.step("ApoLacre", false);
		String lacresExp = lcr.getFields().parallelStream()
						.filter(df -> df.getField().getMetaname().startsWith("ATTLACRE") && !df.getValue().isEmpty())
						.map(df -> df.getValue().trim())
						.sorted()
						.collect(Collectors.joining(","));
		String lacresPort = tmp.getFields().parallelStream()
						.filter(df -> df.getField().getMetaname().startsWith("ATTLACRE") && !df.getValue().isEmpty())
						.map(df -> df.getValue().trim())
						.sorted()
						.collect(Collectors.joining(","));
		p.step("Data", false);

		if (lacresExp.equals(lacresPort)) {
			for (DocumentField tdf : tmp.getFields()) {
				DocumentModelField tdmf = getRegSvc().getDmfSvc().findById(tdf.getField().getId());
				DocumentField docField = d.getFields().parallelStream()
								.filter(df -> df.getField().getId().equals(tdmf.getId()))
								.findAny()
								.orElseGet(() -> new DocumentField(d, tdmf, "PREENCHIDO", tdf.getValue()));

				docField.setValue(tdf.getValue());
				d.getFields().add(docField);
			}

			d.setStatus("PREENCHIDO");
			d.setUser(getUser());
			getRegSvc().getDcSvc().persist(d);
			p.step("Persist", false);
			getAs().onNext(IntegrationReturn.OK);
		} else {
			logger.warn(dtmp.getCode() + " Lacres Não Conferem: " + lacresExp + " != " + lacresPort);
			getAs().onNext(new IntegrationReturn(false, "Lacres Não Conferem " + lacresPort));
		}
		p.done("Response", false, true);
	}

	@OnClose
	@Override
	protected void close(Session ss, CloseReason cr) {
		logger.info("Closed: " + cr.getReasonPhrase());
	}
}
