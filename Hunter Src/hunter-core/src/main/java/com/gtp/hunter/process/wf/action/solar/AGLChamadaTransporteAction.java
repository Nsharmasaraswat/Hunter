package com.gtp.hunter.process.wf.action.solar;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
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
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.BaseAction;

public class AGLChamadaTransporteAction extends BaseAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public AGLChamadaTransporteAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	public String execute(Action t) throws Exception {
		logger.info("RODANDO ACTION DE CHAMADA DE CAMINHAO");
		logger.info(t.getParams());
		JsonReader jsonReader = Json.createReader(new StringReader(t.getParams()));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();
		UUID docId = UUID.fromString(object.getString("doc_id"));
		Document d = getRegSvc().getDcSvc().findById(docId);

		getRsm().getTsm().cancelTask(getUser().getId(), d);
		if (d.getStatus().equalsIgnoreCase(object.getString("statusde"))) {
			DocumentModelField dmf = d.getModel().getFields()
							.stream()
							.filter(ldmf -> ldmf.getMetaname().equals(object.getString("doc-field-address-metaname")))
							.findAny()
							.orElseThrow(() -> new Exception(object.getString("doc-field-address-metaname") + " não existe em " + d.getModel().getMetaname()));
			d.setStatus(object.getString("statuspara"));
			DocumentField df = d.getFields().stream()
							.filter(ldf -> ldf.getField().getId().equals(dmf.getId()))
							.findAny()
							.orElseGet(() -> {
								DocumentField tmp = new DocumentField(d, dmf, "NOVO", object.getString("address_id"));
								d.getFields().add(tmp);
								return tmp;
							});
			df.setValue(object.getString("address_id"));
			getRegSvc().getDcSvc().persist(d);
			DocumentModel dm = getRegSvc().getDmSvc().findByMetaname("APOCHAMADA");
			Document apo = new Document(dm, "Chamada do Transporte " + d.getCode(), "CHM" + d.getCode(), "NOVO");
			apo.setUser(getUser());
			apo.setParent(d);
			getRegSvc().getDcSvc().persist(apo);
			DocumentModel dmChk = getRegSvc().getDmSvc().findByMetaname("APOCHECKLIST");
			Document apochk = new Document(dmChk, "CheckList de Segurança " + d.getCode(), "CHKSEG" + d.getCode(), "NOVO");
			apochk.setParent(d);
			getRegSvc().getDcSvc().persist(apochk);
		}
		getRsm().getTsm().unlockTask(d);
		return t.getRoute();
	}

}
