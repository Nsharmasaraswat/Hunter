package com.gtp.hunter.custom.solar.sap.worker;

import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;

public class ZHWControleTableWorker extends BaseWorker {

	public ZHWControleTableWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc,solar, integrationService);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean work(SAPReadStartDTO rstart) {
//		sap.setRfc("Z_HW_READ_CONTROLE");
//		sap.getParams().put("I_CONTROLE", "26181210645075000183550050001201201841846976");
//		return sap.callSAP("Z_HW_READ_CONTROLE",new HashMap<String,String>());
		return false;
	}

	@Override
	public boolean external(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
