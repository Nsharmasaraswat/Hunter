package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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

public class ZHWControleQualidadeWorker extends BaseWorker {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ZHWControleQualidadeWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		return true;
	}

	@Override
	public boolean external(Object obj) {
		logger.info("================================================================" + Constants.RFC_QUALIDADE + "===========================================================================");
		Document d = (Document) obj;
		AtomicInteger cnt = new AtomicInteger(0);
		LinkedList<LinkedHashMap<String, Object>> ret = new LinkedList<LinkedHashMap<String, Object>>();
		LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();

		logger.info("********************************************************************");
		logger.info("CODE: " + d.getCode());
		logger.info("********************************************************************");

		for (DocumentItem di : d.getItems()) {
			item.put("MANDT", 120);
			item.put("PLANT", ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"));
			item.put("CODE", 5);
			item.put("IDENT", "");
			item.put("CONTROLE", d.getCode() + cnt.getAndIncrement());
			item.put("REF_DOC_NO", d.getCode() + cnt.getAndIncrement());
			item.put("PSTNG_DATE", Calendar.getInstance().getTime());
			item.put("DOC_DAT", Calendar.getInstance().getTime());
			item.put("MATERIAL", di.getProduct().getSku());
			//O tipo de movimento para bloqueio é 344 e para desbloqueio é o 343.
			item.put("MOVE_TYPE", "TR");
			item.put("MOVE_COD", 311);
			item.put("BATCH", "");
			item.put("STGE_LOC", "PA01");
			item.put("MOVE_STLOC", "PA04");
			item.put("ENTRY_QNT", di.getQty());
			item.put("UNID_MED", di.getMeasureUnit());
			item.put("MOVE_PLANT", ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"));
			item.put("MOVE_BATCH", "");
			item.put("TIPO_NRHUNTER", d.getCode());
			item.put("ANO", Calendar.getInstance().get(Calendar.YEAR));
			item.put("DOCUMENTO", d.getCode());
		}
		ret.add(item);
		sendToSap(ret, d);
		return true;
	}

	private void sendToSap(LinkedList<LinkedHashMap<String, Object>> ret, Document d) {
		Executors.newSingleThreadExecutor().execute(() -> {
			boolean hasErrors = false;
			if (ConfigUtil.get("hunter-custom-solar", "integration_sap_enabled", "true").equalsIgnoreCase("true")) {
				ToJsonSAP jcoSonStart = new ToJsonSAP(getSolar().getFunc(Constants.RFC_QUALIDADE));

				jcoSonStart.setTableParameter(Constants.TBL_QUALIDADE, ret);

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
					getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, d.getCode(), Constants.RFC_QUALIDADE, e.getCause().getLocalizedMessage()));
				}

				logJcoError(readFieldsSap, d.getCode(), Constants.RFC_QUALIDADE);

				for (int i = 0; i < readFieldsSap.getReturnDTOs().size(); i++) {
					SAPReturnDTO msg = readFieldsSap.getReturnDTOs().get(i);
					AlertSeverity sev = AlertSeverity.INFO;

					if (msg.getTipo().equals("E")) {
						sev = AlertSeverity.ERROR;
						hasErrors = true;
					} else if (msg.getTipo().equals("W"))
						sev = AlertSeverity.WARNING;
					getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, d.getCode(), Constants.RFC_QUALIDADE, i + " - " + msg.getMensagem()));
				}
			} else
				logger.warn("SAP Integration disabled in config file");

			if (hasErrors) {
			} else {
			}
		});
	}
}
