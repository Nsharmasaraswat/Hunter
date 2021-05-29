package com.gtp.hunter.process.wf.action.solar;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.BaseAction;

public class SolarExitToReturnAction extends BaseAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public SolarExitToReturnAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	public String execute(Action t) throws Exception {
		logger.info("RODANDO ACTION DE SAIDA DE CAMINHAO PARA RETORNO");
		logger.info(t.getParams());
		JsonReader jsonReader = Json.createReader(new StringReader(t.getParams()));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();
		UUID docId = UUID.fromString(object.getString("doc_id"));
		Document transpOld = getRegSvc().getDcSvc().findById(docId);

		getRsm().getTsm().cancelTask(getUser().getId(), transpOld);
		try {
			if (!transpOld.getStatus().equals("CAMINHAO NA ENTRADA")) {
				Document chegada = transpOld.getSiblings().stream().filter(dsib -> dsib.getModel().getMetaname().equals("APOCHEGADA")).findFirst().get();
				Optional<Document> optConf = transpOld.getSiblings().stream().filter(dsib -> dsib.getModel().getMetaname().equals("ORDCONF")).findFirst();
				Set<Document> dNFs = transpOld.getSiblings().stream().filter(dsib -> dsib.getModel().getMetaname().equals("NFENTRADA") || dsib.getModel().getMetaname().equals("NFSAIDA")).collect(Collectors.toSet());
				Set<DocumentField> fields = new HashSet<>(transpOld.getFields());
				Set<DocumentThing> things = new HashSet<>(transpOld.getThings());
				Document transp = new Document(transpOld.getModel(), transpOld.getName(), transpOld.getCode(), "CAMINHAO NA PORTARIA");
				Document apoChegada = new Document(chegada.getModel(), chegada.getName(), chegada.getCode(), chegada.getStatus());

				apoChegada.setCode(chegada.getCode());
				apoChegada.setParent(transp);

				transpOld.setCode("R." + transpOld.getCode());
				if (optConf.isPresent()) {
					Document ordConf = optConf.get();
					ordConf.setStatus("CANCELADO");
					getRegSvc().getDcSvc().persist(ordConf);
				}
				getRegSvc().getDcSvc().createChild(transpOld, "CAMINHAO NA SAIDA", "APOCHECKSAIDA", "NOVO", "CHKEXIT", null, null, null, getUser());
				fields.forEach(df -> {
					DocumentField ndf = new DocumentField();
					ndf.setField(df.getField());
					ndf.setValue(df.getValue());
					ndf.setDocument(transp);
					transp.getFields().add(ndf);
				});
				things.forEach(dt -> {
					DocumentThing ndt = new DocumentThing();
					ndt.setStatus("RETORNO");
					ndt.setThing(dt.getThing());
					ndt.setDocument(transp);
					transp.getThings().add(ndt);
				});
				transp.setPerson(transpOld.getPerson());
				transp.getSiblings().add(apoChegada);
				transp.getSiblings().add(transpOld);
				transp.setUser(getUser());
				dNFs.forEach(d -> {
					d.setParent(transp);
					transp.getSiblings().add(d);
				});
				getRegSvc().getWmsSvc().clearTnpSibs(transpOld.getId().toString(), true, true, true, true);
				getRegSvc().getDcSvc().persist(transp);
				getRegSvc().getAglSvc().sendDocToWMS(transp, "POST");
			} else {
				Document cl = transpOld.getSiblings().stream().filter(ds -> ds.getModel().getMetaname().equals("APOCHECKLIST")).findAny().get();

				getRsm().getTsm().cancelTask(getUser().getId(), cl);
				getRegSvc().getDcSvc().removeById(cl.getId());
				transpOld.getSiblings().remove(cl);
				transpOld.setStatus("CAMINHAO NA PORTARIA");
				getRsm().getTsm().unlockTask(cl);
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
		}
		getRsm().getTsm().unlockTask(transpOld);
		return t.getRoute();
	}
}
