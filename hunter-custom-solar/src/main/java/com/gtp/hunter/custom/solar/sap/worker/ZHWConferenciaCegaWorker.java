package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPConfCegaMsgDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.custom.solar.util.Constants;
import com.gtp.hunter.custom.solar.util.ToJsonSAP;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductField;
import com.gtp.hunter.process.model.util.Documents;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;

public class ZHWConferenciaCegaWorker extends BaseWorker {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ZHWConferenciaCegaWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		return false;
	}

	@Override
	public boolean external(Object obj) {
		logger.info("================================================================" + Constants.RFC_CONFERENCIA_CEGA + "===========================================================================");
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
		DecimalFormat decForm = new DecimalFormat("0.0000", symbols);
		decForm.setRoundingMode(RoundingMode.FLOOR);
		Document transport = (Document) obj;
		logger.info("Document: " + transport.getCode());
		Document ordConf = transport.getSiblings().parallelStream()
						.filter(d -> {
							if (!d.getModel().getMetaname().equals("ORDCONF")) return false;
							if (!d.getStatus().equals("SUCESSO")) return false;
							String type = Documents.getStringField(d, "CONF_TYPE", "");

							return type.equals("EPAPD") || type.equals("EMP");
						})
						.findAny()
						.orElse(null);
		if (ordConf != null) {
			logger.info("Checking: " + ordConf.getCode());
			List<Document> dNFs = new ArrayList<>(transport.getSiblings().stream().filter(d -> d.getModel().getMetaname().equals("NFENTRADA")).collect(Collectors.toSet()));
			logger.info("NFs " + dNFs.size());
			boolean convertBoxes = dNFs.parallelStream().anyMatch(nf -> nf.getPerson() != null && (nf.getPerson().getCode().startsWith("07196033") || nf.getPerson().getCode().startsWith("08715757") || nf.getPerson().getCode().startsWith("10557540")));
			Optional<Document> optRetordconf = ordConf.getSiblings().stream().filter(d -> d.getModel().getMetaname().equals("RETORDCONF") && d.getStatus().equals("SUCESSO")).findFirst();
			Optional<DocumentField> optCTE = transport.getFields().stream().filter(df -> df.getField().getMetaname().equalsIgnoreCase("CTE")).findAny();
			String cte = optCTE.isPresent() ? optCTE.get().getValue() : "";

			if (optRetordconf.isPresent()) {
				Document retOrdConf = optRetordconf.get();
				logger.info("Checking Return: " + retOrdConf.getCode());
				Map<UUID, Double> prdCountMap = new ConcurrentHashMap<>();
				boolean hasKit = false;

				for (DocumentItem di : retOrdConf.getItems()) {
					Product p = di.getProduct();
					Double e = 0d;
					Double qty = di.getQty();

					for (UUID prdId : prdCountMap.keySet()) {
						if (prdId.equals(di.getProduct().getId())) {
							e = prdCountMap.get(p.getId());
							break;
						}
					}
					if (convertBoxes && !(p.getModel().getMetaname().equals("MP") || p.getModel().getMetaname().equals("OUT") || p.getModel().getMetaname().equals("VAS"))) {
						qty *= p.getFields().parallelStream()
										.filter(pf -> pf.getModel().getMetaname().equals("UNIT_BOX"))
										.mapToInt(pf -> pf.getValue() == null || pf.getValue().isEmpty() ? 1 : Integer.parseInt(pf.getValue()))
										.findAny()
										.orElse(1);
					}

					if (p.getParent() != null)
						hasKit = true;
					prdCountMap.put(p.getId(), e + qty);
					logger.info("Product " + p.getName() + " Loop: " + e + " Quantidade: " + qty + " Total: " + prdCountMap.get(p.getId()));
				}

				if (hasKit) {//Fcking KITS
					List<UUID> kitIds = new ArrayList<>();
					Set<UUID> children = new HashSet<>();
					for (UUID id : prdCountMap.keySet()) {
						Product prd = getISvc().getRegSvc().getPrdSvc().findById(id);

						if (prd.getParent() != null) {
							Optional<ProductField> optPF = prd.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("KIT_QUANTITY")).findFirst();

							if (optPF.isPresent()) {
								if (!kitIds.contains(id)) {
									Double qty = prdCountMap.get(id);

									try {
										qty = decForm.parse(String.valueOf(qty / Double.parseDouble(optPF.get().getValue().replace(",", ".")))).doubleValue();
										prdCountMap.put(prd.getParent().getId(), qty);
										kitIds.add(id);
										logger.info("KIT: " + prd.getParent().getName() + " ===> " + qty);
									} catch (ParseException pe) {
										logger.error(pe.getLocalizedMessage());
										logger.trace(pe.getLocalizedMessage(), pe);
									}
								}
								children.add(id);
							} else
								logger.warn("Child without Conversion: " + prd.getSku());
						}
					}
					for (UUID id : children) {
						prdCountMap.remove(id);
					}
				}
				if (prdCountMap.isEmpty())
					throw new RuntimeException("No Product to send?");
				for (int i = 0; i < dNFs.size() && !prdCountMap.isEmpty(); i++) {
					Document dNF = dNFs.get(i);
					logger.info("Loop NF " + i);
					LinkedList<LinkedHashMap<String, Object>> ret = new LinkedList<LinkedHashMap<String, Object>>();
					final String srnf = dNF.getFields().stream().filter(df -> df.getField().getMetaname().equals("SERIE_NF")).findFirst().get().getValue();
					final Date dtnf = parseDate(dNF.getFields().stream().filter(df -> df.getField().getMetaname().equals("DATA_NF")).findFirst().get().getValue());
					final Map<UUID, List<DocumentItem>> itemNFSMap = dNF.getItems().stream().collect(Collectors.groupingBy(di -> di.getProduct().getId()));
					logger.info("Item NF Map Keys: " + itemNFSMap.keySet().size());

					for (UUID pId : itemNFSMap.keySet()) {
						List<DocumentItem> diList = itemNFSMap.get(pId);
						logger.info("DocumentItem Size: " + diList.size());
						Product p = diList.stream().map(di -> di.getProduct()).findFirst().get();
						Double qty = diList.stream().filter(di -> di.getProduct().getId().equals(pId)).mapToDouble(di -> di.getQty()).sum();
						String orderNumber = diList.stream().filter(di -> di.getProduct().getId().equals(pId)).findFirst().get().getProperties().get("DOC_COMPRAS");

						if (prdCountMap.containsKey(p.getId())) {
							LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
							String dep = "PA01";
							Double countProductQty = prdCountMap.remove(p.getId());
							logger.info("Count Qty: " + countProductQty);
							double nfQuantity = qty;
							logger.info("NF Qty: " + nfQuantity);
							double leftover = countProductQty == null ? 0 : countProductQty - nfQuantity;
							logger.info("Leftover: " + leftover);
							double toSend = countProductQty;

							if (dNFs.size() > (i + 1)) {//ainda tem NF
								for (int j = i + 1; j < dNFs.size(); j++) {
									if (dNFs.get(j).getItems().stream().anyMatch(futDi -> futDi.getProduct().getId().equals(p.getId()))) {
										toSend = nfQuantity;
										break;
									}
								}
							}
							if (leftover > 0)
								prdCountMap.put(p.getId(), leftover);
							switch (p.getModel().getMetaname()) {
								case "OUT":
								case "VAS":
									dep = "PD01";
									break;
								case "MP":
									dep = "MP01";
									break;
							}
							logger.info("********************************************************************");
							logger.info("CODE: " + dNF.getCode() + " SERIE: " + srnf + " DATA: " + dtnf);
							logger.info("********************************************************************");
							item.put("MANDT", "120");//FIXED
							item.put("NUMERO_NF", String.valueOf(Integer.parseInt(dNF.getCode())));//NUMERO NF
							item.put("SERIE_NF", srnf);//SERIE NF
							item.put("DATA_NF", dtnf);//DATA NF
							item.put("EBELN", orderNumber);//DF da NFENTRADA
							item.put("MATNR", p.getSku());//SKU
							item.put("QTDE_CONTADA", toSend);//QTD CONTADA
							item.put("LGORT", dep);//destino
							item.put("FRBNR", cte);//CTE
							item.put("CENTRO", ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"));//CENTRO
							ret.add(item);
						} else {
							getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.INFO, transport.getCode(), Constants.RFC_CONFERENCIA_CEGA, "Item " + p.getSku() + " da nota fiscal " + dNF.getCode() + " não está presente na conferência " + ordConf.getCode()));
						}
					}

					if (ret.isEmpty()) {
						getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.INFO, transport.getCode(), Constants.RFC_CONFERENCIA_CEGA, "Nenhum item da conferência " + ordConf.getCode() + " presente na nota fiscal " + dNF.getCode()));
					} else {
						sendToSap(ret, transport, dNF, ordConf, retOrdConf);
					}
				}
				return true;
			} else {
				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.WARNING, transport.getCode(), Constants.RFC_CONFERENCIA_CEGA, "Nenhuma conferência preenchida no transporte"));
				return false;
			}
		} else {
			getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.WARNING, transport.getCode(), Constants.RFC_CONFERENCIA_CEGA, "Nenhuma conferência existente no transporte"));
			return false;
		}
	}

	private void sendToSap(LinkedList<LinkedHashMap<String, Object>> ret, Document transport, Document dNF, Document ordConf, Document retOrdConf) {
		Executors.newSingleThreadExecutor().execute(() -> {
			String tknum = Documents.getStringField(dNF, dNF.getModel().getMetaname().equals("NFSAIDA") ? "TICKET" : "TRANSPORTE_SAP");
			String logCode = transport.getCode();
			boolean hasErrors = false;

			if (!tknum.isEmpty())
				logCode += "(" + tknum + ")";

			if (ConfigUtil.get("hunter-custom-solar", "integration_sap_enabled", "true").equalsIgnoreCase("true")) {

				retOrdConf.setStatus("ENVIANDO SAP");
				ordConf.setStatus("ENVIANDO SAP");
				getISvc().getRegSvc().getDcSvc().persist(ordConf);
				JCoFunction func = getSolar().getFunc(Constants.RFC_CONFERENCIA_CEGA);

				if (func != null) {
					ReadFieldsSap readFieldsSap = null;
					ToJsonSAP jcoSonStart = new ToJsonSAP(func);
					DocumentModelField dmfMIGO = ordConf.getModel().getFields().stream().filter(dmf -> dmf.getMetaname().equalsIgnoreCase("DOC_MIGO")).findFirst().get();
					Optional<DocumentField> optDF = ordConf.getFields().stream().filter(df -> df.getField().getId().equals(dmfMIGO.getId())).findFirst();
					DocumentField df = optDF.isPresent() ? optDF.get() : new DocumentField(ordConf, dmfMIGO, "NOVO", "");
					Pattern regPattern = Pattern.compile("NUMERO REGISTRO: (\\d+)");

					jcoSonStart.setTableParameter(Constants.TBL_CONFERENCIA_CEGA, ret);
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
						getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, logCode, Constants.RFC_CONFERENCIA_CEGA, e.getCause().getLocalizedMessage()));
					}

					logJcoError(readFieldsSap, logCode, Constants.RFC_CONFERENCIA_CEGA);

					for (int i = 0; i < readFieldsSap.getConfCegaDTOs().size(); i++) {
						SAPConfCegaMsgDTO msg = readFieldsSap.getConfCegaDTOs().get(i);
						AlertSeverity sev = AlertSeverity.INFO;
						Matcher m = regPattern.matcher(msg.getMensagem());

						if (m.matches()) {
							df.setValue(m.group(1));
							getISvc().getRegSvc().getDfSvc().persist(df);
							logger.info("<=============== REGISTRO: " + m.group(1) + " ======================>");
						}
						switch (msg.getTipo()) {
							case "E":
								sev = AlertSeverity.ERROR;
								hasErrors = true;
								break;
							case "W":
								sev = AlertSeverity.WARNING;
								break;
							default:
								sev = AlertSeverity.INFO;
								break;
						}
						getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, logCode, Constants.RFC_CONFERENCIA_CEGA, String.valueOf(i) + " - " + msg.getMensagem()));
					}
				} else
					getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, logCode, Constants.RFC_CONFERENCIA_CEGA, "SAP UNAVAILABLE"));

			} else {
				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.WARNING, logCode, Constants.RFC_CONFERENCIA_CEGA, "SAP INTEGRATION DISABLED"));
			}

			if (hasErrors) {
				ordConf.setStatus("FALHA SAP");
				retOrdConf.setStatus("FALHA SAP");
			} else {
				dNF.setStatus("RECEBIDO");
				ordConf.setStatus("CONFERIDO");
				retOrdConf.setStatus("SUCESSO");
				getISvc().getRegSvc().getDcSvc().persist(dNF);
			}
			getISvc().getRegSvc().getDcSvc().persist(ordConf);
		});
	}

	private Date parseDate(String d) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(d);
		} catch (ParseException e) {
			logger.error(e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
		}
		return null;
	}
}
