package com.gtp.hunter.process.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.repository.CustomRepository;
import com.gtp.hunter.ui.json.PalletHistory;
import com.gtp.hunter.ui.json.RNCProductStub;

@Stateless
public class CustomService {

	@Inject
	private CustomRepository	uiRep;

	@Inject
	private ThingService		thSvc;

	@Inject
	private DocumentService		dcSvc;

	public List<RNCProductStub> getStubsByProductId(UUID prdId) {
		return uiRep.getStubsByProductId(prdId);
	}

	public PalletHistory getPalletHistory(UUID thingId) {
		PalletHistory ret = new PalletHistory();
		Thing tmp = thSvc.findById(thingId);

		if (tmp != null) {
			Thing thing = tmp;

			if (tmp.getParent() != null)
				thing = tmp.getParent();
			List<Document> parents = dcSvc.listByThingId(thing.getId());

			ret.setThing(thing);
			ret.setParents(parents.stream()
							.filter(d -> (d.getModel().getMetaname().equals("ORDCONF") && d.getFields().parallelStream().anyMatch(df -> df.getValue().equals("EPAPD")) || (!d.getModel().getMetaname().equals("ORDCONF") && !d.getModel().getMetaname().equals("RETORDCONF"))))
							.map(d -> d.getModel().getMetaname().equals("ORDCRIACAO") ? d.getParent() : d)
							.collect(Collectors.toList()));
		}
		return ret;
	}

	public String findParentDriver(Document child) {
		return uiRep.findParentDriver(child.getId());
	}

	public String findParentLoad(Document child) {
		return uiRep.findParentLoad(child.getId());
	}

	public String findParentDelivery(Document child) {
		return uiRep.findParentDelivery(child.getId());
	}

	public String findParentPlates(Document child) {
		return uiRep.findParentPlates(child.getId());
	}

	public String findParentSupplierCustomer(Document child) {
		return uiRep.findParentSupplierCustomer(child.getId());
	}

	public String findParentCarrier(Document child) {
		return uiRep.findParentCarrier(child.getId());
	}
}
