package com.gtp.hunter.process.wf.process.activity.solar;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.BaseProcess;

public class CheckoutProcessActivity extends TruckAddressBaseActivity {

	private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public CheckoutProcessActivity(ProcessActivity model, BaseOrigin origin) {
		super(model, origin);
	}

	protected void process(BaseProcess p, Document d, Thing truck, Address addr) {
		String serviceType = Documents.getStringField(d, "SERVICE_TYPE", "TERCEIRO");
		String THING_STATUS = (String) params.get("thing-status");
		String checkMeta = null;
		DocumentModel dm = p.getRegSvc().getDmSvc().findByMetaname("APOLIBERACAO");

		switch (serviceType) {
			case "TERCEIRO":
				checkMeta = "APOCHECKSAIDA";
				break;
			case "ROTA":
				List<Thing> things = d.getThings().stream()
								.map(dt -> dt.getThing())
								.filter(dtt -> !dtt.getProduct().getModel().getMetaname().equals("TRUCK") && !dtt.getProduct().getModel().getMetaname().equals("FORKLIFT"))
								.collect(Collectors.toList());

				for (Thing t : things) {
					t.setStatus(THING_STATUS);
					t.setAddress(null);
					for (Thing ts : t.getSiblings()) {
						ts.setStatus(THING_STATUS);
						ts.setAddress(null);
						p.getRegSvc().getWmsSvc().updateThingStatus(ts.getId(), THING_STATUS);
					}
					p.getRegSvc().getWmsSvc().updateThingStatus(t.getId(), THING_STATUS);
					p.getRegSvc().getWmsSvc().deleteStkThing(t.getId().toString());
				}
				for (Document ds : d.getSiblings()) {
					if (ds.getStatus().equals("ROMANEIO")) {
						ds.setStatus("CANCELADO");
						ds.getSiblings().forEach(dss -> dss.setStatus("CANCELADO"));
					}
				}
				p.getRegSvc().getThSvc().multiPersist(things);
				if (ConfigUtil.get("hunter-custom-solar", "checkout-portaria", "FALSE").equalsIgnoreCase("TRUE")) {
					checkMeta = "CHECKOUTPORTARIA";
				}
				break;
			default:
				logger.error("Service Type not implemented: " + serviceType);
		}
		if (!d.getSiblings().parallelStream().anyMatch(ds -> ds.getModel().getId().equals(dm.getId()))) {
			Document apo = new Document(dm, dm.getName() + " " + d.getCode(), "LIB" + d.getCode(), "NOVO");

			apo.setParent(d);
			d.getSiblings().add(p.getRegSvc().getDcSvc().persist(apo));
		}
		if (checkMeta != null) {
			DocumentModel dmChk = p.getRegSvc().getDmSvc().findByMetaname(checkMeta);

			if (!d.getSiblings().parallelStream().anyMatch(ds -> ds.getModel().getId().equals(dmChk.getId()))) {
				Document apochk = new Document(dmChk, dmChk.getName() + " " + d.getCode(), "CHKEXIT" + d.getCode(), "NOVO");

				apochk.setParent(d);
				d.getSiblings().add(p.getRegSvc().getDcSvc().persist(apochk));
			}
		}
		d.setStatus(statusTo);
		p.getRegSvc().getDcSvc().persist(d);
	}
}