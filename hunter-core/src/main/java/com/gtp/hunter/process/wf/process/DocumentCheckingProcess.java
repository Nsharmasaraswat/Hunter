package com.gtp.hunter.process.wf.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.ui.json.process.BaseProcessMessage;

public class DocumentCheckingProcess extends OriginProcess {

	@Inject
	private static transient Logger		logger;

	private String[]					params;
	private Document					container;
	private Document					parent;
	private String						statusWrong;
	private String						statusRight;
	private String						errorStatus;
	private String						errorNotInDoc;
	private String						docStatusSuccess;
	private List<Thing>					allThings	= new ArrayList<Thing>();
	private List<Thing>					wrongThings	= new ArrayList<Thing>();
	private List<Thing>					rightThings	= new ArrayList<Thing>();
	private Map<Product, List<Thing>>	countProds	= new HashMap<Product, List<Thing>>();

	@Override
	public void onInit() {
		params = getModel().getParam().split(",");
		if (params.length > 2) {
			this.container = getRegSvc().getDcSvc().findById(UUID.fromString(params[0]));
			if (this.container.getParent() != null) {
				this.parent = this.container.getParent();
			}
			getParametros().put("doc", this.container);
			getParametros().put("parent", this.parent);
			this.statusWrong = params[1];
			this.statusRight = params[2];
			this.errorStatus = params[3];
			this.errorNotInDoc = params[4];
			this.docStatusSuccess = params[5];
			Set<DocumentThing> things = new HashSet<DocumentThing>();
			things.addAll(this.container.getThings());
			if (this.parent != null)
				things.addAll(this.parent.getThings());
			things.forEach(dt -> {
				Thing t = dt.getThing();
				if (t.getStatus().equals(this.statusWrong)) {
					t.setUnitModel(getRegSvc().getUnSvc().getAllUnitById(t.getUnits()));
					this.allThings.add(t);
					t.getErrors().add(this.errorStatus);
					this.wrongThings.add(t);
				} else if (t.getStatus().equals(this.statusRight)) {
					t.setUnitModel(getRegSvc().getUnSvc().getAllUnitById(t.getUnits()));
					this.allThings.add(t);
					this.rightThings.add(t);
				}
			});

			this.container.getItems().forEach(di -> {
				List<Thing> prdTngs = this.rightThings.stream().filter(t -> t.getProduct().equals(di.getProduct())).collect(Collectors.toList());
				this.countProds.put(di.getProduct(), prdTngs);
			});

			this.countProds.keySet().stream().map(prd -> this.countProds.get(prd)).forEach(lst -> System.out.println(logPrefix() + lst.size()));
			// TODO: remove after parametrizing BaseSingleProcess
			this.autoValidate = true;
		}

	}

	@Override
	public void message(BaseProcessMessage msg) {
		logger.info("Message Received: " + msg.toString());
	}

	@Override
	protected void processAfter(Thing t) {
		System.out.println(logPrefix() + "Processando o ProcessAfter");
		if (this.allThings.contains(t)) {
			System.out.println(logPrefix() + "ACHEI O THING");
			if (this.wrongThings.contains(t)) {
				System.out.println(logPrefix() + "TAVA NO WRONG");
				this.wrongThings.remove(this.wrongThings.indexOf(t));
				this.rightThings.add(t);
				resend(t);
			}
		} else {
			System.out.println(logPrefix() + "NÃO ACHEI O THING - ");
			t.setCancelProcess(true);
			t.getErrors().add(this.errorStatus);
			resend(t);
			this.setFailure("NÃO ACHEI O THING");
			this.finish();
		}
	}

	@Override
	protected void success() {
		Set<Thing> param = new HashSet<>();

		for (Thing t : rightThings) {
			if (!validatedThings.contains(t)) {
				// t.setStatus(getModel().getEstadoPara());
				// persistThing(t);
				System.out.println(logPrefix() + "Validando Thing");
				getRegSvc().getThSvc().getThRep().quickUpdateThingStatus(t.getId(), getModel().getEstadoPara());
				param.add(t);
				validatedThings.add(t);
			} else {
				System.out.println(logPrefix() + "Thing já validado");
			}
		}
		if (allThings.size() > 0 && validatedThings.size() == allThings.size()) {
			// this.container.setStatus(getModel().getEstadoPara());
			// persistDoc(this.container);
			getRegSvc().getDcSvc().quickUpdateStatus(this.container.getId(), getModel().getEstadoPara());
		}
		getParametros().put("itens", param);
	}

	@Override
	protected void failure() {

	}

	@Override
	protected void connect() {
		System.out.println(logPrefix() + "Enviando Errados: " + this.wrongThings.size());
		this.wrongThings.stream().forEach(t -> resend(t));
		System.out.println(logPrefix() + "Enviando Certos: " + this.rightThings.size());
		this.rightThings.stream().forEach(t -> resend(t));
	}

	@Override
	protected void processBefore(ComplexData rd) {

	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

}
