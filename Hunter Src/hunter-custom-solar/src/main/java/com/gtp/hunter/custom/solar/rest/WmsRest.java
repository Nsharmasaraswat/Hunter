package com.gtp.hunter.custom.solar.rest;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.jsonstubs.WMSRule;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.AddressField;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.DocumentTransport;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductField;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Things;
import com.gtp.hunter.process.wf.action.BaseAction;
import com.gtp.hunter.process.wf.action.WebSocketAction;
import com.gtp.hunter.ui.json.SolarCreatePallet;
import com.gtp.hunter.ui.json.SolarPickingResupply;
import com.gtp.hunter.ui.json.action.InteractionMessage;

@RequestScoped
@Path("/wms")
public class WmsRest {

	@Inject
	private transient Logger					logger;

	@Inject
	private IntegrationService					iSvc;

	@Resource
	private ManagedExecutorService				exSvc;

	private SimpleDateFormat					prFormatter				= new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
	private SimpleDateFormat					uiParser				= new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat					lotFormatter			= new SimpleDateFormat("ddMMyy");
	private DecimalFormat						DF						= new DecimalFormat("0.0000");

	private static final Comparator<Address>	compareByOrder			= (Address o1, Address o2) -> {
																			if (o1 == null && o2 == null) return 0;
																			if (o2 == null) return 1;
																			if (o1 == null) return -1;
																			AddressField ordF1 = o1.getFields().stream()
																							.filter(af -> af.getModel().getMetaname().equals("ROAD_SEQ"))
																							.findFirst()
																							.orElse(null);
																			AddressField ordF2 = o2.getFields().stream().filter(af -> af.getModel().getMetaname().equals("ROAD_SEQ"))
																							.findFirst()
																							.orElse(null);
																			if (ordF1 == null && ordF2 == null) return 0;
																			if (ordF2 == null) return 1;
																			if (ordF1 == null) return -1;
																			int ord1 = Integer.parseInt(ordF1.getValue());
																			int ord2 = Integer.parseInt(ordF2.getValue());
																			return ord1 - ord2;
																		};
	private static final Comparator<Thing>		compareThingsByAddress	= (Thing t1, Thing t2) -> {
																			if (t1 == null && t2 == null) return 0;
																			if (t2 == null) return -1;
																			if (t1 == null) return 1;
																			Address o1 = t1.getAddress();
																			Address o2 = t2.getAddress();

																			return compareByOrder.compare(o1, o2) * -1;
																		};

	/*
	 * TRANSPORT
	 */

	@GET
	@Path("/listRules/{prgid}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response listRules(@PathParam("prgid") int prgid) {
		return Response.ok(iSvc.getRegSvc().getWmsSvc().listRules(prgid)).build();
	}

	@PUT
	@Path("/changetransportrule/{tnpid}/{tplid}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn changeRule(@Context HttpHeaders rs, @PathParam("tnpid") UUID tnpId, @PathParam("tplid") int tplId) {
		IntegrationReturn ret = new IntegrationReturn(true, null);
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		Document transport = iSvc.getRegSvc().getDcSvc().findById(tnpId);

		if (transport == null) {
			ret = new IntegrationReturn(false, "Transporte Inexistente");
		} else {
			List<Document> prdshrtList = transport.getSiblings().stream().filter(ds -> ds.getModel().getMetaname().equals("PRDSHORTAGE")).collect(Collectors.toList());

			if (prdshrtList.isEmpty()) {
				ret = new IntegrationReturn(false, "Não existe notificação de falta de produtos para o transporte");
			} else {
				Optional<Document> optprdShrt = prdshrtList.stream().filter(ds -> ds.getStatus().equals("NOVO")).findFirst();

				if (!optprdShrt.isPresent()) {
					ret = new IntegrationReturn(false, "Notificação de falta de produtos já alterada");
				} else {
					Document prdShrt = optprdShrt.get();
					Optional<DocumentField> optNewField = prdShrt.getFields().stream().filter(df -> df.getField().getMetaname().equals("NEW_TEMPLATE")).findFirst();

					prdShrt.setStatus("VERIFICADO");
					prdShrt.setUser(usr);
					if (!optNewField.isPresent()) {
						DocumentModelField newModelField = prdShrt.getModel().getFields().stream().filter(dmf -> dmf.getMetaname().equals("NEW_TEMPLATE")).findFirst().get();

						prdShrt.getFields().add(new DocumentField(prdShrt, newModelField, "NOVO", String.valueOf(tplId)));
					} else
						optNewField.get().setValue(String.valueOf(tplId));
					transport.getFields().stream().filter(df -> df.getField().getMetaname().equals("RULE_TEMPLATE")).findFirst().get().setValue(String.valueOf(tplId));
					iSvc.getRegSvc().getDcSvc().persist(transport);
				}
			}
		}
		return ret;
	}

