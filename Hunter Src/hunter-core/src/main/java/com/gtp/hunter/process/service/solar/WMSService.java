package com.gtp.hunter.process.service.solar;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.AccessTimeout;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.hibernate.Hibernate;
import org.slf4j.Logger;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.repository.UnitRepository;
import com.gtp.hunter.core.service.PrefixService;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.jsonstubs.StockDueDateJson;
import com.gtp.hunter.process.jsonstubs.WMSRule;
import com.gtp.hunter.process.jsonstubs.WMSStkSnapshot;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.DocumentTransport;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Addresses;
import com.gtp.hunter.process.model.util.DocumentModels;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.model.util.Products;
import com.gtp.hunter.process.model.util.Things;
import com.gtp.hunter.process.repository.AddressRepository;
import com.gtp.hunter.process.repository.DocumentModelRepository;
import com.gtp.hunter.process.repository.DocumentThingRepository;
import com.gtp.hunter.process.repository.PropertyRepository;
import com.gtp.hunter.process.repository.ThingRepository;
import com.gtp.hunter.process.repository.agl.WMSRepository;
import com.gtp.hunter.process.service.AlertService;
import com.gtp.hunter.process.service.DocumentService;
import com.gtp.hunter.process.service.ProductService;
import com.gtp.hunter.ui.json.AddressOcupationStub;
import com.gtp.hunter.ui.json.SolarPickingResupply;

@Stateless
@AccessTimeout(value = 15000)
public class WMSService {

