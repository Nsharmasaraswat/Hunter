package com.gtp.hunter.process.wf.process.activity.solar;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.BaseProcess;

public class CheckinProcessActivity extends TruckAddressBaseActivity {

	private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public CheckinProcessActivity(ProcessActivity model, BaseOrigin origin) {
		super(model, origin);
		this.params = JsonUtil.jsonToMap(model.getParam());
	}

	protected void process(BaseProcess p, Document d, Thing truck, Address addr) {
		logger.info(MethodHandles.lookup().lookupClass().getName() + " - " + d.getName() + " - " + truck.getName() + " - " + addr.getMetaname());
		if (ConfigUtil.get("hunter-custom-solar", "checkin-checkout-rota", "FALSE").equalsIgnoreCase("TRUE")) {
			if (d.getFields().parallelStream().anyMatch(df -> df.getField().getMetaname().equals("SERVICE_TYPE") && df.getValue().equals("ROTA"))) {
				DocumentModel dmChkin = p.getRegSvc().getDmSvc().findByMetaname("CHECKINPORTARIA");
				Document dCheckinPortaria = new Document(dmChkin, dmChkin.getName() + " " + d.getCode(), "CHKIN" + d.getCode(), "NOVO");

				dCheckinPortaria.setParent(d);
				d.getSiblings().add(p.getRegSvc().getDcSvc().persist(dCheckinPortaria));
				d.setStatus(statusTo);
				p.getRegSvc().getDcSvc().persist(d);
			}
		}
	}
}