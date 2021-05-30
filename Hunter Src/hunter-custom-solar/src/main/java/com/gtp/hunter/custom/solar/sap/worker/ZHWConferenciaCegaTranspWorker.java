package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
import com.gtp.hunter.process.model.util.Documents;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;

public class ZHWConferenciaCegaTranspWorker extends BaseWorker {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ZHWConferenciaCegaTranspWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		return false;
	}

	@Override
	public boolean external(Object obj) {
		logger.info("================================================================" + Constants.RFC_CONFERENCIA_CEGA + " PA ===========================================================================");
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
		DecimalFormat decForm = new DecimalFormat("0.0000", symbols);
		decForm.setRoundingMode(RoundingMode.FLOOR);
		Document transport = (Document) obj;
		logger.info("Document: " + transport.getCode());
		List<Document> dNFs = new ArrayList<>(transport.getSiblings().stream().filter(d -> d.getModel().getMetaname().equals("NFENTRADA")).collect(Collectors.toSet()));
		logger.info("NFs " + dNFs.size());
		boolean convertBoxes = dNFs.parallelStream().anyMatch(nf -> nf.getPerson() != null && (nf.getPerson().getCode().startsWith("07196033") || nf.getPerson().getCode().startsWith("08715757") || nf.getPerson().getCode().startsWith("10557540")));
		Optional<DocumentField> optCTE = transport.getFields().stream().filter(df -> df.getField().getMetaname().equalsIgnoreCase("CTE")).findAny();
		String cte = optCTE.isPresent() ? optCTE.get().getValue() : "";

		for (int i = 0; i < dNFs.size(); i++) {
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
				LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
				Product p = diList.stream().map(di -> di.getProduct()).findFirst().get();
				Double qty = diList.stream().filter(di -> di.getProduct().getId().equals(pId)).mapToDouble(di -> di.getQty()).sum();
				String orderNumber = diList.stream().filter(di -> di.getProduct().getId().equals(pId)).findFirst().get().getProperties().get("DOC_COMPRAS");
				String dep = "PA01";
				if (convertBoxes && !(p.getModel().getMetaname().equals("MP") || p.getModel().getMetaname().equals("OUT") || p.getModel().getMetaname().equals("VAS"))) {
					qty *= p.getFields().parallelStream()
									.filter(pf -> pf.getModel().getMetaname().equals("UNIT_BOX"))
									.mapToInt(pf -> pf.getValue() == null || pf.getValue().isEmpty() ? 1 : Integer.parseInt(pf.getValue()))
									.findAny()
									.orElse(1);
				}

				double nfQuantity = qty;

				switch (p.getModel().getMetaname()) {
					case "OUT":
					case "VAS":
						dep = "PD01";
						break;
				}

				logger.info(p.getSku() + " - " + p.getName() + " NF Qty: " + nfQuantity);
				logger.info("********************************************************************");
				logger.info("CODE: " + dNF.getCode() + " SERIE: " + srnf + " DATA: " + dtnf);
				logger.info("********************************************************************");
				item.put("MANDT", "120");//FIXED
				item.put("NUMERO_NF", String.valueOf(Integer.parseInt(dNF.getCode())));//NUMERO NF
				item.put("SERIE_NF", srnf);//SERIE NF
				item.put("DATA_NF", dtnf);//DATA NF
				item.put("EBELN", orderNumber);//DF da NFENTRADA
				item.put("MATNR", p.getSku());//SKU
				item.put("QTDE_CONTADA", qty);//QTD CONTADA
				item.put("LGORT", dep);//destino
				item.put("FRBNR", cte);//CTE
				item.put("CENTRO", ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"));//CENTRO
				ret.add(item);
			}
			if (ret.isEmpty()) {
				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.INFO, transport.getCode(), Constants.RFC_CONFERENCIA_CEGA, "Nenhum item presente na nota fiscal " + dNF.getCode()));
			} else {
				sendToSap(ret, transport, dNF);
			}
		}
		return true;
	}

	private void sendToSap(LinkedList<LinkedHashMap<String, Object>> ret, Document transport, Document dNF) {
		Executors.newSingleThreadExecutor().execute(() -> {
			String tknum = Documents.getStringField(dNF, "TRANSPORTE_SAP");
			String logCode = transport.getCode();
			boolean hasErrors = false;

			if (!tknum.isEmpty())
				logCode += "(" + tknum + ")";

			if (ConfigUtil.get("hunter-custom-solar", "integration_sap_enabled", "true").equalsIgnoreCase("true")) {
				JCoFunction func = getSolar().getFunc(Constants.RFC_CONFERENCIA_CEGA);

				if (func != null) {
					ReadFieldsSap readFieldsSap = null;
					ToJsonSAP jcoSonStart = new ToJsonSAP(func);
					DocumentModelField dmfMIGO = dNF.getModel().getFields().stream().filter(dmf -> dmf.getMetaname().equalsIgnoreCase("DOC_MIGO")).findAny().get();
					Optional<DocumentField> optDF = dNF.getFields().parallelStream().filter(df -> df.getField().getId().equals(dmfMIGO.getId())).findAny();
					DocumentField df = optDF.isPresent() ? optDF.get() : new DocumentField(dNF, dmfMIGO, "NOVO", "");
					//					Pattern migoPattern = Pattern.compile("Migo Atualizado Com Sucesso, Documento: ([0-9]+)  pedido: ([0-9]+) -");
					//					Pattern mb0aPattern = Pattern.compile("MB0A Gerado Com Sucesso: ([0-9]+)\\s+Pedido: ([0-9]+)");
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
				dNF.setStatus("FALHA SAP");
			} else {
				dNF.setStatus("RECEBIDO");
			}
			getISvc().getRegSvc().getDcSvc().persist(dNF);
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
