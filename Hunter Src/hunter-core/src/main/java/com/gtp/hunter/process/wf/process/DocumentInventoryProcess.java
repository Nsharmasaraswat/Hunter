package com.gtp.hunter.process.wf.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.ui.json.process.BaseProcessMessage;

public class DocumentInventoryProcess extends OriginProcess {

	private Document			doc;
	private List<DocumentItem>	inventory	= new ArrayList<DocumentItem>();
	private String				thingStatusActual;
	private String				thingStatusSuccess;

	// public DocumentInventoryProcess(Process model, RegisterService tRep, BaseOrigin origin, ProcessStreamManager psm) {
	// super(model, tRep, origin, psm);
	// }

	@Override
	public void onInit() {
		Profiler p = new Profiler();
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
			inventory.add(di);
		}
		// TODO: remove after parametrizing BaseSingleProcess
		this.autoValidate = false;
		p.done("End Init", true, false);
	}

	@Override
	protected void connect() {
		Profiler p = new Profiler();

		for (DocumentItem di : inventory) {
			resend(di);
		}
		p.done("End", true, false);
	}

	@Override
	protected void processBefore(ComplexData rd) {

	}

	@Override
	public void message(BaseProcessMessage msg) {
	}

	@Override
	protected void processAfter(Thing rd) {
		Profiler p = new Profiler();

		rd.getUnitModel().stream().forEach(u -> p.step("Thing: " + u.getTagId(), true));
		p.step("Stream iteration", true);
		for (DocumentItem di : inventory) {
			p.step("Verificando se " + rd.getProduct().getName() + " = " + di.getProduct().getName(), true);
			if (di.getProduct().getId().equals(rd.getProduct().getId())) {
				p.step("É IGUAL", true);
				if (!di.getThings().contains(rd)) {
					p.step("Thing Novo", true);
					if (di.getThings().size() >= di.getQty()) {
						p.step("KABUUUUMMMMM", true);
						setFailure("PRODUTO A MAIS");
						rd.getErrors().add("PRODUTO A MAIS");
						rd.setCancelProcess(true);
					} else {
						rd.getErrors().add("Picking não realizado");
					}
					p.step("Thing Adicionado", true);
					di.getThings().add(rd);
					resend(di);
				}
			}
		}
		// resend(rd);
		p.done("End", true, false);
	}

	@Override
	protected void success() {
		Profiler p = new Profiler();
		Set<Thing> param = new HashSet<>();

		boolean docCompleto = true;
		for (DocumentItem di : inventory) {
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
			p.step("Loop", true);
		}
		if (docCompleto) {
			this.doc.setStatus(getModel().getEstadoPara());
		}
		persistDoc(this.doc);
		getParametros().put("doc", this.doc);
		getParametros().put("itens", param);
		p.done("End", true, false);
	}

	@Override
	protected void failure() {

	}

	@Override
	public void cancel() {

	}

}
