package com.gtp.hunter.process.wf.process;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.ui.json.process.BaseProcessMessage;

public class DocumentPickingProcess extends OriginProcess {

	@Inject
	private static transient Logger	logger;

	private Document				doc;
	private List<DocumentItem>		picking	= new ArrayList<DocumentItem>();
	private String					thingStatusActual;
	private String					thingStatusSuccess;

	// public DocumentPickingProcess(Process model, RegisterService tRep, BaseOrigin origin, ProcessStreamManager psm) {
	// super(model, tRep, origin, psm);
	// }

	@Override
	public void onInit() {
		String[] params = getModel().getParam().split(",");
		this.doc = getRegSvc().getDcSvc().findById(UUID.fromString(params[0]));
		this.thingStatusActual = params[1];
		this.thingStatusSuccess = params[2];
		for (DocumentItem di : doc.getItems()) {
			for (DocumentThing dt : doc.getThings()) {
				if (dt.getThing().getProduct().equals(di.getProduct())) {
					di.getThings().add(dt.getThing());
				}
			}
			picking.add(di);
		}
		// TODO: remove after parametrizing BaseSingleProcess
		this.autoValidate = false;
	}

	@Override
	protected void connect() {
		for (DocumentItem di : picking) {
			resend(di);
		}
	}

	@Override
	protected void processBefore(ComplexData rd) {

	}
	
	@Override
	public void message(BaseProcessMessage msg) {
		logger.info("Message Received: " + msg.toString());
	}

	@Override
	protected void processAfter(Thing rd) {
		rd.getUnitModel().stream().forEach(u -> logger.debug("Thing: " + u.getTagId()));
		for (DocumentItem di : picking) {
			logger.debug("Verificando se " + rd.getProduct().getName() + " = " + di.getProduct().getName());
			if (di.getProduct().getId().equals(rd.getProduct().getId())) {
				if (!di.getThings().contains(rd)) {
					if (di.getThings().size() >= di.getQty()) {
						setFailure("PRODUTO A MAIS");
						rd.getErrors().add("PRODUTO A MAIS");
						rd.setCancelProcess(true);
					} else {
						rd.getErrors().add("Picking não realizado");
					}
					logger.debug("Thing Adicionado");
					di.getThings().add(rd);
					resend(di);
				}
			}
		}
		// resend(rd);
	}

	@Override
	protected void success() {
		ArrayList param = new ArrayList();

		boolean docCompleto = true;
		for (DocumentItem di : picking) {
			for (Thing t : di.getThings()) {
				boolean cadastrar = true;
				for (DocumentThing dt : this.doc.getThings()) {
					if (dt.getThing().getId().equals(t.getId())) {
						cadastrar = false;
						break;
					}
				}
				if (cadastrar) {
					t.setStatus(this.thingStatusSuccess);
					persistThing(t);
					DocumentThing dt = new DocumentThing(this.doc, t, thingStatusSuccess);
					persistDocThing(dt);
					doc.getThings().add(dt);
					param.add(t);
				}
				if (di.getThings().size() != di.getQty()) {
					docCompleto = false;
				}
			}
		}
		if (docCompleto) {
			this.doc.setStatus(getModel().getEstadoPara());
		}
		persistDoc(this.doc);
		getParametros().put("doc", this.doc);
		getParametros().put("itens", param);
	}

	@Override
	protected void failure() {

	}

	@Override
	public void cancel() {

	}

}
