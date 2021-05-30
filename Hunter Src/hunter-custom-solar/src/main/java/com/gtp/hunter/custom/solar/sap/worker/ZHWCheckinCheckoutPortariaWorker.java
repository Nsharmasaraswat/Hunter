package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.custom.solar.util.Constants;
import com.gtp.hunter.custom.solar.util.ToJsonSAP;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;

public class ZHWCheckinCheckoutPortariaWorker extends BaseWorker {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ZHWCheckinCheckoutPortariaWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		return false;
	}

	@Override
	public boolean external(Object obj) {
		if (ConfigUtil.get("hunter-custom-solar", "checkout-portaria", "FALSE").equalsIgnoreCase("TRUE")) {
			logger.info("================================================================" + Constants.RFC_CHECKINOUT_PORTARIA + "===========================================================================");
			LinkedList<LinkedHashMap<String, Object>> ret = new LinkedList<LinkedHashMap<String, Object>>();
			Document transport = (Document) obj;
			logger.info("Document: " + transport.getCode());
			boolean rota = transport.getFields().parallelStream().anyMatch(df -> df.getField().getMetaname().equals("SERVICE_TYPE") && df.getValue().equals("ROTA"));
			String funcao = transport.getStatus().equals("CAMINHAO NA PORTARIA") ? "E" : "S";
			Document chk = transport.getSiblings().parallelStream()
							.filter(ds -> ds.getModel().getMetaname().equals("APOCHECKSAIDA") || ds.getModel().getMetaname().equals(funcao.equals("E") ? "CHECKINPORTARIA" : "CHECKOUTPORTARIA"))
							.findAny()
							.get();
			int carrinhos = chk.getFields().parallelStream()
							.filter(df -> df.getField().getMetaname().equals("ATTCARRINHO") && df.getValue() != null && !df.getValue().isEmpty())
							.mapToInt(df -> Integer.valueOf(df.getValue()))
							.findAny()
							.orElse(0);
			int km = chk.getFields().parallelStream().filter(df -> df.getField().getMetaname().equals("ATTKILOMETRAGEM") && df.getValue() != null && !df.getValue().isEmpty())
							.mapToInt(df -> Integer.valueOf(df.getValue()))
							.findAny()
							.orElse(0);
			int cones = chk.getFields().parallelStream().filter(df -> df.getField().getMetaname().equals("ATTCONE") && df.getValue() != null && !df.getValue().isEmpty())
							.mapToInt(df -> Integer.valueOf(df.getValue()))
							.findAny()
							.orElse(0);
			String obs = chk.getFields().parallelStream().filter(df -> df.getField().getMetaname().equals("ATTOBSERVACAO"))
							.map(df -> df.getValue())
							.findAny()
							.orElse("");
			boolean itensSegur = chk.getFields().parallelStream().anyMatch(df -> df.getField().getMetaname().equals("ATTITENSEGUR") && df.getValue() != null && df.getValue().equalsIgnoreCase("true"));
			boolean extintor = chk.getFields().parallelStream().anyMatch(df -> df.getField().getMetaname().equals("ATTEXTINTOR") && df.getValue() != null && df.getValue().equalsIgnoreCase("true"));
			List<String> tkNumList = rota ? Arrays.asList(transport.getCode().replace("R", "000")) : transport.getSiblings().parallelStream()
							.flatMap(ds -> ds.getFields().parallelStream())
							.filter(df -> funcao.equals("S") ? df.getField().getMetaname().equals("TICKET") : df.getField().getMetaname().equals("TRANSPORTE_SAP"))
							.map(df -> String.join("", Collections.nCopies(10 - df.getValue().length(), "0")) + df.getValue())
							.distinct()
							.collect(Collectors.toList());
			for (String tkNum : tkNumList) {
				LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();

				item.put("MANDT", "120");
				item.put("TKNUM", tkNum);
				item.put("SAIENT", funcao);
				item.put("CARRINHOS", carrinhos);
				item.put("CONES", cones);
				item.put("KMSAIENT", km);
				item.put("OBSERVACAO", obs);
				item.put("ITENSEGUR", itensSegur ? "X" : "");
				item.put("EXTINTOR", extintor ? "X" : "");
				ret.add(item);
				logger.info("Transporte: " + tkNum + " " + funcao + " Carrinhos: " + carrinhos + " Cones: " + cones + " KM: " + km + " Obs: " + obs + " Itens: " + (itensSegur ? "X" : "") + " Extintor: " + (extintor ? "X" : ""));
				Executors.newSingleThreadExecutor().execute(() -> {
					if (ConfigUtil.get("hunter-custom-solar", "integration_sap_enabled", "true").equalsIgnoreCase("true")) {
						JCoFunction func = getSolar().getFunc(Constants.RFC_CHECKINOUT_PORTARIA);

						if (func != null) {
							ReadFieldsSap readFieldsSap = null;
							ToJsonSAP jcoSonStart = new ToJsonSAP(func);

							logger.trace(func.toString());
							logger.trace(func.getTableParameterList().getTable(Constants.TBL_CHECKINOUT_PORTARIA).toString());
							jcoSonStart.setTableParameter(Constants.TBL_CHECKINOUT_PORTARIA, ret);
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
								getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, transport.getCode() + " (" + tkNum + ")", Constants.RFC_CHECKINOUT_PORTARIA, e.getCause().getLocalizedMessage()));
							}

							logJcoError(readFieldsSap, transport.getCode() + " (" + tkNum + ")", Constants.RFC_CHECKINOUT_PORTARIA);

							String msg = readFieldsSap.geteMensagem();
							AlertSeverity sev = AlertSeverity.INFO;

							switch (readFieldsSap.geteRetorno()) {
								case "":
								case "E":
									sev = AlertSeverity.ERROR;
									break;
								case "W":
									sev = AlertSeverity.WARNING;
									break;
							}
							getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, transport.getCode() + " (" + tkNum + ")", Constants.RFC_CHECKINOUT_PORTARIA, msg));
						} else
							getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, transport.getCode() + " (" + tkNum + ")", Constants.RFC_CHECKINOUT_PORTARIA, "SAP UNAVAILABLE"));

					} else {
						getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.WARNING, transport.getCode() + " (" + tkNum + ")", Constants.RFC_CHECKINOUT_PORTARIA, "SAP INTEGRATION DISABLED"));
					}
				});
			}
		}
		return true;
	}
}
