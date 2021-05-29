package com.gtp.hunter.process.wf.process;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Thing;

public class MobileShippingProcess extends ContinuousProcess {

	@Inject
	private static transient Logger	logger;

	private String					statusFrom;
	private String					statusTo;
	private String					statusDocThingFrom;
	private String					statusDocThingTo;
	private String					metaParentDocModel;
	private String					metaChildDocModel;
	private String					wrongStatus;
	private Document				prDocument;

	@Override
	public void onInit() {
		this.statusDocThingFrom = getParametros().get("statusDocThingFrom").toString().replaceAll("\"", "");
		this.statusDocThingTo = getParametros().get("statusDocThingTo").toString().replaceAll("\"", "");
		this.metaParentDocModel = getParametros().get("metaParentDocModel").toString().replaceAll("\"", "");
		this.metaChildDocModel = getParametros().get("metaChildDocModel").toString().replaceAll("\"", "");
		this.statusFrom = getModel().getEstadoDe();
		this.statusTo = getModel().getEstadoPara();
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
		JsonReader jsonReader = Json.createReader(new StringReader(cd.getPayload()));
		JsonObject payload = jsonReader.readObject();

		if (this.prDocument == null) {
			String docCode = payload.getString("Document");

			prDocument = getRegSvc().getDcSvc().quickFindByCodeAndModelMetaname(docCode, this.metaChildDocModel);
			if (prDocument == null) {
				Document parent = getRegSvc().getDcSvc().quickFindByCodeAndModelMetaname(docCode, this.metaParentDocModel);

				if (parent == null)
					this.lockdown("Parent não encontrado");
				else {
					List<DocumentItem> lst = getRegSvc().getDiSvc().quickListByDocumentId(parent.getId());
					DocumentModel model = getRegSvc().getDmSvc().findByMetaname(this.metaChildDocModel);

					prDocument = new Document(model, model.getName() + " #" + docCode, docCode, statusFrom);
					prDocument.setPerson(parent.getPerson());
					prDocument.setParent(parent);
					getRegSvc().getDcSvc().persist(prDocument);
					lst.forEach(di -> {
						DocumentItem r = new DocumentItem();

						r.setMetaname(di.getMetaname());
						r.setQtdThings(di.getQtdThings());
						r.setQty(di.getQty());
						r.setName(di.getName());
						r.setProduct(di.getProduct());
						r.setProperties(di.getProperties());
						r.setStatus(this.statusDocThingTo);
						r.setThings(di.getThings());
						r.setDocument(prDocument);
						getRegSvc().getDiSvc().persist(r);
					});
					System.out.println(logPrefix() + "Document saved: " + prDocument.getId().toString());
				}
			}
		}
		jsonReader.close();
	}

	@Override
	protected void processAfter(Thing t) {
		if (t != null) {
			if (t.getProduct() != null) {
				if (t.getProduct().getModel() != null) {
					System.out.println(logPrefix() + "Processando Thing: " + t.getProduct().getModel().getMetaname());
					if (!t.getStatus().equals(this.statusFrom) && !t.getStatus().equals(this.statusTo)) {
						this.lockdown("Thing no Status errado");
						t.setCancelProcess(true);
						t.getErrors().add(this.wrongStatus);
					}
				}
			}
			t.getUnitModel().stream().forEach(u -> System.out.println(logPrefix() + "UNIT: " + u.getTagId()));
		} else {
			System.out.println(logPrefix() + "RD NULO!!!!!");
		}
	}

	@Override
	public void timeout(Map<String, Thing> itens) {
		if (prDocument != null) {
			for (Thing t : itens.values()) {
				if (t.getStatus().equals(this.statusFrom)) {
					getRegSvc().getThSvc().getThRep().quickUpdateThingStatus(t.getId(), this.statusTo);
					System.out.println(logPrefix() + "Procurandoo dt c thing: " + t.getId() + " e c o dm: " + this.metaParentDocModel);
					DocumentThing dt = getRegSvc().getDtSvc().quickFindByThingIdAndDocModelMeta(t.getId(), this.metaChildDocModel);

					if (dt == null) {
						getRegSvc().getDtSvc().quickInsert(this.prDocument.getId(), t.getId(), this.statusDocThingTo);
					} else {
						if (dt != null) {
							t.setDocument(dt.getDocument().getId());
						}
						if (dt != null && dt.getStatus().equals(this.statusDocThingFrom)) {
							getRegSvc().getDtSvc().quickUpdateStatus(dt.getId(), this.statusDocThingTo);
						}
					}
				} else if (!t.getStatus().equals(this.statusTo)) {
					this.lockdown("Thing com Status errado");
					t.getErrors().add(this.wrongStatus);
				}
			}
			System.out.println(logPrefix() + "Quantidade de Itens Embarcados: " + itens.size());
			this.runSucess();
		} else
			System.out.println(logPrefix() + "Document não enviado");
		this.unlock();
		this.prDocument = null;
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
		if (!getParametros().containsKey("metaParentDocModel"))
			throw new Exception("Parâmetro 'metaParentDocModel' não encontrado.");
		if (!getParametros().containsKey("metaChildDocModel"))
			throw new Exception("Parâmetro 'metaChildDocModel' não encontrado.");
	}

	@Override
	protected void processUnknown(ComplexData rd) {
		// TODO Auto-generated method stub

	}
}
