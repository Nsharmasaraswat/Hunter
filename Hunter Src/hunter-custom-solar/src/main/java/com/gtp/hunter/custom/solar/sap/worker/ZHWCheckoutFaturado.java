package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.util.StreamUtil;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPCheckoutFaturadoItemDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.custom.solar.util.Constants;
import com.gtp.hunter.custom.solar.util.ToJsonSAP;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.DocumentTransport;
import com.gtp.hunter.process.model.PersonField;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Documents;
import com.sap.conn.jco.JCoException;

public class ZHWCheckoutFaturado extends BaseWorker {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ZHWCheckoutFaturado(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		return false;
	}

	@Override
	public boolean external(Object obj) {
		logger.info("================================================================" + Constants.RFC_CHECKOUT_FATURADO + "===========================================================================");
		Document transport = (Document) obj;

		ReadFieldsSap readFieldsSap = null;
		DecimalFormat DF = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.US));
		LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
		ToJsonSAP jcoSonStart = new ToJsonSAP(getSolar().getFunc(Constants.RFC_CHECKOUT_FATURADO));

		item.put(Constants.I_TKNUM, transport.getCode().replace("R", "000"));
		logger.info(item.toString());
		jcoSonStart.setParameters(item);
		try {
			readFieldsSap = getGson().fromJson(jcoSonStart.execute(getSolar().getDestination()), ReadFieldsSap.class);
		} catch (JCoException e) {
			RuntimeException t = new RuntimeException();
			t.setStackTrace(e.getStackTrace());
			t.initCause(e.getCause());
			throw t;
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
			getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, transport.getCode(), Constants.RFC_CHECKOUT_FATURADO, e.getCause().getLocalizedMessage()));
		}

		logJcoError(readFieldsSap, transport.getCode(), Constants.RFC_CHECKOUT_FATURADO);
		AlertSeverity sev;

		switch (readFieldsSap.geteRetorno()) {
			case "E":
				sev = AlertSeverity.ERROR;
				break;
			default:
				sev = AlertSeverity.INFO;
				break;
		}
		getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, transport.getCode(), Constants.RFC_CHECKOUT_FATURADO, readFieldsSap.geteMensagem()));

		//		if (sev != AlertSeverity.ERROR) {
		Map<String, Double> fatList = readFieldsSap.getCheckoutFaturadoItemDTOs()
						.parallelStream()
						.sorted((d1, d2) -> d1.getMatnr().compareTo(d2.getMatnr()))
						.collect(Collectors.groupingBy(f -> String.valueOf(Integer.parseInt(f.getMatnr())), Collectors.summingDouble(SAPCheckoutFaturadoItemDTO::getLfimg)));
		Map<String, Double> sepList = transport.getSiblings()
						.stream()
						.filter(ds -> ds.getModel().getMetaname().equals("PICKING") && !ds.getStatus().equals("CANCELADO"))
						.map(pk -> getISvc().getRpSvc().listPickingItems(pk))
						.flatMap(ds -> ds.getItems().parallelStream())
						.collect(Collectors.groupingBy(di -> String.valueOf(Integer.parseInt(di.getProduct().getSku())), Collectors.summingDouble(DocumentItem::getQty)));
		Set<String> nFatSku = sev == AlertSeverity.ERROR ? new HashSet<>() : sepList.keySet().parallelStream().filter(s -> !fatList.containsKey(s)).collect(Collectors.toSet());

		for (Entry<String, Double> fat : fatList.entrySet()) {
			String fatSku = fat.getKey();
			Double fatQty = fat.getValue();
			boolean prdMissing = !sepList.containsKey(fatSku);
			Double diff = (prdMissing ? 0 : sepList.get(fatSku) - fatQty);

			if (prdMissing && fatQty > 0) {
				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, transport.getCode(), Constants.RFC_CHECKOUT_FATURADO, "Produto " + fatSku + " Faturado porem faltante na lista de separação " + DF.format(diff) + " CX"));
			} else if (diff > 0) {
				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.WARNING, transport.getCode(), Constants.RFC_CHECKOUT_FATURADO, "Produto " + fatSku + " Divergente do faturamento em " + DF.format(diff) + " CX"));
				transport.getSiblings()
								.stream()
								.filter(ds -> ds.getModel().getMetaname().equals("PICKING") && !ds.getStatus().equals("CANCELADO"))
								.flatMap(pck -> pck.getItems().stream())
								.filter(di -> di.getProduct().getSku().equals(fatSku) && di.getQty() >= diff)
								.sorted((di1, di2) -> (int) (di1.getQty() - di2.getQty()))
								.findFirst()
								.ifPresent(di -> {
									logger.warn("Produto " + fatSku + " Divergente do faturamento. Doc: " + di.getDocument().getCode());
									di.setQty(di.getQty() - diff);
									if (di.getId() != null)
										getISvc().getRegSvc().getDiSvc().updateDocumentItemQuantity(di.getId(), (int) di.getQty());
								});
			}
		}
		create(transport, nFatSku);
		return true;
		//		}
		//		return false;
	}

	private void create(Document transport, Set<String> nFatSku) {
		DocumentModel osgModel = getISvc().getRegSvc().getDmSvc().findByMetaname("OSG");
		DocumentModel ordmovModel = getISvc().getRegSvc().getDmSvc().findByMetaname("ORDMOV");
		DocumentModelField dmfOMPrio = ordmovModel.getFields().stream().filter(dmf -> dmf.getMetaname().equals("PRIORITY")).findAny().get();
		DocumentModelField dmType = ordmovModel.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TYPE")).findAny().get();
		DocumentModelField dmTitle = ordmovModel.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TITLE")).findAny().get();
		DocumentModelField dmfStage = osgModel.getFields().stream().filter(dmf -> dmf.getMetaname().equals("STAGE_ID")).findAny().get();
		DocumentModelField dmfLoad = osgModel.getFields().stream().filter(dmf -> dmf.getMetaname().equals("LOAD_ID")).findAny().get();
		DocumentModel ordconfModel = getISvc().getRegSvc().getDmSvc().findByMetaname("ORDCONF");
		DocumentModelField dmfConfType = ordconfModel.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("CONF_TYPE")).findAny().get();
		DocumentModelField dmfSvcType = ordconfModel.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("SERVICE_TYPE")).findAny().get();
		DocumentModelField dmfCnfPrio = ordconfModel.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("PRIORITY")).findAny().get();
		DocumentModelField dmfCnfLoad = ordconfModel.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("LOAD_ID")).findAny().get();
		Product pallet = getISvc().getRegSvc().getPrdSvc().findBySKU("1404020");
		Product eucatex = getISvc().getRegSvc().getPrdSvc().findBySKU("1207778");

		List<Document> docPicking = transport.getSiblings().stream().filter(ds -> ds.getModel().getMetaname().equals("PICKING")).collect(Collectors.toList());
		int qtdPallets = (int) docPicking.stream().count();
		List<String> stageIds = getISvc().getRegSvc().getWmsSvc().listPickingStages(qtdPallets);
		String load = Documents.getStringField(transport, "OBS", "Carga: ");
		List<Document> fullPallets = docPicking.stream()
						.filter(ds -> ds.getFields().stream()
										.filter(df -> df.getField().getMetaname().equals("FULL"))
										.allMatch(df -> df.getValue().equalsIgnoreCase("TRUE")))
						.collect(Collectors.toList());
		Map<Product, List<DocumentItem>> fpItems = fullPallets.parallelStream()
						.flatMap(d -> d.getItems().parallelStream().filter(di -> !nFatSku.contains(di.getProduct().getSku())))
						.filter(StreamUtil.distinctByKey(DocumentItem::getDocument))
						.collect(Collectors.groupingBy(DocumentItem::getProduct));
		List<Document> pick = docPicking.stream()
						.filter(d -> !fullPallets.contains(d))
						.collect(Collectors.toList());

		if (stageIds.size() >= pick.size()) {
			logger.info(transport.getCode() + " - Stages " + stageIds.size() + " Picks: " + pick.size());
			for (Document picking : pick) {
				Set<DocumentItem> items = picking.getItems().parallelStream().filter(di -> !nFatSku.contains(di.getProduct().getSku())).collect(Collectors.toSet());
				Document osg = new Document(osgModel, osgModel.getName() + " " + picking.getCode(), "OSG" + picking.getCode(), "ATIVO");
				DocumentField dfStage = new DocumentField(osg, dmfStage, "NOVO", stageIds.remove(0));
				DocumentField dfLoad = new DocumentField(osg, dmfLoad, "NOVO", load);
				AtomicInteger cnt = new AtomicInteger(1);

				osg.getFields().add(dfStage);
				osg.getFields().add(dfLoad);
				osg.setParent(picking);
				items.stream()
								.sorted((di1, di2) -> {
									int lyr1 = Integer.parseInt(di1.getProperties().get("LAYER"));
									int lyr2 = Integer.parseInt(di2.getProperties().get("LAYER"));

									if (lyr1 == lyr2) {
										boolean isHigh1 = di1.getProperties().get("HIGHLIGHT").equalsIgnoreCase("TRUE");
										boolean isHigh2 = di2.getProperties().get("HIGHLIGHT").equalsIgnoreCase("TRUE");
										boolean isFirst1 = di1.getProperties().get("IS_FIRST_ITEM").equalsIgnoreCase("TRUE");
										boolean isFirst2 = di2.getProperties().get("IS_FIRST_ITEM").equalsIgnoreCase("TRUE");
										boolean isLast1 = di1.getProperties().get("IS_LAST_ITEM").equalsIgnoreCase("TRUE");
										boolean isLast2 = di2.getProperties().get("IS_LAST_ITEM").equalsIgnoreCase("TRUE");

										if (isHigh1 && isFirst1) return -1;
										if (isHigh2 && isFirst2) return 1;
										if (isHigh1 && isLast1) return 1;
										if (isHigh2 && isLast2) return -1;
										if (di1.getProduct().getModel().getMetaname().equals(di2.getProduct().getModel().getMetaname())) {
											String sz1 = di1.getProduct().getFields().parallelStream()
															.filter(pf -> pf.getModel().getMetaname().equals("SIZE") && !pf.getValue().isEmpty())
															.map(pf -> pf.getValue())
															.findAny()
															.orElse("");
											String sz2 = di2.getProduct().getFields().parallelStream()
															.filter(pf -> pf.getModel().getMetaname().equals("SIZE") && !pf.getValue().isEmpty())
															.map(pf -> pf.getValue())
															.findAny()
															.orElse("");

											return sz1.compareTo(sz2);
										}
										return di1.getProduct().getModel().getMetaname().compareTo(di2.getProduct().getModel().getMetaname());
									}
									return lyr1 - lyr2;
								})
								.map(di -> {
									di.getProperties().put("SEQ", String.valueOf(cnt.getAndIncrement()));
									return di;
								})
								.forEachOrdered(di -> {
									List<String> pkAddList = getISvc().getRegSvc().getWmsSvc().listPickingByProduct(di.getProduct().getId());
									String addId = pkAddList.isEmpty() ? "" : pkAddList.get(0);
									DocumentItem ndi = new DocumentItem();

									ndi.setDocument(osg);
									ndi.setMeasureUnit(di.getMeasureUnit());
									ndi.setProduct(di.getProduct());
									ndi.setStatus(di.getStatus());
									ndi.setQty(di.getQty());
									ndi.setProperties(new HashMap<String, String>(di.getProperties()));
									ndi.getProperties().put("ADDRESS_ID", addId);
									osg.getItems().add(ndi);
									if (addId.isEmpty()) getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.WMS, AlertSeverity.WARNING, osg.getCode(), di.getProduct().getSku(), "PRODUTO " + di.getProduct().getName() + " NÃO ENCONTRADO NO PICKING"));
								});
				picking.getSiblings().add(osg);
				picking.setStatus("SEPARACAO");
				getISvc().getRegSvc().getDcSvc().persist(osg);
				getISvc().getRegSvc().getDcSvc().persist(picking);
			}

			Set<UUID> reserved = new HashSet<>();
			for (Entry<Product, List<DocumentItem>> e : fpItems.entrySet()) {
				Product p = e.getKey();
				List<DocumentItem> dis = e.getValue();

				for (DocumentItem di : dis) {
					Document pfp = di.getDocument();
					Supplier<Stream<PersonField>> psfSup = () -> transport.getSiblings().stream()
									.filter(ds -> ds.getModel().getMetaname().equals("NFSAIDA"))
									.map(Document::getPerson)
									.flatMap(ps -> ps.getFields().stream());
					int shelf = p.getFields().stream()
									.filter(pf -> pf.getModel().getMetaname().equals("SHELFLIFE"))
									.mapToInt(pf -> {
										if (pf.getValue().isEmpty()) return 0;
										return Integer.parseInt(pf.getValue());
									})
									.findAny()
									.orElse(0);
					int val = 15;
					String solarMarket = psfSup.get()
									.filter(pf -> pf.getField().getMetaname().equals("SOLAR_MARKET"))
									.map(pf -> pf.getValue())
									.findAny()
									.orElse("");
					String keyAccount = psfSup.get()
									.filter(pf -> pf.getField().getMetaname().equals("KEY_ACCOUNT"))
									.map(pf -> pf.getValue())
									.findAny()
									.orElse("");
					String custClass = psfSup.get()
									.filter(pf -> pf.getField().getMetaname().equals("CLIENT_CLASS"))
									.map(pf -> pf.getValue())
									.findAny()
									.orElse("");
					if (keyAccount.equals("00037") || keyAccount.equals("00324"))
						val = (int) Math.max(Math.round(shelf * 0.75), 15);
					else if (keyAccount.equals("01021"))
						val = (int) Math.max(Math.round(shelf * 0.6), 15);
					else if (keyAccount.equals("00298") || custClass.equals("SL") || solarMarket.equals("02"))
						val = 30;
					List<UUID> thList = getISvc().getRegSvc().getWmsSvc().findFIFOByProduct(p.getId(), dis.size() * fpItems.keySet().size(), val);

					try {
						thList.removeIf(tt -> reserved.contains(tt));
						if (!thList.isEmpty()) {
							Document ordmov = new Document(ordmovModel, ordmovModel.getName() + " " + pfp.getCode(), "ROT" + pfp.getCode(), "ROTA");
							Address dest = getISvc().getRegSvc().getAddSvc().findById(UUID.fromString(stageIds.remove(0)));
							Thing th = getISvc().getRegSvc().getThSvc().findById(thList.remove(0));

							ordmov.setParent(pfp);
							ordmov.getItems().add(new DocumentItem(ordmov, p, di.getQty(), "ROTA", di.getMeasureUnit()));
							ordmov.getThings().add(new DocumentThing(ordmov, th, "PICKING"));
							ordmov.getFields().add(new DocumentField(ordmov, dmType, "NOVO", "ROTA"));
							ordmov.getFields().add(new DocumentField(ordmov, dmTitle, "NOVO", load));
							ordmov.getFields().add(new DocumentField(ordmov, dmfOMPrio, "NOVO", "1"));
							ordmov.getTransports().add(new DocumentTransport(ordmov, 1, th, dest));
							pfp.getSiblings().add(ordmov);
							pfp.setStatus("MOVIMENTACAO");
							getISvc().getRegSvc().getDtSvc().persist(new DocumentThing(pfp, th, "MOVIMENTACAO"));
							getISvc().getRegSvc().getDcSvc().persist(ordmov);
							getISvc().getRegSvc().getDcSvc().persist(pfp);
							try {
								getISvc().getRegSvc().getAglSvc().sendDocToWMS(ordmov, "POST").get();
								reserved.add(th.getId());
							} catch (Exception ignored) {
								ignored.printStackTrace();
							}
						} else {
							getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.WMS, AlertSeverity.ERROR, pfp.getCode(), di.getProduct().getSku(), "PRODUTO " + di.getProduct().getName() + " NÃO EXISTE NO ESTOQUE COM MAIS DE " + val + " DIAS DE VALIDADE"));
							Document ordconf = new Document(ordconfModel, ordconfModel.getName() + " " + pfp.getCode(), "CONF" + pfp.getCode(), "ATIVO");

							ordconf.setParent(pfp);
							ordconf.getFields().add(new DocumentField(ordconf, dmfConfType, "NOVO", "SPA"));
							ordconf.getFields().add(new DocumentField(ordconf, dmfSvcType, "NOVO", "ROTA"));
							ordconf.getFields().add(new DocumentField(ordconf, dmfCnfPrio, "NOVO", "1"));
							ordconf.getFields().add(new DocumentField(ordconf, dmfCnfLoad, "NOVO", load.replace("Carga: ", "")));
							ordconf.getItems().add(new DocumentItem(ordconf, di.getProduct(), di.getQty(), "NOVO", di.getMeasureUnit()));
							ordconf.getItems().add(new DocumentItem(ordconf, pallet, 1, "NOVO", "UN"));
							ordconf.getItems().add(new DocumentItem(ordconf, eucatex, 0, "NOVO", "UN"));
							pfp.getSiblings().add(ordconf);
							pfp.setStatus("SEPARADO");
							getISvc().getRegSvc().getDcSvc().persist(ordconf);
							getISvc().getRegSvc().getDcSvc().persist(pfp);
						}
					} catch (ArrayIndexOutOfBoundsException aioobe) {
						String msg = aioobe.getLocalizedMessage();

						if (thList == null)
							msg = "thList is null????";
						else if (thList.isEmpty())
							msg = "thList is empty?";
						else
							msg = "thList size: " + thList.size();
						getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.WMS, AlertSeverity.ERROR, pfp.getCode(), di.getProduct().getSku(), msg));
					}
				}
			}
			transport.setStatus("CAMINHAO NA DOCA");
		} else {
			getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.WMS, AlertSeverity.ERROR, transport.getCode(), "FALTA DE ESPAÇO NO STAGE", "QUANTIDADE DE ESPAÇOS VAZIOS NO STAGE (" + stageIds.size() + ") É MENOR QUE QUANTIDADE DE ROMANEIOS DO TRANSPORTE (" + pick.size() + ")"));
			transport.setStatus("INTEGRADO");
		}
		getISvc().getRegSvc().getDcSvc().persist(transport);
	}
}
