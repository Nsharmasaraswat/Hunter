package com.gtp.hunter.custom.solar.sap;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;

@Startup
@Singleton
public class SAPSolar {

	private JCoDestination					destination;
	private final String					destName	= "ABAP_AS";
	private transient static final Logger	logger		= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@PostConstruct
	public void init() {
		logger.info("INICIANDO JCO");
		new SAPDestinationProvider(destName, getDestinationProperties());
		try {
			destination = JCoDestinationManager.getDestination(destName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Properties getDestinationProperties() {
		Properties connectProperties = new Properties();

		connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, ConfigUtil.get("hunter-custom-solar", "JCO_ASHOST", "10.62.150.214"));
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, ConfigUtil.get("hunter-custom-solar", "JCO_SYSNR", "00"));
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, ConfigUtil.get("hunter-custom-solar", "JCO_CLIENT", "120"));
		connectProperties.setProperty(DestinationDataProvider.JCO_USER, ConfigUtil.get("hunter-custom-solar", "JCO_USER", "SRVIWM"));
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, ConfigUtil.get("hunter-custom-solar", "JCO_PASSWD", "Solar@4545"));
		connectProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, ConfigUtil.get("hunter-custom-solar", "JCO_POOL_CAPACITY", "10"));
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, ConfigUtil.get("hunter-custom-solar", "JCO_LANG", "pt"));

		return connectProperties;
	}

	public JCoFunction getFunc(String rfc) {
		JCoFunction fnPortal = null;
		try {
			fnPortal = destination.getRepository().getFunction(rfc);
		} catch (JCoException e) {
			e.printStackTrace();
		}
		return fnPortal;
	}

	public Map<String, JCoTable> callSAP(String rfc, Map<String, String> params, LinkedHashMap<String, JCoTable> tblParams) {
		Map<String, JCoTable> ret = new HashMap<String, JCoTable>();

		try {
			JCoFunction fnPortal = getFunc(rfc);
			logger.info("Function: " + fnPortal.toString());
			if ((fnPortal != null) && (fnPortal.getImportParameterList() != null)) {
				fnPortal.getImportParameterList().forEach(f -> {
					logger.info(f.getName() + " - " + f.getDescription());
				});
				for (String k : params.keySet()) {
					logger.info("Parâmetro: " + k + ": " + params.get(k));
					fnPortal.getImportParameterList().setValue(k, params.get(k));
				}
			}

			if (tblParams != null) {
				for (String tbl : tblParams.keySet()) {
					fnPortal.getTableParameterList().setValue(tbl, tblParams.get(tbl));
				}
			}
			fnPortal.execute(destination);
			logger.info("Existem parâmetros de saída? " + (fnPortal.getExportParameterList() != null));
			logger.info("Existem tabelas? " + (fnPortal.getExportParameterList() != null));
			fnPortal.getTableParameterList().forEach(f -> {
				JCoTable tbl = fnPortal.getTableParameterList().getTable(f.getName());
				ret.put(f.getName(), tbl);
			});
		} catch (JCoException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public JCoDestination getDestination() {
		return destination;
	}
}
