package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReturnDTO;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.custom.solar.util.Constants;
import com.gtp.hunter.custom.solar.util.ToJsonSAP;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.sap.conn.jco.JCoException;

public class ZHWTransferenciaWorker extends BaseWorker {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ZHWTransferenciaWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		return false;
	}

	@Override
	public boolean external(Object obj) {
		logger.info("================================================================" + Constants.RFC_TRANSFERENCIA + "===========================================================================");
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
		final DecimalFormat DF = new DecimalFormat("#.0000", symbols);
		Document transf = (Document) obj;
		LinkedList<LinkedHashMap<String, Object>> ret = new LinkedList<LinkedHashMap<String, Object>>();

		for (DocumentItem di : transf.getItems()) {
			LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
			double qty = transf.getThings().stream().map(dt -> dt.getThing())//DocumentThing to Thing
							.filter(t -> t.getProduct().getId().equals(di.getProduct().getId()))//DocumentItem Product
							.flatMap(t -> t.getProperties().stream())//Thing to Property
							.filter(pr -> pr.getField().getMetaname().equals("QUANTITY"))//Filter Quantity Property
							.mapToDouble(pr -> Double.parseDouble(pr.getValue().replace(",", ".")))//Property to double (value)
							.sum();//SUM

			//		item.put("MSEG-WERKS", "CNAT");//CENTRO
			//		item.put("MSEG-MBLBNR", value);//DOCUMENTO DE MATERIAL
			//		item.put("MSEG-MJAHR", value);//ANO
			//		item.put("MSEG-BWART", value);//TIPO DE MOVIMENTO (311/312)
			//		item.put("MSEG-LGORT", value);//DEPOSITO DE ORIGEM
			//		item.put("MSEG-LGORT", value);//DEPOSITO DE DESTINO
			//		item.put("MSEG-MATNR", value);//CODIGO DO PRODUTO (di.p.SKU)
			//		item.put("MSEG-MENGE", value);//QUANTIDADE (di.qty)
			//		item.put("MSEG-MEINS", value);//UNIDADE DE MEDIDA(di.measureUnit)
			//		item.put("MSEG-SHKZG", value);//DEBITO E CRÉDITO (H-Debito / S-Credito)
			logger.info("********************************************************************");
			logger.info("CODE: " + transf.getCode() + " Product: " + di.getProduct().getSku() + " QTY: " + qty);
			logger.info("********************************************************************");
			item.put("MANDT", 120);
			item.put("CODE", 3);
			item.put("IDENT", 0);
			item.put("CENTRO", "CNAT");
			item.put("MATERIAL", di.getProduct().getSku());
			item.put("DOCUMENTO", "");
			item.put("ANO", Calendar.getInstance().get(Calendar.YEAR));
			item.put("TIPOMOV", 311);
			item.put("DEPORIGEM", transf.getFields().stream().filter(df -> df.getField().getMetaname().equals("FROM")).map(df -> df.getValue()).findFirst().get());
			item.put("DEPDESTINO", transf.getFields().stream().filter(df -> df.getField().getMetaname().equals("TO")).map(df -> df.getValue()).findFirst().get());
			item.put("QUANTIDADE", DF.format(qty));
			item.put("UNID_MED", di.getMeasureUnit());
			item.put("DEBCRED", "H");
			item.put("TIPO_NRHUNTER", transf.getCode());
			ret.add(item);
		}
		transf.setStatus("ENVIANDO SAP");
		getISvc().getRegSvc().getDcSvc().persist(transf);
		sendToSAP(ret, transf);
		return true;
	}

	private void sendToSAP(LinkedList<LinkedHashMap<String, Object>> ret, Document transf) {
		Executors.newSingleThreadExecutor().execute(() -> {
			boolean hasErrors = false;

			if (ConfigUtil.get("hunter-custom-solar", "integration_sap_enabled", "true").equalsIgnoreCase("true")) {
				ToJsonSAP jcoSonStart = new ToJsonSAP(getSolar().getFunc(Constants.RFC_TRANSFERENCIA));

				jcoSonStart.setTableParameter(Constants.TBL_TRANSFERENCIA, ret);

				ReadFieldsSap readFieldsSap = null;
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
					getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, transf.getCode(), Constants.RFC_TRANSFERENCIA, e.getCause().getLocalizedMessage()));
				}

				logJcoError(readFieldsSap, transf.getCode(), Constants.RFC_TRANSFERENCIA);

				for (SAPReturnDTO msg : readFieldsSap.getReturnDTOs()) {
					AlertSeverity sev = AlertSeverity.INFO;

					if (msg.getTipo().equals("E")) {
						sev = AlertSeverity.ERROR;
						hasErrors = true;
					} else if (msg.getTipo().equals("W"))
						sev = AlertSeverity.WARNING;
					getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, transf.getCode(), Constants.RFC_TRANSFERENCIA, msg.getSeq() + " - " + msg.getMensagem()));
				}
			} else
				logger.warn("SAP Integration disabled in config file");
			if (hasErrors) {
				String newCode = transf.getCode().replace("TRN", "");
				int pos = newCode.indexOf("-");

				if (pos > -1) {
					int cnt = Integer.parseInt(newCode.substring(pos + 1));
					newCode = newCode.substring(0, pos) + "-" + ++cnt;
				} else {
					newCode = newCode + "-1";
				}
				transf.setCode("TRN" + newCode);
				transf.setStatus("FALHA SAP");
				//				try {
				//					getISvc().getMail().sendmail(new String[] { "mateus@gtpautomation.com" }, new String[] { "mateus@eazycomm.com" }, new String[] { "mateus@gtptecnologia.com" }, "Test Title", "Test Body");
				//				} catch (Exception e) {
				//					logger.error(e.getLocalizedMessage());
				//					logger.trace(e.getLocalizedMessage(), e);
				//				}
			} else {
				transf.setStatus("SEPARADO");
			}
			getISvc().getRegSvc().getDcSvc().persist(transf);
		});
	}
}
