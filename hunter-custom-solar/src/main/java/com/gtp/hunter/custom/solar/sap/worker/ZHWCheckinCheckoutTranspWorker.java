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
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.Product;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;

public class ZHWCheckinCheckoutTranspWorker extends BaseWorker {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final ExecutorService			exec	= Executors.newCachedThreadPool();
	private boolean							checkin;

	public ZHWCheckinCheckoutTranspWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		return false;
	}

	@Override
	public boolean external(Object obj) {
		logger.info("================================================================" + Constants.RFC_CHECKINOUT + "===========================================================================");
		List<Future<IntegrationReturn>> futureList = new ArrayList<>();
		Document transport = (Document) obj;
		logger.info("Document: " + transport.getCode());
		boolean confOutCompleted = transport.getSiblings().stream()
						.anyMatch(ds -> ds.getModel().getMetaname().equals("ORDCONF") && ds.getFields().parallelStream()
										.anyMatch(df -> df.getField().getMetaname().equals("CONF_TYPE") && df.getValue().equalsIgnoreCase("SPAPD")) && ds.getStatus().equals("SUCESSO"));
		boolean confInCompleted = transport.getSiblings().stream()
						.anyMatch(ds -> ds.getModel().getMetaname().equals("ORDCONF") && ds.getFields().parallelStream()
										.anyMatch(df -> df.getField().getMetaname().equals("CONF_TYPE") && df.getValue().equalsIgnoreCase("EPAPD")) && ds.getStatus().equals("SUCESSO"));
		String lacres = transport.getSiblings().parallelStream()
						.filter(ds -> ds.getModel().getMetaname().equals("APOLACRE"))
						.flatMap(ds -> ds.getFields().parallelStream().filter(df -> !df.getValue().isEmpty()))
						.map(df -> df.getValue())
						.distinct()
						.collect(Collectors.joining(","));

		logger.info(transport.getCode() + " - ConfOutCompleted: " + confOutCompleted + " ConfInCompleted: " + confInCompleted + " Lacres: " + lacres + " Checkin: " + checkin);
		if (confOutCompleted) {
			List<String> tkNumOut = transport.getSiblings().parallelStream()
							.flatMap(ds -> ds.getFields().parallelStream())
							.filter(df -> df.getField().getMetaname().equals("TICKET") && !df.getValue().isEmpty())
							.map(df -> String.join("", Collections.nCopies(TKNUM_LENGTH - df.getValue().length(), "0")) + df.getValue())
							.distinct()
							.collect(Collectors.toList());

			logger.info(transport.getCode() + " tkNum Saida: " + tkNumOut.parallelStream().collect(Collectors.joining(",")));
			for (String tkNum : tkNumOut) {
				futureList.add(sendToSap(transport, tkNum, "NFSAIDA", "CHECKOUT", lacres));
			}
		}

		if (confInCompleted && checkin) {
			List<String> tkNumIn = transport.getSiblings().parallelStream()
							.flatMap(ds -> ds.getFields().parallelStream())
							.filter(df -> df.getField().getMetaname().equals("TRANSPORTE_SAP") && !df.getValue().isEmpty())
							.map(df -> String.join("", Collections.nCopies(TKNUM_LENGTH - df.getValue().length(), "0")) + df.getValue())
							.distinct()
							.collect(Collectors.toList());

			logger.info(transport.getCode() + " tkNum Entrada: " + tkNumIn.parallelStream().collect(Collectors.joining(",")));
			for (String tkNum : tkNumIn) {
				futureList.add(sendToSap(transport, tkNum, "NFENTRADA", "CHECKIN", lacres));
			}
		}
		exec.execute(() -> {
			for (Future<IntegrationReturn> f : futureList) {
				try {
					IntegrationReturn ir = f.get();
					if (!ir.isResult())
						logger.error(ir.getMessage());
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		});
		return false;
	}

	public void setCheckin(boolean v) {
		this.checkin = v;
	}

	private Future<IntegrationReturn> sendToSap(Document transport, String tkNum, String nfModel, String funcao, String lacres) {
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
		DecimalFormat decForm = new DecimalFormat("0.0000", symbols);
		LinkedList<LinkedHashMap<String, Object>> ret = new LinkedList<LinkedHashMap<String, Object>>();
		Map<Product, Double> prdMap = transport.getSiblings().parallelStream()
						.filter(ds -> ds.getModel().getMetaname().equals(nfModel) && ds.getFields().parallelStream().anyMatch(df -> tkNum.endsWith(df.getValue())))
						.flatMap(ds -> ds.getItems().parallelStream())
						.collect(Collectors.groupingBy(DocumentItem::getProduct, Collectors.summingDouble((DocumentItem di) -> {
							Product p = di.getProduct();
							int unitBox = p.getFields().parallelStream().filter(pf -> pf.getModel().getMetaname().equals("UNIT_BOX") && !pf.getValue().isEmpty()).mapToInt(pf -> Integer.parseInt(pf.getValue())).findAny().orElse(1);

							if (di.getDocument().getModel().getMetaname().equals("NFSAIDA") || di.getDocument().getPerson().getCode().startsWith("07196033") || di.getDocument().getPerson().getCode().startsWith("08715757") || di.getDocument().getPerson().getCode().startsWith("10557540"))
								return di.getQty() / unitBox;
							else
								return di.getQty();
						})));
		Product pallet = prdMap.keySet().parallelStream().filter(p -> p.getSku().equals("1404020")).findAny().orElse(null);
		Product pbr2 = prdMap.keySet().parallelStream().filter(p -> p.getSku().equals("1404625")).findAny().orElse(null);
		Product eucatex = prdMap.keySet().parallelStream().filter(p -> p.getSku().equals("1207778")).findAny().orElse(null);

		decForm.setRoundingMode(RoundingMode.FLOOR);
		final int palletCnt = pallet != null && prdMap.containsKey(pallet) ? prdMap.remove(pallet).intValue() : 0;
		final int pbr2Cnt = pbr2 != null && prdMap.containsKey(pbr2) ? prdMap.remove(pbr2).intValue() : 0;
		final int eucatexCnt = eucatex != null && prdMap.containsKey(eucatex) ? prdMap.remove(eucatex).intValue() : 0;
		if (ConfigUtil.get("hunter-custom-solar", "integration_sap_enabled", "true").equalsIgnoreCase("true")) {
			for (Entry<Product, Double> en : prdMap.entrySet()) {
				Product p = en.getKey();
				Double qty = en.getValue();
				LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
				logger.info("********************************************************************");
				logger.info("TRANSPORTE: " + tkNum + "PRD: " + p.getSku() + " - " + p.getName() + " QTD: " + decForm.format(qty));
				logger.info("********************************************************************");
				item.put("MANDT", "120");//FIXED
				item.put("FUNCAO", funcao);
				item.put("ID_CONF", 1);
				item.put("TKNUM", tkNum);//TRANSPORTE SAP
				item.put("MATNR", p.getSku());//SKU
				item.put("LFIMG", qty);//QTD CONTADA
				item.put("FINAL", "X");
				ret.add(item);
			}
			if (ret.isEmpty()) {
				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.INFO, transport.getCode() + " (" + tkNum + ")", Constants.RFC_CHECKINOUT, "Nenhum item da conferência presente na(s) nota(s) fisca(is)"));
				return CompletableFuture.completedFuture(new IntegrationReturn(false, "Nenhum item da conferência presente na(s) nota(s) fisca(is)"));
			} else {
				return exec.submit(new Callable<IntegrationReturn>() {
					public IntegrationReturn call() {
						JCoFunction func = getSolar().getFunc(Constants.RFC_CHECKINOUT);
						String errorMsg = "";

						if (func != null) {
							ReadFieldsSap readFieldsSap = null;
							ToJsonSAP jcoSonStart = new ToJsonSAP(func);

							jcoSonStart.setSimpleParameter(Constants.I_EUCATEX, eucatexCnt);
							jcoSonStart.setSimpleParameter(Constants.I_PALLET, palletCnt + pbr2Cnt);
							jcoSonStart.setSimpleParameter(Constants.I_LACRE, lacres);
							jcoSonStart.setTableParameter(Constants.TBL_CHECKINOUT, ret);
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
								getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, transport.getCode() + " (" + tkNum + ")", Constants.RFC_CHECKINOUT, e.getCause().getLocalizedMessage()));
							}

							logJcoError(readFieldsSap, transport.getCode() + " (" + tkNum + ")", Constants.RFC_CHECKINOUT);

							for (int i = 0; i < readFieldsSap.getConfCegaDTOs().size(); i++) {
								SAPConfCegaMsgDTO msg = readFieldsSap.getConfCegaDTOs().get(i);
								AlertSeverity sev = AlertSeverity.INFO;
								switch (msg.getTipo()) {
									case "E":
										sev = AlertSeverity.ERROR;
										errorMsg = msg.getMensagem();
										break;
									case "W":
										sev = AlertSeverity.WARNING;
										break;
									default:
										sev = AlertSeverity.INFO;
										break;
								}
								getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, transport.getCode() + " (" + tkNum + ")", Constants.RFC_CHECKINOUT, String.valueOf(i) + " - " + msg.getMensagem()));
							}
							if (!errorMsg.isEmpty())
								return new IntegrationReturn(false, errorMsg);
							return IntegrationReturn.OK;
						} else
							getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, transport.getCode() + " (" + tkNum + ")", Constants.RFC_CHECKINOUT, "SAP UNAVAILABLE"));
						return new IntegrationReturn(false, "SAP UNAVAILABLE");
					}
				});
			}
		} else {
			getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.WARNING, transport.getCode() + " (" + tkNum + ")", Constants.RFC_CHECKINOUT, "SAP INTEGRATION DISABLED"));
			return CompletableFuture.completedFuture(new IntegrationReturn(false, "SAP INTEGRATION DISABLED"));
		}
	}
}
