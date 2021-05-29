package com.gtp.hunter.process.wf.process;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Thing;

public class SimpleContinuousCheckingProcess extends ContinuousProcess {

	@Inject
	private static transient Logger	logger;

	private String					statusFrom;
	private String					statusTo;
	private String					statusDocThingFrom;
	private String					statusDocThingTo;
	private String					metaDocModel;
	private String					wrongStatus;

	@Override
	public void onInit() {
		this.statusDocThingFrom = getParametros().get("statusDocThingFrom").toString();
		this.statusDocThingTo = getParametros().get("statusDocThingTo").toString();
		this.metaDocModel = getParametros().get("metaDocModel").toString().replaceAll("\"", "");
		this.statusFrom = getModel().getEstadoDe();
		this.statusTo = getModel().getEstadoPara();
		this.wrongStatus = getParametros().get("wrongStatus").toString();
	}

	@Override
	public void cancel() {

	}

	@Override
	protected void connect() {

	}

	@Override
	protected void processBefore(ComplexData rd) {

	}

	@Override
	protected void processAfter(Thing rd) {

		if (rd != null) {
			if (rd.getProduct() != null) {
				if (rd.getProduct().getModel() != null) {
					System.out.println(logPrefix() + "Processando Thing: " + rd.getProduct().getModel().getMetaname());
					if (!rd.getStatus().equals(this.statusFrom) && !rd.getStatus().equals(this.statusTo)) {
						this.lockdown("Problemas com o Status de Thing");
						rd.setCancelProcess(true);
						rd.getErrors().add(this.wrongStatus);
					}
				} else {
					System.out.println(logPrefix() + "Thing " + rd.getId() + " sem productmodel");
				}
			} else {
				System.out.println(logPrefix() + "Thing " + rd.getId() + " sem product");
			}
			rd.getUnitModel().stream().forEach(u -> System.out.println(logPrefix() + "UNIT: " + u.getTagId()));
		} else {
			System.out.println(logPrefix() + "RD NULO!!!!!");
		}

	}

	@Override
	public void timeout(Map<String, Thing> itens) {
		for (Thing t : itens.values()) {
			if (t.getStatus().equals(this.statusFrom)) {
				getRegSvc().getThSvc().getThRep().quickUpdateThingStatus(t.getId(), this.statusTo);
				System.out.println(logPrefix() + "Procurandoo dt c thing: " + t.getId() + " e c o dm: " + this.metaDocModel);
				DocumentThing dt = getRegSvc().getDtSvc().quickFindByThingIdAndDocModelMeta(t.getId(), this.metaDocModel);
				if (dt != null) {
					t.setDocument(dt.getDocument().getId());
				}
				if (dt != null && dt.getStatus().equals(this.statusDocThingFrom)) {
					getRegSvc().getDtSvc().quickUpdateStatus(dt.getId(), this.statusDocThingTo);
				}
			} else if (!t.getStatus().equals(this.statusTo)) {
				this.lockdown("Item ja processado");
				t.getErrors().add(this.wrongStatus);
			}
			resend(t);
		}
		System.out.println(logPrefix() + "Quantidade de Itens Recebidos: " + itens.size());
	}

	@Override
	protected void success() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void failure() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void checkParams() throws Exception {
		if (!getParametros().containsKey("wrongStatus"))
			throw new Exception("Parâmetro 'wrongStatus' não encontrado.");
		if (!getParametros().containsKey("statusDocThingFrom"))
			throw new Exception("Parâmetro 'statusDocThingFrom' não encontrado.");
		if (!getParametros().containsKey("statusDocThingTo"))
			throw new Exception("Parâmetro 'statusDocThingTo' não encontrado.");
		if (!getParametros().containsKey("metaDocModel"))
			throw new Exception("Parâmetro 'metaDocModel' não encontrado.");
	}

	@Override
	protected void processUnknown(ComplexData rd) {
		// TODO Auto-generated method stub

	}

}
