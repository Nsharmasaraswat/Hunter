package com.gtp.hunter.custom.solar.sap.worker;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;

public class ZHWProdutosWorker extends BaseWorker {

	public ZHWProdutosWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	private static final boolean logimmediately = true;

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		Profiler prof = new Profiler();
		prof.done("Final", logimmediately, true);
		return false;
	}

	@Override
	public boolean external(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}

	public void registerProducts(ReadFieldsSap readFieldsSap) {
		super.registerProducts(readFieldsSap.getProductDTOs());
	}

}
