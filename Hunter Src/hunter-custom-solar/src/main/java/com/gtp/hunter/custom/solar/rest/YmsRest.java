package com.gtp.hunter.custom.solar.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Person;
import com.gtp.hunter.process.model.PersonField;
import com.gtp.hunter.process.model.PersonModel;
import com.gtp.hunter.process.model.PersonModelField;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;

@RequestScoped
@Path("/yms")
public class YmsRest {

	@Inject
	private Logger						logger;

	@Inject
	private IntegrationService			iSvc;

	private static final List<String>	processed		= new ArrayList<>();

	private DocumentModel				dmtransp		= null;
	private DocumentModel				dmapochegada	= null;

	private PersonModel					pmdriver		= null;

	private PropertyModel				pmTruck			= null;
	private PropertyModelField			pmfBrand		= null;
	private PropertyModelField			pmfModel		= null;
	private PropertyModelField			pmfCode			= null;
	private PropertyModelField			pmfSvcType		= null;
	private PropertyModelField			pmfCapacity		= null;
	private PropertyModelField			pmfForklift		= null;
	private PropertyModelField			pmfLeftCap		= null;
	private PropertyModelField			pmfRightCap		= null;
	private PropertyModelField			pmfCarrier		= null;

	private Product						prdTruck		= null;

	private DocumentModelField			dmfTruckId		= null;
	private DocumentModelField			dmfDriverId		= null;
	private DocumentModelField			dmfSvcType		= null;
	private DocumentModelField			dmfObs			= null;
	private DocumentModelField			dmfRule			= null;
	private DocumentModelField			dmfPrio			= null;

	private PersonModelField			pmfcnh			= null;
	private PersonModelField			pmfrg			= null;

