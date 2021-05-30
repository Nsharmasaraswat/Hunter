package com.gtp.hunter.custom.solar.filter.trigger;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.DocumentTransport;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Products;
import com.gtp.hunter.process.model.util.Things;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;

public class WMSRepackTrigger extends BaseTrigger {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private IntegrationService				iSvc;

	public WMSRepackTrigger(IntegrationService iSvc) {
		super(new FilterTrigger());
		this.iSvc = iSvc;
	}

	public WMSRepackTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		Document repack = (Document) mdl.getModel();
		logger.info("Resupply Trigger: " + repack.getCode());
		Prefix pfx = iSvc.getRegSvc().getPfxSvc().findNext("PCK", 9);
		Thing t = repack.getThings().iterator().next().getThing();
		Address orig = t.getAddress();
		DocumentModel dmOrdmov = iSvc.getRegSvc().getDmSvc().findByMetaname("ORDMOV");
		DocumentModelField dmfPriority = dmOrdmov.getFields().stream().filter(dmf -> dmf.getMetaname().equals("PRIORITY")).findAny().get();
		DocumentModelField dmType = dmOrdmov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TYPE")).findAny().get();
		DocumentModelField dmTitle = dmOrdmov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TITLE")).findAny().get();

		Document ordmov = new Document(dmOrdmov, dmOrdmov.getName() + " " + pfx.getCode(), pfx.getPrefix() + pfx.getCode(), "ATIVO");
		Address dest = iSvc.getRegSvc().getAddSvc().findByMetaname("PICKSTAGE01.01");

		for (Thing ts : t.getSiblings()) {
			Product p = ts.getProduct();
			double qty = Things.getDoubleProperty(ts, "QUANTITY", 0d);
			String mu = Products.getStringField(p, "GROUP_UM", "UN");

			ordmov.getItems().add(new DocumentItem(ordmov, p, qty, "NOVO", mu));
		}
		ordmov.getThings().add(new DocumentThing(ordmov, t, t.getStatus()));
		ordmov.getFields().add(new DocumentField(ordmov, dmfPriority, "NOVO", "1"));
		ordmov.getFields().add(new DocumentField(ordmov, dmType, "NOVO", "RESUPPLY"));
		ordmov.getFields().add(new DocumentField(ordmov, dmTitle, "NOVO", "PÁTIO: RESSUPRIMENTO"));
		ordmov.getTransports().add(new DocumentTransport(ordmov, 1, t, dest, orig));
		ordmov.setParent(repack);
		repack.getSiblings().add(ordmov);
		iSvc.getRegSvc().getDcSvc().persist(ordmov);
		iSvc.getRegSvc().getAglSvc().sendDocToWMS(ordmov, "POST");
		return true;
	}
}