	private static final DecimalFormat	DF	= new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.US));

	@Inject
	private transient Logger			logger;

	@Inject
	private AddressRepository			addRep;

	@Inject
	private AGLConvertService			aglSvc;

	@Inject
	private AlertService				alertSvc;

	@Inject
	private DocumentService				dcSvc;

	@Inject
	private DocumentModelRepository		dmRep;

	@Inject
	private DocumentThingRepository		dtRep;

	@Inject
	private PrefixService				pfxSvc;

	@Inject
	private ProductService				prdSvc;

	@Inject
	private PropertyRepository			prRep;

	@Inject
	private ThingRepository				thRep;

	@Inject
	private UnitRepository				uRep;

	@Inject
	private WMSRepository				wmsRep;

	public void removePallet(Thing t) {
		wmsRep.removePallet(t);
	}

	public void updateThingStatus(UUID thId, String thStatus) {
		wmsRep.updateThingStatus(thId, thStatus);
	}

	public void updateThingStkLteStatus(UUID thingId, String lte, String status) {
		wmsRep.updateThingStkLteStatus(thingId, lte, status);
	}

	public void updateThingQuantity(UUID thId, BigDecimal qty) {
		wmsRep.updateThingQuantity(thId, qty);
	}

	public void updateLte(String oldLte, String newLte) {
		wmsRep.updateLte(oldLte, newLte);
	}

	public void deleteStkThing(String thId) {
		wmsRep.deleteStkThing(thId);
	}

	public void updateStkPicking(String thingId, BigDecimal qty) {
		wmsRep.updateStkPicking(thingId, qty);
	}

	public Map<String, Double> needsResupply(UUID prdId, Double qty) {
		return wmsRep.needsResupply(prdId, qty);
	}

	public List<String> listPickingByProduct(UUID prdId) {
		return wmsRep.listPickingByProduct(prdId);
	}

	public int cancelOrdmov(String movordId) {
		return wmsRep.removeWmsMov(movordId);
	}

	public void insertStkInventoryCount(String doc_id, String armloc_id, String prd_id, int qtd) {
		wmsRep.insertStkInventoryCount(doc_id, armloc_id, prd_id, qtd);
	}

	public void updateAddressCode(UUID addressId, String code) {
		wmsRep.updateAddressCode(addressId, code);
	}

	public List<UUID> findFIFOByProduct(UUID productId, int quantity, int minResupply) {
		return wmsRep.findFIFOByProduct(productId, quantity, minResupply);
	}

	public List<String> listPickingStages(int qtdPallets) {
		return wmsRep.listPickingStages(qtdPallets);
	}

	public List<String> findDestResupply(UUID prdId) {
		return wmsRep.findDestResupply(prdId);
	}

	public void clearTnpSibs(String tnpId, boolean updateEnt, boolean updateExt, boolean resetEnt, boolean resetExt) {
		wmsRep.clearTnpSibs(tnpId, updateEnt, updateExt, resetEnt, resetExt);
	}

	public Map<Integer, List<UUID>> listAddresStock(UUID addressId) {
		return wmsRep.listAddresStock(addressId);
	}

	public Map<Integer, List<UUID>> listDockStock() {
		return wmsRep.listDockStock();
	}

	public List<UUID> findAllocation(UUID destinationId) {
		return wmsRep.findAllocation(destinationId);
	}

	public List<WMSRule> listRules(int prgid) {
		return wmsRep.listRules(prgid);
	}

	public WMSRule findRule(int prgid, int ruleId) {
		return wmsRep.findRule(prgid, ruleId);
	}

	public int insertRule(int baseId, String newName, List<Product> products) {
		return wmsRep.insertRule(baseId, newName, products);
	}

	public List<UUID> listNewestPalletsProduct(UUID prdId, int quantity) {
		return wmsRep.listNewestPalletsProduct(prdId, quantity);
	}

	public UUID getNextAddress(String addressId) {
		return wmsRep.getNextAddress(addressId);
	}

	public void updateOrdMov(UUID movId, int seq, UUID thId, UUID originId, UUID destId) {
		wmsRep.updateOrdMov(movId, seq, thId, originId, destId);
	}

	public void updateStkMov(UUID thId, UUID destParentId, UUID destId, String lte_id, UUID prdId, int qtd) {
		wmsRep.updateStkMov(thId, destParentId, destId, lte_id, prdId, qtd);
	}

	public List<AddressOcupationStub> listStock() {
		return wmsRep.listStock();
	}

	public List<UUID> listAllocationsByAddress(UUID address_id) {
		return wmsRep.listAllocationsByAddress(address_id);
	}

	public List<StockDueDateJson> listStockDateByProduct(String prdId) {
		return wmsRep.listStockDateByProduct(prdId);
	}

	public List<WMSStkSnapshot> listStkSnapshotByDocument(String doc_id) {
		return wmsRep.listStkSnapshotByDocument(doc_id);
	}

	public Map<String, Integer> createStkSnapshot(String doc_id, String whereAddress) {
		return wmsRep.createStkSnapshot(doc_id, whereAddress);
	}

	public double getAddressQuantity(String dst) {
		return wmsRep.getAddressQuantity(dst);
	}

	public void insertOrUpdateThing(UUID thingId, UUID parentId, String name, Date createdAt, Date updatedAt, String status, UUID addId, UUID prdId, String lte_id, BigDecimal qtd, Date man, Date exp, BigDecimal strWeight, BigDecimal actWeight) {
		wmsRep.insertOrUpdateThing(thingId, parentId, name, createdAt, updatedAt, status, addId, prdId, lte_id, qtd, man, exp, strWeight, actWeight);
	}

	public void changeAddress(UUID thingId, String status, UUID destParentId, UUID destId, UUID prdId, String lte_id, BigDecimal qtd, Date man, Date exp) {
		wmsRep.deleteStkThing(thingId.toString());
		wmsRep.insertStk(thingId, destParentId, destId, lte_id, prdId, qtd, man, exp);
		wmsRep.updateThingAddress(thingId, destId);
		wmsRep.updateThingStatus(thingId, status);
	}

	public IntegrationReturn createResupply(UUID destId, SolarPickingResupply pkResupply[], User usr) {
		IntegrationReturn iRet = new IntegrationReturn(false, " Unknown Error");

		if (pkResupply == null || pkResupply.length == 0) {
			iRet = new IntegrationReturn(false, "Lista de ressuprimento vazia");
		} else {
			Address addressDest = addRep.findById(destId);
			DocumentModel dmOrdMov = dmRep.findByMetaname("ORDMOV");
			String prefix = pkResupply[0].getPrefix() == null || pkResupply[0].getPrefix().isEmpty() ? "MOV" : pkResupply[0].getPrefix();
			String status = pkResupply[0].getStatus() == null || pkResupply[0].getStatus().isEmpty() ? "ATIVO" : pkResupply[0].getStatus();
			Prefix pfx = pfxSvc.findNext(prefix, 9);
			Document d = new Document(dmOrdMov, dmOrdMov.getName() + " " + pfx.getCode(), pfx.getPrefix() + pfx.getCode(), status);
			DocumentModelField dmPri = dmOrdMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("PRIORITY")).findAny().get();
			DocumentModelField dmType = dmOrdMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TYPE")).findAny().get();
			DocumentModelField dmTitle = dmOrdMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TITLE")).findAny().get();
			int seq = 0;

			d.getFields().add(new DocumentField(d, dmPri, "NOVO", "1"));
			d.getFields().add(new DocumentField(d, dmType, "NOVO", "RESUPPLY"));
			d.getFields().add(new DocumentField(d, dmTitle, "NOVO", "PÁTIO: RESSUPRIMENTO"));
			if (usr != null) d.setUser(usr);
			for (SolarPickingResupply resupply : pkResupply) {
				List<UUID> pltOrigins = wmsRep.findFIFOByProduct(resupply.getProduct_id(), resupply.getAmmount(), resupply.getMinExpiry());

				if (pltOrigins.size() > 0) {
					for (UUID origId : pltOrigins) {
						Thing t = thRep.findById(origId);
						Thing ts = t.getSiblings().parallelStream().findAny().get();
						Product p = ts.getProduct();
						double qty = Double.parseDouble(ts.getProperties().parallelStream().filter(pr -> pr.getField().getMetaname().equals("QUANTITY")).findAny().get().getValue());
						String mu = p.getFields().parallelStream().filter(pf -> pf.getModel().getMetaname().equals("GROUP_UM")).findAny().get().getValue();

						d.getTransports().add(new DocumentTransport(d, ++seq, t, addressDest));
						if (d.getThings().parallelStream().noneMatch(dt -> dt.getThing().getId().equals(t.getId())))
							d.getThings().add(new DocumentThing(d, t, "RESUPPLY"));
						d.getItems().add(new DocumentItem(d, p, qty, "RESSUPRIMENTO", mu));
					}
				} else {
					iRet = new IntegrationReturn(false, "Produto " + resupply.getProduct_sku() + " - " + resupply.getProduct_name() + " não encontrado no estoque.");
					alertSvc.persist(new Alert(AlertType.PROCESS, AlertSeverity.WARNING, resupply.getProduct_sku(), "Produto " + resupply.getProduct_sku() + " - " + resupply.getProduct_name() + " não encontrado no estoque.", "Ressuprimento do picking: " + resupply.getProduct_name()));
					break;
				}
				iRet = new IntegrationReturn(true, null);
			}
			if (iRet.isResult()) {
				dcSvc.persist(d);
				aglSvc.sendDocToWMS(d, "POST");
			}
		}
		return iRet;
	}

	/*CUSTOM SOLAR*/
	@Transactional(value = TxType.REQUIRES_NEW)
	public void cancelOrdMov(Document d, User us) {
		int rem = cancelOrdmov(d.getId().toString());
		Document parent = d.getParent() != null && Hibernate.isInitialized(d.getParent()) ? d.getParent() : dcSvc.findParent(d);
		DocumentModel dmCanc = dmRep.findByMetaname("CANCELDOC");
		Document dCanc = new Document(dmCanc, dmCanc.getName() + " " + d.getCode(), "CANC" + d.getCode(), "NOVO");

		if (parent != null) {
			for (DocumentThing dt : d.getThings()) {
				DocumentThing tmpdtp = parent.getThings().parallelStream().filter(dtp -> dtp.getThing().getId().equals(dt.getThing().getId())).findAny().orElse(null);

				dt.setStatus("CANCELADO");
				if (tmpdtp != null) {
					List<Document> sibs = parent.getSiblings()
									.parallelStream()
									.filter(ds -> ds.getModel().getMetaname().equals("ORDCONF"))
									.collect(Collectors.toList());

					dtRep.removeById(tmpdtp.getId());
					parent.getSiblings().parallelStream()
									.filter(ds -> ds.getModel().getMetaname().equals("ORDCONF"))
									.flatMap(ds -> ds.getThings().parallelStream())
									.filter(dtp -> dtp.getId().equals(tmpdtp.getId()))
									.forEach(dtc -> {
										dtRep.removeById(dtc.getId());
										dtc.setStatus("REMOVE");
									});
					parent.getThings().removeIf(dtp -> dtp.getId().equals(tmpdtp.getId()));
					for (Document cnf : sibs) {
						cnf.getThings().removeIf(dtp -> dtp.getStatus().equals("REMOVE"));
					}
				}
			}
		}

		for (DocumentTransport dtr : d.getTransports()) {
			dtr.setStatus("CANCELADO");
			if (rem <= 0) {
				logger.info("Movord Not Present Removing Each Transport " + dtr.getSeq() + " = " + wmsRep.cancelMovByThing(dtr.getThing().getId().toString()));
				wmsRep.deleteStkThingAlocacao(dtr.getThing().getId().toString(), 1);
				wmsRep.changeStkAlocacao(dtr.getThing().getId().toString(), 2, 3);
			}
		}

		if (parent != null) {
			Supplier<Stream<Document>> supSib = () -> parent.getSiblings().parallelStream();
			boolean rota = Documents.getStringField(parent, "SERVICE_TYPE", "").equals("ROTA");
			boolean nfsaida = supSib.get().anyMatch(ds -> ds.getModel().getMetaname().equals("NFSAIDA"));
			boolean confsCompleted = rota ? !supSib.get()
							.flatMap(ds -> ds.getSiblings().parallelStream())//PICKINGS
							.filter(ds -> ds.getModel().getMetaname().equals("ORDCONF"))
							.anyMatch(ds -> ds.getStatus().equals("ATIVO")) : !supSib.get()
											.filter(ds -> ds.getModel().getMetaname().equals("ORDCONF"))
											.anyMatch(ds -> Documents.getStringField(ds, "CONF_TYPE").equals("SPAPD") && ds.getStatus().equals("ATIVO"));
			boolean movsLdtCompleted = !supSib.get()
							.filter(ds -> ds.getModel().getMetaname().equals("ORDMOV") && ds.getCode().startsWith("LDT") && !ds.getId().equals(d.getId()))
							.anyMatch(ds -> ds.getStatus().equals("LOAD"));
			boolean picksCompleted = !supSib.get()
							.filter(ds -> ds.getModel().getMetaname().equals("PICKING"))
							.anyMatch(ds -> Arrays.asList("ROMANEIO", "SEPARACAO", "MOVIMENTACAO", "FALHA").contains(ds.getStatus()));
			boolean picksChecked = !rota || supSib.get()
							.filter(ds -> ds.getModel().getMetaname().equals("PICKING"))
							.allMatch(ds -> ds.getStatus().equals("CONFERIDO"));
			logger.info(parent.getCode() + " - PicksCompleted: " + picksCompleted + " ConfsCompleted: " + confsCompleted + " MovsLDTCompleted: " + movsLdtCompleted + " Picks Checked: " + picksChecked);
			if (nfsaida && picksCompleted && confsCompleted && movsLdtCompleted && picksChecked) {
				createLacre(parent);
			}
		}
		dCanc.setUser(us);
		dCanc.setParent(d);
		d.setStatus("CANCELADO");
		d.getSiblings().add(dCanc);
	}

	public void completeOrdMov(Document ordmov) {
		Profiler prf = new Profiler("Complete Ordmov");
		boolean log = false;
		Document parent = Hibernate.isInitialized(ordmov.getParent()) ? ordmov.getParent() : dcSvc.findParent(ordmov);
		boolean repack = false;

		prf.step("Find Parent: " + (parent == null ? "NULL" : parent.getCode()), log);
		if (parent != null) {
			Supplier<Stream<Document>> supSib = () -> parent.getSiblings().parallelStream();
			final List<DocumentTransport> movThList = supSib.get()
							.filter(ds -> ds.getModel().getMetaname().equals("ORDMOV") && Documents.getStringField(ds, "MOV_TYPE").equals("UNLOAD") && ds.getStatus().equals("SUCESSO"))
							.flatMap(mov -> mov.getTransports().stream())
							.collect(Collectors.toList());

			prf.step("ULTMovs", log);
			if (parent.getModel().getMetaname().equals("TRANSPORT")) {
				boolean rota = Documents.getStringField(parent, "SERVICE_TYPE", "").equals("ROTA");
				boolean nfsaida = supSib.get().anyMatch(ds -> ds.getModel().getMetaname().equals("NFSAIDA"));
				boolean nfentrada = supSib.get().anyMatch(ds -> ds.getModel().getMetaname().equals("NFENTRADA"));
				boolean confsCompleted = rota ? !supSib.get()
								.flatMap(ds -> ds.getSiblings().parallelStream())//PICKINGS
								.filter(ds -> ds.getModel().getMetaname().equals("ORDCONF"))
								.anyMatch(ds -> ds.getStatus().equals("ATIVO")) : !supSib.get()
												.filter(ds -> ds.getModel().getMetaname().equals("ORDCONF"))
												.anyMatch(ds -> Documents.getStringField(ds, "CONF_TYPE").equals("SPAPD") && ds.getStatus().equals("ATIVO"));
				boolean movsUltCompleted = !supSib.get()
								.filter(ds -> ds.getModel().getMetaname().equals("ORDMOV") && ds.getCode().startsWith("ULT") && !ds.getId().equals(ordmov.getId()))
								.anyMatch(ds -> ds.getStatus().equals("UNLOAD"));
				boolean movsLdtCompleted = !supSib.get()
								.filter(ds -> ds.getModel().getMetaname().equals("ORDMOV") && ds.getCode().startsWith("LDT") && !ds.getId().equals(ordmov.getId()))
								.anyMatch(ds -> ds.getStatus().equals("LOAD"));
				boolean picksCompleted = !supSib.get()
								.filter(ds -> ds.getModel().getMetaname().equals("PICKING"))
								.anyMatch(ds -> Arrays.asList("ROMANEIO", "SEPARACAO", "MOVIMENTACAO", "FALHA").contains(ds.getStatus()));
				boolean picksChecked = !rota || supSib.get()
								.filter(ds -> ds.getModel().getMetaname().equals("PICKING"))
								.allMatch(ds -> ds.getStatus().equals("CONFERIDO"));
				boolean carga = supSib.get().anyMatch(ds -> ds.getModel().getMetaname().equals("APOCARGA"));
				boolean descarga = supSib.get().anyMatch(ds -> ds.getModel().getMetaname().equals("APODESCARGA"));
				boolean chkexit = supSib.get().anyMatch(ds -> ds.getModel().getMetaname().equals("APOCHECKSAIDA"));
				boolean exit = supSib.get().anyMatch(ds -> ds.getModel().getMetaname().equals("APOSAIDA"));
				boolean sealed = supSib.get().anyMatch(ds -> ds.getModel().getMetaname().equals("APOLACRE"));

				String parentStatus = parent.getStatus();

				Optional<Document> optConf = parent.getSiblings().stream()
								.filter(ds -> ds.getModel().getMetaname().equals("ORDCONF") && ds.getFields().parallelStream()
												.anyMatch(df -> df.getField().getMetaname().equals("CONF_TYPE") && df.getValue().equalsIgnoreCase("EPAPD")))
								.filter(cnf -> cnf.getStatus().equals("ATIVO") || cnf.getStatus().equals("AGUARDANDO DESCARGA"))
								.findAny();

				prf.step("Variables", log);
				if (optConf.isPresent()) {
					final Document conf = optConf.get();

					parent.getSiblings()
									.stream()
									.filter(ds -> ds.getModel().getMetaname().equals("ORDMOV") && Documents.getStringField(ds, "MOV_TYPE").equals("UNLOAD") && ds.getStatus().equals("SUCESSO"))
									.flatMap(mov -> mov.getTransports().stream())
									.forEach(dtr -> {
										Thing t = dtr.getThing();

										if (conf.getThings().parallelStream().noneMatch(dt -> dt.getThing().getId().equals(t.getId()))) {
											DocumentThing dt = new DocumentThing(conf, t, t.getStatus());

											dtRep.persist(dt);
											conf.getThings().add(dt);
										}
									});
					if (movsUltCompleted && conf.getStatus().equals("AGUARDANDO DESCARGA")) {
						conf.setStatus("ATIVO");
						dcSvc.fireUpdate(conf);
					}
					prf.step("Conference", log);
				} else {
					logger.info("Parent doesn't have conf");
				}
				if (!descarga && nfentrada && movsUltCompleted) {
					if (exit)
						parentStatus = "LIBERADO";
					else if (chkexit)
						parentStatus = "CAMINHAO NA SAIDA";
					else if (parentStatus.equals("CAMINHAO NO PATIO") || parentStatus.equals("CAMINHAO NA DOCA"))
						parentStatus = "CAMINHAO DESCARREGADO";
					dcSvc.fireUpdate(dcSvc.createChild(parent, parentStatus, "APODESCARGA", "NOVO", "DES", null, null, null, null));
					prf.step("Unload Truck", log);
				} else if (!carga && movsLdtCompleted && nfsaida && (picksCompleted || !rota)) {
					if (exit)
						parentStatus = "LIBERADO";
					else if (chkexit)
						parentStatus = "CAMINHAO NA SAIDA";
					else if (parentStatus.equals("CAMINHAO NO PATIO") || parentStatus.equals("CAMINHAO NA DOCA") || parentStatus.equals("CAMINHAO DESCARREGADO"))
						parentStatus = "CAMINHAO CARREGADO";
					dcSvc.createChild(parent, parentStatus, "APOCARGA", "NOVO", "CAR", null, null, null, null);
					prf.step("Load Truck", log);
				} else
					dcSvc.persist(parent);
				logger.info(parent.getCode() + " - PicksCompleted: " + picksCompleted + " ConfsCompleted: " + confsCompleted + " MovsLDTCompleted: " + movsLdtCompleted + " Sealed: " + sealed + " Picks Checked: " + picksChecked);
				if (nfsaida && picksCompleted && confsCompleted && movsLdtCompleted && !sealed && picksChecked) {
					createLacre(parent);
				}
			} else if (parent.getModel().getMetaname().equals("PICKING")) {
				parent.setStatus("SEPARADO");
				dcSvc.persist(parent);
				Document transport = dcSvc.findParent(parent);
				boolean loaded = transport.getSiblings().parallelStream()
								.anyMatch(ds -> ds.getModel().getMetaname().equals("APOCARGA"));
				boolean picked = transport.getSiblings().parallelStream()
								.filter(ds -> ds.getModel().getId().equals(parent.getModel().getId()) && !ds.getId().equals(parent.getId()))
								.allMatch(ds -> ds.getStatus().equals("SEPARADO"));
				Thing t = ordmov.getThings().parallelStream().map(dt -> dt.getThing()).findAny().orElse(null);

				if (t != null) {
					if (transport.getThings().parallelStream().noneMatch(dt -> dt.getThing().getId().equals(t.getId())))
						transport.getThings().add(new DocumentThing(transport, t, "SEPARADO"));
					if (parent.getThings().parallelStream().noneMatch(dt -> dt.getThing().getId().equals(t.getId())))
						parent.getThings().add(new DocumentThing(parent, t, "SEPARADO"));
				}
				if (!loaded && picked)
					dcSvc.createChild(transport, "CAMINHAO CARREGADO", "APOCARGA", "NOVO", "CAR", null, null, null, null);
				if (transport.getSiblings().parallelStream().anyMatch(ds -> ds.getModel().getMetaname().equals("APOLACRE") && ds.getStatus().equals("PREENCHIDO")))
					transport.setStatus("CAMINHAO LACRADO");
				prf.step("Picking", log);
			} else if (parent.getModel().getMetaname().equals("REPACK")) {
				repack = true;
			} else {
				prf.step("Parent Model = " + parent.getModel().getMetaname(), log);
			}
		} else
			prf.step("Parent is null", log);
		if (ordmov.getStatus().equals("SUCESSO")) {
			int cnt = 0;
			int total = ordmov.getTransports().size();

			for (DocumentTransport dtr : ordmov.getTransports()) {
				Thing t = dtr.getThing();
				Address pos = dtr.getAddress();
				Address r = Hibernate.isInitialized(pos.getParent()) ? pos.getParent() : addRep.quickFindParent(pos.getId());
				String status = t.getStatus();
				boolean resupply = false;

				if (!status.equals("BLOQUEADO")) {
					Address a = addRep.quickFindParent(r.getId());
					UUID addId = a.getId();

					while (a != null) {
						addId = a.getId();
						a = addRep.quickFindParent(addId);
					}
					a = addRep.findById(addId);
					switch (a.getModel().getMetaname()) {
						case "WAREHOUSE":
							if (ordmov.getCode().startsWith("PLS"))
								status = "BLOQUEADO";
							else
								status = "ARMAZENADO";
							break;
						case "TRUCK":
							status = "CARREGADO";
							break;
						case "DOCK_WH":
							if (ordmov.getCode().startsWith("ROT"))
								status = "SEPARADO";
							else
								status = "DESCARREGADO";
							break;
						case "SEGREGATION":
							status = "SEGREGADO";
							break;
						case "STAGE_PCK":
							if (ConfigUtil.get("hunter-custom-solar", "enable-resupply", "false").equalsIgnoreCase("true")) {
								status = "RESSUPRIMENTO";
								ordmov.setStatus("RESSUPRIMENTO");
							} else
								status = "EXPEDIDO";
							break;
						case "EXTERNAL":
							status = "EXPEDIDO";
							break;
						case "PICKING":
							resupply = true;
							status = resupply(dtr, repack, ++cnt >= total);
							break;
					}
				}
				t.setAddress(status.equals("EXPEDIDO") ? null : dtr.getAddress());
				t.setStatus(status);
				for (Thing ts : t.getSiblings()) {
					ts.setAddress(status.equals("EXPEDIDO") ? null : dtr.getAddress());
					ts.setStatus(status);
				}
				thRep.persist(t);
				if (!resupply) {
					Thing ts = t.getSiblings().parallelStream().findAny().orElse(t);
					Product p = ts.getProduct();
					String plot = Things.getStringProperty(t, "LOT_ID");
					BigDecimal pqty = new BigDecimal(Things.getDoubleProperty(t, "QUANTITY"));
					BigDecimal pstWg = new BigDecimal(Things.getDoubleProperty(t, "STARTING_WEIGHT"));
					BigDecimal pacWg = new BigDecimal(Things.getDoubleProperty(t, "ACTUAL_WEIGHT"));
					Date pman = Things.getDateProperty(t, "MANUFACTURING_BATCH", new Date());
					Date pexp = Things.getDateProperty(t, "LOT_EXPIRE", new Date());
					String lot = Things.getStringProperty(ts, "LOT_ID");
					BigDecimal qty = new BigDecimal(Things.getDoubleProperty(ts, "QUANTITY"));
					BigDecimal stWg = new BigDecimal(Things.getDoubleProperty(ts, "STARTING_WEIGHT"));
					BigDecimal acWg = new BigDecimal(Things.getDoubleProperty(ts, "ACTUAL_WEIGHT"));
					Date man = Things.getDateProperty(ts, "MANUFACTURING_BATCH", new Date());
					Date exp = Things.getDateProperty(ts, "LOT_EXPIRE", new Date());

					insertOrUpdateThing(t.getId(), null, t.getName(), t.getCreatedAt(), t.getUpdatedAt(), t.getStatus(), t.getAddress_id(), t.getProduct_id(), plot, pqty, pman, pexp, pstWg, pacWg);
					insertOrUpdateThing(ts.getId(), t.getId(), ts.getName(), ts.getCreatedAt(), ts.getUpdatedAt(), ts.getStatus(), ts.getAddress_id(), p.getId(), lot, qty, man, exp, stWg, acWg);
					changeAddress(t.getId(), t.getStatus(), r.getId(), pos.getId(), p.getId(), lot, qty, man, exp);
				}
			}
			prf.step("Transports Treated", log);
		}
		dcSvc.persist(ordmov);
		prf.done("Persisted", log, !log);
	}

	//CUSTOM-SOLAR
	public void generatePicking(Document d, List<Address> dest) {
		if (ConfigUtil.get("hunter-custom-solar", "generate-picking-transport", "false").equalsIgnoreCase("TRUE")) {
			boolean hasPicking = d.getSiblings().parallelStream()
							.anyMatch(s -> s.getModel().getMetaname().equals("PICKING") && !s.getStatus().equals("CANCELADO") && !s.getStatus().equals("SEPARADO"));

			if (!hasPicking) {
				final DocumentModel pkDocModel = dmRep.findByMetaname("PICKING");
				final DocumentModel osgDocModel = dmRep.findByMetaname("OSG");
				Supplier<Stream<Document>> nfsSup = () -> d.getSiblings().stream()
								.filter(ds -> ds.getModel().getMetaname().equals("NFSAIDA") && !Documents.getStringField(ds, "ZTRANS", "").equalsIgnoreCase("N"));
				final Map<Product, List<DocumentItem>> itemNFSMap = nfsSup.get()
								.flatMap(nf -> nf.getItems().stream())
								.collect(Collectors.groupingBy(di -> di.getProduct()));

				final Map<Product, DocumentItem> incompletePallets = new HashMap<>();
				final String clientClass = nfsSup.get()
								.map(nf -> nf.getPerson())
								.flatMap(ps -> ps.getFields().stream()
												.filter(psf -> psf.getField().getMetaname().equals("CLIENT_CLASS")))
								.map(psf -> psf.getValue())
								.findAny()
								.orElse("Sem Classe");

				for (Entry<Product, List<DocumentItem>> en : itemNFSMap.entrySet()) {
					Product prd = en.getKey();
					List<DocumentItem> diList = en.getValue();
					double palletBox = prd.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("PALLET_BOX") && !pf.getValue().isEmpty()).map(pf -> Double.valueOf(pf.getValue())).findAny().orElse(1d);
					double unitBox = prd.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("UNIT_BOX") && !pf.getValue().isEmpty()).map(pf -> Double.valueOf(pf.getValue())).findAny().orElse(1d);
					DocumentItem di = diList.stream().reduce(new DocumentItem(), (acc, nxt) -> {
						acc.setQty(acc.getQty() + nxt.getQty());
						return acc;
					});
					double boxes = di.getQty() / unitBox;
					double pallets = boxes / palletBox;

					if (pallets != Math.floor(pallets)) {
						//TODO: GAMBI DO INFERNO DAS MINI-PET DO CARALHO
						if (prd.getSku().equals("1001541") || prd.getSku().equals("1002063") || prd.getSku().equals("1002220")) {
							double altPallets = boxes / 280;

							//TODO: Check Average Pallet Quantity
							if (altPallets == Math.floor(altPallets)) continue;
						}
						di.setQty(boxes % palletBox);
						di.setMeasureUnit("CX");
						di.setProduct(prd);
						incompletePallets.put(prd, di);
					}
				}
				if (incompletePallets.size() > 0) {
					AtomicInteger seq = new AtomicInteger(0);
					AtomicInteger lyr = new AtomicInteger(1);
					double totalPacks = incompletePallets.values().stream().mapToDouble(di -> di.getQty()).sum();
					int totSku = incompletePallets.keySet().size();
					Document pick = new Document(pkDocModel, pkDocModel.getName() + " " + d.getCode(), "PIC" + d.getCode(), "SEPARACAO");
					Document osg = new Document(osgDocModel, osgDocModel.getName() + " " + pick.getCode(), "OSG" + pick.getCode(), "ATIVO");
					DocumentModelField dmfStage = osgDocModel.getFields().stream().filter(dmf -> dmf.getMetaname().equals("STAGE_ID")).findAny().get();
					DocumentModelField dmfLoad = osgDocModel.getFields().stream().filter(dmf -> dmf.getMetaname().equals("LOAD_ID")).findAny().get();
					DocumentField dfStage = new DocumentField(osg, dmfStage, "NOVO", dest.remove(0).getId().toString());
					DocumentField dfLoad = new DocumentField(osg, dmfLoad, "NOVO", clientClass);

					//Gerar Separações/Produto
					incompletePallets.values()
									.stream()
									.sorted((di1, di2) -> (int) (di2.getQty() - di1.getQty()))
									.forEachOrdered(di -> {
										Product p = di.getProduct();
										List<String> pkAddList = wmsRep.listPickingByProduct(p.getId());
										int boxLayer = p.getFields().parallelStream()
														.filter(pf -> pf.getModel().getMetaname().equals("BOX_LAYER"))
														.mapToInt(pf -> Integer.valueOf(pf.getValue()))
														.findAny()
														.getAsInt();
										int layers = (int) Math.floor(di.getQty() / boxLayer);
										String addId = pkAddList.isEmpty() ? "" : pkAddList.get(0);

										di.setDocument(pick);
										di.getProperties().put("LAYER", layers > 1 ? Integer.toString(lyr.getAndAdd(layers)) : Integer.toString(lyr.get()));
										di.getProperties().put("SEQ", Integer.toString(seq.incrementAndGet()));
										di.getProperties().put("PRODUCT_DESCRIPTION_LONG", di.getProduct().getName());
										di.getProperties().put("PRODUCT_DESCRIPTION_SHORT", di.getProduct().getName());
										di.getProperties().put("HIGHLIGHT", Boolean.toString(false));
										di.getProperties().put("SEPARATOR", Boolean.toString(layers > 0));
										di.getProperties().put("FULL_PALLET", Boolean.toString(false));
										pick.getItems().add(di);

										DocumentItem ndi = new DocumentItem();
										ndi.setDocument(osg);
										ndi.setMeasureUnit(di.getMeasureUnit());
										ndi.setProduct(di.getProduct());
										ndi.setStatus(di.getStatus());
										ndi.setQty(di.getQty());
										ndi.setProperties(new HashMap<String, String>(di.getProperties()));
										ndi.getProperties().put("ADDRESS_ID", addId);
										osg.getItems().add(ndi);
										if (addId.isEmpty()) alertSvc.persist(new Alert(AlertType.WMS, AlertSeverity.WARNING, osg.getCode(), di.getProduct().getSku(), "PRODUTO " + di.getProduct().getName() + " NÃO ENCONTRADO NO PICKING"));
									});
					osg.getFields().add(dfLoad);
					osg.getFields().add(dfStage);
					pick.getSiblings().add(osg);
					osg.setParent(pick);

					for (DocumentModelField dmf : pkDocModel.getFields()) {
						DocumentField df = new DocumentField(pick, dmf, "NOVO", "");

						switch (dmf.getMetaname()) {
							case "CASES_PHYSICAL":
								df.setValue(String.valueOf(totalPacks));
								break;
							case "SKUS":
								df.setValue(String.valueOf(totSku));
								break;
							case "BAY_ID":
								df.setValue(String.valueOf(dest.get(0).getParent().getId()));
								break;
							case "BAY_DESC":
								df.setValue(dest.get(0).getParent().getName());
								break;
							case "FULL":
								df.setValue(Boolean.toString(false));
								break;
							case "PLANT":
								df.setValue(ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"));
								break;
							case "DELIVERY_DATE":
								df.setValue(new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()));
								break;
							case "TICKET_MESSAGE":
								df.setValue("");
								break;
							case "CONTAINER_ID":
								df.setValue("");
								break;
							case "CONTAINER_LEVELS":
								df.setValue("0");
								break;
							default:
								logger.warn("Mistyped " + dmf.getMetaname() + " property");
								break;
						}
						pick.getFields().add(df);
					}
					pick.setParent(d);
					//			d.getSiblings().add(pick);
					dcSvc.persist(pick);
				}
			} else
				logger.warn("Picking " + d.getCode() + " Already Crated");
		} else
			logger.warn("Picking " + d.getCode() + " Disabled");
	}

	public void finishTransport(UUID id) {
		if (id != null)
			wmsRep.clearStkByTnp(id.toString());
	}

	@Asynchronous
	@Transactional(value = TxType.REQUIRES_NEW)
	public void checkResupply(Map<Product, Double> prdSet) {
		if (ConfigUtil.get("hunter-custom-solar", "auto-resupply", "false").equalsIgnoreCase("TRUE")) {
			Profiler pf = new Profiler();

			for (Entry<Product, Double> pq : prdSet.entrySet()) {
				Product p = pq.getKey();
				Double qty = pq.getValue();

				Map<String, Double> addrResupply = needsResupply(p.getId(), qty);//TODO: Juntar em um só
				pf.step("Found " + addrResupply.size() + " Addresses to resupply product " + p.getSku() + " - " + p.getName() + " Qty: " + DF.format(qty) + " (" + addrResupply
								.entrySet()
								.parallelStream()
								.map(en -> en.getKey() + " (" + DF.format(en.getValue()) + ")")
								.collect(Collectors.joining()) + ")", false);
				List<String> ongoingResupply = listActiveResupplyIds(p.getId());
				addrResupply.keySet().removeAll(ongoingResupply);
				pf.step("Remaining " + addrResupply.size() + " Addresses to Resupply", false);
				if (!addrResupply.isEmpty()) {
					//Address stage = getRegSvc().getAddSvc().findById(UUID.fromString("354a3c85-348f-11ea-8a83-005056a19775"));
					SolarPickingResupply pkResupply[] = new SolarPickingResupply[1];
					SolarPickingResupply resupply = new SolarPickingResupply();

					resupply.setAmmount(1);
					resupply.setMinExpiry(15);
					resupply.setPrefix("PCK");
					resupply.setProduct_id(p.getId());
					resupply.setProduct_name(p.getName());
					resupply.setProduct_sku(p.getSku());
					resupply.setStatus("PICKING");
					pkResupply[0] = resupply;
					IntegrationReturn iRet = createResupply(UUID.fromString("354a3c85-348f-11ea-8a83-005056a19775"), pkResupply, null);
					if (iRet.isResult())
						pf.step("Resupply Created for product " + p.getSku() + " - " + p.getName(), false);
					else
						pf.step("Resupply Failed: " + iRet.getMessage(), false);
				}
			}
			pf.done("Resupply Complete", false, false).forEach(logger::info);
		}
	}

	@Asynchronous
	@Transactional
	public void checkResupplyMin() {
		List<String> prdList = wmsRep.checkMinResupply();
		ToDoubleFunction<Product> rspQty = p -> Products.getDoubleField(p, "RESUPPLY_QUANTITY", 0d);
		Map<Product, Double> prdSet = prdList.stream()
						.map(s -> prdSvc.findById(UUID.fromString(s)))
						.collect(Collectors.groupingBy(Function.identity(), Collectors.summingDouble(rspQty)));

		logger.info("Resuply Threshold Quantity " + prdSet.size() + " Products");
		checkResupply(prdSet);
	}

	public String resupply(DocumentTransport dtr, boolean keepPickingPallet, boolean last) {
		//TODO: jogar palete do dest pra pilha de palete (PD)
		Thing t = dtr.getThing();
		List<Thing> dstList = thRep.listByAddressId(dtr.getAddress().getId());
		Thing dest = dstList.parallelStream()
						.filter(th -> th.getParent() == null && !th.getId().equals(t.getId()))
						.findAny()
						.orElse(null);
		if (dest != null) {
			Thing destSib = dest.getSiblings().stream().findAny().get();
			Thing sib = t.getSiblings().stream()
							.filter(ts -> ts.getProduct().getId().equals(destSib.getProduct().getId()))
							.findAny()
							.orElse(null);
			Property prQty = Things.findProperty(sib, "QUANTITY");
			Property prLot = Things.findProperty(sib, "LOT_ID");
			Property prDestQty = Things.findProperty(destSib, "QUANTITY");
			Property prDestLot = Things.findProperty(destSib, "LOT_ID");

			if (keepPickingPallet) {
				prDestQty.setValue(String.valueOf(Double.valueOf(prDestQty.getValue()) + Double.valueOf(prQty.getValue())));
				wmsRep.updateStkPicking(dest.getId().toString(), new BigDecimal(prDestQty.getValue()));
				if (last) {
					t.setAddress(null);
					t.setStatus("EXPEDIDO");
					for (Thing ts : t.getSiblings()) {
						ts.setAddress(null);
						ts.setStatus("EXPEDIDO");
					}
					wmsRep.deleteStkThing(t.getId().toString());
				}
			} else {
				prQty.setValue(String.valueOf(Double.valueOf(prQty.getValue()) + Double.valueOf(prDestQty.getValue())));
				dest.setAddress(null);
				dest.setStatus("EXPEDIDO");
				for (Thing ts : dest.getSiblings()) {
					ts.setAddress(null);
					ts.setStatus("EXPEDIDO");
				}
				t.setAddress(dtr.getAddress());
				t.setStatus("PICKING");
				sib.setAddress(dtr.getAddress());
				sib.setStatus("PICKING");
				wmsRep.deleteStkThing(dest.getId().toString());
				wmsRep.deleteStkThingAlocacao(t.getId().toString(), 2);
				wmsRep.resupplyStk(t.getId().toString(), prLot.getValue(), prQty.getValue());
			}
			wmsRep.updateThingStatus(t.getId(), t.getStatus());
			wmsRep.updateThingStatus(dest.getId(), dest.getStatus());
			wmsRep.updateThingStatusQuantityLot(destSib.getId(), destSib.getStatus(), new BigDecimal(prDestQty.getValue()), prDestLot.getValue());
			wmsRep.updateThingStatusQuantityLot(sib.getId(), sib.getStatus(), new BigDecimal(prQty.getValue()), prLot.getValue());
			thRep.persist(t);
			thRep.persist(dest);
			logger.info("Resupply Procedure Successful!");
		} else
			logger.error("Destination does not contain valid pallet for resupply");
		return t.getStatus();
	}

	public List<String> listActiveResupplyIds(UUID prdId) {
		Profiler pf = new Profiler();
		List<String> ongoingResupply = wmsRep.findOngoingResupply(prdId);
		logger.info(pf.step("Found " + ongoingResupply.size() + " Addresses with resupply activities", false));
		List<Document> activeResupply = dcSvc.listByTypeAndStatus("ORDMOV", "PICKING");
		if (activeResupply == null) activeResupply = new ArrayList<>();
		logger.info(pf.step("Found " + activeResupply.size() + " Transport Documents active", false));
		List<Product> prdList = activeResupply.parallelStream()
						.flatMap(d -> d.getTransports().parallelStream())
						.flatMap(dtr -> dtr.getThing().getSiblings().parallelStream())
						.map(t -> t.getProduct())
						.filter(prd -> prd.getId().equals(prdId))
						.collect(Collectors.toList());

		prdList.forEach(prd -> {
			ongoingResupply.addAll(listPickingByProduct(prd.getId()));
		});
		pf.done("Active Resupplies Found " + ongoingResupply.size(), false, false).forEach(logger::info);
		return ongoingResupply;
	}

	public Document createRouteLoadSingle(Document transport) {
		logger.info("Generating Single Load Transport");
		Thing truck = transport.getThings().parallelStream()
						.filter(dt -> dt.getThing().getProduct().getModel().getMetaname().equals("TRUCK"))
						.map(dt -> dt.getThing())
						.findAny()
						.orElse(null);

		if (truck != null) {
			DocumentModel dmMov = dmRep.findByMetaname("ORDMOV");
			DocumentModelField dmfPrio = dmMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("PRIORITY")).findAny().get();
			DocumentModelField dmType = dmMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TYPE")).findAny().get();
			DocumentModelField dmTitle = dmMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TITLE")).findAny().get();
			Document ordMov = new Document(dmMov, dmMov.getName() + " " + transport.getCode(), "LDT" + transport.getCode(), "LOAD");
			List<Document> pkList = transport.getSiblings().parallelStream()
							.filter(ds -> ds.getModel().getMetaname().equals("PICKING") && !ds.getStatus().equals("CANCELADO"))
							.collect(Collectors.toList());
			AtomicInteger seq = new AtomicInteger(1);

			ordMov.setParent(transport);
			ordMov.getFields().add(new DocumentField(ordMov, dmfPrio, "NOVO", "2"));
			ordMov.getFields().add(new DocumentField(ordMov, dmType, "NOVO", "LOAD"));
			ordMov.getFields().add(new DocumentField(ordMov, dmTitle, "NOVO", Documents.getStringField(transport, "OBS")));

			for (Document picking : pkList) {
				String bay = picking.getFields().parallelStream()
								.filter(df -> df.getField().getMetaname().equals("BAY_ID") && !df.getValue().isEmpty())
								.map(df -> df.getValue())
								.findAny()
								.orElse("1");
				Address bayAddress = truck.getAddress().getSiblings().parallelStream()
								.filter(dock -> dock.getFields().parallelStream()
												.anyMatch(af -> af.getModel().getMetaname().equals("ROAD_SEQ") && af.getValue().equals(bay)))
								.findAny()
								.orElse(null);

				picking.getThings().forEach(dt -> {
					Thing th = dt.getThing();
					Map<Product, Double> prdMap = th.getSiblings().parallelStream()
									.collect(Collectors.groupingBy(Thing::getProduct, Collectors.summingDouble(ts -> ts.getProperties().parallelStream()
													.filter(pr -> pr.getField().getMetaname().equals("QUANTITY"))
													.mapToDouble(pr -> Double.parseDouble(pr.getValue()))
													.sum())));

					ordMov.getThings().add(new DocumentThing(ordMov, th, "CONFERIDO"));
					ordMov.getTransports().add(new DocumentTransport(ordMov, seq.getAndIncrement(), th, bayAddress, th.getAddress()));
					for (Entry<Product, Double> en : prdMap.entrySet())
						ordMov.getItems().add(new DocumentItem(ordMov, en.getKey(), en.getValue(), "CONFERIDO", en.getKey().getFields().parallelStream()
										.filter(pf -> pf.getModel().getMetaname().equals("GROUP_UM"))
										.map(pf -> pf.getValue())
										.findAny()
										.orElse("CX")));
				});
			}
			return dcSvc.persist(ordMov);
		} else
			alertSvc.persist(new Alert(AlertType.WMS, AlertSeverity.WARNING, transport.getCode(), "TRANSPORTE SEM VEÍCULO", "Não há veículo associado ao transporte"));
		return null;
	}

	public Document createRouteLoad(Document picking) {
		if (ConfigUtil.get("hunter-custom-solar", "checkin-checkout-rota", "FALSE").equalsIgnoreCase("TRUE")) {
			logger.info("Generating Load Transport");
			Document transport = dcSvc.findParent(picking);
			Thing truck = transport.getThings().parallelStream()
							.filter(dt -> dt.getThing().getProduct().getModel().getMetaname().equals("TRUCK"))
							.map(dt -> dt.getThing())
							.findAny()
							.orElse(null);

			if (truck != null) {
				DocumentModel dmMov = dmRep.findByMetaname("ORDMOV");
				DocumentModelField dmfPrio = dmMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("PRIORITY")).findAny().get();
				DocumentModelField dmType = dmMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TYPE")).findAny().get();
				DocumentModelField dmTitle = dmMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TITLE")).findAny().get();

				Document ordMov = new Document(dmMov, dmMov.getName() + " " + picking.getCode(), "LDT" + picking.getCode(), "LOAD");
				AtomicInteger seq = new AtomicInteger(1);
				String bay = Documents.getStringField(picking, "BAY_ID", "1");
				Address bayAddress = truck.getAddress().getSiblings().parallelStream()
								.filter(dock -> dock.getFields().parallelStream()
												.anyMatch(af -> af.getModel().getMetaname().equals("ROAD_SEQ") && af.getValue().equals(bay)))
								.findAny()
								.orElse(null);

				ordMov.setParent(transport);
				ordMov.getFields().add(new DocumentField(ordMov, dmfPrio, "NOVO", "2"));
				ordMov.getFields().add(new DocumentField(ordMov, dmType, "NOVO", "LOAD"));
				ordMov.getFields().add(new DocumentField(ordMov, dmTitle, "NOVO", Documents.getStringField(transport, "OBS") + "." + Documents.getStringField(picking, "BAY_DESC")));
				picking.getThings().forEach(dt -> {
					Thing th = dt.getThing();
					Map<Product, Double> prdMap = th.getSiblings().parallelStream()
									.collect(Collectors.groupingBy(Thing::getProduct, Collectors.summingDouble(ts -> ts.getProperties().parallelStream()
													.filter(pr -> pr.getField().getMetaname().equals("QUANTITY"))
													.mapToDouble(pr -> Double.parseDouble(pr.getValue()))
													.sum())));

					ordMov.getThings().add(new DocumentThing(ordMov, th, "CONFERIDO"));
					ordMov.getTransports().add(new DocumentTransport(ordMov, seq.getAndIncrement(), th, bayAddress, th.getAddress()));
					for (Entry<Product, Double> en : prdMap.entrySet())
						ordMov.getItems().add(new DocumentItem(ordMov, en.getKey(), en.getValue(), "CONFERIDO", en.getKey().getFields().parallelStream()
										.filter(pf -> pf.getModel().getMetaname().equals("GROUP_UM"))
										.map(pf -> pf.getValue())
										.findAny()
										.orElse("CX")));
				});
				return dcSvc.persist(ordMov);
			} else
				alertSvc.persist(new Alert(AlertType.WMS, AlertSeverity.WARNING, picking.getCode(), "TRANSPORTE SEM VEÍCULO", "Não há veículo associado ao transporte"));
		} else
			logger.info("NOT Generating Load Transport (checkin-checkout-rota = FALSE)");
		return null;
	}

	@Asynchronous
	@Transactional(value = TxType.REQUIRES_NEW)
	public void createLacre(Document transp) {
		if (ConfigUtil.get("hunter-custom-solar", "create-truck-seal", "FALSE").equalsIgnoreCase("TRUE")) {
			if (!transp.getSiblings().parallelStream().anyMatch(ds -> ds.getModel().getMetaname().equals("APOLACRE"))) {
				DocumentModel dmLacre = dmRep.findByMetaname("APOLACRE");
				Document apoLacre = new Document(dmLacre, dmLacre.getName() + " " + transp.getCode(), "LCR" + transp.getCode(), "NOVO");

				apoLacre.setParent(transp);
				transp.getSiblings().add(dcSvc.persist(apoLacre));
			} else {
				logger.info("Lacre já gerado para o transporte " + transp.getCode());
			}
		}
	}

	@Transactional(value = TxType.REQUIRED)
	public void consumePickingQuantity(Document d) {
		for (DocumentItem diOsg : d.getItems()) {
			String addId = diOsg.getProperties().get("ADDRESS_ID");
			if (addId.isEmpty()) throw new IllegalArgumentException("Endereço do item vazio? di.id=" + diOsg.getId().toString());

			List<Thing> stkList = thRep.listByAddressIdNoOrphan(UUID.fromString(addId));

			if (!stkList.isEmpty()) {
				Thing stkThing = stkList.get(0);

				Property prQty = stkThing.getProperties().stream()
								.filter(pr -> pr.getField().getMetaname().equals("QUANTITY"))
								.findAny()
								.get();
				Property prLot = stkThing.getProperties().stream()
								.filter(pr -> pr.getField().getMetaname().equals("LOT_ID"))
								.findAny()
								.get();
				Property prMan = stkThing.getProperties().stream()
								.filter(pr -> pr.getField().getMetaname().equals("MANUFACTURING_BATCH"))
								.findAny()
								.get();
				Property prExp = stkThing.getProperties().stream()
								.filter(pr -> pr.getField().getMetaname().equals("LOT_EXPIRE"))
								.findAny()
								.get();
				double qty = new Double(prQty.getValue()) - diOsg.getQty();

				d.getThings().stream()
								.flatMap(dt -> dt.getThing().getSiblings().stream()
												.filter(ts -> ts.getProduct().getId().equals(stkThing.getProduct().getId())))
								.flatMap(t -> t.getProperties().stream())
								.filter(pr -> pr.getValue() != null && pr.getValue().equals("TEMPORARIO"))
								.forEach(pr -> {
									switch (pr.getField().getMetaname()) {
										case "LOT_ID":
											pr.setValue(prLot.getValue());
											break;
										case "MANUFACTURING_BATCH":
											pr.setValue(prMan.getValue());
											break;
										case "LOT_EXPIRE":
											pr.setValue(prExp.getValue());
											break;
									}
									prRep.quickInsert(pr.getThing().getId(), pr.getField().getId(), pr.getValue());
								});
				if (ConfigUtil.get("hunter-custom-solar", "auto-resupply", "FALSE").equalsIgnoreCase("TRUE")) {
					prRep.quickInsert(stkThing.getId(), prQty.getField().getId(), DF.format(qty));
					updateStkPicking(stkThing.getParent().getId().toString(), new BigDecimal(qty));
					updateThingQuantity(stkThing.getId(), new BigDecimal(qty));
				}
			}
		}
	}

	public void checkoutThings(Document transp) {
		String tStatus = "EXPEDIDO";
		String movModelMeta = "ORDMOV";
		String movType = "LOAD";
		String movStatus = "SUCESSO";
		String pickModelMeta = "PICKING";
		String pickStatus = "SEPARADO";

		if (transp.getSiblings().stream().anyMatch(ds -> ds.getModel().getMetaname().equals("NFSAIDA"))) {
			List<Thing> things = transp.getSiblings().stream()
							.filter(ds -> ds.getModel().getMetaname().equals(movModelMeta) || ds.getModel().getMetaname().equals(pickModelMeta))
							.filter(ds -> (Documents.getStringField(ds, "MOV_TYPE", "").equals(movType) && ds.getStatus().equals(movStatus)) || (ds.getStatus().equals(pickStatus)))
							.flatMap(ds -> ds.getThings().parallelStream())
							.map(dt -> dt.getThing())
							.filter(t -> transp.getThings().parallelStream()
											.anyMatch(dtt -> dtt.getThing().getId().equals(t.getId())))
							.distinct()
							.collect(Collectors.toList());

			for (Thing th : things) {
				th.setStatus(tStatus);
				th.setAddress(null);
				for (Thing ts : th.getSiblings()) {
					ts.setStatus(tStatus);
					ts.setAddress(null);
					updateThingStatus(ts.getId(), tStatus);
				}
				updateThingStatus(th.getId(), tStatus);
				deleteStkThing(th.getId().toString());
			}
			thRep.multiPersist(things);
		}
	}

	//CUSTOM-SOLAR
	public void createOutboundChecking(Document d) {
		if (ConfigUtil.get("hunter-custom-solar", "generate-outbound-checking", "FALSE").equalsIgnoreCase("TRUE")) {
			boolean hasConfOut = d.getSiblings().parallelStream()
							.filter(s -> s.getModel().getMetaname().equals("ORDCONF") && !s.getModel().getStatus().equals("CANCELADO") && !s.getModel().getStatus().equals("SUCESSO"))
							.anyMatch(cnf -> cnf.getFields().parallelStream()
											.filter(df -> df.getField().getMetaname().equals("CONF_TYPE"))
											.allMatch(df -> df.getValue().equals("SPAPD")) && !cnf.getStatus().equals("CANCELADO"));

			if (!hasConfOut) {
				Map<Product, Double> prdCountMap = d.getSiblings().parallelStream()
								.filter(ds -> ds.getModel().getMetaname().equals("NFSAIDA") && !Documents.getStringField(ds, "ZTRANS", "").equalsIgnoreCase("N"))
								.flatMap(ds -> ds.getItems().parallelStream())
								.filter(di -> {
									Map<String, String> props = di.getProduct().getModel().getProperties();
									boolean blindConf = props.containsKey("blind_conf") && props.get("blind_conf").equalsIgnoreCase("true");
									boolean outboundConf = props.containsKey("conf_out") && props.get("conf_out").equalsIgnoreCase("true");

									return blindConf || outboundConf;
								})
								.collect(Collectors.groupingBy(DocumentItem::getProduct, Collectors.summingDouble(DocumentItem::getQty)));

				if (prdCountMap.size() > 0) {
					DocumentModel ordConfModel = dmRep.findByMetaname("ORDCONF");
					DocumentModelField dmfConfType = ordConfModel.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("CONF_TYPE")).findAny().get();
					DocumentModelField dmfSvcType = ordConfModel.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("SERVICE_TYPE")).findAny().get();
					DocumentModelField dmfPrio = ordConfModel.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("PRIORITY")).findAny().get();
					Document ordconf = new Document(ordConfModel, ordConfModel.getName() + " " + d.getCode(), "CONFS" + d.getCode(), "ATIVO");

					d.getSiblings().add(ordconf);
					ordconf.setParent(d);
					ordconf.getFields().add(new DocumentField(ordconf, dmfConfType, "NOVO", "SPAPD"));
					ordconf.getFields().add(new DocumentField(ordconf, dmfSvcType, "NOVO", "TERCEIRO"));
					ordconf.getFields().add(new DocumentField(ordconf, dmfPrio, "NOVO", "5"));
					ordconf.getThings().addAll(d.getThings().parallelStream()
									.filter(dt -> !dt.getThing().getProduct().getModel().getMetaname().equals("TRUCK"))
									.map(dt -> new DocumentThing(ordconf, dt.getThing(), "CARREGADO"))
									.collect(Collectors.toSet()));
					for (Entry<Product, Double> en : prdCountMap.entrySet()) {
						Product p = en.getKey();
						double unids = en.getValue();
						String um = Products.getStringField(p, "GROUP_UM");
						int unitBox = Products.getIntegerField(p, "UNIT_BOX", 1);

						double packs = unids / unitBox;
						ordconf.getItems().add(new DocumentItem(ordconf, p, packs, "NOVO", um));
					}
					dcSvc.persist(ordconf);
				}
			} else {
				logger.warn("OutboundChecking " + d.getCode() + " Already Created");
			}
		} else {
			logger.warn(d.getCode() + " OutboundChecking disabled");
		}
	}

	public void createPicking(Document transport) {
		String sTruck = Documents.getStringField(transport, "TRUCK_ID");
		String sDock = Documents.getStringField(transport, "DOCK");
		Thing truck = thRep.findById(UUID.fromString(sTruck));
		truck.getUnitModel().addAll(uRep.listById(truck.getUnits()));
		Address docaVirtual = addRep.findById(UUID.fromString(sDock));
		String plates = truck.getUnitModel().stream().filter(u -> u.getType() == UnitType.LICENSEPLATES).findFirst().get().getTagId();
		int left = Things.getIntProperty(truck, "LEFT_SIDE_QUANTITY");
		int right = Things.getIntProperty(truck, "RIGHT_SIDE_QUANTITY");
		List<Address> bays = docaVirtual.getSiblings()
						.parallelStream()
						.filter(a -> Addresses.getIntField(a, "ROAD_SEQ") <= (left + right))
						.sorted((a1, a2) -> Addresses.getIntField(a1, "ROAD_SEQ") - Addresses.getIntField(a2, "ROAD_SEQ"))
						.collect(Collectors.toList());
		for (Address baia : bays)
			updateAddressCode(baia.getId(), plates);
		if (transport.getSiblings().stream().anyMatch(ds -> ds.getModel().getMetaname().equals("NFSAIDA") && !Documents.getStringField(ds, "ZTRANS", "").equalsIgnoreCase("N"))) {
			Collections.reverse(bays);
			generatePicking(transport, bays);
		}
	}

	public void createReturnChecking(UUID id, Map<String, String> caskMap) {
		if (ConfigUtil.get("hunter-custom-solar", "checkin-checkout-rota", "FALSE").equalsIgnoreCase("TRUE")) {
			Document trn = dcSvc.findById(id);
			boolean noCheck = trn != null && trn.getSiblings().parallelStream()
							.filter(ds -> ds.getModel().getMetaname().equals("ORDCONF") && !ds.getStatus().equals("CANCELADO"))
							.flatMap(chk -> chk.getFields().parallelStream())
							.filter(df -> df.getField().getMetaname().equals("CONF_TYPE"))
							.noneMatch(confType -> confType.getValue().equals("RPAPD"));

			if (noCheck) {
				logger.info("Creating Return Checking for Transport " + trn.getCode());
				DocumentModel chkMdl = dmRep.findByMetaname("ORDCONF");
				Product palletPrd = prdSvc.findBySKU("1404020");
				Product eucatexPrd = prdSvc.findBySKU("1207778");
				Document chk = new Document(chkMdl, chkMdl.getName() + " " + trn.getCode(), "CONF" + trn.getCode(), "ATIVO");
				DocumentModelField dmfType = DocumentModels.findField(chkMdl, "CONF_TYPE");
				DocumentModelField dmfPrio = DocumentModels.findField(chkMdl, "PRIORITY");
				DocumentModelField dmfSvcType = DocumentModels.findField(chkMdl, "SERVICE_TYPE");
				List<Document> nfss = trn.getSiblings().parallelStream()
								.filter(ds -> ds.getModel().getMetaname().equals("NFSAIDA"))
								.collect(Collectors.toList());
				List<Document> pents = trn.getSiblings().parallelStream()
								.filter(ds -> ds.getModel().getMetaname().equals("NFPENTREGA"))
								.collect(Collectors.toList());
				List<Document> sRocs = trn.getSiblings().parallelStream()
								.flatMap(ds -> ds.getSiblings().parallelStream())
								.filter(ds -> ds.getModel().getMetaname().equals("ORDCONF") && ds.getFields().parallelStream()
												.anyMatch(df -> df.getField().getMetaname().equals("CONF_TYPE") && df.getValue().equals("SPA")))
								.flatMap(cnf -> cnf.getSiblings().parallelStream().filter(roc -> roc.getStatus().equals("SUCESSO")))
								.collect(Collectors.toList());
				double palletQty = sRocs.parallelStream()
								.flatMap(roc -> roc.getItems().parallelStream())
								.filter(di -> di.getProduct().getSku().equals(palletPrd.getSku()))
								.mapToDouble(di -> di.getQty())
								.sum();
				double eucatexQty = sRocs.parallelStream()
								.flatMap(roc -> roc.getItems().parallelStream())
								.filter(di -> di.getProduct().getSku().equals(eucatexPrd.getSku()))
								.mapToDouble(di -> di.getQty())
								.sum();

				for (Document nfs : nfss) {
					boolean nfRecusada = nfs.getSiblings().parallelStream().anyMatch(ds -> ds.getModel().getMetaname().equals("RECUSANF"));

					for (DocumentItem diNF : nfs.getItems()) {
						if (diNF.getProperties().containsKey("CFOP") && (diNF.getProperties().get("CFOP").equals("590801") || diNF.getProperties().get("CFOP").equals("690801"))) {
							logger.info(diNF.getDocument().getCode() + " item " + diNF.getProduct().getSku() + " - " + diNF.getProduct().getName() + " CFOP: " + diNF.getProperties().get("CFOP") + " ignored");
							continue;
						}
						Product tmpPrd = diNF.getProduct();
						boolean isVas = tmpPrd.getModel().getMetaname().equals("VAS");
						Product tmpGenCaskPrd = caskMap.containsKey(tmpPrd.getSku()) ? prdSvc.findBySKU(caskMap.get(tmpPrd.getSku())) : tmpPrd;
						final Product prd = tmpGenCaskPrd == null ? tmpPrd : tmpGenCaskPrd;
						int unitBox = Products.getIntegerField(prd, "UNIT_BOX", 1);
						double qty = diNF.getQty() / unitBox;

						if (nfRecusada) {
							DocumentItem diChk = chk.getItems()
											.parallelStream()
											.filter(di -> di.getProduct().getId().equals(prd.getId()))
											.findAny()
											.orElseGet(() -> {
												DocumentItem tmp = new DocumentItem(chk, prd, 0, "RECUSA", Products.getStringField(prd, "GROUP_UM", "CX"));

												chk.getItems().add(tmp);
												return tmp;
											});

							diChk.setQty(diChk.getQty() + qty);
						} else {
							String sPkg = Products.getStringField(prd, "PACKAGE_ID");
							String sCask = Products.getStringField(prd, "CASK_ID");

							if (!sPkg.isEmpty()) {
								Product pkg = prdSvc.findById(UUID.fromString(sPkg));
								DocumentItem diChk = chk.getItems()
												.parallelStream()
												.filter(di -> di.getProduct().getId().equals(pkg.getId()))
												.findAny()
												.orElseGet(() -> {
													DocumentItem tmp = new DocumentItem(chk, pkg, 0, "EMBALAGEM", Products.getStringField(pkg, "GROUP_UM", "UN"));

													chk.getItems().add(tmp);
													return tmp;
												});

								diChk.setQty(diChk.getQty() + (isVas ? -qty : qty));
							}
							if (!sCask.isEmpty()) {
								Product tmpCask = prdSvc.findById(UUID.fromString(sCask));
								Product tmpGenCask = caskMap.containsKey(tmpCask.getSku()) ? prdSvc.findBySKU(caskMap.get(tmpCask.getSku())) : tmpCask;
								final Product cask = tmpGenCask == null ? tmpCask : tmpGenCask;
								DocumentItem diChk = chk.getItems()
												.parallelStream()
												.filter(di -> di.getProduct().getId().equals(cask.getId()))
												.findAny()
												.orElseGet(() -> {
													DocumentItem tmp = new DocumentItem(chk, cask, 0, "VASILHAME", Products.getStringField(cask, "GROUP_UM", "CX"));

													chk.getItems().add(tmp);
													return tmp;
												});

								diChk.setQty(diChk.getQty() + qty);
							}
						}
					}
				}
				for (Document pent : pents) {
					for (DocumentItem diPent : pent.getItems()) {
						double qtyVendida = diPent.getQty();

						if (qtyVendida > 0) {
							Product prd = diPent.getProduct();
							String sPkg = Products.getStringField(prd, "PACKAGE_ID");
							String sCask = Products.getStringField(prd, "CASK_ID");

							if (!sPkg.isEmpty()) {
								Product pkg = prdSvc.findById(UUID.fromString(sPkg));
								DocumentItem diChk = chk.getItems()
												.parallelStream()
												.filter(di -> di.getProduct().getId().equals(pkg.getId()))
												.findAny()
												.orElseGet(() -> {
													DocumentItem tmp = new DocumentItem(chk, pkg, 0, "PENTREGA_EMB", Products.getStringField(pkg, "GROUP_UM", "UN"));

													chk.getItems().add(tmp);
													return tmp;
												});

								diChk.setQty(diChk.getQty() + qtyVendida);
							}
							if (!sCask.isEmpty()) {//TOFIX: Terrible
								Product tmpCask = prdSvc.findById(UUID.fromString(sCask));
								Product tmpGenCask = caskMap.containsKey(tmpCask.getSku()) ? prdSvc.findBySKU(caskMap.get(tmpCask.getSku())) : tmpCask;
								final Product cask = tmpGenCask == null ? tmpCask : tmpGenCask;
								DocumentItem diChk = chk.getItems()
												.parallelStream()
												.filter(di -> di.getProduct().getId().equals(cask.getId()))
												.findAny()
												.orElseGet(() -> {
													DocumentItem tmp = new DocumentItem(chk, cask, 0, "PENTREGA_VAS", Products.getStringField(cask, "GROUP_UM", "CX"));

													chk.getItems().add(tmp);
													return tmp;
												});

								diChk.setQty(diChk.getQty() + qtyVendida);
							}
							DocumentItem diChk = chk.getItems()
											.parallelStream()
											.filter(di -> di.getProduct().getId().equals(prd.getId()))
											.findAny()
											.orElseGet(() -> {
												DocumentItem tmp = new DocumentItem(chk, prd, 0, "PENTREGA", Products.getStringField(prd, "GROUP_UM", "CX"));

												chk.getItems().add(tmp);
												return tmp;
											});

							diChk.setQty(diChk.getQty() - qtyVendida);
						}
					}
				}
				chk.setParent(trn);
				chk.getItems().removeIf(diChk -> diChk.getQty() == 0);
				chk.getItems().add(new DocumentItem(chk, palletPrd, palletQty, "NOVO", Products.getStringField(palletPrd, "GROUP_UM", "UN")));
				chk.getItems().add(new DocumentItem(chk, eucatexPrd, eucatexQty, "NOVO", Products.getStringField(eucatexPrd, "GROUP_UM", "UN")));
				chk.getFields().add(new DocumentField(chk, dmfType, "NOVO", "RPAPD"));
				chk.getFields().add(new DocumentField(chk, dmfPrio, "NOVO", "6"));
				chk.getFields().add(new DocumentField(chk, dmfSvcType, "NOVO", "ROTA"));
				dcSvc.persist(chk);
			} else
				logger.error("Transport is null");
		} else {
			logger.warn("Return not enabled on config (checkin-checkout-rota)");
		}
	}

	public void clearArmloc(String armlocId) {
		wmsRep.clearArmloc(armlocId);
	}

	public boolean isMultiExpiry(UUID armlocId) {
		return wmsRep.isMultiExiry(armlocId);
	}
}