	@PUT
	@Path("/reevaluatetransportrule/{tnpid}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.NOT_SUPPORTED)
	public IntegrationReturn reevaluateTransport(@Context HttpHeaders rs, @PathParam("tnpid") UUID tnpId) {
		IntegrationReturn ret = new IntegrationReturn(true, null);
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		Document transport = iSvc.getRegSvc().getDcSvc().findById(tnpId);

		if (transport.getStatus().equals("CAMINHAO NA PORTARIA") || transport.getStatus().equals("CAMINHAO NA ENTRADA") || transport.getStatus().equals("CAMINHAO NO PATIO")) {
			try {
				Optional<Document> optPrds = transport.getSiblings().stream()
								.filter(ds -> ds.getModel().getMetaname().equals("PRDSHORTAGE"))
								.findAny();
				if (optPrds.isPresent()) {
					Document prds = iSvc.getRegSvc().getDcSvc().findById(optPrds.get().getId());

					//					iSvc.getRegSvc().getDiSvc().quickRemoveByIds(prds.getItems().stream().map(di -> di.getId()).collect(Collectors.toList()));
					prds.setUser(usr);
					prds.getItems().forEach(item -> iSvc.getRegSvc().getDiSvc().removeById(item.getId()));
					prds.getItems().clear();
					iSvc.getRegSvc().getDiSvc().flush();
					iSvc.getRegSvc().getDcSvc().flush();
				}
				ret = iSvc.getRegSvc().getAglSvc().sendDocToWMS(transport, "PUT").get();
			} catch (InterruptedException | ExecutionException ex) {
				ret = new IntegrationReturn(false, ex.getLocalizedMessage());
			}
		} else {
			ret = new IntegrationReturn(false, "Status não permite reavaliação (" + transport.getStatus() + "). Usar Gerenciamento de Transporte");
		}

		return ret;
	}

	@PUT
	@Path("/buildnewrule/{tnpid}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn buildNewRule(@Context HttpHeaders rs, @PathParam("tnpid") UUID tnpId) {
		IntegrationReturn ret = new IntegrationReturn(true, null);
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		Document transport = iSvc.getRegSvc().getDcSvc().findById(tnpId);

		try {
			Document prds = transport.getSiblings().stream()
							.filter(ds -> ds.getModel().getMetaname().equals("PRDSHORTAGE"))
							.findAny()
							.get();
			List<Product> missingProducts = prds.getItems().stream()
							.map(di -> di.getProduct())
							.collect(Collectors.toList());
			DocumentModelField dmfNewRule = prds.getModel().getFields().stream()
							.filter(dmf -> dmf.getMetaname().equals("NEW_TEMPLATE"))
							.findAny()
							.get();
			DocumentField transpRule = transport.getFields().stream()
							.filter(df -> df.getField().getMetaname().equals("RULE_TEMPLATE"))
							.findAny()
							.get();
			DocumentField ruleField = prds.getFields().stream()
							.filter(df -> df.getField().getMetaname().equals("TEMPLATE_ID"))
							.findAny()
							.get();
			DocumentField newRuleField = prds.getFields().stream()
							.filter(df -> df.getField().getId().equals(dmfNewRule.getId()))
							.findAny()
							.orElseGet(() -> new DocumentField(prds, dmfNewRule, "NOVO", ""));
			WMSRule rule = iSvc.getRegSvc().getWmsSvc().findRule(2, Integer.parseInt(ruleField.getValue()));
			String newName = rule.getName() + "_" + transport.getCode();

			missingProducts.removeIf(prd -> iSvc.getRegSvc().getThSvc().listByProductIdAndNotStatus(prd.getId(), "EXPEDIDO").size() == 0);
			if (missingProducts.size() > 0) {
				int newRuleId = iSvc.getRegSvc().getWmsSvc().insertRule(rule.getId(), newName, missingProducts);

				transpRule.setValue("" + newRuleId);
				newRuleField.setValue("" + newRuleId);
				iSvc.getRegSvc().getDfSvc().persist(newRuleField);
				iSvc.getRegSvc().getDfSvc().persist(transpRule);
				prds.getFields().add(newRuleField);
				if (missingProducts.size() == prds.getItems().size()) {
					prds.setStatus("VERIFICADO");
				}
				prds.setUser(usr);
				iSvc.getRegSvc().getDiSvc().quickRemoveByIds(prds.getItems().stream().map(di -> di.getId()).collect(Collectors.toList()));
				prds.getItems().clear();
				ret = iSvc.getRegSvc().getAglSvc().sendDocToWMS(transport, "PUT").get();
			} else
				ret = new IntegrationReturn(false, "PRODUTOS FALTANTES NO ESTOQUE");
		} catch (InterruptedException | ExecutionException ex) {
			ret = new IntegrationReturn(false, ex.getLocalizedMessage());
		}
		return ret;
	}

	@PUT
	@Path("/addtnppallet/{tnpid}/{productid}/{quantity}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public Response addPalletMov(@Context HttpHeaders rs, @PathParam("tnpid") UUID tnpId, @PathParam("productid") UUID prdId, @PathParam("quantity") int quantity) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		Document transport = iSvc.getRegSvc().getDcSvc().findById(tnpId);
		DocumentModel dmOrdMov = iSvc.getRegSvc().getDmSvc().findByMetaname("ORDMOV");
		DocumentModelField dmPrio = dmOrdMov.getFields().stream().filter(dmf -> dmf.getMetaname().equals("PRIORITY")).findAny().get();
		DocumentModelField dmType = dmOrdMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TYPE")).findAny().get();
		DocumentModelField dmTitle = dmOrdMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TITLE")).findAny().get();
		Prefix pfx = iSvc.getRegSvc().getPfxSvc().findNext("LDT", 9);
		Document d = new Document(dmOrdMov, dmOrdMov.getName() + " " + pfx.getPrefix() + pfx.getCode(), pfx.getPrefix() + pfx.getCode(), "ATIVO");
		Thing truck = transport.getThings().stream()
						.filter(dt -> dt.getThing().getModel().getMetaname().equals("TRUCK"))
						.map(dt -> dt.getThing())
						.findAny()
						.get();
		int truckSize = truck.getProperties().stream()
						.filter(pr -> pr.getField().getMetaname().endsWith("QUANTITY"))
						.map(pr -> Integer.parseInt(pr.getValue()))
						.reduce((q1, q2) -> q1 + q2)
						.get();
		List<Document> ordmovs = transport.getSiblings().stream()
						.filter(ds -> ds.getModel().getMetaname().equals("ORDMOV") && ds.getCode().startsWith("LDT") && ds.getStatus().equals("LOAD"))
						.sorted((om1, om2) -> om1.getTransports().size() - om2.getTransports().size())
						.collect(Collectors.toList());
		List<UUID> tIds = iSvc.getRegSvc().getWmsSvc().listNewestPalletsProduct(prdId, quantity);

		for (int i = 0; i < quantity; i++) {
			Thing t = iSvc.getRegSvc().getThSvc().findById(tIds.get(i));

			//			return Response.status(Status.BAD_REQUEST).entity("NÃO IMPLEMENTADO").build();
		}
		d.getFields().add(new DocumentField(d, dmPrio, "NOVO", "1"));
		d.getFields().add(new DocumentField(d, dmType, "NOVO", "LOAD"));
		d.getFields().add(new DocumentField(d, dmTitle, "NOVO", "PATIO: CARREGAMENTO"));
		d.setUser(usr);
		d.setParent(transport);
		transport.getSiblings().add(d);
		iSvc.getRegSvc().getDcSvc().persist(d);
		iSvc.getRegSvc().getAglSvc().sendDocToWMS(d, "POST");
		return Response.ok(d).build();
	}

	@PUT
	@Path("/addmovpallet/{tnpid}/{productid}/{quantity}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn addMovPallet(@Context HttpHeaders rs, @PathParam("tnpid") UUID tnpId, @PathParam("productid") UUID prdId, @PathParam("quantity") int quantity) {
		IntegrationReturn ret = new IntegrationReturn(true, null);
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		Document transport = iSvc.getRegSvc().getDcSvc().findById(tnpId);
		Thing truck = transport.getThings().stream()
						.filter(dt -> dt.getThing().getModel().getMetaname().equals("TRUCK"))
						.map(dt -> dt.getThing())
						.findAny()
						.get();
		int truckSize = truck.getProperties().stream()
						.filter(pr -> pr.getField().getMetaname().endsWith("QUANTITY"))
						.map(pr -> Integer.parseInt(pr.getValue()))
						.reduce((q1, q2) -> q1 + q2)
						.get();
		List<Document> ordmovs = transport.getSiblings().stream()
						.filter(ds -> ds.getModel().getMetaname().equals("ORDMOV") && ds.getCode().startsWith("LDT") && ds.getStatus().equals("LOAD"))
						.sorted((om1, om2) -> om1.getTransports().size() - om2.getTransports().size())
						.collect(Collectors.toList());

		if (ordmovs != null && ordmovs.size() > 0) {
			Document prds = transport.getSiblings().stream()
							.filter(ds -> ds.getModel().getMetaname().equals("PRDSHORTAGE"))
							.findAny()
							.get();
			int movsSize = ordmovs.stream()
							.map(d -> d.getTransports().size())
							.reduce((s1, s2) -> s1 + s2)
							.get();

			if ((movsSize + quantity) <= truckSize) {
				int added = 0;

				List<UUID> tIds = iSvc.getRegSvc().getWmsSvc().listNewestPalletsProduct(prdId, quantity);

				for (int i = 0; i < tIds.size(); i++) {
					UUID tId = tIds.get(i);
					int index = i % ordmovs.size();
					Document ordmov = ordmovs.get(index);
					if (!iSvc.getRsm().getTsm().isTaskLocked(ordmov.getId())) {
						Address last = ordmov.getTransports().stream()
										.sorted((dtr1, dtr2) -> dtr2.getSeq() - dtr1.getSeq())
										.findFirst()
										.get()
										.getAddress();
						UUID addrId = iSvc.getRegSvc().getWmsSvc().getNextAddress(last.getId().toString());
						Thing t = iSvc.getRegSvc().getThSvc().findById(tId);
						int seq = ordmov.getTransports().size() + 1;
						Address a = iSvc.getRegSvc().getAddSvc().findById(addrId);
						Product p = t.getSiblings().parallelStream().map(ts -> ts.getProduct()).findAny().get();
						String um = p.getFields().parallelStream().filter(pf -> pf.getModel().getMetaname().equals("GROUP_UM")).findAny().get().getValue();
						String lte_id = t.getSiblings().parallelStream()
										.flatMap(ts -> ts.getProperties().stream())
										.filter(pr -> pr.getField().getMetaname().equals("LOT_ID"))
										.map(pr -> pr.getValue())
										.findAny()
										.get();
						int qtd = t.getSiblings().parallelStream()
										.flatMap(ts -> ts.getProperties().stream())
										.filter(pr -> pr.getField().getMetaname().equals("QUANTITY"))
										.map(pr -> {
											try {
												return DF.parse(pr.getValue()).intValue();
											} catch (ParseException pe) {
												return 0;
											}
										})
										.findAny()
										.get();

						ordmov.getTransports().add(new DocumentTransport(ordmov, seq, t, a));
						ordmov.getThings().add(new DocumentThing(ordmov, t, t.getStatus()));
						ordmov.getItems().add(new DocumentItem(ordmov, p, qtd, "ADICIONADO", um));
						transport.getThings().add(new DocumentThing(transport, t, t.getStatus()));
						iSvc.getRegSvc().getWmsSvc().updateOrdMov(ordmov.getId(), seq, t.getId(), t.getAddress().getId(), a.getId());
						iSvc.getRegSvc().getWmsSvc().updateStkMov(t.getId(), a.getParent().getId(), a.getId(), lte_id, p.getId(), qtd);
						added++;
					} else
						return new IntegrationReturn(false, "MOVIMENTAÇÃO " + ordmov.getCode() + " EM EXECUÇÃO");
				}

				if (added == quantity) {
					DocumentItem diShrt = prds.getItems().stream()
									.filter(di -> di.getProduct().getId().equals(prdId))
									.findAny()
									.get();

					iSvc.getRegSvc().getDiSvc().removeById(diShrt.getId());
					prds.getItems().removeIf(di -> di.getId().equals(diShrt.getId()));
					prds.setUser(usr);
				}
				if (prds.getItems().size() == 0)
					prds.setStatus("VERIFICADO");
			} else
				ret = new IntegrationReturn(false, "NÃO CABEM MAIS PALLETS");
		} else
			ret = new IntegrationReturn(false, "TRANSPORTE SEM MOVIMENTAÇÕES");
		return ret;
	}

	/*
	 * STOCK
	 */
	@GET
	@Path("/listStock")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response listStock() {
		return Response.ok(iSvc.getRegSvc().getWmsSvc().listStock()).build();
	}

	@DELETE
	@Path("/clearAddress/{addressId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn cleanAddress(@Context HttpHeaders rs, @PathParam("addressId") UUID id) {
		Profiler prof = new Profiler("clearAddress");
		AtomicInteger ai = new AtomicInteger(0);
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		List<Thing> tList = iSvc.getRegSvc().getThSvc().listParentByChildAddressId(id);
		prof.step("List Things", false);
		Map<Integer, List<UUID>> all = iSvc.getRegSvc().getWmsSvc().listAddresStock(id);
		prof.step("List Stock", false);

		tList.addAll(iSvc.getRegSvc().getThSvc().listById(all.values()
						.parallelStream()
						.flatMap(lti -> lti.parallelStream())
						.filter(thid -> tList.parallelStream()
										.noneMatch(t -> t.getId().equals(thid)))
						.collect(Collectors.toSet())));
		prof.step("List Stock Things", false);
		tList.forEach(t -> {
			removePallet(t);
			//			prof.step("Removed " + ai.incrementAndGet(), false);
		});
		prof.step("Removed " + tList.size() + " Pallets", false);
		iSvc.getRegSvc().getWmsSvc().clearArmloc(id.toString());
		exSvc.execute(() -> {
			Address a = iSvc.getRegSvc().getAddSvc().findById(id);

			iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.WMS, AlertSeverity.INFO, a == null ? "SEM ENDEREÇO" : a.getMetaname(), "REMOÇÃO DE PRODUTO", "Usuário " + usr.getName() + " Limpou a rua " + a == null ? "SEM ENDEREÇO" : a.getMetaname()));
		});
		prof.done("Clear Armloc " + usr.getName(), false, false).forEach(logger::info);
		return IntegrationReturn.OK;
	}

	@PUT
	@Path("/changePalletStatusByAddress/{addressId}/{status}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn unblockPalletsByAddress(@Context HttpHeaders rs, @PathParam("addressId") UUID id, @PathParam("status") String status) {
		IntegrationReturn ret = new IntegrationReturn(true, null);
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);

		try {
			Address a = iSvc.getRegSvc().getAddSvc().findById(id);

			for (Thing t : iSvc.getRegSvc().getThSvc().listParentByChildAddressId(id)) {
				t.setStatus(status);
				t.setUpdatedAt(new Date());
				t.getSiblings().forEach(ts -> {
					ts.setStatus(status);
					ts.setUpdatedAt(new Date());
				});
				ret = iSvc.getRegSvc().getAglSvc().sendThingToWMS(t, "PUT").get();
			}

			logger.info("Usuário " + usr.getName() + " Alterou status dos produtos do endereço " + a.getMetaname() + " para " + status);
			iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.WMS, AlertSeverity.INFO, a.getMetaname(), "ALTERAÇÃO DE STATUS", "Usuário " + usr.getName() + " Alterou status dos produtos do endereço " + a.getMetaname() + " para " + status));
		} catch (InterruptedException | ExecutionException ignored) {
		}

		return ret;
	}

	@DELETE
	@Path("/removeFromAddress/{addressId}/{quantity}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn cleanAddress(@Context HttpHeaders rs, @PathParam("addressId") UUID id, @PathParam("quantity") int qty) {
		IntegrationReturn iRet = IntegrationReturn.OK;
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		Address parent = iSvc.getRegSvc().getAddSvc().findById(id);

		if (parent.getSiblings().size() < qty) {
			iRet = new IntegrationReturn(false, "Endereço contém menos posíções do que o informado " + parent.getSiblings().size() + " < " + qty);
		} else {
			List<Address> sibs = new ArrayList<>(parent.getSiblings());
			Comparator<Address> compareByMetaname = (Address o1, Address o2) -> {
				if (o1 == null && o2 == null) return 0;
				if (o2 == null) return 1;
				if (o1 == null) return -1;
				if (o1.getMetaname() == null && o2.getMetaname() == null) return 0;
				if (o2.getMetaname() == null) return 1;
				if (o1.getMetaname() == null) return -1;
				return o1.getMetaname().compareTo(o2.getMetaname()) * -1;
			};

			sibs.sort(compareByMetaname);
			Iterator<Address> addIt = sibs.iterator();
			for (int i = 0; i < qty && addIt.hasNext(); i++) {
				Address ta = addIt.next();
				Set<Thing> tSet = new HashSet<>(iSvc.getRegSvc().getThSvc().listByAddressId(ta.getId()));

				if (tSet.size() > 0) {
					for (Thing t : tSet) {
						if (t.getSiblings().size() > 0)
							tSet.addAll(t.getSiblings());
						else if (t.getParent() != null)
							tSet.add(t.getParent());
						tSet.add(t);
					}
					Iterator<Thing> it = tSet.iterator();

					while (it.hasNext()) {
						removePallet(it.next());
					}
				} else
					i--;

			}
			exSvc.execute(() -> logger.info("User " + usr.getName() + " Cleared address " + parent.getName()));
		}
		return iRet;
	}

	@DELETE
	@Path("/removePallet/{thingId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn removePallet(@Context HttpHeaders rs, @PathParam("thingId") UUID id) {
		IntegrationReturn iRet = IntegrationReturn.OK;
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		Thing t = iSvc.getRegSvc().getThSvc().findById(id);

		if (t != null) {
			Address a = t.getAddress();
			String prdName = t.getSiblings().size() > 0 ? t.getSiblings().parallelStream()
							.filter(ts -> ts.getProduct() != null)
							.map(ts -> ts.getProduct().getSku() + " - " + ts.getProduct().getName())
							.distinct()
							.collect(Collectors.joining(",")) : t.getProduct() == null ? "SEM PRODUTO" : t.getProduct().getSku() + " - " + t.getProduct().getName();

			if (t.getSiblings().size() == 0) {
				if (t.getParent() != null)//id = prd (thingsib)
					t = t.getParent();
				else {//may have pending transport
					List<Document> qList = iSvc.getRegSvc().getDcSvc().quickListByThingId(id);
					Document mov = qList.parallelStream()
									.filter(d -> d.getStatus().equals("UNLOAD"))
									.findAny()
									.orElse(null);

					if (mov != null)
						return new IntegrationReturn(false, "Movimentação de descarga " + mov.getCode() + " pendente");
				}
			}
			removePallet(t);
			for (Thing ts : t.getSiblings())
				removePallet(ts);
			exSvc.execute(() -> {
				logger.info("User " + usr.getName() + " Removed Thing");
				iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.WMS, AlertSeverity.INFO, a == null ? "SEM ENDEREÇO" : a.getMetaname(), "REMOÇÃO DE PRODUTO", "Usuário " + usr.getName() + " Removeu " + prdName + " em " + a == null ? "SEM ENDEREÇO" : a.getMetaname()));
			});
		}
		return iRet;
	}

	@POST
	@Path("/transportPallets/{destination}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn transportPallets(@Context HttpHeaders rs, @PathParam("destination") UUID destinationId, List<UUID> thIdList) {
		IntegrationReturn iRet = IntegrationReturn.OK;
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		Address destination = iSvc.getRegSvc().getAddSvc().findById(destinationId);

		if (thIdList != null && !thIdList.isEmpty()) {
			if (destination != null) {
				List<Address> sibsDestination = new ArrayList<>(destination.getSiblings());
				List<Thing> thingsOrigin = iSvc.getRegSvc().getThSvc().listById(thIdList);
				List<Thing> thingsDestination = iSvc.getRegSvc().getThSvc().listByChildAddressId(destinationId);
				List<UUID> destAllocation = iSvc.getRegSvc().getWmsSvc().findAllocation(destinationId);

				thingsOrigin.sort(compareThingsByAddress);

				boolean removeFromHere = false;

				sibsDestination.sort(compareByOrder);
				for (int i = sibsDestination.size() - 1; i >= 0; i--) {
					if (removeFromHere) {
						Address a = sibsDestination.remove(i);

						logger.info("Removing " + a.getMetaname() + "? " + removeFromHere);
						continue;
					}
					Address current = sibsDestination.get(i);
					removeFromHere = thingsDestination.parallelStream()
									.anyMatch(t -> t.getAddress().getId().equals(current.getId())) || destAllocation.contains(current.getId());
					if (removeFromHere) i++;
				}
				if (sibsDestination.size() >= thingsOrigin.size()) {
					DocumentModel dmOrdMov = iSvc.getRegSvc().getDmSvc().findByMetaname("ORDMOV");
					DocumentModelField dmPrio = dmOrdMov.getFields().stream().filter(dmf -> dmf.getMetaname().equals("PRIORITY")).findAny().get();
					DocumentModelField dmType = dmOrdMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TYPE")).findAny().get();
					DocumentModelField dmTitle = dmOrdMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TITLE")).findAny().get();
					Prefix pfx = iSvc.getRegSvc().getPfxSvc().findNext("MOV", 9);
					Document d = new Document(dmOrdMov, dmOrdMov.getName() + " " + pfx.getPrefix() + pfx.getCode(), pfx.getPrefix() + pfx.getCode(), "ATIVO");

					Iterator<Address> iter = sibsDestination.iterator();
					Address dest = null;
					int seq = 0;

					for (Thing t : thingsOrigin) {
						if (iter.hasNext())
							dest = iter.next();
						d.getTransports().add(new DocumentTransport(d, ++seq, t, dest));
						d.getThings().add(new DocumentThing(d, t, "NOVO"));
						for (Thing ts : t.getSiblings()) {
							Product prd = ts.getProduct();
							String um = prd.getFields().parallelStream().filter(pf -> pf.getModel().getMetaname().equals("GROUP_UM")).findAny().get().getValue();
							double qtd = Double.parseDouble(ts.getProperties().parallelStream().filter(pr -> pr.getField().getMetaname().equals("QUANTITY")).findAny().get().getValue());

							d.getItems().add(new DocumentItem(d, prd, qtd, "MOVIMENTACAO", um));
						}
					}
					d.getFields().add(new DocumentField(d, dmPrio, "NOVO", "2"));
					d.getFields().add(new DocumentField(d, dmType, "NOVO", "REORG"));
					d.getFields().add(new DocumentField(d, dmTitle, "NOVO", "PATIO: REORGANIZAÇÃO"));
					d.setUser(usr);
					iSvc.getRegSvc().getDcSvc().persist(d);
					try {
						iRet = iSvc.getRegSvc().getAglSvc().sendDocToWMS(d, "POST").get();
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				} else
					iRet = new IntegrationReturn(false, "Capacidade do destino é insuficiente.");
			} else
				iRet = new IntegrationReturn(false, "Destino não encontrado.");
		} else
			iRet = new IntegrationReturn(false, "Origem não encontrada.");
		return iRet;
	}

	@POST
	@Path("/transportPallet/{origin}/{destination}/{quantity}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn transportPallet(@Context HttpHeaders rs, @PathParam("origin") UUID originId, @PathParam("destination") UUID destinationId, @PathParam("quantity") int qty) {
		IntegrationReturn iRet = IntegrationReturn.OK;
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		Address origin = iSvc.getRegSvc().getAddSvc().findById(originId);
		Address destination = iSvc.getRegSvc().getAddSvc().findById(destinationId);

		if (origin != null) {
			if (destination != null) {
				List<Address> sibsOrigin = new ArrayList<>(origin.getSiblings());

				if (sibsOrigin.size() > 0) {
					List<Address> sibsDestination = new ArrayList<>(destination.getSiblings());
					Map<Integer, List<UUID>> originOcupation = iSvc.getRegSvc().getWmsSvc().listAddresStock(originId);
					List<Thing> thingsOrigin = iSvc.getRegSvc().getThSvc().listByChildAddressId(originId);
					List<Thing> thingsDestination = iSvc.getRegSvc().getThSvc().listByChildAddressId(destinationId);
					List<UUID> destAllocation = iSvc.getRegSvc().getWmsSvc().findAllocation(destinationId);

					thingsOrigin.removeIf(t -> originOcupation.containsKey(2) && originOcupation.get(2).parallelStream().anyMatch(id -> id.equals(t.getParent().getId())));
					if (thingsOrigin.size() >= qty) {
						thingsOrigin.sort(compareThingsByAddress);
						thingsOrigin = thingsOrigin.subList(0, qty);

						boolean removeFromHere = false;

						sibsDestination.sort(compareByOrder);
						for (int i = sibsDestination.size() - 1; i >= 0; i--) {
							if (removeFromHere) {
								Address a = sibsDestination.remove(i);

								logger.info("Removing " + a.getMetaname() + "? " + removeFromHere);
								continue;
							}
							Address current = sibsDestination.get(i);
							removeFromHere = thingsDestination.parallelStream()
											.anyMatch(t -> t.getAddress().getId().equals(current.getId())) || destAllocation.contains(current.getId());
							if (removeFromHere) i++;
						}
						if (sibsDestination.size() >= thingsOrigin.size()) {
							DocumentModel dmOrdMov = iSvc.getRegSvc().getDmSvc().findByMetaname("ORDMOV");
							DocumentModelField dmPrio = dmOrdMov.getFields().stream().filter(dmf -> dmf.getMetaname().equals("PRIORITY")).findAny().get();
							DocumentModelField dmType = dmOrdMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TYPE")).findAny().get();
							DocumentModelField dmTitle = dmOrdMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TITLE")).findAny().get();
							Prefix pfx = iSvc.getRegSvc().getPfxSvc().findNext("MOV", 9);
							Document d = new Document(dmOrdMov, dmOrdMov.getName() + " " + pfx.getPrefix() + pfx.getCode(), pfx.getPrefix() + pfx.getCode(), "ATIVO");

							Iterator<Address> iter = sibsDestination.iterator();
							Address dest = null;

							for (int i = 0; i < thingsOrigin.size() && i < qty; i++) {
								Thing t = thingsOrigin.get(i).getParent();

								if (iter.hasNext())
									dest = iter.next();
								d.getTransports().add(new DocumentTransport(d, i + 1, t, dest));
								d.getThings().add(new DocumentThing(d, t, "NOVO"));
								for (Thing ts : t.getSiblings()) {
									Product prd = ts.getProduct();
									String um = prd.getFields().parallelStream().filter(pf -> pf.getModel().getMetaname().equals("GROUP_UM")).findAny().get().getValue();
									double qtd = Double.parseDouble(ts.getProperties().parallelStream().filter(pr -> pr.getField().getMetaname().equals("QUANTITY")).findAny().get().getValue());

									d.getItems().add(new DocumentItem(d, prd, qtd, "MOVIMENTACAO", um));
								}
							}
							d.getFields().add(new DocumentField(d, dmPrio, "NOVO", "2"));
							d.getFields().add(new DocumentField(d, dmType, "NOVO", "REORG"));
							d.getFields().add(new DocumentField(d, dmTitle, "NOVO", "PATIO: REORGANIZAÇÃO"));
							d.setUser(usr);
							iSvc.getRegSvc().getDcSvc().persist(d);
							try {
								iRet = iSvc.getRegSvc().getAglSvc().sendDocToWMS(d, "POST").get();
							} catch (InterruptedException | ExecutionException e) {
								e.printStackTrace();
							}
						} else
							iRet = new IntegrationReturn(false, "Capacidade do destino é insuficiente.");
					} else
						iRet = new IntegrationReturn(false, "Quantidade de Origem menor que quantidade pedida.");
				} else
					iRet = new IntegrationReturn(false, "Origem Vazia.");
			} else
				iRet = new IntegrationReturn(false, "Destino não encontrado.");
		} else
			iRet = new IntegrationReturn(false, "Origem não encontrada.");
		return iRet;
	}

	@POST
	@Path("/createPallet")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn createPallet(@Context HttpHeaders rs, SolarCreatePallet crPallet) {
		IntegrationReturn iRet = IntegrationReturn.OK;
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		DecimalFormatSymbols decFormatSymbol = DecimalFormatSymbols.getInstance(Locale.US);
		Product pallet = iSvc.getRegSvc().getPrdSvc().findById(UUID.fromString("95b564e9-ea5a-4caa-adbe-06fc7dd0b966"));
		Product p = iSvc.getRegSvc().getPrdSvc().findById(crPallet.getProduct_id());
		Address a = iSvc.getRegSvc().getAddSvc().findById(crPallet.getAddress_id());
		ArrayList<Address> sibs = new ArrayList<>(a.getSiblings());
		PropertyModel prm = p.getModel().getPropertymodel();
		PropertyModelField prmfQty = prm.getFields().stream().filter(prmf -> prmf.getMetaname().equals("QUANTITY")).findFirst().orElseGet(null);
		PropertyModelField prmfMan = prm.getFields().stream().filter(prmf -> prmf.getMetaname().equals("MANUFACTURING_BATCH")).findFirst().orElseGet(null);
		PropertyModelField prmfExp = prm.getFields().stream().filter(prmf -> prmf.getMetaname().equals("LOT_EXPIRE")).findFirst().orElseGet(null);
		PropertyModelField prmfLot = prm.getFields().stream().filter(prmf -> prmf.getMetaname().equals("LOT_ID")).findFirst().orElseGet(null);
		PropertyModelField prmfStrW = prm.getFields().stream().filter(prmf -> prmf.getMetaname().equals("STARTING_WEIGHT")).findFirst().orElseGet(null);
		PropertyModelField prmfActW = prm.getFields().stream().filter(prmf -> prmf.getMetaname().equals("ACTUAL_WEIGHT")).findFirst().orElseGet(null);

		decFormatSymbol.setDecimalSeparator('.');
		DF.setDecimalFormatSymbols(decFormatSymbol);
		if (crPallet.getVolumes() <= sibs.size()) {
			try {
				String lotId = crPallet.getLot_prefix().toUpperCase() + lotFormatter.format(crPallet.getManufacture()) + p.getSku();
				Comparator<Address> compareByMetaname = (Address o1, Address o2) -> {
					if (o1 == null && o2 == null) return 0;
					if (o2 == null) return -1;
					if (o1 == null) return 1;
					if (o1.getMetaname() == null && o2.getMetaname() == null) return 0;
					if (o2.getMetaname() == null) return -1;
					if (o1.getMetaname() == null) return 1;
					return o1.getMetaname().compareTo(o2.getMetaname());
				};

				sibs.sort(compareByMetaname);//inverse
				Iterator<Address> addIt = sibs.iterator();
				for (int i = 0; i < crPallet.getVolumes() && addIt.hasNext(); i++) {
					Address ta = addIt.next();
					List<Thing> addThing = iSvc.getRegSvc().getThSvc().listByAddressId(ta.getId());

					if (addThing.size() == 0) {
						Thing plt = new Thing(pallet.getName(), pallet, prm, "INVENTARIO");
						Thing t = new Thing(p.getName(), p, prm, "INVENTARIO");

						plt.getProperties().add(new Property(plt, prmfQty, "1.000"));
						plt.getProperties().add(new Property(plt, prmfMan, prFormatter.format(crPallet.getManufacture())));
						plt.getProperties().add(new Property(plt, prmfExp, prFormatter.format(crPallet.getExpire())));
						plt.getProperties().add(new Property(plt, prmfLot, lotId));
						plt.getProperties().add(new Property(plt, prmfStrW, "0"));
						plt.getProperties().add(new Property(plt, prmfActW, "0"));
						plt.setAddress(ta);
						plt.getSiblings().add(t);
						t.getProperties().add(new Property(t, prmfQty, DF.format(crPallet.getQuantity())));
						t.getProperties().add(new Property(t, prmfMan, prFormatter.format(crPallet.getManufacture())));
						t.getProperties().add(new Property(t, prmfExp, prFormatter.format(crPallet.getExpire())));
						t.getProperties().add(new Property(t, prmfLot, lotId));
						t.getProperties().add(new Property(t, prmfStrW, "0"));
						t.getProperties().add(new Property(t, prmfActW, "0"));
						t.setParent(plt);
						t.setAddress(ta);
						iSvc.getRegSvc().getThSvc().persist(plt);

						iRet = iSvc.getRegSvc().getAglSvc().sendThingToWMS(plt, "POST").get();

						if (iRet.isResult()) {
							plt.setStatus(crPallet.getStatus());
							t.setStatus(crPallet.getStatus());
						} else {
							iSvc.getRegSvc().getThSvc().remove(t);
							iSvc.getRegSvc().getThSvc().remove(plt);
							return iRet;
						}
						logger.info("Usuário " + usr.getName() + " Adicionou palete de " + p.getSku() + " - " + p.getName() + " em " + a.getMetaname());
						iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.WMS, AlertSeverity.INFO, a.getMetaname(), "ADIÇÃO DE PRODUTO", "Usuário " + usr.getName() + " Adicionou palete de " + p.getSku() + " - " + p.getName() + " em " + a.getMetaname()));
					} else
						i--;
				}
			} catch (Exception e) {
				iRet = new IntegrationReturn(false, e.getLocalizedMessage());
			}
		} else {
			iRet = new IntegrationReturn(false, "Endereço não comporta quantidade informada de Paletes " + crPallet.getVolumes());
		}
		return iRet;
	}

	@PUT
	@Path("/editPallet/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn editPallet(@Context HttpHeaders rs, @PathParam("id") UUID thingId, SolarCreatePallet crPallet) {
		IntegrationReturn iRet = IntegrationReturn.OK;
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		DecimalFormatSymbols decFormatSymbol = DecimalFormatSymbols.getInstance(Locale.US);
		Thing t = iSvc.getRegSvc().getThSvc().findById(thingId);
		Thing plt = t.getParent();
		Product oldProduct = t.getProduct();
		Product newProduct = iSvc.getRegSvc().getPrdSvc().findById(crPallet.getProduct_id());

		if (!oldProduct.getId().equals(crPallet.getProduct_id()))
			t.setProduct(newProduct);
		decFormatSymbol.setDecimalSeparator('.');
		DF.setDecimalFormatSymbols(decFormatSymbol);
		t.setStatus(crPallet.getStatus());
		t.getProperties().forEach(pr -> {
			String newValue = null;

			switch (pr.getField().getMetaname()) {
				case "LOT_ID":
					String oldLot = pr.getValue();
					String lotId = crPallet.getLot_prefix().toUpperCase() + lotFormatter.format(crPallet.getManufacture()) + newProduct.getSku();

					if (!oldLot.equals(lotId))
						newValue = lotId;
					break;
				case "QUANTITY":
					double oldQty = Double.parseDouble(pr.getValue());

					if (oldQty != crPallet.getQuantity())
						newValue = DF.format(crPallet.getQuantity());
					break;
				case "MANUFACTURING_BATCH":
					Date oldMan = null;

					try {
						oldMan = prFormatter.parse(pr.getValue());
					} catch (ParseException pe) {
						try {
							oldMan = uiParser.parse(pr.getValue());
						} catch (ParseException pe2) {
							logger.error("Error parsing Manufacture Date - " + pr.getValue());
						}
					}
					if (oldMan != null && crPallet.getManufacture().compareTo(oldMan) != 0)
						newValue = prFormatter.format(crPallet.getManufacture());
					break;
				case "LOT_EXPIRE":
					Date oldExp = null;

					try {
						oldExp = prFormatter.parse(pr.getValue());
					} catch (ParseException pe) {
						try {
							oldExp = uiParser.parse(pr.getValue());
						} catch (ParseException pe2) {
							logger.error("Error parsing Expiry Date - " + pr.getValue());
						}
					}
					if (oldExp != null && crPallet.getExpire().compareTo(oldExp) != 0)
						newValue = prFormatter.format(crPallet.getExpire());
					break;
			}
			if (newValue != null) {
				plt.getProperties().stream()
								.filter(ppr -> ppr.getField().getId().equals(pr.getField().getId()))
								.findFirst()
								.get()
								.setValue(newValue);
				pr.setValue(newValue);
			}
		});
		try {
			iRet = iSvc.getRegSvc().getAglSvc().sendThingToWMS(plt, "PUT").get();
			logger.info("Usuário " + usr.getName() + " Editou palete de " + oldProduct.getSku() + " - " + oldProduct.getName() + " em " + t.getAddress().getMetaname());
			iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.WMS, AlertSeverity.INFO, t.getAddress().getMetaname(), "EDIÇÃO DE PRODUTO", "Usuário " + usr.getName() + " Editou palete de " + oldProduct.getSku() + " - " + oldProduct.getName() + " em " + t.getAddress().getMetaname()));
		} catch (ExecutionException | InterruptedException ie) {
			iRet = new IntegrationReturn(false, ie.getLocalizedMessage());
		}
		return iRet;
	}

	@PUT
	@Path("changePalletAddress/{thingid}/{destination}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn changeThingAddress(@Context HttpHeaders rs, @PathParam("thingid") UUID thingId, @PathParam("destination") UUID destId) {
		IntegrationReturn iRet = IntegrationReturn.OK;
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		Address a = iSvc.getRegSvc().getAddSvc().findById(destId);
		Thing t = iSvc.getRegSvc().getThSvc().findById(thingId);
		Thing ts = t.getSiblings().size() == 0 ? t : t.getSiblings().parallelStream().findAny().get();
		Address oldAddress = t.getAddress();
		Product p = ts.getProduct();
		String lte_id = Things.getStringProperty(ts, "LOT_ID");
		BigDecimal qtd = new BigDecimal(Things.getDoubleProperty(ts, "QUANTITY"));
		BigDecimal stWg = new BigDecimal(Things.getDoubleProperty(ts, "STARTING_WEIGHT"));
		BigDecimal acWg = new BigDecimal(Things.getDoubleProperty(ts, "ACTUAL_WEIGHT"));
		Date man = Things.getDateProperty(ts, "MANUFACTURING_BATCH", new Date());
		Date exp = Things.getDateProperty(ts, "LOT_EXPIRE", new Date());

		t.setAddress(a);
		t.getSiblings().forEach(sts -> sts.setAddress(a));
		String plot = Things.getStringProperty(t, "LOT_ID");
		BigDecimal pqty = new BigDecimal(Things.getDoubleProperty(t, "QUANTITY"));
		BigDecimal pstWg = new BigDecimal(Things.getDoubleProperty(t, "STARTING_WEIGHT"));
		BigDecimal pacWg = new BigDecimal(Things.getDoubleProperty(t, "ACTUAL_WEIGHT"));
		Date pman = Things.getDateProperty(t, "MANUFACTURING_BATCH", new Date());
		Date pexp = Things.getDateProperty(t, "LOT_EXPIRE", new Date());

		iSvc.getRegSvc().getWmsSvc().insertOrUpdateThing(t.getId(), null, t.getName(), t.getCreatedAt(), t.getUpdatedAt(), t.getStatus(), t.getAddress_id(), t.getProduct_id(), plot, pqty, pman, pexp, pstWg, pacWg);
		iSvc.getRegSvc().getWmsSvc().insertOrUpdateThing(ts.getId(), t.getId(), ts.getName(), ts.getCreatedAt(), ts.getUpdatedAt(), ts.getStatus(), ts.getAddress_id(), p.getId(), lte_id, qtd, man, exp, stWg, acWg);
		iSvc.getRegSvc().getWmsSvc().changeAddress(thingId, t.getStatus(), a.getParent().getId(), a.getId(), p.getId(), lte_id, qtd, man, exp);
		iSvc.getRegSvc().getThSvc().persist(t);
		logger.info("Usuário " + usr.getName() + " Alterou local de palete de " + oldAddress.getMetaname() + " para " + a.getMetaname());
		iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.WMS, AlertSeverity.INFO, oldAddress.getMetaname() + " - " + a.getMetaname(), "ALTERAÇÃO DE ENDEREÇO", "Usuário " + usr.getName() + " Alterou local de palete de " + oldAddress.getMetaname() + " para " + a.getMetaname()));
		return iRet;
	}

	@POST
	@Path("/resupply/{destination}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public IntegrationReturn resupplyPicking(@Context HttpHeaders rs, @PathParam("destination") UUID destId, SolarPickingResupply[] pkResupply) {
		IntegrationReturn iRet = new IntegrationReturn(true, null);

		if (pkResupply.length > 0) {
			String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
			User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);

			if (destId.toString().equals("354a3c85-348f-11ea-8a83-005056a19775")) {
				for (SolarPickingResupply resupply : pkResupply) {
					List<String> destIds = iSvc.getRegSvc().getWmsSvc().findDestResupply(resupply.getProduct_id());
					List<String> resupplyIds = iSvc.getRegSvc().getWmsSvc().listActiveResupplyIds(resupply.getProduct_id());
					List<String> result = new ArrayList<>(destIds);

					result.removeAll(resupplyIds);
					if (destIds.size() == 0) {
						iRet = new IntegrationReturn(false, "Produto " + resupply.getProduct_sku() + " - " + resupply.getProduct_name() + "(" + destIds.size() + ") não encontrado no picking");
						break;
					} else if (result.size() <= 0) {
						iRet = new IntegrationReturn(false, resupplyIds.size() + " tarefa" + (resupplyIds.size() == 1 ? "" : "s") + " de ressuprimento para o produto " + resupply.getProduct_sku() + " - " + resupply.getProduct_name() + " já em andamento. Quantidade de endereços no picking = " + destIds.size() + ")");
						break;
					}
					double qty = result.stream()
									.map(dst -> iSvc.getRegSvc().getWmsSvc().getAddressQuantity(dst))
									.reduce((d1, d2) -> d1 + d2)
									.orElse(0d);
					iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.PROCESS, AlertSeverity.INFO, resupply.getProduct_sku(), "Quantidade existente do produto: " + DF.format(qty) + " (" + result.stream()
									.map(r -> iSvc.getRegSvc().getAddSvc().findById(UUID.fromString(r)).getParent().getName())
									.collect(Collectors.joining(", ")) + ")", "Ressuprimento manual do picking: " + resupply.getProduct_name()));
				}
			}

			if (iRet.isResult()) {
				iRet = iSvc.getRegSvc().getWmsSvc().createResupply(destId, pkResupply, usr);
			}
		} else
			iRet = new IntegrationReturn(false, "Lista de Ressuprimento Vazia");
		return iRet;
	}

	private void removePallet(Thing t) {
		logger.info("Removing " + t.getId());
		for (Thing ts : t.getSiblings()) {
			iSvc.getRegSvc().getThSvc().remove(ts);
			logger.info("Removed Product " + ts.getId().toString());
		}
		logger.info("Removed Pallet");
		iSvc.getRegSvc().getWmsSvc().removePallet(t);
		iSvc.getRegSvc().getThSvc().remove(t);
		logger.info("Removed " + t.getId());
	}

	@GET
	@Path("/allocationByAddress/{address_id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response allocationByAddress(@PathParam("address_id") UUID address_id) {
		List<UUID> allocId = iSvc.getRegSvc().getWmsSvc().listAllocationsByAddress(address_id);

		return Response.ok(iSvc.getRegSvc().getThSvc().listById(allocId).stream().flatMap(t -> t.getSiblings().stream()).collect(Collectors.toList())).build();
	}

	@GET
	@Path("/stkDockAllocation")
	@Produces(MediaType.APPLICATION_JSON)
	public Response stockDockList() {
		Map<Integer, List<UUID>> allocMap = iSvc.getRegSvc().getWmsSvc().listDockStock();
		List<Thing> ret = iSvc.getRegSvc().getThSvc().listById(allocMap.values().parallelStream().flatMap(thList -> thList.parallelStream()).collect(Collectors.toList()));

		for (int key : allocMap.keySet()) {
			List<UUID> thIdList = allocMap.get(key);

			for (UUID thId : thIdList) {
				Thing t = ret.stream().filter(th -> th.getId().equals(thId)).findAny().orElse(null);

				if (t != null)
					t.setPayload("{\"allocation\":" + key + "}");
			}
		}
		return Response.ok(ret).build();
	}

	@GET
	@Path("/stkAddressList/{address_id}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response stockAddressList(@PathParam("address_id") UUID address_id) {
		Map<Integer, List<UUID>> allocMap = iSvc.getRegSvc().getWmsSvc().listAddresStock(address_id);
		List<Thing> ret = iSvc.getRegSvc().getThSvc().listById(allocMap.values().parallelStream().flatMap(thList -> thList.parallelStream()).collect(Collectors.toList()));

		for (int key : allocMap.keySet()) {
			List<UUID> thIdList = allocMap.get(key);

			for (UUID thId : thIdList) {
				Thing t = ret.stream().filter(th -> th.getId().equals(thId)).findAny().orElse(null);

				if (t != null)
					t.setPayload("{\"allocation\":" + key + "}");
			}
		}
		return Response.ok(ret).build();
	}

	@GET
	@Path("/stockbyproduct/{product_id}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response stockByProduct(@PathParam("product_id") UUID product_id) {
		return Response.ok(iSvc.getRegSvc().getWmsSvc().listStockDateByProduct(product_id.toString())).build();
	}

	@GET
	@Path("/stksnapshot/{doc_id}")
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response stkSnapshotByDocument(@PathParam("doc_id") String doc_id) {
		return Response.ok(iSvc.getRegSvc().getWmsSvc().listStkSnapshotByDocument(doc_id)).build();
	}

	@POST
	@Path("/stksnapshot")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public Response createStkSnapshot(@Context HttpHeaders rs, JsonArray addresses) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		DocumentModel dm = iSvc.getRegSvc().getDmSvc().findByMetaname("INVENTORY");
		DocumentModel dmAdd = iSvc.getRegSvc().getDmSvc().findByMetaname("APORUAINV");
		DocumentModelField dmf = dm.getFields().stream().filter(mf -> mf.getMetaname().equals("INV_TYPE")).findAny().orElse(null);
		DocumentModelField dmfInvAdd = dmAdd.getFields().stream().filter(mf -> mf.getMetaname().equals("INVADDRESS")).findAny().orElse(null);
		DocumentModelField dmfInvOrd = dmAdd.getFields().stream().filter(mf -> mf.getMetaname().equals("INVADDRESSORDER")).findAny().orElse(null);
		DocumentModelField dmfInvPrd = dmAdd.getFields().stream().filter(mf -> mf.getMetaname().equals("INVADDRESSPRD")).findAny().orElse(null);
		Prefix pfx = iSvc.getRegSvc().getPfxSvc().findNext("INV", 8);
		Document d = new Document(dm, dm.getName() + " - " + pfx.getCode(), pfx.getPrefix() + pfx.getCode(), "NOVO");
		DocumentField df = new DocumentField(d, dmf, "NOVO", "DRONE MANUAL");
		int order = 1;//TODO: Ordenar por plano de voo

		for (JsonValue val : addresses) {
			JsonObject obj = (JsonObject) val;
			UUID addId = UUID.fromString(obj.getString("id"));
			Product prd = iSvc.getRegSvc().getPrdSvc().findByAddress(addId);
			Prefix pfxAri = iSvc.getRegSvc().getPfxSvc().findNext("ARI", 10);
			Document ds = new Document(dmAdd, dmAdd.getName() + " - " + pfxAri.getCode(), pfxAri.getPrefix() + pfxAri.getCode(), "NOVO");

			ds.getFields().add(new DocumentField(ds, dmfInvAdd, "NOVO", addId.toString()));
			ds.getFields().add(new DocumentField(ds, dmfInvOrd, "NOVO", String.valueOf(order++)));
			ds.getFields().add(new DocumentField(ds, dmfInvPrd, "NOVO", prd == null ? "" : prd.getId().toString()));
			ds.setParent(d);
			d.getSiblings().add(ds);
		}

		d.getFields().add(df);
		d.setUser(usr);
		iSvc.getRegSvc().getDcSvc().persist(d);
		Map<String, Integer> prdMap = iSvc.getRegSvc().getWmsSvc().createStkSnapshot(d.getId().toString(), d.getSiblings().isEmpty() ? null : "'" + d.getSiblings()
						.stream()
						.filter(ds -> ds.getModel().getId().equals(dmAdd.getId()))
						.flatMap(ds -> ds.getFields().stream())
						.map(dsf -> dsf.getValue()).collect(Collectors.joining("','")) + "'");
		for (Entry<String, Integer> e : prdMap.entrySet()) {
			Product prd = iSvc.getRegSvc().getPrdSvc().findById(UUID.fromString(e.getKey()));
			ProductField pfUm = prd.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("GROUP_UM")).findAny().orElse(null);
			ProductField pfPb = prd.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("PALLET_BOX")).findAny().orElse(null);
			DocumentItem di = new DocumentItem(d, prd, e.getValue() * Integer.parseInt(pfPb.getValue()), "SNAPSHOT");

			if (pfUm != null)
				di.setMeasureUnit(pfUm.getValue());
			d.getItems().add(di);
		}
		return Response.ok(d).build();
	}

	@PUT
	@Path("/sapinventory/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public Response attachSAPInventory(@Context HttpHeaders rs, @PathParam("id") UUID docId, JsonArray ids) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		Document inv = iSvc.getRegSvc().getDcSvc().findById(docId);
		Document adj = inv.getSiblings().parallelStream().filter(ds -> ds.getModel().getMetaname().equals("INVADJUST")).findAny().orElse(null);

		for (int i = 0; i < ids.size(); i++) {
			JsonObject o = ids.getJsonObject(i);
			UUID sapId = UUID.fromString(o.getString("id"));
			Document sap = iSvc.getRegSvc().getDcSvc().findById(sapId);

			for (DocumentItem diInv : sap.getItems()) {
				Optional<DocumentItem> contDi = inv.getItems().parallelStream().filter(di -> di.getProduct().getId().equals(diInv.getProduct().getId())).findAny();
				Optional<DocumentItem> optAdjDi = adj == null ? Optional.empty() : adj.getItems().parallelStream().filter(di -> di.getProduct().getId().equals(diInv.getProduct().getId())).findAny();

				if (contDi.isPresent())
					diInv.setQty(contDi.get().getQty() + (optAdjDi.isPresent() ? optAdjDi.get().getQty() : 0));
			}
			sap.setParent(inv);
			sap.setStatus("SAP");
			sap.setUser(usr);
			inv.getSiblings().add(iSvc.getRegSvc().getDcSvc().persist(sap));
		}
		inv.setStatus("SUCESSO");
		inv.setUser(usr);
		return Response.ok().build();
	}

	@POST
	@Path("/sapinventoryadjustment/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public Response addAdjustmentSAPInventory(@Context HttpHeaders rs, @PathParam("id") UUID docId, DocumentItem[] items) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		Document inv = iSvc.getRegSvc().getDcSvc().findById(docId);
		Optional<Document> optAdj = inv.getSiblings().stream().filter(ds -> ds.getModel().getMetaname().equals("INVADJUST")).findAny();
		Document adj = null;

		if (optAdj.isPresent()) {
			adj = optAdj.get();
		} else {
			DocumentModel dmAdj = iSvc.getRegSvc().getDmSvc().findByMetaname("INVADJUST");
			adj = new Document(dmAdj, dmAdj.getName() + " " + inv.getCode().replace("INV", ""), inv.getCode().replace("INV", "ADJ"), "NOVO");
			adj.setParent(inv);
		}

		for (DocumentItem tmpDi : items) {
			Optional<DocumentItem> optDi = adj.getItems().parallelStream().filter(di -> di.getProduct().getId().equals(tmpDi.getProduct().getId())).findAny();

			if (optDi.isPresent())
				optDi.get().setQty(tmpDi.getQty());
			else
				adj.getItems().add(new DocumentItem(adj, tmpDi.getProduct(), tmpDi.getQty(), tmpDi.getStatus(), tmpDi.getMeasureUnit()));
		}

		adj.setUser(usr);
		inv.getSiblings().add(adj);
		return Response.ok(inv).build();
	}

	@POST
	@Path("/inventory/{type}/{quantity}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(value = TxType.REQUIRES_NEW)
	public Response createInventory(@Context HttpHeaders rs, @PathParam("type") String invType, @PathParam("quantity") int qtdContagens, JsonArray ids) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		boolean logimmediately = false;
		Profiler prof = new Profiler();
		List<Address> addr = new ArrayList<>();
		Prefix pfxInv = iSvc.getRegSvc().getPfxSvc().findNext("INV", 6);
		logger.info(prof.step("List Warehouses", logimmediately));
		DocumentModel dmInv = iSvc.getRegSvc().getDmSvc().findByMetaname("INVENTORY");
		logger.info(prof.step("Find DocumentModel INVENTORY", logimmediately));
		final Document dInv = new Document(dmInv, dmInv.getName() + " - " + pfxInv.getCode(), pfxInv.getPrefix() + pfxInv.getCode(), "ATIVO");
		DocumentModelField dmfType = dmInv.getFields().stream().filter(dmf -> dmf.getMetaname().equals("INV_TYPE")).findAny().get();
		String parentCode = pfxInv.getCode();

		dInv.getFields().add(new DocumentField(dInv, dmfType, "NOVO", invType));
		dInv.setUser(usr);
		if (invType.equals("INVPA")) {
			//			addr.addAll(iSvc.getRegSvc().getAddSvc().listByModelMetaname("WAREHOUSE"));
			//			addr.addAll(iSvc.getRegSvc().getAddSvc().listByModelMetaname("PICKING"));
			//			addr.addAll(iSvc.getRegSvc().getAddSvc().listByModelMetaname("SEGREGATION"));
			for (int i = 0; i < ids.size(); i++) {
				JsonObject o = ids.getJsonObject(i);
				addr.add(iSvc.getRegSvc().getAddSvc().findById(UUID.fromString(o.getString("id"))));
			}
		} else
			addr.add(iSvc.getRegSvc().getAddSvc().findById(UUID.fromString("27998254-563a-11e9-b375-005056a19775")));
		exSvc.execute(() -> createInvCount(iSvc.getRegSvc().getDcSvc().persist(dInv), parentCode, addr, qtdContagens, invType));
		prof.done("INVENTORY CREATED", logimmediately, false);
		return Response.ok(dInv).build();
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	private void createInvCount(Document dInv, String parentCode, List<Address> addr, int qtdContagens, String invType) {
		DocumentModel dmCount = iSvc.getRegSvc().getDmSvc().findByMetaname("APOCONTINV");
		DocumentModelField dmfWarehouse = dmCount.getFields().stream().filter(dmf -> dmf.getMetaname().equals("WAREHOUSE")).findAny().get();
		DocumentModelField dmfType = dmCount.getFields().stream().filter(dmf -> dmf.getMetaname().equals("CONTTYPE")).findAny().get();
		List<Document> sibs = new ArrayList<>();

		for (Address a : addr) {
			Prefix pfx = iSvc.getRegSvc().getPfxSvc().findNext("CNT" + parentCode, 3);
			boolean rollBack = false;

			for (int i = 1; i <= qtdContagens; i++) {
				if (!dInv.getSiblings().stream()
								.filter(ds -> ds.getModel().getId().equals(dmCount.getId()))
								.flatMap(ds -> ds.getFields().stream())
								.anyMatch(df -> df.getValue().equals(a.getName()))) {

					Document dCount = new Document(dmCount, dmCount.getName() + " " + parentCode + " " + pfx.getCode() + "-" + i, pfx.getPrefix() + pfx.getCode() + "-" + i, "ATIVO");
					DocumentField dfWarehouse = new DocumentField(dCount, dmfWarehouse, "NOVO", a.getName());
					DocumentField dfType = new DocumentField(dCount, dmfType, "NOVO", "CONT_" + invType.substring(invType.length() - 2));

					for (Address as : a.getSiblings()) {
						DocumentModel dmAri = iSvc.getRegSvc().getDmSvc().findByMetaname("APORUAINV");
						DocumentModelField dmfAddr = dmAri.getFields().stream().filter(dmf -> dmf.getMetaname().equals("INVADDRESS")).findAny().get();
						Prefix pfxAri = iSvc.getRegSvc().getPfxSvc().findNext("ARI", 10);
						Document dAri = new Document(dmAri, dmAri.getName() + " - " + pfxAri.getCode(), pfxAri.getPrefix() + pfxAri.getCode(), "NOVO");
						DocumentField dfAddr = new DocumentField(dAri, dmfAddr, "NOVO", as.getId().toString());

						dAri.getFields().add(dfAddr);
						dAri.setParent(dCount);
						dCount.getSiblings().add(dAri);
					}

					dCount.getFields().add(dfWarehouse);
					dCount.getFields().add(dfType);
					dCount.setParent(dInv);
					iSvc.getRegSvc().getDcSvc().persist(dCount);
					sibs.add(dCount);
				} else
					rollBack = true;
			}
			if (rollBack)
				iSvc.getRegSvc().getPfxSvc().rollBack(pfx);
		}
		iSvc.getRegSvc().getWmsSvc().createStkSnapshot(dInv.getId().toString(), "'" + sibs
						.stream()
						.flatMap(ds -> ds.getSiblings()
										.stream()
										.filter(dss -> dss.getModel().getMetaname().equals("APORUAINV")))
						.flatMap(ds -> ds.getFields().stream())
						.map(dsf -> dsf.getValue()).collect(Collectors.joining("','")) + "'");
	}

	@PUT
	@Path("/interactWithAction/{userId}/{msg}")
	public Response interactiWithAction(@Context HttpHeaders rs, @PathParam("userId") UUID userId, @PathParam("msg") String message) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		BaseAction ba = iSvc.getRsm().getTsm().getRegisteredAction(userId);

		if (ba instanceof WebSocketAction) {
			InteractionMessage msg = new InteractionMessage();

			msg.setData(message);
			((WebSocketAction) ba).interact(msg);
			iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.TASK, AlertSeverity.INFO, usr.getName(), "Habilitação de Visualização", "Conferência habilitada para visualização"));
		}

		return Response.ok().build();
	}

	@PUT
	@Path("/unblockproducts")
	@Transactional(value = TxType.REQUIRES_NEW)
	public Response unblockProducts(@Context HttpHeaders rs, List<UUID> addrIds) {
		String token = rs.getHeaderString(HttpHeaders.AUTHORIZATION).split(" ")[1];
		User usr = iSvc.getRegSvc().getAuthSvc().getUser(token);
		String lotPrefix = ConfigUtil.get("hunter-custom-solar", "plant_production_code", "CNAT");
		String tStatus = "ARMAZENADO";
		JsonArrayBuilder arrBuilder = Json.createArrayBuilder();

		for (UUID addrId : addrIds) {
			Address a = iSvc.getRegSvc().getAddSvc().findById(addrId);
			List<Thing> tList = iSvc.getRegSvc().getThSvc().listParentByChildAddressId(addrId);
			JsonObjectBuilder objBuilder = Json.createObjectBuilder();
			AtomicBoolean bool = new AtomicBoolean(false);

			for (Thing t : tList) {
				for (Thing ts : t.getSiblings()) {
					ts.setStatus(tStatus);
					ts.getProperties().stream()
									.filter(pr -> pr.getField().getMetaname().equals("LOT_ID"))
									.forEach(pr -> {
										String oldLte = pr.getValue();
										String newLte = oldLte.replace("PROD", lotPrefix);

										pr.setValue(newLte);
										iSvc.getRegSvc().getWmsSvc().updateLte(oldLte, newLte);
									});
				}
				t.setStatus(tStatus);
				t.getProperties().stream()
								.filter(pr -> pr.getField().getMetaname().equals("LOT_ID"))
								.forEach(pr -> {
									String oldLte = pr.getValue();
									String newLte = oldLte.replace("PROD", lotPrefix);

									pr.setValue(newLte);
									iSvc.getRegSvc().getWmsSvc().updateThingStkLteStatus(t.getId(), newLte, tStatus);
									if (!bool.get()) {
										objBuilder.add(addrId.toString(), newLte);
										bool.set(true);
									}
								});
			}
			logger.info("Usuário " + usr.getName() + " Alterou status dos produtos do endereço " + a.getMetaname() + " para ARMAZENADO");
			iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.WMS, AlertSeverity.INFO, a.getMetaname(), "ALTERAÇÃO DE STATUS", "Usuário " + usr.getName() + " Alterou status dos produtos do endereço " + a.getMetaname() + " para ARMAZENADO"));
			arrBuilder.add(objBuilder.build());
		}
		return Response.ok(arrBuilder.build()).build();
	}
}