	@PostConstruct
	public void init() {
		this.dmtransp = iSvc.getRegSvc().getDmSvc().findByMetaname("TRANSPORT");
		this.dmapochegada = iSvc.getRegSvc().getDmSvc().findByMetaname("APOCHEGADA");

		this.pmdriver = iSvc.getRegSvc().getPsmSvc().findByMetaname("DRIVER");

		this.pmTruck = iSvc.getRegSvc().getPrmSvc().findByMetaname("TRUCK");

		this.pmfBrand = iSvc.getRegSvc().getPrmfSvc().findByModelAndMetaname(this.pmTruck, "BRAND");
		this.pmfModel = iSvc.getRegSvc().getPrmfSvc().findByModelAndMetaname(this.pmTruck, "MODEL");
		this.pmfCode = iSvc.getRegSvc().getPrmfSvc().findByModelAndMetaname(this.pmTruck, "CODE");
		this.pmfSvcType = iSvc.getRegSvc().getPrmfSvc().findByModelAndMetaname(this.pmTruck, "SERVICE_TYPE");
		this.pmfCapacity = iSvc.getRegSvc().getPrmfSvc().findByModelAndMetaname(this.pmTruck, "TRUCK_CAPACITY");
		this.pmfForklift = iSvc.getRegSvc().getPrmfSvc().findByModelAndMetaname(this.pmTruck, "FORKLIFT_TYPE");
		this.pmfLeftCap = iSvc.getRegSvc().getPrmfSvc().findByModelAndMetaname(this.pmTruck, "LEFT_SIDE_QUANTITY");
		this.pmfRightCap = iSvc.getRegSvc().getPrmfSvc().findByModelAndMetaname(this.pmTruck, "RIGHT_SIDE_QUANTITY");
		this.pmfCarrier = iSvc.getRegSvc().getPrmfSvc().findByModelAndMetaname(this.pmTruck, "CARRIER");

		this.dmfTruckId = iSvc.getRegSvc().getDmfSvc().findByModelAndMetaname(this.dmtransp, "TRUCK_ID");
		this.dmfDriverId = iSvc.getRegSvc().getDmfSvc().findByModelAndMetaname(this.dmtransp, "DRIVER_ID");
		this.dmfSvcType = iSvc.getRegSvc().getDmfSvc().findByModelAndMetaname(this.dmtransp, "SERVICE_TYPE");
		this.dmfRule = iSvc.getRegSvc().getDmfSvc().findByModelAndMetaname(this.dmtransp, "RULE_TEMPLATE");
		this.dmfObs = iSvc.getRegSvc().getDmfSvc().findByModelAndMetaname(this.dmtransp, "OBS");
		this.dmfPrio = iSvc.getRegSvc().getDmfSvc().findByModelAndMetaname(this.dmtransp, "PRIORITY");

		this.pmfcnh = iSvc.getRegSvc().getPsmfSvc().findByMetaname("CNH");
		this.pmfrg = iSvc.getRegSvc().getPsmfSvc().findByMetaname("RG");

		this.prdTruck = iSvc.getRegSvc().getPrdSvc().findByMetaname("TRUCK");
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> processed.clear(), 1, 1, TimeUnit.HOURS);//avoid stack overflow
	}

	@GET
	@PermitAll
	@Path("/findPersonByModelField/{model}/{field}/{value}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Person findPersonByByPropModelAndMetaname(@PathParam("model") String model, @PathParam("field") String field, @PathParam("value") String value) {
		Person ret = null;
		List<Person> prs = iSvc.getRegSvc().getPsSvc().getByPropModelAndMetaname(model, field, value);

		if (prs.size() > 0) ret = prs.get(0);
		return ret;
	}

	@GET
	@PermitAll
	@Path("/findDocumentByModelAndCode/{model}/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Document findDocumentByByPropModelAndMetaname(@PathParam("model") String model, @PathParam("code") String code) {
		Document d = null;

		d = iSvc.getRegSvc().getDcSvc().quickFindByCodeAndModelMetaname(code, model);
		d = iSvc.getRegSvc().getDcSvc().findById(d.getId());
		return d;
	}

	@GET
	@Path("/findDocumentBySAPTransport/{sap}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Document findDocumentBySAPTransport(@PathParam("sap") String code) {
		List<DocumentField> dfList = iSvc.getRegSvc().getDfSvc().listByValue(code);

		return dfList.parallelStream()
						.filter(df -> df.getDocument() != null && df.getDocument().getParent() != null)
						.findAny()
						.map(df -> iSvc.getRegSvc().getDcSvc().findById(df.getDocument().getParent().getId()))
						.orElse(null);
	}

	@GET
	@PermitAll
	@Path("/findThingByModelUnit/{model}/{value}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Thing findThingByunit(@PathParam("model") String model, @PathParam("value") String value) {
		Thing t = null;

		t = iSvc.getRegSvc().getThSvc().quickFindByUnitTagId(value);
		return t;
	}

	@PUT
	@Path("/changeTruck/{docid}/{truckid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn changeTruck(@Context HttpHeaders rs, @PathParam("docid") UUID docId, @PathParam("truckid") UUID truckId) {
		IntegrationReturn ret = IntegrationReturn.OK;
		Document transp = iSvc.getRegSvc().getDcSvc().findById(docId);
		Thing truck = iSvc.getRegSvc().getThSvc().findById(truckId);
		DocumentField dfTruck = transp.getFields().parallelStream().filter(df -> df.getField().getMetaname().equals("TRUCK_ID")).findAny().orElse(null);

		if (dfTruck != null && !dfTruck.getValue().isEmpty()) {
			Thing oldTruck = iSvc.getRegSvc().getThSvc().findById(UUID.fromString(dfTruck.getValue()));
			DocumentThing oldDt = transp.getThings().parallelStream().filter(dt -> dt.getThing().getId().equals(oldTruck.getId())).findAny().orElse(null);
			Address dock = oldTruck.getAddress();

			oldTruck.setAddress(null);
			iSvc.getRegSvc().getDtSvc().removeById(oldDt.getId());
			transp.getThings().removeIf(dt -> dt.getId().equals(oldDt.getId()));
			truck.setAddress(dock);
		}
		dfTruck.setValue(truck.getId().toString());
		transp.getThings().add(new DocumentThing(transp, truck, "NOVO"));
		return ret;
	}

	@PUT
	@Path("/changeTransportSeals/{docid}/{seals}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn changeSeal(@Context HttpHeaders rs, @PathParam("docid") UUID docId, @PathParam("seals") String seals) {
		IntegrationReturn ret = IntegrationReturn.OK;
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		String[] sArray = seals.split(",");
		Document transp = iSvc.getRegSvc().getDcSvc().findById(docId);
		Document apoLacre = transp.getSiblings().parallelStream()
						.filter(ds -> Arrays.asList("APOLACRE", "APOCHECKLIST").contains(ds.getModel().getMetaname()))
						.findAny()
						.orElse(null);
		String lacres = apoLacre.getFields().parallelStream()
						.filter(df -> df.getField().getMetaname().startsWith("ATTLACRE"))
						.map(df -> df.getValue())
						.distinct()
						.sorted()
						.collect(Collectors.joining(","));

		apoLacre.getFields().parallelStream()
						.filter(df -> df.getField().getMetaname().startsWith("ATTLACRE"))
						.forEach(df -> df.setValue(""));

		for (int i = 0; i < sArray.length; i++) {
			String sFieldMeta = "ATTLACRE_" + (i + 1);
			String sFieldValue = sArray[i];

			apoLacre.getFields().parallelStream()
							.filter(df -> df.getField().getMetaname().equals(sFieldMeta))
							.forEach(df -> df.setValue(sFieldValue));

		}
		iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.WMS, AlertSeverity.INFO, transp.getCode(), "ALTERAÇÃO DE LACRES", "Usuário " + usr.getName() + " alterou lacres de " + lacres + " para " + seals));
		return ret;
	}

	@POST
	@Path("/lobby")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn postLobby(@Context HttpHeaders rs, JsonObject json) {
		String jsonString = json.toString();//Not optimal because json does not guarantee properties order, but its fine for duplicate requests because they are actually the same.

		if (!processed.contains(jsonString)) {//Avoid duplicate Requests
			processed.add(jsonString);
			String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
			User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
			logger.info(jsonString);
			JsonArray nfs = json.getJsonArray("nfs");
			Prefix pfx = iSvc.getRegSvc().getPfxSvc().findNext("TR", 6);
			String code = pfx.getCode();
			Document transp = new Document(this.dmtransp, "Transporte NF " + code, code, "CAMINHAO NA PORTARIA");
			Document apocheg = new Document(this.dmapochegada, "Chegada do Transporte " + code, "CHG" + code, "NOVO");
			apocheg.setUser(usr);

			DocumentField dftrSvcType = new DocumentField();
			dftrSvcType.setField(this.dmfSvcType);
			dftrSvcType.setValue("TERCEIRO");
			dftrSvcType.setDocument(transp);
			transp.getFields().add(dftrSvcType);

			DocumentField dftrObs = new DocumentField();
			dftrObs.setField(this.dmfObs);
			dftrObs.setValue(json.getString("obs"));
			dftrObs.setDocument(transp);
			transp.getFields().add(dftrObs);

			DocumentField dftrRuleTpl = new DocumentField();
			dftrRuleTpl.setField(this.dmfRule);
			dftrRuleTpl.setValue("0");
			dftrRuleTpl.setDocument(transp);
			transp.getFields().add(dftrRuleTpl);

			DocumentField dftrPrio = new DocumentField();
			dftrPrio.setField(this.dmfPrio);
			dftrPrio.setValue("5");
			dftrPrio.setDocument(transp);
			transp.getFields().add(dftrPrio);

			Thing tmptTruck = iSvc.getRegSvc().getThSvc().quickFindByUnitTagId(json.getString("placa").toUpperCase());
			Thing tTruck = null;

			if (tmptTruck != null) {
				tTruck = iSvc.getRegSvc().getThSvc().findById(tmptTruck.getId());
			}
			Person driver = iSvc.getRegSvc().getPsSvc().findByCode(json.getString("cnh"));
			transp.getSiblings().add(apocheg);
			apocheg.setParent(transp);
			if (tTruck == null) {
				Unit u = new Unit(json.getString("placa").toUpperCase(), UnitType.LICENSEPLATES);

				iSvc.getRegSvc().getUnSvc().persist(u);

				tTruck = new Thing(json.getString("marca").toUpperCase() + " " + json.getString("modelo").toUpperCase(), this.prdTruck, this.pmTruck, "CAMINHAO NA PORTARIA");
				tTruck.setCreatedAt(new Date());
				tTruck.setUpdatedAt(new Date());
				tTruck.setMetaname(json.getString("placa").toUpperCase());
				tTruck.getUnits().add(u.getId());
				tTruck.getUnitModel().add(u);

				tTruck.getProperties().add(new Property(tTruck, pmfBrand, json.getString("marca")));
				tTruck.getProperties().add(new Property(tTruck, pmfModel, json.getString("modelo")));
				tTruck.getProperties().add(new Property(tTruck, pmfCode, json.getString("placa")));
				tTruck.getProperties().add(new Property(tTruck, pmfSvcType, "TERCEIRO"));
				tTruck.getProperties().add(new Property(tTruck, pmfCapacity, "1000"));
				tTruck.getProperties().add(new Property(tTruck, pmfForklift, "SIMPLE"));
				tTruck.getProperties().add(new Property(tTruck, pmfLeftCap, "" + json.getInt("posesq", 14)));
				tTruck.getProperties().add(new Property(tTruck, pmfRightCap, "" + json.getInt("posdir", 14)));
				tTruck.getProperties().add(new Property(tTruck, pmfCarrier, json.getString("tNome")));
			} else {
				List<UUID> docs = iSvc.getRegSvc().getDcSvc().listIdsByModelMetanameAndTagIDAndNotStatus("TRANSPORT", json.getString("placa").toUpperCase(), "LIBERADO");

				if (docs.size() > 0) {
					processed.remove(jsonString);
					return new IntegrationReturn(false, "CAMINHÃO COM TRANSPORTE EM PROCESSAMENTO");
				}
				tTruck.setStatus("CAMINHAO NA PORTARIA");
				tTruck.setMetaname(json.getString("placa").toUpperCase());
				tTruck.setName(json.getString("marca").toUpperCase() + " " + json.getString("modelo").toUpperCase());
				tTruck.getProperties().stream().filter(pr -> pr.getField().getId().equals(pmfBrand.getId())).findFirst().get().setValue(json.getString("marca"));
				tTruck.getProperties().stream().filter(pr -> pr.getField().getId().equals(pmfModel.getId())).findFirst().get().setValue(json.getString("modelo"));
				tTruck.getProperties().stream().filter(pr -> pr.getField().getId().equals(pmfCarrier.getId())).findFirst().get().setValue(json.getString("tNome"));
			}

			iSvc.getRegSvc().getThSvc().persist(tTruck);
			DocumentThing transpTruck = new DocumentThing(transp, tTruck, "NOVO");

			DocumentField dftrTruck = new DocumentField();
			dftrTruck.setField(this.dmfTruckId);
			dftrTruck.setValue(tTruck.getId().toString());
			dftrTruck.setDocument(transp);
			transp.getFields().add(dftrTruck);

			if (driver == null) {
				driver = new Person(json.getString("nome"), pmdriver, json.getString("cnh"), "NOVO");

				PersonField fldcnh = new PersonField(driver, pmfcnh, json.getString("cnh"));
				PersonField fldrg = new PersonField(driver, pmfrg, json.getString("rg"));

				driver.setMetaname(pmdriver.getMetaname());
				driver.setStatus("NOVO");
				driver.getFields().add(fldcnh);
				driver.getFields().add(fldrg);

				fldcnh.setMetaname(pmfcnh.getMetaname());
				fldcnh.setName(pmfcnh.getName());
				fldcnh.setStatus("NOVO");
				driver.getFields().add(fldcnh);

				fldrg.setMetaname(pmfrg.getMetaname());
				fldrg.setName(pmfrg.getName());
				fldrg.setStatus("NOVO");
				driver.getFields().add(fldrg);
			} else {
				driver.setName(json.getString("nome"));
				driver.getFields().stream().filter(pr -> pr.getField().getMetaname().equals(pmfrg.getMetaname())).findFirst().get().setValue(json.getString("rg"));
			}

			iSvc.getRegSvc().getPsSvc().persist(driver);

			DocumentField dftrDriver = new DocumentField();
			dftrDriver.setField(this.dmfDriverId);
			dftrDriver.setValue(driver.getId().toString());
			dftrDriver.setDocument(transp);
			transp.getFields().add(dftrDriver);

			List<Document> siblings = new ArrayList<Document>();

			for (JsonValue jv : nfs) {
				Profiler pf = new Profiler();
				JsonObject jo = (JsonObject) jv;
				logger.info("Procurando nota: " + jo.getString("numero"));
				Document nf = iSvc.getRegSvc().getDcSvc().findById(UUID.fromString(jo.getString("doc")));
				if (nf != null) {
					nf.setUser(usr);
					siblings.add(nf);
					pf.step("Find Full " + nf.getModel().getMetaname(), false);
				} else {
					processed.remove(jsonString);
					logger.info("NF N ENCONTRADA");
					return new IntegrationReturn(false, "NF Não Encontrada");
				}
				pf.done("Attached NFs", false, true);
			}

			for (Document dlst : siblings) {
				dlst.setParent(transp);
				transp.getSiblings().add(dlst);
			}
			transp.setPerson(driver);
			transp.getThings().add(transpTruck);
			iSvc.getRegSvc().getDcSvc().persist(transp);
			asyncSendToWMS(transp, tTruck);
		}
		return IntegrationReturn.OK;
	}

	private void asyncSendToWMS(Document transp, Thing tTruck) {
		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				List<Document> nfsList = transp.getSiblings().stream().filter(ds -> ds.getModel().getMetaname().equals("NFSAIDA")).collect(Collectors.toList());
				List<Document> nfeList = transp.getSiblings().stream().filter(ds -> ds.getModel().getMetaname().equals("NFENTRADA")).collect(Collectors.toList());
				IntegrationReturn iRet = iSvc.getRegSvc().getAglSvc().sendTruckToWMS(tTruck, "POST").get();

				if (!iRet.isResult() && iRet.getMessage().toUpperCase().contains("JA EXISTE O REGISTRO")) {
					iSvc.getRegSvc().getAglSvc().sendTruckToWMS(tTruck, "PUT");
				}
				iRet = iSvc.getRegSvc().getAglSvc().sendDocToWMS(transp, "POST").get();
				if (!iRet.isResult() && iRet.getMessage().toUpperCase().contains("JA EXISTE O REGISTRO")) {
					iSvc.getRegSvc().getAglSvc().sendDocToWMS(transp, "PUT");
				}
				for (Document nfs : nfsList) {
					iRet = iSvc.getRegSvc().getAglSvc().sendCustomerToWMS(nfs.getPerson(), "POST").get();
					if (!iRet.isResult() && iRet.getMessage().toUpperCase().contains("JA EXISTE O REGISTRO")) {
						iSvc.getRegSvc().getAglSvc().sendCustomerToWMS(nfs.getPerson(), "PUT");
					}
				}
				for (Document nfe : nfeList) {
					iRet = iSvc.getRegSvc().getAglSvc().sendSupplierToWMS(nfe.getPerson(), "POST").get();
					if (!iRet.isResult() && iRet.getMessage().toUpperCase().contains("JA EXISTE O REGISTRO")) {
						iSvc.getRegSvc().getAglSvc().sendSupplierToWMS(nfe.getPerson(), "PUT");
					}
				}
			} catch (Exception e) {
				logger.error("Failed to integrate with WMS: " + e.getLocalizedMessage());
				logger.trace("Failed to integrate with WMS: " + e.getLocalizedMessage(), e);
			}
		});
	}

}
