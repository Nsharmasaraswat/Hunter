package com.gtp.hunter.process.wf.process.activity.solar;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.BaseProcess;

public class RouteProcessActivity extends TruckAddressBaseActivity {

	private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public RouteProcessActivity(ProcessActivity model, BaseOrigin origin) {
		super(model, origin);
	}

	protected void process(BaseProcess p, Document d, Thing truck, Address addr) {
		if (d.getFields().parallelStream().anyMatch(df -> df.getField().getMetaname().equals("SERVICE_TYPE") && df.getValue().equals("ROTA"))) {
			d.setStatus(statusTo);
			p.getRegSvc().getDcSvc().persist(d);
		}
	}
}