package com.gtp.hunter.process.wf.action.solar;

import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Products;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.BaseAction;

public class CompletePickingAction extends BaseAction {
	private static final boolean			PROFILE				= false;
	private transient static final Logger	logger				= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static boolean					checkingResupply	= false;

	public CompletePickingAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
	}

	@Override
	public String execute(Action act) throws Exception {
		Profiler prof = new Profiler();
		DecimalFormat DF = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.US));
		SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		UUID pkId = UUID.fromString(act.getParams());
		Document picking = getRegSvc().getDcSvc().findById(pkId);

		getRsm().getTsm().cancelTask(getUser().getId(), pkId);
		try {
			getRegSvc().getTskSvc().startTask(picking, getUser());
			Document transport = getRegSvc().getDcSvc().findParent(picking);
			Document d = picking.getSiblings().parallelStream().filter(ds -> ds.getModel().getMetaname().equals("OSG")).findAny().orElse(null);
			String thStatus = "SEPARADO";

			if (d != null) {
				Map<Product, Double> prdSet = new HashMap<>();
				Product palletPrd = getRegSvc().getPrdSvc().findBySKU("1404020");
				PropertyModel plpmdl = palletPrd.getModel().getPropertymodel();
				Address stage = d.getFields().parallelStream()
								.filter(df -> df.getField().getMetaname().equals("STAGE_ID"))
								.map(df -> getRegSvc().getAddSvc().findById(UUID.fromString(df.getValue())))
								.findAny()
								.orElse(null);
				Thing th = new Thing(palletPrd.getName(), palletPrd, palletPrd.getModel().getPropertymodel(), thStatus);

				th.getProperties().add(new Property(th, plpmdl.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("ACTUAL_WEIGHT")).findAny().get(), DF.format(15)));
				th.getProperties().add(new Property(th, plpmdl.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("LOT_EXPIRE")).findAny().get(), SDF.format(new Date())));
				th.getProperties().add(new Property(th, plpmdl.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("LOT_ID")).findAny().get(), "VARIADO"));
				th.getProperties().add(new Property(th, plpmdl.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("MANUFACTURING_BATCH")).findAny().get(), SDF.format(new Date())));
				th.getProperties().add(new Property(th, plpmdl.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("QUANTITY")).findAny().get(), DF.format(1)));
				th.getProperties().add(new Property(th, plpmdl.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("STARTING_WEIGHT")).findAny().get(), DF.format(15)));
				th.setAddress(stage);
				logger.info(prof.step("Got Document", PROFILE));
				for (DocumentItem di : d.getItems()) {//Buscar locais recentes dos produtos
					List<String> pkAddList = getRegSvc().getWmsSvc().listPickingByProduct(di.getProduct().getId());
					String addId = pkAddList.isEmpty() ? "" : pkAddList.get(0);
					PropertyModel pmdl = di.getProduct().getModel().getPropertymodel();
					Thing ts = new Thing(di.getProduct().getName(), di.getProduct(), pmdl, thStatus);
					int unitBox = Products.getIntegerField(di.getProduct(), "UNIT_BOX", 1);
					double weight = unitBox * Products.getDoubleField(di.getProduct(), "GROSS_WEIGHT", 1d);

					ts.getProperties().add(new Property(ts, pmdl.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("ACTUAL_WEIGHT")).findAny().get(), DF.format(di.getQty() * weight)));
					ts.getProperties().add(new Property(ts, pmdl.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("LOT_EXPIRE")).findAny().get(), "TEMPORARIO"));
					ts.getProperties().add(new Property(ts, pmdl.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("LOT_ID")).findAny().get(), "TEMPORARIO"));
					ts.getProperties().add(new Property(ts, pmdl.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("MANUFACTURING_BATCH")).findAny().get(), "TEMPORARIO"));
					ts.getProperties().add(new Property(ts, pmdl.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("QUANTITY")).findAny().get(), DF.format(di.getQty())));
					ts.getProperties().add(new Property(ts, pmdl.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("STARTING_WEIGHT")).findAny().get(), DF.format(weight)));
					ts.setAddress(stage);
					ts.setParent(th);
					th.getSiblings().add(ts);
					if (addId.isEmpty()) {
						Address tmp = getRegSvc().getAddSvc().findRandomByModelFieldValue("SKU", di.getProduct().getSku());//SHIT MODAFOCA GAMBS!

						addId = tmp == null ? "" : tmp.getId().toString();
					}
					if (!addId.isEmpty()) {
						di.getProperties().put("ADDRESS_ID", addId);
						getRegSvc().getDiSvc().persist(di);
						logger.info(prof.step("DI Persisted", PROFILE));
					}
					if (prdSet.containsKey(di.getProduct()))
						prdSet.put(di.getProduct(), prdSet.get(di.getProduct()) + di.getQty());
					else
						prdSet.put(di.getProduct(), di.getQty());
				}
				logger.info(prof.step("Updated Item Addresses", PROFILE));
				getRegSvc().getWmsSvc().checkResupply(prdSet);
				getRegSvc().getThSvc().persist(th);
				th.setStatus("INVENTARIO");
				getRegSvc().getAglSvc().sendThingToWMS(th, "POST");
				th.setStatus(thStatus);
				getRegSvc().getWmsSvc().updateThingStatus(th.getId(), thStatus);
				th.getSiblings().forEach(ts -> getRegSvc().getWmsSvc().updateThingStatus(ts.getId(), thStatus));
				d.getThings().add(new DocumentThing(d, th, thStatus));
				d.setStatus("SUCESSO");
				d.setUser(getUser());
				//SEPARAÇAO COMPELTA
				logger.info(prof.step("Find Picking, Status: " + picking.getStatus(), PROFILE));
				getRegSvc().getWmsSvc().consumePickingQuantity(d);
				logger.info(prof.step("Update Items", PROFILE));
				getRegSvc().getDtSvc().persist(new DocumentThing(transport, th, thStatus));
				getRegSvc().getDtSvc().persist(new DocumentThing(picking, th, thStatus));
				picking.setStatus(thStatus);
				picking.setUser(getUser());
				getRegSvc().getDcSvc().persist(picking);
				logger.info(prof.step("Picking Persisted", PROFILE));
				if (!checkingResupply) {
					checkingResupply = true;
					Executors.newSingleThreadExecutor().submit(() -> {
						getRegSvc().getWmsSvc().checkResupplyMin();
						checkingResupply = false;
					});
				}
			}
			getRegSvc().getDcSvc().flush();//FUCKING TRANSPORT FROM HELL
			getRegSvc().getDcSvc().persist(d);
			getRegSvc().getTskSvc().completeTask(picking, getUser());
		} catch (Exception e) {
			e.printStackTrace();
		}
		getRsm().getTsm().unlockTask(picking);
		return act.getRoute();
	}

}
