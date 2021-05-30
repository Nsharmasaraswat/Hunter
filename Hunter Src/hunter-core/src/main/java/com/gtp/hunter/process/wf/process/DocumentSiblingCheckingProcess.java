package com.gtp.hunter.process.wf.process;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.wf.process.activity.ProcessActivityPhase;
import com.gtp.hunter.ui.json.process.BaseProcessMessage;

public class DocumentSiblingCheckingProcess extends OriginProcess {

	@Inject
	private static transient Logger		logger;

	private String[]					params;
	private Document					container;
	private List<Document>				children;
	private List<DocumentItem>			items;
	private String						statusWrong;
	private String						statusRight;
	private String						errorStatus;
	private String						errorNotInDoc;
	private String						docStatusSuccess;
	private List<Thing>					allThings	= new CopyOnWriteArrayList<>();
	private List<Thing>					wrongThings	= new CopyOnWriteArrayList<>();
	private List<Thing>					rightThings	= new CopyOnWriteArrayList<>();
	private Map<Product, List<Thing>>	countProds	= new HashMap<Product, List<Thing>>();

	// public DocumentCheckingProcess(Process model, RegisterService tRep, BaseOrigin origin, ProcessStreamManager psm) {
	// super(model, tRep, origin, psm);
	// }

	@Override
	public void onInit() {
		this.items = new CopyOnWriteArrayList<>();
		this.params = getModel().getParam().split(",");
		this.validationTimer = new Timer();
		if (params.length > 2) {
			Set<DocumentThing> things = new HashSet<DocumentThing>();

			this.container = getRegSvc().getDcSvc().findById(UUID.fromString(params[0]));
			this.children = getRegSvc().getDcSvc().listByParent(container.getId());
			getParametros().put("doc", this.container);
			this.statusWrong = params[1];
			this.statusRight = params[2];
			this.errorStatus = params[3];
			this.errorNotInDoc = params[4];
			this.docStatusSuccess = params[5];
			this.validationDelay = params.length > 6 ? Long.parseLong(params[6]) : 7000;
			if (this.container.getThings() != null)
				things.addAll(this.container.getThings());
			if (this.container.getItems() != null)
				items.addAll(this.container.getItems());
			if (this.children != null)
				this.children.forEach(d -> {
					for (DocumentItem di : d.getItems()) {
						for (DocumentThing dt : d.getThings()) {
							if (dt.getThing().getProduct().equals(di.getProduct())) {
								di.getThings().add(dt.getThing());
							}
						}
						items.add(di);
					}
					things.addAll(d.getThings());
				});
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
			System.out.println(logPrefix() + "NÃO ACHEI O THING");
			t.setCancelProcess(true);
			t.getErrors().add(this.errorStatus);
			resend(t);
			this.setFailure("NÃO ACHEI O THING");
			this.finish();
		}
	}

	// TODO: Fix send per children
	@Override
	protected void success() {
		System.out.println(logPrefix() + "SibblingProcess: SUCCESS BEGIN");
		Map<Document, Set<Thing>> toSendThingsPerChildren = new HashMap<>();

		for (Thing t : rightThings) {
			if (!validatedThings.contains(t)) {
				Set<Thing> param;
				Document child = null;

				System.out.println(logPrefix() + "SibblingProcess: SEARCHING DOCUMENT");
				for (Document d : this.children) {
					for (DocumentThing dt : d.getThings()) {
						if (dt.getThing().equals(t)) {
							System.out.println(logPrefix() + "SibblingProcess: DOCUMENT FOUND: " + d.getName());
							child = d;
							break;
						}
					}

				}
				if (child == null)// fodeu, q q esse item ta fazendo aqui?
					continue;
				if (toSendThingsPerChildren.containsKey(child)) {
					System.out.println(logPrefix() + "SibblingProcess: FOUND MAP");
					param = toSendThingsPerChildren.get(child);
				} else {
					System.out.println(logPrefix() + "SibblingProcess: NOT ON MAP");
					param = new HashSet<>();
				}
				getRegSvc().getThSvc().getThRep().quickUpdateThingStatus(t.getId(), getModel().getEstadoPara());
				getRegSvc().getDtSvc().quickInsert(this.container.getId(), t.getId(), getModel().getEstadoPara());
				param.add(t);
				toSendThingsPerChildren.put(child, param);
				validatedThings.add(t);
			} else {
				System.out.println(logPrefix() + "Thing já validado");
			}
		}
		if (allThings.size() > 0 && validatedThings.size() == allThings.size()) {
			this.container.setStatus(getModel().getEstadoPara());
			getRegSvc().getDcSvc().quickUpdateStatus(this.container.getId(), this.docStatusSuccess);
		}
		toSendThingsPerChildren.keySet().forEach(d -> {
			Set<Thing> param = toSendThingsPerChildren.get(d);

			getParametros().remove("doc");
			getParametros().remove("itens");
			getParametros().put("doc", d);
			getParametros().put("itens", param);
			filters.stream().filter(a -> a.getModel().getPhase().equals(ProcessActivityPhase.INTEGRATION)).forEach(a -> {
				System.out.println(logPrefix() + "SibblingProcess: RUNNING INTEGRATION FOR " + d.getName() + " Items: " + param.size());
				a.execute(this);
			});
			getParametros().remove("itens");
		});
		getParametros().put("doc", this.container);
		System.out.println(logPrefix() + "SibblingProcess: SUCCESS END");
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
