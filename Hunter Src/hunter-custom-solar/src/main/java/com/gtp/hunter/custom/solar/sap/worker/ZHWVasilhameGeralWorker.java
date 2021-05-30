package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.custom.solar.util.Constants;
import com.gtp.hunter.custom.solar.util.ToJsonSAP;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;

public class ZHWVasilhameGeralWorker extends BaseWorker {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ZHWVasilhameGeralWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		logger.info("================================================================" + Constants.RFC_VASILHAME_GERAL + "===========================================================================");
		logger.info("SKU: " + rstart.getControle() + " GERAL: " + getVasilhameGeral(rstart.getControle()));
		return true;
	}

	@Override
	public boolean external(Object obj) {
		logger.info("================================================================" + Constants.RFC_VASILHAME_GERAL + "===========================================================================");
		return false;
	}

	public String getVasilhameGeral(String sku) {
		Profiler prof = new Profiler("VASILHAME GERAL");
		logger.info("================================================================" + Constants.RFC_VASILHAME_GERAL + "===========================================================================");
		JCoFunction func = getSolar().getFunc(Constants.RFC_VASILHAME_GERAL);

		prof.step("GET FUNC", false);
		if (func != null) {
			try {
				ReadFieldsSap readFieldsSap = null;
				ToJsonSAP jcoSonStart = new ToJsonSAP(func);

				readFieldsSap = getGson().fromJson(jcoSonStart.execute(getSolar().getDestination()), ReadFieldsSap.class);
				if (readFieldsSap.getVasilhameGeralDTO() != null) {
					return readFieldsSap.getVasilhameGeralDTO().parallelStream()
									.filter(dto -> String.valueOf(Integer.parseInt(dto.getMatnr_orig())).equals(sku))
									.map(dto -> String.valueOf(Integer.parseInt(dto.getMatnr_switch())))
									.findAny()
									.orElse(null);
				}
			} catch (JCoException je) {
				logger.error(je.getLocalizedMessage(), je);
			}
		} else {
			logger.warn("Função Inexistente: " + Constants.RFC_VASILHAME_GERAL);
		}
		prof.done("Executed", false, true);
		return null;
	}

	public Map<String, String> getVasilhameGeralMap() {
		Profiler prof = new Profiler("VASILHAME GERAL");
		logger.info("================================================================" + Constants.RFC_VASILHAME_GERAL + "===========================================================================");
		JCoFunction func = getSolar().getFunc(Constants.RFC_VASILHAME_GERAL);

		prof.step("GET FUNC", false);
		if (func != null) {
			try {
				ReadFieldsSap readFieldsSap = null;
				ToJsonSAP jcoSonStart = new ToJsonSAP(func);

				readFieldsSap = getGson().fromJson(jcoSonStart.execute(getSolar().getDestination()), ReadFieldsSap.class);

				logJcoError(readFieldsSap, "TODOS", Constants.RFC_VASILHAME_GERAL);

				if (readFieldsSap.getVasilhameGeralDTO() != null) {
					return readFieldsSap.getVasilhameGeralDTO().parallelStream()
									.collect(Collectors.toMap(dto -> String.valueOf(Integer.parseInt(dto.getMatnr_orig())), dto -> String.valueOf(Integer.parseInt(dto.getMatnr_switch()))));
				}
			} catch (JCoException je) {
				logger.error(je.getLocalizedMessage(), je);
			}
		} else {
			logger.warn("Função Inexistente: " + Constants.RFC_VASILHAME_GERAL);
		}
		prof.done("Executed", false, true);
		return new HashMap<>();
	}
}