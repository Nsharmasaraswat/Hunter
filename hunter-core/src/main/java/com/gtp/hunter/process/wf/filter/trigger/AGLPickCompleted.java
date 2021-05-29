package com.gtp.hunter.process.wf.filter.trigger;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;

public class AGLPickCompleted extends BaseTrigger {

	private transient static final Logger	logger		= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final List<UUID>			processing	= new CopyOnWriteArrayList<>();

	public AGLPickCompleted(FilterTrigger model) {
		super(model);
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> processing.clear(), 1, 6, TimeUnit.HOURS);//TODO: Check why its duplicating trigger
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		Document picking = (Document) mdl.getModel();
		if (picking == null || picking.getModel() == null || picking.getModel().getMetaname() == null || picking.getStatus() == null)
			return false;
		if (!processing.contains(picking.getId())) {
			processing.add(picking.getId());
			logger.info("PICKING COMPELTED: " + picking.getCode());
			Document transport = mdl.getRegSvc().getDcSvc().findParent(picking);
			boolean isRoute = Documents.getStringField(transport, "SERVICE_TYPE").equals("ROTA");
			boolean hasConf = picking.getSiblings().parallelStream()
							.anyMatch(ds -> ds.getModel().getMetaname().equals("ORDCONF") && ds.getCode().equals("CONF" + picking.getCode()));
			boolean loaded = transport.getSiblings().parallelStream().anyMatch(ds -> ds.getModel().getMetaname().equals("APOCARGA"));
			boolean picked = transport.getSiblings().parallelStream()
							.filter(ds -> ds.getModel().getMetaname().equals("PICKING") && !ds.getId().equals(picking.getId()))
							.allMatch(ds -> ds.getStatus().equals("SEPARADO"));

			if (picked && !loaded) {
				mdl.getRegSvc().getDcSvc().createChild(transport, "CAMINHAO CARREGADO", "APOCARGA", "NOVO", "CAR", null, null, null, null);
			}
			if (ConfigUtil.get("hunter-custom-solar", "checkin-checkout-rota", "FALSE").equalsIgnoreCase("TRUE")) {
				logger.warn(picking.getCode() + " isRoute: " + isRoute + " hasConf: " + hasConf);
				if (isRoute && !hasConf) {
					Document osgMov = picking.getSiblings().parallelStream()
									.filter(ds -> ds.getModel().getMetaname().equals("OSG") || ds.getModel().getMetaname().equals("ORDMOV"))
									.findAny()
									.get();
					DocumentModel ordConfModel = mdl.getRegSvc().getDmSvc().findByMetaname("ORDCONF");
					DocumentModelField dmfConfType = ordConfModel.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("CONF_TYPE")).findAny().get();
					DocumentModelField dmfSvcType = ordConfModel.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("SERVICE_TYPE")).findAny().get();
					DocumentModelField dmfCnfPrio = ordConfModel.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("PRIORITY")).findAny().get();
					DocumentModelField dmfCnfLoad = ordConfModel.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("LOAD_ID")).findAny().get();
					Product pallet = mdl.getRegSvc().getPrdSvc().findBySKU((String) getParams().get("pallet_sku"));
					Product eucatex = mdl.getRegSvc().getPrdSvc().findBySKU((String) getParams().get("eucatex_sku"));
					Document ordconf = new Document(ordConfModel, ordConfModel.getName() + " " + picking.getCode(), "CONF" + picking.getCode(), "ATIVO");
					Map<Product, Double> prdCountMap = osgMov.getItems().stream()
									.collect(Collectors.groupingBy(DocumentItem::getProduct, Collectors.summingDouble(DocumentItem::getQty)));

					ordconf.setParent(picking);
					ordconf.getFields().add(new DocumentField(ordconf, dmfConfType, "NOVO", "SPA"));
					ordconf.getFields().add(new DocumentField(ordconf, dmfSvcType, "NOVO", "ROTA"));
					ordconf.getFields().add(new DocumentField(ordconf, dmfCnfPrio, "NOVO", "1"));
					ordconf.getFields().add(new DocumentField(ordconf, dmfCnfLoad, "NOVO", Documents.getStringField(transport, "OBS").replace("CARGA: ", "")));
					ordconf.getThings().addAll(osgMov.getThings().parallelStream().map(dt -> new DocumentThing(ordconf, dt.getThing(), "SEPARADO")).collect(Collectors.toSet()));
					for (Entry<Product, Double> en : prdCountMap.entrySet()) {
						String um = en.getKey().getFields().parallelStream().filter(pf -> pf.getModel().getMetaname().equals("GROUP_UM")).findAny().get().getValue();
						ordconf.getItems().add(new DocumentItem(ordconf, en.getKey(), en.getValue(), "NOVO", um));
					}
					ordconf.getItems().add(new DocumentItem(ordconf, pallet, 1, "NOVO", "UN"));
					ordconf.getItems().add(new DocumentItem(ordconf, eucatex, 0, "NOVO", "UN"));
					mdl.getRegSvc().getDcSvc().persist(ordconf);
				} else if (!isRoute) {
					boolean confsCompleted = !transport.getSiblings().parallelStream()
									.filter(ds -> ds.getModel().getMetaname().equals("ORDCONF"))
									.anyMatch(ds -> Documents.getStringField(ds, "CONF_TYPE").equals("SPAPD") && ds.getStatus().equals("ATIVO"));
					boolean movsLdtCompleted = !transport.getSiblings().parallelStream()
									.filter(ds -> ds.getModel().getMetaname().equals("ORDMOV") && ds.getCode().startsWith("LDT"))
									.anyMatch(ds -> ds.getStatus().equals("LOAD"));

					logger.info("Gerar Lacre? rota: " + isRoute + " ConfsCompleted: " + confsCompleted + " MovsLdtCompleted: " + movsLdtCompleted);
					if (confsCompleted && movsLdtCompleted)
						mdl.getRegSvc().getWmsSvc().createLacre(transport);
				}
			} else
				logger.warn("checkin-checkout-rota DISABLED in configFile");
		}
		return true;
	}
}