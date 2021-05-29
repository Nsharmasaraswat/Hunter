package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.Locale;

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
import com.gtp.hunter.process.model.util.Documents;
import com.sap.conn.jco.JCoException;

public class ZHWApontamentoProducao extends BaseWorker {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ZHWApontamentoProducao(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		return false;
	}

	@Override
	public boolean external(Object obj) {
		logger.info("================================================================" + Constants.RFC_PSC + "===========================================================================");
		Document apoprd = (Document) obj;
		Document op = apoprd.getParent();
		boolean lineEnabled = ConfigUtil.get("hunter-custom-solar", "auto-prd-lines", "").toUpperCase().contains(Documents.getStringField(op, "LINHA_PROD").toUpperCase());

		if (op != null && op.getModel().getMetaname().equals("ORDPROD") && lineEnabled) {
			ReadFieldsSap readFieldsSap = null;
			DecimalFormat DF = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.US));
			LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
			ToJsonSAP jcoSonStart = new ToJsonSAP(getSolar().getFunc(Constants.RFC_PSC));
			DocumentItem di = apoprd.getItems().parallelStream().findAny().get();
			double qty = di.getQty();

			item.put(Constants.I_PLANT, ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"));
			item.put(Constants.I_ORDER_NUMBER, op.getCode().replace("OP", ""));
			item.put(Constants.I_QTD_PRODUCED, DF.format(qty));
			jcoSonStart.setParameters(item);
			logger.info("PSC Product: " + di.getProduct().getSku() + " - " + di.getProduct().getName() + " Quantity: " + DF.format(qty));
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
				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, op.getCode(), Constants.RFC_PSC, e.getCause().getLocalizedMessage()));
			}

			logJcoError(readFieldsSap, op.getCode(), Constants.RFC_PSC);

			for (SAPReturnDTO msg : readFieldsSap.getReturnDTOs()) {
				AlertSeverity sev = AlertSeverity.INFO;

				if (msg.getType() == null)
					msg.setType("E");

				if (msg.getType().equals("E")) {
					sev = AlertSeverity.ERROR;
				} else if (msg.getType().equals("W"))
					sev = AlertSeverity.WARNING;
				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, op.getCode() + " - " + Documents.getStringField(op, "LINHA_PROD"), Constants.RFC_PSC, msg.getMessage()));
			}
			return true;
		} else
			logger.info(Documents.getStringField(op, "LINHA_PROD") + " Não habilitada para lançamento");
		return false;
	}
}
