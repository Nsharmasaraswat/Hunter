package com.gtp.hunter.process.wf.process;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.Thing;

public class MobilePickingProcess extends ContinuousProcess {

	private Logger		logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private String		statusFrom;
	private String		statusTo;
	private String		statusDocTo;
	private String		statusDocThingTo;
	private String		metaDocModel;
	private String		unitDocProperty;
	private String		userDocProperty;
	private String		wrongStatus;
	private Document	prDocument;

	@Override
	public void onInit() {
		this.statusFrom = getModel().getEstadoDe();
		this.statusTo = getModel().getEstadoPara();
		this.statusDocTo = getParametros().get("statusDocTo").toString().replaceAll("\"", "");
		this.statusDocThingTo = getParametros().get("statusDocThingTo").toString().replaceAll("\"", "");
		this.metaDocModel = getParametros().get("metaDocModel").toString().replaceAll("\"", "");
		this.unitDocProperty = getParametros().get("unitDocProperty").toString().replaceAll("\"", "");
		this.userDocProperty = getParametros().get("userDocProperty").toString().replaceAll("\"", "");
		this.wrongStatus = getParametros().get("wrongStatus").toString().replaceAll("\"", "");
	}

	@Override
	public void cancel() {

	}

	@Override
	protected void connect() {

	}

	@Override
	protected void processBefore(ComplexData cd) {

		if (this.prDocument == null) {
			JsonReader jsonReader = Json.createReader(new StringReader(cd.getPayload()));
			JsonObject payload = jsonReader.readObject();
			String docCode = payload.getString("Document");

			prDocument = getRegSvc().getDcSvc().quickFindByCodeAndModelMetaname(docCode, this.metaDocModel);
			jsonReader.close();
		}
	}

	@Override
	protected void processAfter(Thing t) {

	}

	@Override
	public void timeout(Map<String, Thing> itens) {
		Profiler p = new Profiler();

		try {
			p.step(logPrefix() + "INICIO DO TIMEOUT DO COLETOR " + this.getModel().getMetaname(), false);
			if (this.prDocument != null) {
				p.step(logPrefix() + "DOCUMENTO ENCONTRADO >>>" + this.prDocument.getCode(), false);
				List<DocumentItem> lst = getRegSvc().getDiSvc().quickListByDocumentId(this.prDocument.getId());
				if (itens != null && itens.size() > 0) {
					p.step(logPrefix() + "QUANTIDADE DE ITENS: " + itens.size(), false);
					HashSet<Thing> changes = new HashSet<Thing>();
					HashSet<Thing> tChg = new HashSet<Thing>();
					boolean cancel = false;
					List<String> toRemove = new ArrayList<String>();
					for (String cntRd : itens.keySet()) {
						Thing rd = itens.get(cntRd);
						if (rd != null && !cancel) {
							if (rd.getProduct() != null) {
								if (this.statusFrom.equals(rd.getStatus())) {
									for (DocumentItem di : lst) {
										if (di.getProduct().getId().equals(rd.getProduct().getId())) {
											if (di.getQtdThings() >= di.getQty()) {
												cancel = true;
												rd.getErrors().add(logPrefix() + this.wrongStatus);
												rd.setDocument(this.prDocument.getId());
												resend(rd);
												break;
											} else {
												System.out.println(logPrefix() + logPrefix() + "Thing Adicionado ");
												rd.setDocument(this.prDocument.getId());
												di.setQtdThings(di.getQtdThings() + 1);
												tChg.add(rd);
												toRemove.add(cntRd);
												changes.add(rd);
												resend(rd);
											}
										} else {
											System.out.println(logPrefix() + rd.getProduct().getId() + " != " + di.getProduct().getId());
										}
									}
								} else {
									toRemove.add(cntRd);
								}
							}
						}
					}
					p.step(logPrefix() + "QTD DE ITENS OK " + tChg.size(), false);
					for (String s : toRemove) {

						itens.remove(s);
					}
					if (!cancel && itens.isEmpty()) {
						getParametros().put("doc", this.prDocument);
						getParametros().put("itens", changes);
						for (Thing t : tChg) {
							getRegSvc().getThSvc().getThRep().quickUpdateThingStatus(t.getId(), this.statusTo);
							getRegSvc().getDtSvc().quickInsert(this.prDocument.getId(), t.getId(), this.statusDocThingTo);
						}
						boolean alterarDoc = true;
						for (DocumentItem di : lst) {
							if (new Double(di.getQty()).intValue() > di.getQtdThings()) {
								alterarDoc = false;
								break;
							}
						}
						if (alterarDoc) {
							DocumentModelField docModelFrom = getRegSvc().getDmfSvc().findByMetaname(this.unitDocProperty);
							DocumentModelField docModelTo = getRegSvc().getDmfSvc().findByMetaname(this.userDocProperty);

							getRegSvc().getDcSvc().quickUpdateStatus(this.prDocument.getId(), this.statusDocTo);
							getRegSvc().getDfSvc().quickChangeModel(this.prDocument.getId(), docModelFrom.getId(), docModelTo.getId());
						}
						this.prDocument = null;
						this.runSucess();
					} else {
						if (!itens.isEmpty()) {
							for (String s : itens.keySet()) {
								Thing t = itens.get(s);
								if (t == null)
									System.out.println(logPrefix() + "WUUUUUT " + s);
								t.getErrors().add("Item fora do escopo da nota");
								t.setDocument(this.prDocument.getId());
								resend(t);
							}
						}
						this.prDocument = null;
						this.lockdown("Produto incorreto ou Status inválido");
					}
				} else {
					System.out.println(logPrefix() + "NAO ACHEI O DOC FINAL");
					this.prDocument = null;
					this.lockdown("Documento não encontrado");
				}
			} else {
				p.step(logPrefix() + "DOC NULL????? ", false);
			}
			this.prDocument = null;
			p.done(logPrefix() + "FIM DO TIMEOUT.", false, true);
		} catch (Exception e) {//TODO: Remove after fix
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			p.done(logPrefix() + "Exception: " + e.getLocalizedMessage(), false, true);
		}
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
		if (!getParametros().containsKey("statusDocTo"))
			throw new Exception("Parâmetro 'statusDocTo' não encontrado.");
		if (!getParametros().containsKey("statusDocThingTo"))
			throw new Exception("Parâmetro 'statusDocThingTo' não encontrado.");
		if (!getParametros().containsKey("metaDocModel"))
			throw new Exception("Parâmetro 'metaDocModel' não encontrado.");
		if (!getParametros().containsKey("wrongStatus"))
			throw new Exception("Parâmetro 'wrongStatus' não encontrado.");
		if (!getParametros().containsKey("unitDocProperty"))
			throw new Exception("Parâmetro 'unitDocProperty' não encontrado.");
		if (!getParametros().containsKey("userDocProperty"))
			throw new Exception("Parâmetro 'userDocProperty' não encontrado.");
	}

	@Override
	protected void processUnknown(ComplexData rd) {
		// TODO Auto-generated method stub

	}
}
