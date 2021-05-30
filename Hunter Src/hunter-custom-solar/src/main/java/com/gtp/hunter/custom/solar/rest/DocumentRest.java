package com.gtp.hunter.custom.solar.rest;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.Asynchronous;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.Query;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.common.util.DateUtil;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReturnDTO;
import com.gtp.hunter.custom.solar.service.DocumentService;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.ThingService;
import com.gtp.hunter.custom.solar.util.ToJsonSAP;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.jsonstubs.AGLDocModelForm;
import com.gtp.hunter.process.jsonstubs.AGLDocModelItem;
import com.gtp.hunter.process.jsonstubs.AGLDocTransport;
import com.gtp.hunter.process.jsonstubs.AGLThing;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.DocumentTransport;
import com.gtp.hunter.process.model.Location;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Addresses;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.model.util.Products;
import com.gtp.hunter.process.model.util.Things;
import com.sap.conn.jco.JCoException;

@RequestScoped
@Path("/document")
public class DocumentRest {

	private static final List<String>		processing	= new CopyOnWriteArrayList<>();
	private static final DecimalFormat		DF			= new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.US));

	@Resource
	private ManagedScheduledExecutorService	mes;

	@Inject
	private SAPSolar						solar;

	@Inject
	private IntegrationService				iSvc;

	@Inject
	private DocumentService					dcSvc;

	@Inject
	private ThingService					thSvc;

	@Inject
	private Logger							logger;

	@DELETE
	@PermitAll
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public IntegrationReturn cancelDocument(@PathParam("id") UUID id) {
		dcSvc.deleteDocument(id);
		return IntegrationReturn.OK;
	}

	@GET
	@PermitAll
	@Path("/grandparent/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Document findParent(@PathParam("id") String id) {
		Document parent = iSvc.getRegSvc().getDcSvc().quickFindParentDoc(iSvc.getRegSvc().getDcSvc().quickFindParentDoc(id).getId().toString());

		return iSvc.getRegSvc().getDcSvc().findById(parent.getId());
	}

	@GET
	@PermitAll
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<AGLDocModelForm> getAllDocuments() {
		System.out.println("/DOCUMENT/ALL");
		return new ArrayList<AGLDocModelForm>();
		//return dcsv.listAllDocument();
		//return iSvc.getrSvc().getAglSvc().
	}

	@GET
	@PermitAll
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public AGLDocModelForm getOneDocument(@PathParam("id") UUID id) {
		System.out.println("/DOCUMENT/" + id.toString());
		Document d = iSvc.getRegSvc().getDcSvc().findById(id);
		AGLDocModelForm aglDoc = iSvc.getRegSvc().getAglSvc().convertDocToAgl(d);

		System.out.println(new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().toJson(aglDoc));
		return aglDoc;
	}

	@GET
	@PermitAll
	@Path("/quickByTypeAndCode/{metaname}/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	public AGLDocModelForm getOneDocumentBy(@PathParam("code") String code, @PathParam("metaname") String metaname) {
		return new AGLDocModelForm();
	}

	@GET
	@PermitAll
	@Path("byMetaname/{model}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AGLDocModelForm> getAllDocumentsByModel(@PathParam("model") String model) {
		return new ArrayList<AGLDocModelForm>();

	}

	@GET
	@PermitAll
	@Path("/bypersontypecode/{persontype}/{personcode}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Document> customQuickListOrphanByPersonTypeAndCode(@PathParam("persontype") String personModel, @PathParam("personcode") String personCode) {
		return dcSvc.getCustomYMSNF(personModel, personCode);
	}

	@GET
	@PermitAll
	@Path("/productionOrder/{lineId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Document getRunningPO(@PathParam("lineId") String prdLine) {
		Document d = iSvc.getRegSvc().getDcSvc().findLastByTypeAndStatusAndFieldValue(iSvc.getRegSvc().getDmSvc().findByMetaname("ORDPROD"), "ATIVO", "LINHA_PROD", prdLine);

		if (d != null) {
			d.getFields().clear();
			d.getFields().addAll(iSvc.getRegSvc().getDfSvc().listByDocumentId(d.getId()));
		}
		return d;
	}

	@POST
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn addDocument(AGLDocModelForm document) {
		System.out.println("CHEGOU POST DO DISCO VOADOR");
		System.out.println(document.toString());
		final Profiler prof = new Profiler("DocumentRest");
		boolean resendAfterSave = false;

		if (document.getMetaname() != null && !document.getMetaname().isEmpty()) {
			Date now = Calendar.getInstance().getTime();
			final DocumentModel dm = iSvc.getRegSvc().getDmSvc().findByMetaname(document.getMetaname());

			if (dm != null) {
				List<AGLThing> agltList = document.getThings().stream().filter(aglt -> aglt.getId() != null && !aglt.getId().isEmpty()).collect(Collectors.toList());

				logger.info(prof.step(document.getMetaname() + " Remove things without ID", false));
				for (AGLThing dt : agltList) {
					Thing t = iSvc.getRegSvc().getThSvc().findById(UUID.fromString(dt.getId()));

					if (t == null) {
						if (dt.getProduct_id() != null && dt.getProduct_id().isEmpty()) {
							final Product pallet = iSvc.getRegSvc().getPrdSvc().findById(UUID.fromString("95b564e9-ea5a-4caa-adbe-06fc7dd0b966"));
							DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
							DecimalFormat decForm = new DecimalFormat("#0.0000", symbols);

							dt.setProduct_id("95b564e9-ea5a-4caa-adbe-06fc7dd0b966");
							dt.setName(pallet.getName());
							if (dt.getAddress_id() != null && !dt.getAddress_id().isEmpty()) {
								Address a = iSvc.getRegSvc().getAddSvc().findById(UUID.fromString(dt.getAddress_id()));

								if (a != null && a.getModel() != null && !a.getModel().getMetaname().equalsIgnoreCase("ADDRESS") && !a.getSiblings().isEmpty()) {
									Address s = a.getSiblings().stream().findFirst().get();

									dt.setAddress_id(s.getId().toString());
									for (AGLThing dts : dt.getSiblings())
										dts.setAddress_id(s.getId().toString());
								}
							}
							if (dt.getParent_id() != null && dt.getParent_id().isEmpty()) dt.setParent_id(null);
							if (dt.getSiblings() != null && !dt.getSiblings().isEmpty()) {
								AGLThing at = dt.getSiblings().stream().findFirst().get();
								Product prd = iSvc.getRegSvc().getPrdSvc().findById(UUID.fromString(at.getProduct_id()));
								String prdWeights = prd.getFields().stream().filter(pf -> pf.getModel().getMetaname().equalsIgnoreCase("GROSS_WEIGHT")).findFirst().get().getValue();

								dt.setProps(at.getProps());
								dt.getProps().put("quantity", "1.0");
								try {
									Number prdWeight = decForm.parse(prdWeights);
									Number qt = decForm.parse(at.getProps().get("quantity"));

									at.getProps().put("starting_weight", String.valueOf(prdWeight.doubleValue() * qt.intValue()));
									at.getProps().put("actual_weight", String.valueOf(prdWeight.doubleValue() * qt.intValue()));
								} catch (ParseException pe) {
									logger.error("Product weight " + prdWeights + " or Thing quantity " + at.getProps().get("quantity") + " invalid");
								}
							} else
								logger.info("Thing has no siblings");
						}

						t = iSvc.getRegSvc().getAglSvc().convertAGLThingToThing(dt);
						if (t != null && t.getSiblings() != null && t.getSiblings().isEmpty() && dt.getSiblings() != null && !dt.getSiblings().isEmpty())
							for (AGLThing dts : dt.getSiblings())
								iSvc.getRegSvc().getThSvc().quickUpdateParentId(UUID.fromString(dts.getId()), t.getId());
					}
				}
				logger.info(prof.step(document.getMetaname() + " Convert Things", false));
				try {
					Document prnt = document != null && document.getParent_id() != null && !document.getParent_id().isEmpty() ? iSvc.getRegSvc().getDcSvc().findById(UUID.fromString(document.getParent_id())) : null;
					Prefix pfx = null;

					switch (document.getMetaname()) {
						case "ORDCONF":
							Thread.sleep(800);
							if (document.getParent_id() != null && !document.getParent_id().isEmpty()) {
								Document transp = iSvc.getRegSvc().getDcSvc().findById(UUID.fromString(document.getParent_id()));
								Supplier<Stream<Document>> supMov = () -> transp.getSiblings().parallelStream()
												.filter(ds -> ds.getModel().getMetaname().equals("ORDMOV") && ds.getCode().startsWith("ULT"));
								List<Document> lNF = null;

								for (AGLDocModelItem aglDi : document.getItems()) {
									if (aglDi.getUnit_measure() == null) {
										lNF = transp.getSiblings().stream().filter(s -> s.getModel().getMetaname().equals("NFENTRADA") || s.getModel().getMetaname().equals("NFSAIDA")).collect(Collectors.toList());
										for (Document dNF : lNF) {
											DocumentItem docItem = dNF.getItems().stream().filter(di -> di.getProduct().getId().toString().equals(aglDi.getProduct_id())).findAny().get();

											aglDi.setUnit_measure(docItem.getMeasureUnit());
										}
									}
								}
								supMov.get().filter(mov -> mov.getStatus().equals("SUCESSO"))
												.flatMap(mov -> mov.getTransports().stream())
												.forEach(dtr -> document.getThings().add(iSvc.getRegSvc().getAglSvc().convertThingToAGL(dtr.getThing())));
							}
							if (document.getThings() != null) {
								document.getThings().removeIf(at -> at.getSiblings() == null || at.getSiblings().isEmpty());
							}
							break;
						case "ORDMOV":
							String mvTitle = "";
							document.getTransport().removeIf(tr -> tr.getAddress_id() == null || tr.getThing_id() == null || tr.getAddress_id().isEmpty() || tr.getAddress_id().isEmpty());
							if (document.getTransport().isEmpty()) return new IntegrationReturn(false, "TRANSPORT VAZIO");
							for (AGLDocTransport tr : document.getTransport()) {
								if (tr.getThing_id() == null || tr.getThing_id().isEmpty()) return new IntegrationReturn(false, "TRANSPORT THING VAZIO");
							}

							document.getTransport().removeIf(tr -> tr.getAddress_id() == null || tr.getAddress_id().isEmpty());

							document.getItems().addAll(document.getThings().parallelStream()
											.flatMap(at -> at.getSiblings().parallelStream())
											.map(ats -> {
												AGLDocModelItem adi = new AGLDocModelItem();

												adi.setAddress_id(ats.getAddress_id());
												adi.setLayer(1);
												adi.setProduct_id(ats.getProduct_id());
												adi.setQty(new BigDecimal(ats.getProps().get("quantity")));
												adi.setUnit_measure("CX");
												return adi;
											})
											.collect(Collectors.toList()));
							if (document.getParent_id() == null) {
								pfx = iSvc.getRegSvc().getPfxSvc().findNext("MOV", 9);
								mvTitle = "ARMAZÉM: REORGANIZAÇÃO";
							} else {
								if (prnt == null || prnt.getModel() == null || (prnt.getModel().getMetaname().equals("ORDCRIACAO") && prnt.getParent_id() == null)) {
									pfx = iSvc.getRegSvc().getPfxSvc().findNext("MOV", 9);
									mvTitle = "ARMAZÉM: REORGANIZAÇÃO";
								} else if (prnt.getModel().getMetaname().equals("ORDCRIACAO")) {
									Document gParent = iSvc.getRegSvc().getDcSvc().findById(UUID.fromString(prnt.getParent_id()));

									if (gParent == null || gParent.getModel() == null) {
										pfx = iSvc.getRegSvc().getPfxSvc().findNext("MOV", 9);
										mvTitle = "ARMAZÉM: REORGANIZAÇÃO";
									} else if (gParent.getModel().getMetaname().equals("ORDPROD")) {
										pfx = iSvc.getRegSvc().getPfxSvc().findNext("PLS", 9);
										mvTitle = "FÁBRICA: SAÍDA DE LINHA";
									} else if (gParent.getModel().getMetaname().equals("TRANSPORT")) {
										pfx = iSvc.getRegSvc().getPfxSvc().findNext("STR", 9);
										mvTitle = "PATIO: ARMAZENAMENTO";
									}
								} else if (prnt.getModel().getMetaname().equals("TRANSPORT")) {
									boolean nfsaida = prnt.getSiblings().stream().anyMatch(sib -> sib.getModel().getMetaname().equals("NFSAIDA"));
									boolean nfentrada = prnt.getSiblings().stream().anyMatch(sib -> sib.getModel().getMetaname().equals("NFENTRADA"));
									String cam = prnt.getThings().stream()
													.map(dt -> dt.getThing())
													.filter(t -> t.getUnits().size() > 0)
													.flatMap(t -> {
														t.setUnitModel(iSvc.getRegSvc().getUnSvc().getAllUnitById(t.getUnits()));
														return t.getUnitModel().stream();
													}).filter(u -> u.getType() == UnitType.LICENSEPLATES)
													.map(u -> u.getTagId())
													.distinct()
													.collect(Collectors.joining(","));

									String motora = prnt.getFields().stream()
													.filter(df -> df.getField().getMetaname().equals("DRIVER_ID") && !df.getValue().isEmpty())
													.map(df -> iSvc.getRegSvc().getPsSvc().findById(UUID.fromString(df.getValue())))
													.map(p -> p.getName())
													.collect(Collectors.joining(","));

									if (!cam.isEmpty() && !motora.isEmpty())
										mvTitle = "\r\n" + cam + " (" + motora + ")";
									else if (!cam.isEmpty())
										mvTitle = "\r\n" + cam;
									else if (!motora.isEmpty())
										mvTitle = "\r\n" + motora;

									if (nfsaida && !nfentrada)
										pfx = iSvc.getRegSvc().getPfxSvc().findNext("LDT", 9);
									else if (nfentrada) {
										int thingSiblings = document.getThings().stream().mapToInt(at -> at.getSiblings().size()).sum();

										if (thingSiblings == 0) {
											pfx = iSvc.getRegSvc().getPfxSvc().findNext("ULT", 9);
										} else if (nfsaida)
											pfx = iSvc.getRegSvc().getPfxSvc().findNext("LDT", 9);
										else
											logger.warn("WADAFOCK!?!");
									}
								} else if (prnt.getModel().getMetaname().equals("ORDMOV")) {
									pfx = iSvc.getRegSvc().getPfxSvc().findNext(prnt.getCode().substring(0, 3), 9);
								} else {
									pfx = iSvc.getRegSvc().getPfxSvc().findNext("MOV", 9);
								}
							}
							if (document.getStatus().equals("ATIVO")) {
								if (pfx.getPrefix().equals("LDT")) {
									document.getProps().put("priority", "0");
									document.getProps().put("mov_type", "LOAD");
									document.getProps().put("mov_title", mvTitle);
									document.setStatus("LOAD");
								} else if (pfx.getPrefix().equals("ULT")) {
									document.getProps().put("priority", "0");
									document.getProps().put("mov_type", "UNLOAD");
									document.getProps().put("mov_title", mvTitle);
									document.setStatus("UNLOAD");
								} else if (pfx.getPrefix().equals("PLS")) {
									document.getProps().put("priority", "3");
									document.getProps().put("mov_type", "PROD");
									document.getProps().put("mov_title", mvTitle);
									document.setStatus("ARMPROD");
								} else if (pfx.getPrefix().equals("STR")) {
									document.getProps().put("priority", "5");
									document.getProps().put("mov_type", "STORE");
									document.getProps().put("mov_title", mvTitle);
									document.setStatus("ARMCAM");
								} else if (pfx.getPrefix().equals("MOV")) {
									document.getProps().put("priority", "6");
									document.getProps().put("mov_type", "REORG");
									document.getProps().put("mov_title", mvTitle);
									document.setStatus("ATIVO");
								}
							} else if (document.getStatus().startsWith("WMS")) {
								document.setStatus(document.getStatus().replace("WMS_", ""));
								document.getProps().put("priority", "10");
								document.getProps().put("mov_type", "???");
								document.getProps().put("mov_title", "XXX: ????");
								resendAfterSave = true;
							}
							document.setName(dm.getName() + " " + pfx.getCode());
							document.setCode(pfx.getPrefix() + pfx.getCode());
							break;
						case "ORDCRIACAO":
							if (prnt != null && prnt.getModel() != null && prnt.getModel().getMetaname().equals("ORDPROD")) {
								if (!prnt.getStatus().equals("ATIVO")) return new IntegrationReturn(false, "ORDEM DE PRODUÇÃO JÁ FINALIZADA. ABRA NOVAMANTE E REFAÇA O APONTAMENTO");
								DocumentField dfPltCnt = prnt.getFields().stream()
												.filter(df -> df.getField().getMetaname().equals("PALLET_COUNT"))
												.findFirst()
												.orElse(null);
								DocumentField dfBxCnt = prnt.getFields().stream()
												.filter(df -> df.getField().getMetaname().equals("BOX_COUNT"))
												.findFirst()
												.orElse(null);
								Product prd = document.getThings().parallelStream()
												.flatMap(at -> at.getSiblings().parallelStream())
												.map(ats -> ats.getProduct_id())
												.findAny()
												.map(spId -> iSvc.getRegSvc().getPrdSvc().findById(UUID.fromString(spId)))
												.orElse(null);
								int boxes = document.getThings().parallelStream()
												.flatMap(at -> at.getSiblings().parallelStream())
												.map(ats -> ats.getProps().get("quantity"))
												.mapToInt(s -> Integer.parseInt(s)).sum();
								long pltCnt = prnt.getFields().parallelStream()
												.filter(df -> df.getField().getMetaname().contentEquals("PALLET_COUNT"))
												.mapToLong(df -> Long.parseLong(df.getValue()))
												.sum() + 1;
								long bxCnt = prnt.getFields().parallelStream()
												.filter(df -> df.getField().getMetaname().contentEquals("BOX_COUNT"))
												.mapToLong(df -> Long.parseLong(df.getValue()))
												.sum() + boxes;

								if (dfPltCnt != null) {
									dfPltCnt.setValue("" + pltCnt);
									iSvc.getRegSvc().getDfSvc().quickUpdateValue(dfPltCnt);
								}
								if (dfBxCnt != null) {
									dfBxCnt.setValue("" + bxCnt);
									iSvc.getRegSvc().getDfSvc().quickUpdateValue(dfBxCnt);
								}
								recordProduction(prnt, prd, boxes);
							}
							Prefix prefix = iSvc.getRegSvc().getPfxSvc().findNext("OCR", 9);
							String code = prefix.getCode();

							logger.info(prof.step(document.getMetaname() + " Generate Code", false));
							document.setName(dm.getName() + " " + code);
							document.setCode(prefix.getPrefix() + code);

							if (document.getParent_id() != null && document.getParent_id().isEmpty())
								document.setParent_id(null);
							logger.info(prof.step(document.getMetaname() + " Set Base Fields", false));
							break;
						case "PRDSHORTAGE":
							if (document.getParent_id() == null || document.getParent_id().isEmpty()) return new IntegrationReturn(false, "PRDSHORTAGE SEM TRANSPORT");
							Document transport = iSvc.getRegSvc().getDcSvc().findById(UUID.fromString(document.getParent_id()));

							if (transport == null) return new IntegrationReturn(false, "TRANSPORT (" + document.getParent_id() + ") NÃO EXISTE");
							Optional<Document> optPrds = transport.getSiblings().stream()
											.filter(ds -> ds.getModel().getId().equals(dm.getId()))
											.findAny();
							if (optPrds.isPresent()) {
								Document prds = optPrds.get();

								//TODO: FUCKING SHIT MODAFOCKA HIBERNATE FROM HELL
								return fuckingHibernate(prds, document);
							}
							document.setName(dm.getName() + " " + transport.getCode());
							document.setCode("PRDS" + transport.getCode());
							break;
						case "REPACK":
							try {
								pfx = iSvc.getRegSvc().getPfxSvc().findNext("RPK", 9);
								document.setName(dm.getName() + " " + pfx.getCode());
								document.setCode(pfx.getPrefix() + pfx.getCode());
								for (AGLThing wt : document.getThings()) {
									Thing t = iSvc.getRegSvc().getAglSvc().convertAGLThingToThing(wt);
									t.setStatus("INVENTARIO");//TODO: ARMENGO DO INFERNO
									IntegrationReturn iRet = iSvc.getRegSvc().getAglSvc().sendThingToWMS(t, "POST").get();

									if (iRet.isResult()) {
										t.setStatus("REEMBALADO");
										t.getSiblings().forEach(ts -> ts.setStatus("REEMBALADO"));
										iRet = iSvc.getRegSvc().getAglSvc().sendThingToWMS(t, "PUT").get();
									}
								}
							} catch (ExecutionException | InterruptedException ioe) {
								ioe.printStackTrace();
							}
							break;
						case "APORETORNO":
							pfx = iSvc.getRegSvc().getPfxSvc().findNext("ARR", 9);
							document.setName(dm.getName() + " " + pfx.getCode());
							document.setCode(pfx.getPrefix() + pfx.getCode());
							document.setStatus("NOVO");
					}
					logger.info(prof.step(document.getMetaname() + " Done ", false));
				} catch (Exception e) {
					logger.info(e.getLocalizedMessage());
				}
				document.setCreatedAt(now);
				document.setUpdatedAt(now);
				Document dirty = iSvc.getRegSvc().getAglSvc().convertAGLDocToDoc(document, false);
				Document d = iSvc.getRegSvc().getDcSvc().findById(dirty.getId());
				if (resendAfterSave) {
					for (DocumentThing dt : d.getThings()) {
						iSvc.getRegSvc().getAglSvc().sendThingToWMS(dt.getThing(), "POST");
					}
					iSvc.getRegSvc().getAglSvc().sendDocToWMS(d, "POST");
				}
				iSvc.getRegSvc().getDcSvc().fireUpdate(d);
				workDoc(d);
				prof.done("Save and Extras", false, false).forEach(logger::info);
				return IntegrationReturn.OK;
			} else if (document.getMetaname().equalsIgnoreCase("RESTOCK")) {
				DocumentModel dmOrdMov = iSvc.getRegSvc().getDmSvc().findByMetaname("ORDMOV");
				Address orig = iSvc.getRegSvc().getAddSvc().findById(UUID.fromString("b5044db5-cdd5-11e9-90f5-005056a19775"));
				Prefix prefix = iSvc.getRegSvc().getPfxSvc().findNext("RST", 9);
				String status = "RESTOCK";
				Document ordMov = new Document(dmOrdMov, dmOrdMov.getName() + " " + prefix.getCode(), prefix.getPrefix() + prefix.getCode(), status);

				for (AGLThing at : document.getThings()) {
					Product prd = iSvc.getRegSvc().getPrdSvc().findById(UUID.fromString(at.getSiblings().stream().findFirst().get().getProduct_id()));
					Address dest = iSvc.getRegSvc().getAddSvc().findById(UUID.fromString(at.getAddress_id()));
					Thing t = thSvc.createPallet(prd, orig, status, status);
					DocumentTransport dtr = new DocumentTransport(ordMov, 1, t, dest);
					DocumentThing dt = new DocumentThing(ordMov, t, "NOVO");

					ordMov.getThings().add(dt);
					ordMov.getTransports().add(dtr);
					ordMov.getItems().add(new DocumentItem(ordMov, prd, 1, "RESTOCK", "UN"));
				}
				iSvc.getRegSvc().getDcSvc().persist(ordMov);
				return IntegrationReturn.OK;
			} else
				new IntegrationReturn(false, "Unexpected Document Model: " + document.getMetaname());
		}
		return new IntegrationReturn(false, "Document Model missing");
	}

	@POST
	@Path("/qcblock/{status}/{segregate}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn qcBloc(@Context HttpHeaders rs, @QueryParam("rnc") String rncCode, @PathParam("status") String acao, @PathParam("segregate") boolean segregate, JsonArray tList) {
		try {
			String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
			User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
			DocumentModel model = iSvc.getRegSvc().getDmSvc().findByMetaname("APORNC");
			String status = acao.equals("BLOQUEAR") ? "BLOQUEADO" : "ARMAZENADO";
			List<Thing> thList = new ArrayList<>();
			List<DocumentThing> dtList = new ArrayList<>();
			Double quantity = 0d;

			if (rncCode.isEmpty()) {
				Prefix pfx = iSvc.getRegSvc().getPfxSvc().findNext("RNC", 6);
				rncCode = pfx.getPrefix() + pfx.getCode();
			}
			Document d = iSvc.getRegSvc().getDcSvc().findByModelAndCode(model, rncCode);
			if (d == null) {
				d = new Document(model, "Relatório de Não Conformidade " + rncCode, rncCode, "NOVO");

				d.setCreatedAt(Calendar.getInstance().getTime());
				d.setUpdatedAt(Calendar.getInstance().getTime());
				d.setUser(usr);
				iSvc.getRegSvc().getDcSvc().persist(d);
			}

			for (int i = 0; i < tList.size(); i++) {
				UUID tId = UUID.fromString(tList.getString(i));
				Thing t = iSvc.getRegSvc().getThSvc().findById(tId);
				DocumentThing dt = new DocumentThing(d, t, status);
				Optional<Property> optQty = t.getProperties().stream().filter(pr -> pr.getField().getMetaname().equals("QUANTITY")).findFirst();
				Property pr = optQty.get();

				t.setStatus(status);

				dt.setCreatedAt(Calendar.getInstance().getTime());
				dt.setUpdatedAt(Calendar.getInstance().getTime());
				d.getThings().add(dt);
				quantity += Double.parseDouble(pr.getValue());
				thList.add(t);
				dtList.add(dt);
			}
			if (!thList.isEmpty() && sendQCToSAP(thList.get(0), d, quantity, segregate)) {
				iSvc.getRegSvc().getThSvc().multiPersist(thList);
				iSvc.getRegSvc().getDtSvc().multiPersist(dtList);
				iSvc.getRegSvc().getDcSvc().persist(d);
				return IntegrationReturn.OK;
			}
			return new IntegrationReturn(false, "Não foi possível efetuar bloqueio no SAP.");
		} catch (Exception e) {
			logger.info(e.getLocalizedMessage());
			return new IntegrationReturn(false, e.getLocalizedMessage());
		}
	}

	@POST
	@Path("/createtransfer")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Document createTransfer(@Context HttpHeaders rs, Document tmpDoc) {
		//TODO: Origem Almoxarifado
		UUID origem = UUID.fromString("3d69771f-4c59-11e9-a948-0266c0e70a8c");

		return createTransferOrigem(rs, origem, tmpDoc);
	}

	@POST
	@Query
	@Path("/createtransfer/{origem}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public synchronized Document createTransferOrigem(@Context HttpHeaders rs, @PathParam("origem") UUID origem, Document tmpDoc) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		DocumentModel model = iSvc.getRegSvc().getDmSvc().findByMetaname("ORDTRANSF");
		Location loc = iSvc.getRegSvc().getLocSvc().findById(origem);
		Prefix prefix = iSvc.getRegSvc().getPfxSvc().findNext("TRN", 6);
		String code = prefix.getCode();
		Set<Document> planProds = new HashSet<>(tmpDoc.getSiblings());
		UUID id = UUID.randomUUID();
		Document tmp = new Document(model, "Transferência MP " + code, prefix.getPrefix() + code, "TEMP");

		tmp.setId(id);
		tmp.setCreatedAt(Calendar.getInstance().getTime());
		tmp.setUpdatedAt(Calendar.getInstance().getTime());
		tmp.setUser(usr);
		tmpDoc.setSiblings(new HashSet<>());
		iSvc.getRegSvc().getDcSvc().dirtyInsert(tmp, false);
		//iSvc.getRegSvc().getDcSvc().dirtyInsert(id, null, "Transferência MP " + code, "TEMP", Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), prefix.getPrefix() + code, model.getId(), null, usr.getId(), null, false);
		Document document = iSvc.getRegSvc().getDcSvc().findById(id);

		for (DocumentItem di : tmpDoc.getItems()) {
			Product prd = iSvc.getRegSvc().getPrdSvc().findById(di.getProduct().getId());
			List<Thing> tList = iSvc.getRegSvc().getThSvc().listByLocationAndProduct(loc, prd);
			double qty = di.getQty();

			tList.sort((t1, t2) -> {
				String expT1 = Things.getStringProperty(t1, "LOT_EXPIRE");
				String expT2 = Things.getStringProperty(t2, "LOT_EXPIRE");
				Date dtExpt1 = DateUtil.parseDate(expT1);
				Date dtExpt2 = DateUtil.parseDate(expT2);

				return dtExpt1.compareTo(dtExpt2) * -1;
			});
			Set<String> lotList = new HashSet<>();

			for (int i = 0; i < tList.size(); i++) {
				Thing t = tList.get(0);

				if (qty > 0) {
					String lot = Things.getStringProperty(t, "LOT_ID");
					String tQty = Things.getStringProperty(t, "QUANTITY").replace(",", ".");

					lotList.add(lot);
					qty -= Double.parseDouble(tQty);
				} else
					break;
			}
			if (di.getProperties() == null) di.setProperties(new HashMap<>());
			di.getProperties().put("lot-list", lotList.stream().collect(Collectors.joining("/")));
			di.setDocument(document);
			di.setProduct(prd);
			iSvc.getRegSvc().getDiSvc().persist(di);
		}

		for (DocumentField f : tmpDoc.getFields()) {
			DocumentModelField modelField = iSvc.getRegSvc().getDmfSvc().findById(f.getField().getId());
			DocumentField field = new DocumentField();

			field.setField(modelField);
			field.setDocument(document);
			field.setValue(f.getValue());
			field.setCreatedAt(Calendar.getInstance().getTime());
			field.setUpdatedAt(Calendar.getInstance().getTime());
			field.setStatus(f.getStatus());
			document.getFields().add(field);
		}

		for (Document pp : planProds) {
			pp = iSvc.getRegSvc().getDcSvc().findById(pp.getId());
			pp.setStatus("SELECIONADO");
			pp.setParent(document);
			iSvc.getRegSvc().getDcSvc().persist(pp);
		}
		document.setSiblings(planProds);
		document.setStatus("NOVO");
		iSvc.getRegSvc().getDcSvc().persist(document);
		return document;
	}

	@POST
	@Query
	@Path("/createquality/{transpId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public synchronized IntegrationReturn createQualityControl(@Context HttpHeaders rs, @PathParam("transpId") UUID transportId, JsonArray prdConf) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		DocumentModel model = iSvc.getRegSvc().getDmSvc().findByMetaname("APOQUALIDADE");
		Document transport = iSvc.getRegSvc().getDcSvc().findById(transportId);
		Document d = new Document(model, "Controle de Qualidade " + transport.getCode(), "CQ" + transport.getCode(), "NOVO");
		Document ordConf = transport.getSiblings().stream().filter(s -> s.getModel().getMetaname().equals("ORDCONF")).findFirst().get();

		d.setCreatedAt(Calendar.getInstance().getTime());
		d.setUpdatedAt(Calendar.getInstance().getTime());
		d.setParent(transport);
		d.setUser(usr);
		for (int i = 0; i < prdConf.size(); i++) {
			boolean blk = false;
			List<Thing> tList = new ArrayList<>();
			JsonObject obj = prdConf.getJsonObject(i);
			JsonArray things = obj.getJsonArray("thing_ids");
			Double blkQty = 0d;

			for (int j = 0; j < things.size(); j++) {
				String statusQualidade = obj.getString("rodape");//Estranho nome né?
				Thing t = iSvc.getRegSvc().getThSvc().findById(UUID.fromString(things.getString(j)));
				DocumentThing dt = new DocumentThing(d, t, statusQualidade);
				Optional<Property> optiLot = t.getProperties().stream().filter(pr -> pr.getField().getMetaname().equals("INTERNAL_LOT")).findFirst();
				Optional<Property> optQC = t.getProperties().stream().filter(pr -> pr.getField().getMetaname().equals("QC")).findFirst();
				Optional<Property> optRODAPE = t.getProperties().stream().filter(pr -> pr.getField().getMetaname().equals("LABEL_OBS")).findFirst();

				dt.setCreatedAt(Calendar.getInstance().getTime());
				dt.setUpdatedAt(Calendar.getInstance().getTime());
				blk = statusQualidade.equals("BLOQUEADO");
				if (optiLot.isPresent()) {
					optiLot.get().setValue(obj.getString("internal_lot"));
				} else {
					PropertyModelField pmf = t.getModel().getFields().stream().filter(pmfi -> pmfi.getMetaname().equals("INTERNAL_LOT")).findFirst().get();
					t.getProperties().add(new Property(t, pmf, obj.getString("internal_lot")));
				}
				if (optQC.isPresent()) {
					optQC.get().setValue(obj.getString("qcl"));
				} else {
					PropertyModelField pmf = t.getModel().getFields().stream().filter(pmfq -> pmfq.getMetaname().equals("QC")).findFirst().get();
					t.getProperties().add(new Property(t, pmf, obj.getString("qcl")));
				}
				if (optRODAPE.isPresent()) {
					optRODAPE.get().setValue(statusQualidade);
				} else {
					PropertyModelField pmf = t.getModel().getFields().stream().filter(pmfr -> pmfr.getMetaname().equals("LABEL_OBS")).findFirst().get();
					t.getProperties().add(new Property(t, pmf, statusQualidade));
				}
				t.setStatus(obj.getString("rodape"));
				d.getThings().add(dt);
				tList.add(t);
				if (blk) {
					Property prQty = t.getProperties().parallelStream().filter(pr -> pr.getField().getMetaname().equals("QUANTITY")).findAny().get();

					blkQty += Double.parseDouble(prQty.getValue().replace(",", "."));
				}
			}
			if (blk) {
				sendQCToSAP(tList.get(0), d, blkQty, false);
			}
			iSvc.getRegSvc().getThSvc().multiPersist(tList);
		}
		ordConf.setStatus("QUALIDADE OK");
		ordConf.getSiblings().stream().filter(s -> s.getStatus().equals("SUCESSO")).forEach(ds -> ds.setStatus("QUALIDADE OK"));
		transport.getSiblings().add(d);
		iSvc.getRegSvc().getDcSvc().persist(transport);
		return IntegrationReturn.OK;
	}

	@PUT
	@Query
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public IntegrationReturn updateDocument(@QueryParam("id") UUID id, AGLDocModelForm document) {
		System.out.println("CHEGOU PUT DO DISCO VOADOR COM ID " + id.toString());
		System.out.println(document.toString());
		//dcsv.updateDocument(agldch.convertToDocument(document));
		return IntegrationReturn.OK;
	}

	@PUT
	@Query
	@Path("/failordconf")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn saveFailedOrdConf(@Context HttpHeaders rs, @QueryParam("id") UUID id, Document document) {
		Document retOrdConf = document.getSiblings().stream().filter(d -> d.getStatus().equals("SUCESSO")).findFirst().get();
		String confType = Documents.getStringField(document, "CONF_TYPE", "ENV");
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);

		retOrdConf.setId(null);
		retOrdConf.setName(retOrdConf.getName() + " " + document.getCode().replace("CONF", ""));
		retOrdConf.setUser(usr);
		logger.info("Save Failed Ordconf: " + id);
		document = iSvc.getRegSvc().getDcSvc().findById(id);
		retOrdConf.setParent(document);
		dcSvc.dirtyInsertFullDocument(retOrdConf, false);
		retOrdConf = iSvc.getRegSvc().getDcSvc().findById(retOrdConf.getId());
		createThings(retOrdConf, confType);
		document.setStatus("SUCESSO");
		document.getSiblings().add(retOrdConf);
		iSvc.getRegSvc().getDcSvc().persist(document, false);
		mes.schedule(() -> {
			Document oc = iSvc.getRegSvc().getDcSvc().findById(id);

			iSvc.getRegSvc().getDcSvc().fireUpdate(oc);
		}, 10, TimeUnit.SECONDS);
		return IntegrationReturn.OK;
	}

	@PUT
	@Path("/addNF/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public Document addNF(@Context HttpHeaders rs, @PathParam("id") UUID id, JsonObject childObj) {
		String sNFId = childObj.getString("child-id");

		synchronized (this) {
			logger.info("Add NF " + sNFId + " on transport " + id + ": " + processing.contains(sNFId));
			if (!processing.contains(sNFId)) {
				processing.add(sNFId);

				String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
				User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
				Document parent = iSvc.getRegSvc().getDcSvc().findById(id);
				Document child = iSvc.getRegSvc().getDcSvc().findById(UUID.fromString(sNFId));
				boolean nfIn = child.getModel().getMetaname().equals("NFENTRADA");
				boolean nfOut = child.getModel().getMetaname().equals("NFSAIDA");
				boolean vasout = child.getItems().parallelStream()
								.allMatch(di -> di.getProduct().getModel().getMetaname().equals("OUT") || di.getProduct().getModel().getMetaname().equals("VAS"));

				if (parent.getSiblings().stream()
								.filter(ds -> ds.getModel().getMetaname().equals("ORDMOV") && Documents.getStringField(ds, "MOV_TYPE", "").equals(nfOut ? "LOAD" : "UNLOAD"))
								.anyMatch(om -> om.getStatus().equals("SUCESSO") || iSvc.getRsm().getTsm().isTaskLocked(om.getId())))
					return null;
				child.setUser(usr);
				child.setParent(parent);
				iSvc.getRegSvc().getDcSvc().persist(child);
				parent.getSiblings().add(child);
				if (!vasout) {
					logger.info("Adicao de NF de Produtos " + parent.getCode());

					switch (parent.getStatus()) {
						case "CAMINHAO NA ENTRADA":
							break;
						case "CAMINHAO NO PATIO":
						case "CAMINHAO NA DOCA":
						case "CAMINHAO DESCARREGADO":
						case "CAMINHAO CARREGADO":
							boolean hasConfOut = parent.getSiblings().parallelStream()
											.filter(s -> s.getModel().getMetaname().equals("ORDCONF") && !s.getModel().getStatus().equals("CANCELADO") && !s.getModel().getStatus().equals("SUCESSO"))
											.anyMatch(cnf -> cnf.getFields().stream()
															.filter(df -> df.getField().getMetaname().contentEquals("CONF_TYPE"))
															.allMatch(df -> df.getValue().equals("SPAPD")) && !cnf.getStatus().equals("CANCELADO"));
							boolean hasConfMP = parent.getSiblings().parallelStream()
											.filter(s -> s.getModel().getMetaname().equals("ORDCONF"))
											.anyMatch(cnf -> cnf.getFields().stream()
															.filter(df -> df.getField().getMetaname().contentEquals("CONF_TYPE"))
															.allMatch(df -> df.getValue().equals("EMP")));
							boolean hasPicking = parent.getSiblings().parallelStream()
											.anyMatch(s -> s.getModel().getMetaname().equals("PICKING") && !s.getStatus().equals("CANCELADO") && !s.getStatus().equals("SEPARADO"));

							logger.info(parent.getCode() + " -  nfIn: " + nfIn + " nfOut: " + nfOut + " hasConfOut: " + hasConfOut + " hasConfMP: " + hasConfMP + " hasPicking: " + hasPicking);
							if (nfOut) {
								boolean confCompleted = parent.getSiblings().parallelStream()
												.filter(s -> s.getModel().getMetaname().equals("ORDCONF"))
												.anyMatch(cnf -> cnf.getFields().stream()
																.filter(df -> df.getField().getMetaname().contentEquals("CONF_TYPE"))
																.allMatch(df -> df.getValue().equals("SPAPD")) && cnf.getStatus().equals("SUCESSO"));
								boolean pickCompleted = parent.getSiblings().parallelStream()
												.anyMatch(s -> s.getModel().getMetaname().equals("PICKING") && s.getStatus().equals("SEPARADO"));

								logger.info(parent.getCode() + " - pickCompleted: " + pickCompleted + " confCompleted: " + confCompleted);
								if (hasPicking) {
									Document picking = parent.getSiblings().parallelStream()
													.filter(ds -> ds.getModel().getMetaname().equals("PICKING") && !ds.getStatus().equals("CANCELADO") && !ds.getStatus().equals("SEPARADO"))
													.findAny()
													.get();

									picking.setStatus("CANCELADO");
									parent.getSiblings().removeIf(ds -> ds.getId().equals(picking.getId()));
									iSvc.getRegSvc().getDcSvc().persist(picking);
								}
								if (hasConfOut) {
									Document ordConf = parent.getSiblings().parallelStream()
													.filter(s -> s.getModel().getMetaname().equals("ORDCONF") && !s.getModel().getStatus().equals("CANCELADO") && !s.getModel().getStatus().equals("SUCESSO") && s.getFields().stream()
																	.filter(df -> df.getField().getMetaname().contentEquals("CONF_TYPE"))
																	.allMatch(df -> df.getValue().equals("SPAPD")))
													.findAny()
													.get();

									ordConf.setStatus("CANCELADO");
									ordConf.setParent(null);
									parent.getSiblings().removeIf(ds -> ds.getId().equals(ordConf.getId()));
									iSvc.getRegSvc().getDcSvc().persist(ordConf);
								}

								if (!pickCompleted)
									iSvc.getRegSvc().getWmsSvc().createPicking(parent);
								if (!confCompleted)
									iSvc.getRegSvc().getWmsSvc().createOutboundChecking(parent);
							}

							if (nfIn) {
								if (hasConfMP) {
									Document ordConf = parent.getSiblings().parallelStream()
													.filter(s -> s.getModel().getMetaname().equals("ORDCONF") && s.getFields().stream()
																	.filter(df -> df.getField().getMetaname().contentEquals("CONF_TYPE"))
																	.allMatch(df -> df.getValue().equals("EMP")))
													.findAny()
													.get();

									for (DocumentItem di : child.getItems()) {
										DocumentItem dii = new DocumentItem();
										Optional<DocumentItem> optPrd = ordConf.getItems().stream().filter(oci -> oci.getProduct().getId().equals(di.getProduct().getId())).findAny();

										if (optPrd.isPresent()) {
											dii = optPrd.get();
										} else {
											dii.setQty(0d);
											dii.setDocument(ordConf);
										}
										dii.setQty(dii.getQty() + di.getQty());
										dii.setMeasureUnit(di.getMeasureUnit());
										dii.setProduct(di.getProduct());
										dii.setStatus(di.getStatus());
										dii.setName(di.getName());
										iSvc.getRegSvc().getDiSvc().persist(dii);
									}
								}
							}
							break;
					}
					//Give time for this transaction to be completed before calling WMS
					iSvc.getRegSvc().getWmsSvc().clearTnpSibs(id.toString(), nfIn, nfOut, nfIn, nfOut);
					delayedSendDocument(parent);
				}
				mes.schedule(() -> processing.remove(sNFId), 5, TimeUnit.MINUTES);
				return parent;
			}
		}
		return null;
	}

	@Asynchronous
	@Transactional(value = TxType.NOT_SUPPORTED)
	private void delayedSendDocument(Document parent) {
		logger.info("Scheduling Send Document " + parent.getCode() + " to WMS in 3 SECONDS");
		Executors.newSingleThreadScheduledExecutor()
						.schedule(() -> {
							try {
								updateDockPlates(parent);
								if (!parent.getStatus().equals("CAMINHAO NA PORTARIA") && !parent.getStatus().equals("CAMINHAO NA ENTRADA"))
									parent.setStatus("CAMINHAO NO PATIO");
								iSvc.getRegSvc().getAglSvc().sendDocToWMS(parent, "PUT").get();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}, 3, TimeUnit.SECONDS);
	}

	private void updateDockPlates(Document d) {
		Map<String, UUID> params = new HashMap<>(3, 0.1f);
		params.put("1e0ffd68-4040-11ea-89cb-005056a19775", UUID.fromString("7aa5bb90-97c3-11e9-815b-005056a19775"));
		params.put("1e1011ef-4040-11ea-89cb-005056a19775", UUID.fromString("7aa5f6a7-97c3-11e9-815b-005056a19775"));
		params.put("1e101515-4040-11ea-89cb-005056a19775", UUID.fromString("7aa61b46-97c3-11e9-815b-005056a19775"));
		params.put("27a17630-563a-11e9-b375-005056a19775", UUID.fromString("7aa5f6a7-97c3-11e9-815b-005056a19775"));
		params.put("27998254-563a-11e9-b375-005056a19775", UUID.fromString("ebefbe66-741b-11ea-9d3c-005056a19775"));
		Thing truck = iSvc.getRegSvc().getThSvc().findByUnitId(d.getThings().stream()
						.filter(dt -> dt.getThing().getUnits().size() > 0)
						.findAny().get().getThing().getUnits().stream()
						.findAny().get());
		String plates = truck.getUnitModel().stream().filter(u -> u.getType() == UnitType.LICENSEPLATES).findFirst().get().getTagId();
		String docaChamada = Documents.getStringField(d, "DOCK");
		if (!docaChamada.isEmpty()) {
			Address docaVirtual = iSvc.getRegSvc().getAddSvc().findById(params.get(docaChamada));
			int left = Things.getIntProperty(truck, "LEFT_SIDE_QUANTITY");
			int right = Things.getIntProperty(truck, "RIGHT_SIDE_QUANTITY");
			List<Address> bays = docaVirtual.getSiblings()
							.parallelStream()
							.filter(a -> Addresses.getIntField(a, "ROAD_SEQ") <= (left + right))
							.sorted((a1, a2) -> Addresses.getIntField(a1, "ROAD_SEQ") - Addresses.getIntField(a2, "ROAD_SEQ"))
							.collect(Collectors.toList());
			for (Address baia : bays)
				iSvc.getRegSvc().getWmsSvc().updateAddressCode(baia.getId(), plates);
		} else
			logger.info("Transport " + d.getCode() + " Not called to dock yet " + "(" + d.getStatus() + ")");
	}

	@PUT
	@Path("/remNF/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRED)
	public synchronized Document remNF(@Context HttpHeaders rs, @PathParam("id") UUID id, JsonObject childObj) {
		String sNFId = childObj.getString("child-id");
		logger.info("Rem NF " + sNFId + " on transport " + id + ": " + processing.contains(sNFId));
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		Document parent = iSvc.getRegSvc().getDcSvc().findById(id);
		Document child = iSvc.getRegSvc().getDcSvc().findById(UUID.fromString(sNFId));
		boolean remEntrada = child.getModel().getMetaname().equals("NFENTRADA");
		boolean remSaida = child.getModel().getMetaname().equals("NFSAIDA");
		boolean resetEnt = !parent.getSiblings().stream().filter(ds -> !ds.getId().equals(child.getId())).anyMatch(ds -> ds.getModel().getMetaname().equals("NFENTRADA"));
		boolean resetExt = !parent.getSiblings().stream().filter(ds -> !ds.getId().equals(child.getId())).anyMatch(ds -> ds.getModel().getMetaname().equals("NFSAIDA"));
		List<Document> movList = parent.getSiblings().parallelStream()
						.filter(ds -> ds.getModel().getMetaname().equals("ORDMOV") && !ds.getStatus().equals("CANCELADO"))
						.filter(om -> Documents.getStringField(om, "MOV_TYPE").equals(remEntrada ? "UNLOAD" : (remSaida ? "LOAD" : "NOREM")))
						.collect(Collectors.toList());
		List<Document> confList = parent.getSiblings().parallelStream()
						.filter(ds -> ds.getModel().getMetaname().equals("ORDCONF") && !ds.getStatus().equals("CANCELADO"))
						.filter(om -> Documents.getStringField(om, "CONF_TYPE").equals(remEntrada ? "EPAPD" : (remSaida ? "SPAPD" : "NOREM")))
						.collect(Collectors.toList());
		List<Document> pickList = parent.getSiblings().parallelStream()
						.filter(ds -> ds.getModel().getMetaname().equals("PICKING") && !ds.getStatus().equals("CANCELADO"))
						.collect(Collectors.toList());

		if (movList.stream().anyMatch(m -> m.getStatus().equals("SUCESSO") || iSvc.getRsm().getTsm().isTaskLocked(m.getId())))
			return null;
		if (confList.stream().anyMatch(m -> m.getStatus().equals("SUCESSO") || iSvc.getRsm().getTsm().isTaskLocked(m.getId())))
			return null;
		processing.remove(sNFId);
		child.setUser(usr);
		child.setParent(null);
		iSvc.getRegSvc().getDcSvc().persist(child);
		iSvc.getRegSvc().getWmsSvc().clearTnpSibs(id.toString(), remEntrada, remSaida, resetEnt, resetExt);
		if (parent.getStatus().equalsIgnoreCase("CAMINHAO NA DOCA") || parent.getStatus().equalsIgnoreCase("CAMINHAO DESCARREGADO")) {
			Optional<Document> optConf = parent.getSiblings().stream().filter(s -> s.getModel().getMetaname().equals("ORDCONF")).findAny();

			if (optConf.isPresent()) {
				Document ordConf = optConf.get();

				for (DocumentItem di : child.getItems()) {
					Optional<DocumentItem> optPrd = ordConf.getItems().stream().filter(oci -> oci.getProduct().getId().equals(di.getProduct().getId())).findAny();

					if (optPrd.isPresent()) {
						DocumentItem dii = optPrd.get();

						if (di.getQty() == dii.getQty()) {
							ordConf.getItems().remove(dii);
							iSvc.getRegSvc().getDiSvc().removeById(dii.getId());
						} else {
							dii.setQty(dii.getQty() - di.getQty());
							iSvc.getRegSvc().getDiSvc().persist(dii);
						}
					}
				}

				if (ordConf.getItems().isEmpty()) {
					for (DocumentField df : ordConf.getFields())
						iSvc.getRegSvc().getDfSvc().removeById(df.getId());
					iSvc.getRegSvc().getDcSvc().removeById(ordConf.getId());
				}
			}
		}
		cancelMovs(movList, usr);
		if (remEntrada)
			cancelConfs(confList, usr);
		else if (remSaida) {
			for (Document cnf : confList)
				cancelDoc(cnf, usr);
			iSvc.getRegSvc().getDcSvc().multiPersist(confList);
			for (Document pck : pickList)
				cancelDoc(pck, usr);
			iSvc.getRegSvc().getDcSvc().multiPersist(pickList);
		}
		mes.submit(() -> iSvc.getRegSvc().getAglSvc().sendDocToWMS(child, "DELETE"));
		return iSvc.getRegSvc().getDcSvc().findById(child.getId());
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	private void cancelConfs(List<Document> confList, User usr) {
		mes.submit(() -> {
			for (Document cnf : confList) {
				iSvc.getRsm().getTsm().cancelTask(usr.getId(), cnf);
				try {
					IntegrationReturn iRet = iSvc.getRegSvc().getAglSvc().sendDocToWMS(cnf, "DELETE").get();

					if (iRet.isResult()) {
						cancelDoc(cnf, usr);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				iSvc.getRsm().getTsm().unlockTask(cnf);
			}
		});
	}

	private void cancelDoc(Document doc, User usr) {
		for (DocumentThing dt : doc.getThings())
			dt.setStatus("CANCELADO");
		for (DocumentTransport dtr : doc.getTransports())
			dtr.setStatus("CANCELADO");
		for (DocumentItem di : doc.getItems())
			di.setStatus("CANCELADO");
		doc.setStatus("CANCELADO");
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	private void cancelMovs(List<Document> movList, User usr) {
		mes.submit(() -> {
			for (Document mov : movList) {
				iSvc.getRsm().getTsm().cancelTask(usr.getId(), mov);
				iSvc.getRegSvc().getWmsSvc().cancelOrdMov(mov, usr);
				iSvc.getRsm().getTsm().unlockTask(mov);
			}
		});
	}

	@DELETE
	@Path("/cancellibrota/{id}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public IntegrationReturn cancelLibRota(@Context HttpHeaders rs, @PathParam("id") UUID id) {
		Document transp = iSvc.getRegSvc().getDcSvc().findById(id);

		if (transp.getStatus().equals("ROMANEIO")) {
			transp.getSiblings().parallelStream()
							.filter(ds -> ds.getModel().getMetaname().equals("PICKING") && ds.getStatus().equals("SEPARACAO"))
							.forEach(ds -> {
								ds.getSiblings().forEach(dss -> dss.setStatus("CANCELADO"));
								ds.setStatus("ROMANEIO");
							});
			transp.getSiblings().parallelStream()
							.filter(ds -> ds.getModel().getMetaname().equals("APOINICIOROTA"))
							.findAny()
							.get();
			transp.setStatus("INTEGRADO");
			return IntegrationReturn.OK;
		}
		return new IntegrationReturn(false, "Status Inválido: " + transp.getStatus());
	}

	private void workDoc(Document d) {
		switch (d.getModel().getMetaname()) {
			case "ORDMOV":
				Document prnt = d.getParent_id() != null && !d.getParent_id().isEmpty() ? iSvc.getRegSvc().getDcSvc().findById(UUID.fromString(d.getParent_id())) : null;

				if (prnt != null && prnt.getModel().getMetaname().equals("TRANSPORT")) {
					for (DocumentThing dt : d.getThings()) {
						Thing t = dt.getThing();

						if (prnt.getThings().stream().noneMatch(dtt -> dtt.getThing().getId().equals(t.getId()))) {
							DocumentThing dtt = new DocumentThing(prnt, t, t.getStatus());

							dtt.setCreatedAt(Calendar.getInstance().getTime());
							dtt.setUpdatedAt(Calendar.getInstance().getTime());
							iSvc.getRegSvc().getDtSvc().persist(dtt);
						}
					}
				}
				break;
			case "ORDCONF":
				//Remove Products Without Conference
				//d.getItems().stream()
				//				.map(di -> di.getProduct().getModel())
				//				.anyMatch(pm -> pm.getProperties().containsKey("blind_conf") && pm.getProperties().get("blind_conf").equalsIgnoreCase("true"));
				break;
			case "ORDCRIACAO":
			case "APORNC":
			case "APOAVARIA":
				iSvc.getRegSvc().getAglSvc().sendDocToWMS(d, "POST");
				//				try {
				//					iSvc.getRegSvc().getAglSvc().sendDocToWMS(d, "POST").get();
				//				} catch (InterruptedException | ExecutionException e) {
				//					e.printStackTrace();
				//				}
				break;
			case "REPACK":
				Address a = null;

				if (d.getStatus().equals("RETORNO DE ROTA") || d.getStatus().equals("RETRONO DE ROTA")) {//TODO: Remove once apks are updated
					a = iSvc.getRegSvc().getAddSvc().findById(UUID.fromString("354a3c85-348f-11ea-8a83-005056a19775"));
				} else if (d.getStatus().equals("REEMBALAGEM")) {
					a = iSvc.getRegSvc().getAddSvc().findById(UUID.fromString("2814d99b-34a2-11ea-8a83-005056a19775"));
				}

				if (a != null) {
					DocumentModel dmOM = iSvc.getRegSvc().getDmSvc().findByMetaname("ORDMOV");
					DocumentModelField dmPrio = dmOM.getFields().stream().filter(dmf -> dmf.getMetaname().equals("PRIORITY")).findAny().get();
					DocumentModelField dmType = dmOM.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TYPE")).findAny().get();
					DocumentModelField dmTitle = dmOM.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TITLE")).findAny().get();
					Prefix pfx = iSvc.getRegSvc().getPfxSvc().findNext("RPK", 9);
					Document om = new Document(dmOM, dmOM.getName() + " " + pfx.getCode(), pfx.getPrefix() + pfx.getCode(), "ATIVO");

					om.getFields().add(new DocumentField(om, dmPrio, "NOVO", "1"));
					om.getFields().add(new DocumentField(om, dmType, "NOVO", "RESSUPRIMENTO"));
					om.getFields().add(new DocumentField(om, dmTitle, "NOVO", "PÁTIO: " + d.getStatus()));
					for (DocumentThing dt : d.getThings()) {
						Thing t = dt.getThing();

						om.getThings().add(new DocumentThing(om, t, t.getStatus()));
						om.getTransports().add(new DocumentTransport(om, om.getTransports().size() + 1, t, a));
						for (Thing ts : t.getSiblings()) {
							Product prd = ts.getProduct();
							String um = prd.getFields().parallelStream().filter(pf -> pf.getModel().getMetaname().equals("GROUP_UM")).findAny().get().getValue();
							double qty = Double.parseDouble(ts.getProperties().parallelStream().filter(pr -> pr.getField().getMetaname().equals("QUANTITY")).findAny().get().getValue());

							om.getItems().add(new DocumentItem(om, prd, qty, "REEMBALAGEM", um));
						}
					}
					om.setParent(d);
					d.getSiblings().add(om);
					iSvc.getRegSvc().getDcSvc().persist(om);
				}
				break;
			case "APORETORNO":
				Prefix pfx = iSvc.getRegSvc().getPfxSvc().findNext("OCR", 9);
				DocumentModel dmCri = iSvc.getRegSvc().getDmSvc().findByMetaname("ORDCRIACAO");
				Document oc = new Document(dmCri, dmCri.getName() + " " + pfx.getCode(), pfx.getPrefix() + pfx.getCode(), "NOVO");

				oc.getThings().addAll(d.getThings().parallelStream().map(dt -> new DocumentThing(oc, dt.getThing(), "NOVO")).collect(Collectors.toList()));
				iSvc.getRegSvc().getDcSvc().persist(oc);
				iSvc.getRegSvc().getAglSvc().sendDocToWMS(oc, "POST");
				break;
			default:
				logger.info("No SIM Extras for " + d.getModel().getMetaname());
				break;
		}
	}

	private void createThings(Document retOrdConf, String confType) {
		logger.info("Save RetOrdConf Items: " + retOrdConf.getItems().size());

		for (DocumentItem di : retOrdConf.getItems()) {
			Product p = di.getProduct();
			int vol = Integer.parseInt(di.getProperties().get("volumes"));

			logger.info("Di: " + di.getId() + " Volumes: " + vol + " DiQty: " + di.getQty() + " PropQty: " + di.getProperties().get("quantity"));
			for (int i = 0; i < vol; i++) {
				switch (confType) {
					case "EMP":
						PropertyModel prm = p.getModel().getPropertymodel();
						Thing t = new Thing(p.getName(), p, prm, retOrdConf.getStatus());
						DocumentThing dt = new DocumentThing(retOrdConf, t, retOrdConf.getStatus());

						for (String key : di.getProperties().keySet()) {
							Optional<PropertyModelField> optPrmf = prm.getFields().stream().filter(prmf -> prmf.getMetaname().toUpperCase().equals(key.toUpperCase())).findFirst();

							if (optPrmf.isPresent()) {
								PropertyModelField prmf = optPrmf.get();
								String value = di.getProperties().get(key);
								Property pr = new Property(t, prmf, value);

								t.getProperties().add(pr);
							}
						}
						dt.setName(t.getName());
						dt.setCreatedAt(Calendar.getInstance().getTime());
						dt.setUpdatedAt(Calendar.getInstance().getTime());
						dt.setDocument(retOrdConf);
						iSvc.getRegSvc().getThSvc().persist(t);
						iSvc.getRegSvc().getDtSvc().persist(dt);
						retOrdConf.getThings().add(dt);
						break;
					case "EPAPD":
						DocumentThing dtPA = retOrdConf.getThings().stream().findAny().get();
						Thing tPA = dtPA.getThing();

						for (String key : di.getProperties().keySet()) {
							Optional<Property> optPr = tPA.getProperties().stream().filter(pr -> pr.getField().getMetaname().toUpperCase().equals(key.toUpperCase())).findFirst();

							if (optPr.isPresent()) {
								Property pr = optPr.get();
								String value = di.getProperties().get(key);

								pr.setValue(value);
							}
						}
						iSvc.getRegSvc().getDtSvc().persist(dtPA);
						break;
					default:
						logger.info("ConfType: " + confType);
				}
			}
		}
	}

	private boolean sendQCToSAP(Thing t, Document rnc, Double qty, boolean segregate) {
		boolean retVal = true;
		Gson gson = new GsonBuilder().create();
		LinkedList<LinkedHashMap<String, Object>> ret = new LinkedList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
		ToJsonSAP jcoSonStart = new ToJsonSAP(solar.getFunc("Z_HW_CONTROLE_QA"));
		String batch = t.getProperties().stream().filter(pr -> pr.getField().getMetaname().equals("LOT_ID")).findFirst().get().getValue();
		Product p = t.getProduct();
		Address a = t.getAddress();
		boolean isBlock = t.getStatus().equals("BLOQUEADO");
		boolean move = segregate && isBlock;
		boolean back = segregate && !isBlock;
		boolean blockOnly = !segregate && isBlock;

		item.put("MANDT", "120");//FIXED
		item.put("PLANT", "CNAT");
		item.put("REF_DOC_NO", rnc.getCode());
		item.put("PSTNG_DATE", Calendar.getInstance().getTime());
		item.put("DOC_DAT", Calendar.getInstance().getTime());
		item.put("MATERIAL", t.getProduct().getSku());
		item.put("BATCH", batch);
		if (isBlock) {
			item.put("MOVE_TYPE", 344);
			item.put("MOVE_COD", "BQ");
		} else {
			item.put("MOVE_TYPE", 343);
			item.put("MOVE_COD", "DQ");
		}
		if (p.getModel().getMetaname().equals("MP") || p.getModel().getMetaname().equals("OUT") || p.getModel().getMetaname().equals("VAS")) {
			if (a.getName().equals("ALMOXARIFADO")) {
				item.put("STGE_LOC", "MP01");
				item.put("MOVE_STLOC", "MP01");
			} else {
				item.put("STGE_LOC", "MP04");
				item.put("MOVE_STLOC", "MP04");
			}
		} else {
			if (move) {
				item.put("STGE_LOC", "PA01");
				item.put("MOVE_STLOC", "PA04");
			} else if (back) {
				item.put("STGE_LOC", "PA04");
				item.put("MOVE_STLOC", "PA01");
			} else if (blockOnly) {
				item.put("STGE_LOC", "PA01");
				item.put("MOVE_STLOC", "PA01");
			}
		}
		item.put("ENTRY_QNT", qty);
		item.put("UNID_MED", "");
		item.put("MOVE_PLANT", ConfigUtil.get("hunter-custom-solar", "sap-plant-code", "CNAT"));
		item.put("MOVE_BATCH", batch);
		item.put("TIPO_NRHUNTER", rnc.getCode());
		item.put("ANO", Calendar.getInstance().get(Calendar.YEAR));
		item.put("DOCUMENTO", rnc.getCode());
		ret.add(item);

		jcoSonStart.setTableParameter("T_ZWH_CTLQA", ret);

		ReadFieldsSap readFieldsSap = null;
		try {
			String sapRet = jcoSonStart.execute(solar.getDestination());

			readFieldsSap = gson.fromJson(sapRet, ReadFieldsSap.class);
		} catch (JsonSyntaxException e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			retVal = false;
		} catch (JCoException e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			retVal = false;
		}

		for (SAPReturnDTO msg : readFieldsSap.getReturnDTOs()) {
			AlertSeverity sev = AlertSeverity.INFO;

			if (msg.getTipo().equals("E")) {
				sev = AlertSeverity.ERROR;
			} else if (msg.getTipo().equals("W")) sev = AlertSeverity.WARNING;
			iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.DOCUMENT, sev, "0371831678", msg.getMensagem(), msg.getSeq()));
		}
		return retVal;
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	private IntegrationReturn fuckingHibernate(Document prds, AGLDocModelForm aglDoc) {
		IntegrationReturn iRet = IntegrationReturn.OK;

		prds.getItems().forEach(item -> iSvc.getRegSvc().getDiSvc().removeById(item.getId()));
		prds.getItems().clear();
		prds.getFields().forEach(item -> iSvc.getRegSvc().getDfSvc().removeById(item.getId()));
		prds.getFields().clear();
		for (AGLDocModelItem item : aglDoc.getItems()) {
			Product p = iSvc.getRegSvc().getPrdSvc().findById(UUID.fromString(item.getProduct_id()));
			DocumentItem di = new DocumentItem(prds, p, item.getQty().doubleValue(), "NOVO");

			di.setMeasureUnit(item.getUnit_measure());
			prds.getItems().add(di);
			//			iSvc.getRegSvc().getDiSvc().persist(di);
		}

		for (String key : aglDoc.getProps().keySet()) {
			String value = aglDoc.getProps().get(key);
			DocumentModelField dmf = iSvc.getRegSvc().getDmfSvc().findByModelAndMetaname(prds.getModel(), key.toUpperCase());
			Optional<DocumentField> optDF = prds.getFields().stream().filter(df -> df.getField().getId().equals(dmf.getId())).findFirst();
			DocumentField df = optDF.isPresent() ? optDF.get() : new DocumentField(prds, dmf, "INTWMS", value);

			prds.getFields().add(df);
			//			iSvc.getRegSvc().getDfSvc().persist(df);
		}
		if (prds.getItems().size() > 0)
			prds.setStatus("NOVO");
		return iRet;
	}

	public Document recordProduction(Document prodOrder, Product prd, double quantity) {
		DocumentModel prodRecordMod = iSvc.getRegSvc().getDmSvc().findByMetaname("APOPRODUCAO");
		Prefix recordPfx = iSvc.getRegSvc().getPfxSvc().findNext("APRD", 10);
		Document dRecord = new Document(prodRecordMod, prodRecordMod.getName() + " - " + recordPfx.getCode(), recordPfx.getCode(), "NOVO");
		DocumentField dfBoxCount = prodOrder.getFields().parallelStream()
						.filter(df -> df.getField().getMetaname().equals(""))
						.findAny()
						.orElse(null);

		if (dfBoxCount != null) {
			Integer currVal = Integer.parseInt(dfBoxCount.getValue());
			Integer palletBox = Products.getIntegerField(prd, "PALLET_BOX", 1);

			dfBoxCount.setValue(DF.format(currVal - palletBox + quantity));
			iSvc.getRegSvc().getDfSvc().quickUpdateValue(dfBoxCount);
		}
		dRecord.setParent(prodOrder);
		dRecord.getItems().add(new DocumentItem(dRecord, prd, quantity, "NOVO"));
		return iSvc.getRegSvc().getDcSvc().persist(dRecord);
	}
}
