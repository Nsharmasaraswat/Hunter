package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.util.StreamUtil;
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

public class ZHWCheckinCheckoutWorker extends BaseWorker {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ZHWCheckinCheckoutWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		return false;
	}

	@Override
	public boolean external(Object obj) {
		logger.info("================================================================" + Constants.RFC_CHECKINOUT + "===========================================================================");
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
		DecimalFormat decForm = new DecimalFormat("0.0000", symbols);
		decForm.setRoundingMode(RoundingMode.FLOOR);
		Document transport = (Document) obj;
		String tkNum = transport.getCode().replace("R", "000");
		logger.info("Document: " + transport.getCode());
		String funcao = transport.getStatus().equals("CAMINHAO NO PATIO") ? "CHECKIN" : "CHECKOUT";
		final List<Document> ordConfs = funcao.equals("CHECKOUT") ? transport.getSiblings().parallelStream()
						.filter(ds -> ds.getModel().getMetaname().equals("PICKING"))
						.flatMap(pck -> pck.getSiblings().parallelStream())
						.filter(ds -> ds.getModel().getMetaname().equals("ORDCONF"))
						.collect(Collectors.toList()) : transport.getSiblings().parallelStream()
										.filter(ds -> ds.getModel().getMetaname().equals("ORDCONF"))
										.collect(Collectors.toList());
		logger.info("Checkings: " + ordConfs.size());
		LinkedList<LinkedHashMap<String, Object>> ret = new LinkedList<LinkedHashMap<String, Object>>();
		Map<Product, Double> prdMap = ordConfs.parallelStream()
						.flatMap(cnf -> cnf.getSiblings().parallelStream()
										.filter(ds -> ds.getModel().getMetaname().equals("RETORDCONF") && ds.getStatus().equals("SUCESSO")))
						.filter(StreamUtil.distinctByKey(Document::getCode))
						.flatMap(cnf -> cnf.getItems().parallelStream())
						.collect(Collectors.groupingBy(DocumentItem::getProduct, Collectors.summingDouble(DocumentItem::getQty)));
		Product pallet = prdMap.keySet().parallelStream().filter(p -> p.getSku().equals("1404020")).findAny().orElse(null);
		Product pbr2 = prdMap.keySet().parallelStream().filter(p -> p.getSku().equals("1404625")).findAny().orElse(null);
		Product eucatex = prdMap.keySet().parallelStream().filter(p -> p.getSku().equals("1207778")).findAny().orElse(null);
		final int palletCnt = pallet != null && prdMap.containsKey(pallet) ? prdMap.remove(pallet).intValue() : 0;
		final int pbr2Cnt = pbr2 != null && prdMap.containsKey(pbr2) ? prdMap.remove(pbr2).intValue() : 0;
		final int eucatexCnt = eucatex != null && prdMap.containsKey(eucatex) ? prdMap.remove(eucatex).intValue() : 0;

		for (Entry<Product, Double> en : prdMap.entrySet()) {
			LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();

			item.put("MANDT", "120");
			item.put("TKNUM", tkNum);
			item.put("FUNCAO", funcao);
			item.put("ID_CONF", 1);
			item.put("MATNR", en.getKey().getSku());
			item.put("LFIMG", decForm.format(en.getValue()));
			item.put("FINAL", "X");
			ret.add(item);
			logger.info("Produto: " + en.getKey().getSku() + " - " + en.getKey().getName() + " QTY: " + decForm.format(en.getValue()));
		}

		Executors.newSingleThreadExecutor().execute(() -> {
			boolean hasErrors = false;

			if (ConfigUtil.get("hunter-custom-solar", "integration_sap_enabled", "true").equalsIgnoreCase("true")) {
				//				retOrdConf.setStatus("ENVIANDO SAP");
				ordConfs.forEach(ordConf -> ordConf.setStatus("ENVIANDO SAP"));
				getISvc().getRegSvc().getDcSvc().multiPersist(ordConfs);
				JCoFunction func = getSolar().getFunc(Constants.RFC_CHECKINOUT);

				if (func != null) {
					ReadFieldsSap readFieldsSap = null;
					ToJsonSAP jcoSonStart = new ToJsonSAP(func);
					String lacres = transport.getSiblings().parallelStream()
									.flatMap(ds -> ds.getFields().parallelStream()
													.filter(df -> df.getField().getMetaname().startsWith("ATTLACRE") && !df.getValue().isEmpty()))
									.map(df -> df.getValue())
									.distinct()
									.sorted()
									.collect(Collectors.joining(","));

					logger.trace(func.toString());
					jcoSonStart.setSimpleParameter(Constants.I_EUCATEX, eucatexCnt);
					jcoSonStart.setSimpleParameter(Constants.I_PALLET, palletCnt + pbr2Cnt);
					jcoSonStart.setSimpleParameter(Constants.I_LACRE, lacres);
					jcoSonStart.setSimpleParameter(Constants.I_CONTROLE, tkNum);
					logger.info("Eucatex: " + eucatexCnt + " Pallets: " + (palletCnt + pbr2Cnt) + " Lacres: " + lacres + " Transporte" + tkNum);
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
						getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, transport.getCode(), Constants.RFC_CHECKINOUT, e.getCause().getLocalizedMessage()));
					}
					
					logJcoError(readFieldsSap, transport.getCode() + " (" + tkNum + ")", Constants.RFC_CHECKINOUT);

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
						getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, transport.getCode(), Constants.RFC_CHECKINOUT, String.valueOf(i) + " - " + msg.getMensagem()));
					}
				} else
					getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, transport.getCode(), Constants.RFC_CHECKINOUT, "SAP UNAVAILABLE"));

			} else {
				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.WARNING, transport.getCode(), Constants.RFC_CHECKINOUT, "SAP INTEGRATION DISABLED"));
			}

			if (hasErrors) {
				ordConfs.forEach(ordConf -> ordConf.setStatus("FALHA SAP"));
				//				retOrdConf.setStatus("FALHA SAP");
			} else {
				//				dNF.setStatus("RECEBIDO");
				ordConfs.forEach(ordConf -> ordConf.setStatus("CONFERIDO"));
				//				retOrdConf.setStatus("SUCESSO");
			}
			getISvc().getRegSvc().getDcSvc().multiPersist(ordConfs);
		});
		return true;
	}
}
