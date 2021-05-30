package com.gtp.hunter.custom.solar.service;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductField;
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;

@Stateless
public class ThingService {

	@Inject
	private IntegrationService	iSvc;

	@Inject
	private PropertyService		prpSvc;

	public Thing createPallet(Product prd, Address linhaaddr, String status, String lot) {
		Product prd_pallet = iSvc.getRegSvc().getPrdSvc().findBySKU("1404020");
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		Thing pallet = new Thing(prd_pallet.getName(), prd_pallet, prd_pallet.getModel().getPropertymodel(), status);
		Thing t = new Thing(prd.getName(), prd, prd.getModel().getPropertymodel(), status);
		PropertyModel prm = prd.getModel().getPropertymodel();
		Supplier<Stream<PropertyModelField>> supPalletPrm = () -> prd_pallet.getModel().getPropertymodel().getFields().stream();
		Supplier<Stream<PropertyModelField>> supPrm = () -> prm.getFields().stream();
		Supplier<Stream<ProductField>> supFields = () -> prd.getFields().stream();
		String palletStrtWeight = prd_pallet.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("GROSS_WEIGHT")).findFirst().get().getValue();
		String palletActualWeight = prd_pallet.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("VAR_WEIGHT")).findFirst().get().getValue();
		String prdStrtWeight = supFields.get().filter(pf -> pf.getModel().getMetaname().equals("GROSS_WEIGHT")).findFirst().get().getValue();
		String prdActualWeight = supFields.get().filter(pf -> pf.getModel().getMetaname().equals("VAR_WEIGHT")).findFirst().get().getValue();
		String prdPalletBox = supFields.get().filter(pf -> pf.getModel().getMetaname().equals("PALLET_BOX")).findFirst().get().getValue();
		String shelfLife = supFields.get().filter(pf -> pf.getModel().getMetaname().equals("SHELFLIFE")).findFirst().get().getValue();
		PropertyModelField prmfPalletSTRWGHT = supPrm.get().filter(prmf -> prmf.getMetaname().equals("STARTING_WEIGHT")).findFirst().get();
		PropertyModelField prmfPalletACTWGHT = supPrm.get().filter(prmf -> prmf.getMetaname().equals("ACTUAL_WEIGHT")).findFirst().get();
		PropertyModelField prmfPalletEXP = supPalletPrm.get().filter(prmf -> prmf.getMetaname().equals("MANUFACTURING_BATCH")).findFirst().get();
		PropertyModelField prmfPalletMAN = supPalletPrm.get().filter(prmf -> prmf.getMetaname().equals("LOT_EXPIRE")).findFirst().get();
		PropertyModelField prmfPalletQTY = supPalletPrm.get().filter(prmf -> prmf.getMetaname().equals("QUANTITY")).findFirst().get();
		PropertyModelField prmfPalletLOT = supPalletPrm.get().filter(prmf -> prmf.getMetaname().equals("LOT_ID")).findFirst().get();
		PropertyModelField prmfSTRWGHT = supPrm.get().filter(prmf -> prmf.getMetaname().equals("STARTING_WEIGHT")).findFirst().get();
		PropertyModelField prmfACTWGHT = supPrm.get().filter(prmf -> prmf.getMetaname().equals("ACTUAL_WEIGHT")).findFirst().get();
		PropertyModelField prmfEXP = supPrm.get().filter(prmf -> prmf.getMetaname().equals("MANUFACTURING_BATCH")).findFirst().get();
		PropertyModelField prmfMAN = supPrm.get().filter(prmf -> prmf.getMetaname().equals("LOT_EXPIRE")).findFirst().get();
		PropertyModelField prmfQTY = supPrm.get().filter(prmf -> prmf.getMetaname().equals("QUANTITY")).findFirst().get();
		PropertyModelField prmfLOT = supPrm.get().filter(prmf -> prmf.getMetaname().equals("LOT_ID")).findFirst().get();

		try {
			cal.add(Calendar.DAY_OF_MONTH, Integer.parseInt(shelfLife));
		} catch (NumberFormatException nfe) {
			cal.add(Calendar.DAY_OF_MONTH, 60);
		}
		pallet.getProperties().addAll(prpSvc.getProperties(pallet, prmfPalletSTRWGHT, palletStrtWeight, prmfPalletACTWGHT, palletActualWeight, prmfPalletLOT, lot, prmfPalletMAN, now, prmfPalletEXP, cal.getTime(), prmfPalletQTY, "1"));
		pallet.setAddress(linhaaddr);
		iSvc.getRegSvc().getThSvc().persist(pallet);
		t.setCreatedAt(now);
		t.setUpdatedAt(now);
		t.getProperties().addAll(prpSvc.getProperties(t, prmfSTRWGHT, prdStrtWeight, prmfACTWGHT, prdActualWeight, prmfLOT, lot, prmfMAN, now, prmfEXP, cal.getTime(), prmfQTY, prdPalletBox == null || prdPalletBox.isEmpty() ? "1" : prdPalletBox));
		t.setParent(pallet);
		t.setAddress(linhaaddr);
		pallet.getSiblings().add(t);
		iSvc.getRegSvc().getThSvc().persist(t);
		iSvc.getRegSvc().getThSvc().persist(pallet);
		return pallet;
	}

}
