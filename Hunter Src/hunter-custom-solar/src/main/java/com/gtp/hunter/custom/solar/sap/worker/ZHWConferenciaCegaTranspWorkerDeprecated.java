package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
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
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.Product;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;

@Deprecated
public class ZHWConferenciaCegaTranspWorkerDeprecated extends BaseWorker {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ZHWConferenciaCegaTranspWorkerDeprecated(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		return false;
	}

	@Override
	public boolean external(Object obj) {
		logger.info("================================================================" + Constants.RFC_CONFCEGATRANSP + "===========================================================================");
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
		DecimalFormat decForm = new DecimalFormat("0.0000", symbols);
		decForm.setRoundingMode(RoundingMode.FLOOR);
		Document transport = (Document) obj;
		logger.info("Document: " + transport.getCode());
		List<Document> ordConfs = transport.getSiblings().stream().filter(d -> d.getModel().getMetaname().equals("ORDCONF")).collect(Collectors.toList());
		for (Document ordConf : ordConfs) {
			logger.info("Checking: " + ordConf.getCode());
			List<Document> dNFs = new ArrayList<>(transport.getSiblings().stream().filter(d -> d.getModel().getMetaname().equals("NFENTRADA") || d.getModel().getMetaname().equals("NFSAIDA")).collect(Collectors.toSet()));
			logger.info("NFs " + dNFs.size());
			Optional<Document> optRetordconf = ordConf.getSiblings().stream().filter(d -> d.getModel().getMetaname().equals("RETORDCONF") && d.getStatus().equals("SUCESSO")).findAny();

			if (optRetordconf.isPresent()) {
				Document retOrdConf = optRetordconf.get();
				logger.info("Checking Return: " + retOrdConf.getCode());
				Map<UUID, Double> prdCountMap = retOrdConf.getItems()
								.parallelStream()
								.collect(Collectors.groupingBy(di -> di.getProduct().getId(), Collectors.summingDouble(di -> {
									int boxUnit = di.getProduct().getFields().parallelStream()
													.filter(pf -> pf.getModel().getMetaname().equals("UNIT_BOX"))
													.mapToInt(pf -> pf.getValue().isEmpty() ? 1 : Integer.parseInt(pf.getValue()))
													.sum();
									return di.getQty() * boxUnit;
								})));

				if (prdCountMap.isEmpty())
					throw new RuntimeException("No Product to send?");
				for (int i = 0; i < dNFs.size() && !prdCountMap.isEmpty(); i++) {
					Document dNF = dNFs.get(i);
					logger.info("Loop NF " + i);
					LinkedList<LinkedHashMap<String, Object>> ret = new LinkedList<LinkedHashMap<String, Object>>();
					final Map<UUID, List<DocumentItem>> itemNFSMap = dNF.getItems().stream().collect(Collectors.groupingBy(di -> di.getProduct().getId()));
					logger.info("Item NF Map Keys: " + itemNFSMap.keySet().size());
					String tknum = dNF.getFields().parallelStream()
									.filter(df -> df.getField().getMetaname().equals("TICKET") || df.getField().getMetaname().equals("TRANSPORTE_SAP"))
									.map(df -> String.join("", Collections.nCopies(TKNUM_LENGTH - df.getValue().length(), "0")) + df.getValue())
									.findAny()
									.orElse("");

					for (UUID pId : itemNFSMap.keySet()) {
						List<DocumentItem> diList = itemNFSMap.get(pId);
						logger.info("DocumentItem Size: " + diList.size());
						Product p = diList.stream().map(di -> di.getProduct()).findFirst().get();
						Double qty = diList.stream().filter(di -> di.getProduct().getId().equals(pId)).mapToDouble(di -> di.getQty()).sum();

						if (prdCountMap.containsKey(p.getId())) {
							LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
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
							logger.info("********************************************************************");
							logger.info("CODE: " + dNF.getCode() + " TRANSPORTE: " + tknum + " QTD: " + decForm.format(toSend));
							logger.info("********************************************************************");
							item.put("MANDT", "120");//FIXED
							item.put("TKNUM", tknum);//TRANSPORTE SAP
							item.put("MATNR", p.getSku());//SKU
							item.put("QTDE_CONTADA", toSend);//QTD CONTADA
							ret.add(item);
						} else {
							getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.INFO, transport.getCode(), Constants.RFC_CONFCEGATRANSP, "Item " + p.getSku() + " da nota fiscal " + dNF.getCode() + " não está presente na conferência"));
						}
					}

					if (ret.isEmpty()) {
						getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.INFO, transport.getCode(), Constants.RFC_CONFCEGATRANSP, "Nenhum item da conferência " + ordConf.getCode() + " presente na nota fiscal "));
					} else {
						sendToSap(ret, transport, dNF, ordConf, retOrdConf);
					}
				}
			} else
				throw new RuntimeException("No RetOrdConf found");
			return true;
		}
		return false;
	}

	private void sendToSap(LinkedList<LinkedHashMap<String, Object>> ret, Document transport, Document dNF, Document ordConf, Document retOrdConf) {
		Executors.newSingleThreadExecutor().execute(() -> {
			boolean hasErrors = false;

			if (ConfigUtil.get("hunter-custom-solar", "integration_sap_enabled", "true").equalsIgnoreCase("true")) {
				retOrdConf.setStatus("ENVIANDO SAP");
				ordConf.setStatus("ENVIANDO SAP");
				getISvc().getRegSvc().getDcSvc().persist(ordConf);
				JCoFunction func = getSolar().getFunc(Constants.RFC_CONFCEGATRANSP);

				if (func != null) {
					ReadFieldsSap readFieldsSap = null;
					ToJsonSAP jcoSonStart = new ToJsonSAP(func);

					logger.info(func.getTableParameterList().getTable(Constants.TBL_CONFERENCIA_CEGA_TRANSP).toString());
					jcoSonStart.setTableParameter(Constants.TBL_CONFERENCIA_CEGA_TRANSP, ret);
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
						getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, transport.getCode(), Constants.RFC_CONFCEGATRANSP, e.getCause().getLocalizedMessage()));
					}

					for (int i = 0; i < readFieldsSap.getConfCegaDTOs().size(); i++) {
						SAPConfCegaMsgDTO msg = readFieldsSap.getConfCegaDTOs().get(i);
						AlertSeverity sev = AlertSeverity.INFO;
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
						getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, transport.getCode(), Constants.RFC_CONFCEGATRANSP, String.valueOf(i) + " - " + msg.getMensagem()));
					}
				} else
					getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, transport.getCode(), Constants.RFC_CONFCEGATRANSP, "SAP UNAVAILABLE"));

			} else {
				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.WARNING, transport.getCode(), Constants.RFC_CONFCEGATRANSP, "SAP INTEGRATION DISABLED"));
			}

			if (hasErrors) {
				ordConf.setStatus("FALHA SAP");
				retOrdConf.setStatus("FALHA SAP");
			} else {
				dNF.setStatus(dNF.getModel().getMetaname().equals("NFSAIDA") ? "CONCLUIDO" : "RECEBIDO");
				ordConf.setStatus("CONFERIDO");
				retOrdConf.setStatus("SUCESSO");
				getISvc().getRegSvc().getDcSvc().persist(dNF);
			}
			getISvc().getRegSvc().getDcSvc().persist(ordConf);
		});
	}
}
