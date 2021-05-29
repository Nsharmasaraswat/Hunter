package com.gtp.hunter.process.wf.filter.trigger;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.core.model.BaseModel;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Addresses;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.model.util.Things;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;

public class WMSTruckEnterUpdater extends WMSBaseUpdate {
	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public WMSTruckEnterUpdater(FilterTrigger model) {
		super(model);
	}

	@Override
	protected BaseModel executeImpl(BaseModelEvent mdl) {
		Document d = (Document) mdl.getModel();
		logger.info("WMS Truck Entered " + d.getCode());
		boolean genConf = d.getSiblings().stream()
						.filter(sib -> sib.getModel().getMetaname().equals("NFSAIDA") && !Documents.getStringField(sib, "ZTRANS", "").equalsIgnoreCase("N"))
						.flatMap(nf -> nf.getItems().stream())
						.map(di -> di.getProduct().getModel())
						.anyMatch(pm -> pm.getProperties().containsKey("conf_out") && pm.getProperties().get("conf_out").equalsIgnoreCase("true"));
		boolean genPicking = d.getSiblings().stream().anyMatch(ds -> ds.getModel().getMetaname().equals("NFSAIDA") && !Documents.getStringField(ds, "ZTRANS", "").equalsIgnoreCase("N"));
		Thing truck = mdl.getRegSvc().getThSvc().findByUnitId(d.getThings().parallelStream()
						.filter(dt -> dt.getThing().getUnits().size() > 0)
						.findAny().get().getThing().getUnits().parallelStream()
						.findAny().get());
		String plates = truck.getUnitModel().stream().filter(u -> u.getType() == UnitType.LICENSEPLATES).findFirst().get().getTagId();
		String docaChamada = Documents.getStringField(d, "DOCK");

		logger.info(d.getCode() + " - Truck " + plates + " Dock: " + docaChamada + " genConf: " + genConf + " genPicking: " + genPicking);
		if (getParams().containsKey(docaChamada)) {
			Address docaVirtual = mdl.getRegSvc().getAddSvc().findById(UUID.fromString((String) getParams().get(docaChamada)));
			int left = Things.getIntProperty(truck, "LEFT_SIDE_QUANTITY");
			int right = Things.getIntProperty(truck, "RIGHT_SIDE_QUANTITY");
			List<Address> bays = docaVirtual.getSiblings()
							.parallelStream()
							.filter(a -> Addresses.getIntField(a, "ROAD_SEQ") <= (left + right))
							.sorted((a1, a2) -> Addresses.getIntField(a1, "ROAD_SEQ") - Addresses.getIntField(a2, "ROAD_SEQ"))
							.collect(Collectors.toList());

			for (Address baia : bays)
				mdl.getRegSvc().getWmsSvc().updateAddressCode(baia.getId(), plates);
			if (genPicking) {
				Collections.reverse(bays);
				mdl.getRegSvc().getWmsSvc().generatePicking(d, bays);
			}
			if (genConf) {
				mdl.getRegSvc().getWmsSvc().createOutboundChecking(d);
			}
		} else if (d.getSiblings().stream()
						.filter(ds -> ds.getModel().getMetaname().equals("NFENTRADA"))
						.flatMap(nf -> nf.getItems().stream())
						.map(di -> di.getProduct().getModel())
						.anyMatch(pm -> pm.getMetaname().equals("MP"))) {
		} else
			logger.error("Dock " + docaChamada + " Is not in FilterTrigger Params");
		return d;
	}
}
