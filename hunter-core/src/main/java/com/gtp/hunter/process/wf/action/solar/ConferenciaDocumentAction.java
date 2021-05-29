package com.gtp.hunter.process.wf.action.solar;

import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.model.util.Products;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.DocumentAction;

public class ConferenciaDocumentAction extends DocumentAction {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ConferenciaDocumentAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	protected void open(Action t) {
		logger.debug("Conferencia Document Action");
		getAs().onNext(getDoc());
	}

	@Override
	public void onMessage(Object msg) {
		String json = (String) msg;
		DecimalFormat decf = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.US));
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Document tmp = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(json, Document.class);
		getAs().onNext(IntegrationReturn.OK);
		String confType = Documents.getStringField(tmp, "CONF_TYPE");
		Document tmpPkTr = getRegSvc().getDcSvc().quickFindParentDoc(tmp.getId());
		Document transp = confType.equals("SPA") ? getRegSvc().getDcSvc().findParent(tmpPkTr.getId()) : getRegSvc().getDcSvc().findById(tmpPkTr.getId());
		String serviceType = Documents.getStringField(transp, "SERVICE_TYPE");
		//Update to Persistent Entities
		Document pick = confType.equals("RPAPD") ? null : transp.getSiblings().parallelStream().filter(ds -> ds.getId().equals(tmpPkTr.getId())).findAny().orElse(null);
		Document d = confType.equals("SPA") ? transp.getSiblings().parallelStream()
						.filter(ds -> ds.getId().equals(tmpPkTr.getId()))
						.flatMap(pck -> pck.getSiblings().parallelStream())
						.filter(cnf -> cnf.getId().equals(tmp.getId()))
						.findAny()
						.get() : transp.getSiblings().parallelStream()
										.filter(ds -> ds.getId().equals(tmp.getId()))
										.findAny()
										.get();
		List<Thing> tList = d.getThings().parallelStream().map(dt -> dt.getThing()).collect(Collectors.toList());
		boolean genLacreRota = confType.equals("SPA") && transp.getSiblings().parallelStream()
						.filter(ds -> ds.getModel().getMetaname().equals("PICKING") && !ds.getId().equals(pick.getId()) && !ds.getStatus().equals("CANCELADO"))
						.allMatch(ds -> ds.getStatus().equals("CONFERIDO"));
		boolean genLacreOutros = !serviceType.equals("ROTA") && !transp.getSiblings().parallelStream()
						.filter(ds -> {
							if (ds.getModel().getMetaname().equals("ORDCONF") && !ds.getId().equals(tmp.getId()) && Documents.getStringField(ds, "CONF_TYPE").equals("SPAPD")) return true;
							if (ds.getModel().getMetaname().equals("ORDMOV") && Documents.getStringField(ds, "MOV_TYPE").equals("LOAD")) return true;
							return false;
						})
						.anyMatch(ds -> ds.getStatus().equals("ATIVO") || ds.getStatus().equals("LOAD"));

		for (Document tmpDs : tmp.getSiblings()) {
			DocumentModel rocModel = getRegSvc().getDmSvc().findById(tmpDs.getModel().getId());
			Document retordconf = new Document(rocModel, rocModel.getName() + " " + tmpDs.getCode().replace("ROC", ""), tmpDs.getCode(), tmpDs.getStatus());

			for (DocumentItem tmpDi : tmpDs.getItems()) {
				Product p = getRegSvc().getPrdSvc().findById(tmpDi.getProduct().getId());
				double weight = Products.getDoubleField(p, "GROSS_WEIGHT", 1d);
				DocumentItem rocDi = new DocumentItem(retordconf, p, tmpDi.getQty(), "NOVO");
				Optional<Thing> optTsep = pick != null ? pick.getThings().parallelStream()
								.flatMap(dt -> dt.getThing().getSiblings().parallelStream())
								.filter(t -> t.getProduct().getId().equals(p.getId()))
								.findAny() : transp.getThings().parallelStream()
												.flatMap(dt -> dt.getThing().getSiblings().parallelStream())
												.filter(t -> t.getProduct().getId().equals(p.getId()))
												.findAny();

				if (optTsep.isPresent()) {
					optTsep.get().getProperties().forEach(pr -> rocDi.getProperties().put(pr.getField().getMetaname().toLowerCase(), pr.getValue()));
				} else {
					rocDi.getProperties().put("actual_weight", decf.format(weight * tmpDi.getQty()));
					rocDi.getProperties().put("lot_expire", sdf.format(new Date()));
					rocDi.getProperties().put("lot_id", p.getSku());
					rocDi.getProperties().put("manufacturing_batch", sdf.format(new Date()));
					rocDi.getProperties().put("quantity", decf.format(tmpDi.getQty()));
					rocDi.getProperties().put("volumes", decf.format(1d));
					rocDi.getProperties().put("starting_weight", decf.format(weight));
				}
				if (!rocDi.getProperties().containsKey("volumes"))
					rocDi.getProperties().put("volumes", decf.format(1d));
				retordconf.getItems().add(rocDi);
			}
			retordconf.getThings().addAll(d.getThings().parallelStream().map(dt -> new DocumentThing(retordconf, dt.getThing(), tmpDs.getStatus())).collect(Collectors.toSet()));
			retordconf.setParent(d);
			retordconf.setUser(getUser());
			getRegSvc().getDcSvc().persist(retordconf);
			d.getSiblings().add(retordconf);
		}

		d.setStatus(tmp.getStatus());
		d.setUser(getUser());
		if (d.getStatus().equals("SUCESSO")) {

			tList.forEach(t -> {
				t.setStatus("CONFERIDO");
				t.getSiblings().forEach(ts -> ts.setStatus("CONFERIDO"));
			});
			getRegSvc().getThSvc().multiPersist(tList);

			logger.info("ServiceType: " + serviceType + " - ConfType: " + confType + " - GenLacreOutros: " + genLacreOutros + " - GenLacreRota " + genLacreRota);
			if (genLacreOutros || genLacreRota)
				getRegSvc().getWmsSvc().createLacre(transp);
		}
		updatePicking(d, pick);
	}

	private void updatePicking(Document d, Document pick) {
		if (pick != null) {
			pick.setStatus(d.getStatus().equals("SUCESSO") ? "CONFERIDO" : d.getStatus());
			getRegSvc().getDcSvc().persist(pick);
			getRegSvc().getDcSvc().fireUpdate(d);
		} else {
			getRegSvc().getDcSvc().persist(d);
		}
	}

	@OnClose
	@Override
	protected void close(Session ss, CloseReason cr) {
		logger.info("Closed: " + cr.getReasonPhrase());
	}
}
