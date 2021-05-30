package com.gtp.hunter.process.wf.action.solar;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.BaseAction;
import com.gtp.hunter.process.wf.process.solar.ProductionLineProcess;

public class SolarInitProductionAction extends BaseAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public SolarInitProductionAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	public String execute(Action t) throws Exception {
		//logger.info("RODANDO ACTION DE INÍCIO DE LINHA DE PRODUÇÃO");
		logger.info(t.getParams());
		JsonReader jsonReader = Json.createReader(new StringReader(t.getParams()));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();
		UUID docId = UUID.fromString(object.getString("doc_id"));
		Document planProd = getRegSvc().getDcSvc().findById(docId);

		getRsm().getTsm().cancelTask(getUser().getId(), planProd);
		try {
			JsonObject prodLines = object.getJsonObject("process-map");
			DocumentModel ordprodModel = getRegSvc().getDmSvc().findByMetaname(object.getString("ordprod-meta"));
			Set<DocumentField> fields = new HashSet<>(planProd.getFields());
			Set<DocumentItem> items = new HashSet<>(planProd.getItems());
			Document ordProd = new Document(ordprodModel, ordprodModel.getName() + " " + String.valueOf(Integer.parseInt(planProd.getCode())), "OP" + planProd.getCode(), "ATIVO");
			String line = planProd.getFields().stream().filter(df -> df.getField().getMetaname().equals(object.getString("document-field-line-meta"))).findFirst().get().getValue();

			t.setRoute(t.getRoute() + line.replace("LINHA 0", ""));
			ordProd.setParent(planProd);
			for (DocumentModelField dmf : ordprodModel.getFields()) {
				Optional<DocumentField> optDF = fields.stream().filter(df -> df.getField().getMetaname().equals(dmf.getMetaname())).findFirst();

				if (optDF.isPresent()) {
					DocumentField df = optDF.get();
					DocumentField ndf = new DocumentField(ordProd, dmf, "COPIA", df.getValue());

					ordProd.getFields().add(ndf);
				} else {
					switch (dmf.getType()) {
						case NUMBER:
							ordProd.getFields().add(new DocumentField(ordProd, dmf, "NOVO", "0"));
							break;
						default:
							ordProd.getFields().add(new DocumentField(ordProd, dmf, "NOVO", ""));
							break;
					}
				}
			}

			items.forEach(di -> {
				DocumentItem ndi = new DocumentItem();

				ndi.setDocument(ordProd);
				ndi.setMeasureUnit(di.getMeasureUnit());
				ndi.setProduct(di.getProduct());
				ndi.setStatus(di.getStatus());
				ndi.setQty(di.getQty());
				ndi.setProperties(new HashMap<String, String>(di.getProperties()));
				ordProd.getItems().add(ndi);
			});
			ordProd.setUser(getUser());

			planProd.setStatus("PRODUZINDO");
			logger.info("Initializing Production Process!");
			if (prodLines.containsKey(line)) {
				UUID procId = UUID.fromString(prodLines.getString(line));
				ProductionLineProcess proc = (ProductionLineProcess) getRsm().getPsm().getProcesses().get(procId);
				Document dMaster = proc.getProductionOrder();

				if (dMaster != null) {
					String childModelMeta = object.getString("child-model-meta");

					getRsm().getTsm().cancelTask(getUser().getId(), dMaster);
					if (!dMaster.getSiblings().stream().anyMatch(ds -> ds.getModel().getMetaname().equals(childModelMeta))) {
						String masterStatus = object.getString("master-status");
						String childStatus = object.getString("child-status");
						String childCodePrefix = object.getString("child-code-prefix");

						getRegSvc().getDcSvc().createChild(dMaster, masterStatus, childModelMeta, childStatus, childCodePrefix, null, null, null, getUser());
						proc.stopProduction();
						Thread.sleep(1000);//Fucking Shit ProdNull
					}
					getRsm().getTsm().unlockTask(dMaster);
				}
				getRegSvc().getDcSvc().createChild(ordProd, "ATIVO", object.getString("apoinicio-meta"), "NOVO", "INIT", null, null, null, getUser());
				getRegSvc().getDcSvc().persist(planProd);
				getRegSvc().getDcSvc().flush();
				proc.startProduction(ordProd);
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		getRsm().getTsm().unlockTask(planProd);
		return t.getRoute();
	}
}
