package com.gtp.hunter.process.wf.process.solar;

import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.gtp.hunter.common.model.RawData.RawDataType;
import com.gtp.hunter.common.payload.SimPayload;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.wf.process.ContinuousProcess;

public class AGLDocumentCreateProcess extends ContinuousProcess {

	private transient static final Logger	logger		= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final SimpleDateFormat	LOT_FORMAT	= new SimpleDateFormat("ddMMyy");
	private static final SimpleDateFormat	DATE_PARSE	= new SimpleDateFormat("dd/MM/yyyy");
	private static final SimpleDateFormat	DATE_FORMAT	= new SimpleDateFormat("yyyy-MM-dd");
	private DocumentModel					ordCriacaoModel;
	private Product							prd_pallet;

	/*-------------------------mock-----------------------------*/
	private Address							linhaaddr;//TODO: Passar dinamico por docfield
	private Product							prd;

	@Override
	protected void checkParams() throws Exception {
		if (!getParametros().containsKey("document-meta")) throw new Exception("Parâmetro 'document-meta' não encontrado.");
		if (!getParametros().containsKey("pallet_prd_id")) throw new Exception("Parâmetro 'pallet_prd_id' não encontrado.");
		if (!getParametros().containsKey("starting-weight-property-meta")) throw new Exception("Parâmetro 'starting-weight-property-meta' não encontrado.");
		if (!getParametros().containsKey("actual-weight-property-meta")) throw new Exception("Parâmetro 'actual-weight-property-meta' não encontrado.");
	}

	@Override
	public void onInit() {
		ordCriacaoModel = getRegSvc().getDmSvc().findByMetaname((String) getParametros().get("document-meta"));
		prd_pallet = getRegSvc().getPrdSvc().findById(UUID.fromString((String) getParametros().get("pallet_prd_id")));
		/*-------------------------mock-----------------------------*/
		linhaaddr = getRegSvc().getAddSvc().findById(UUID.fromString("af0c5896-92f3-11e9-815b-005056a19775"));
	}

	@Override
	protected void connect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void timeout(Map<String, Thing> itens) {
		try {
			if (!getParametros().containsKey("noproc")) {
				boolean truck = getParametros().containsKey("truck") ? (boolean) getParametros().get("truck") : false;

				if (prd != null) {
					logger.info("Timeout");
					Calendar cal = Calendar.getInstance();
					Date now = cal.getTime();
					Date man = cal.getTime();
					Date exp = cal.getTime();
					int qty = 0;
					int palletCount = 1;
					String lot = (getParametros().containsKey("lot") ? (String) getParametros().get("lot") : "RN5" + LOT_FORMAT.format(now)) + Integer.parseInt(prd.getSku());
					Prefix prefix = getRegSvc().getPfxSvc().findNext("OCR", 9);
					cal.add(Calendar.MONTH, 6);
					exp = cal.getTime();
					Document d = new Document(ordCriacaoModel, ordCriacaoModel.getName() + " " + prefix.getCode(), prefix.getPrefix() + prefix.getCode(), "NOVO");

					try {

						if (getParametros().containsKey("fab")) {
							man = DATE_PARSE.parse((String) getParametros().get("fab"));
							getParametros().remove("fab");
						}
						if (getParametros().containsKey("val")) {
							exp = DATE_PARSE.parse((String) getParametros().get("val"));
							getParametros().remove("val");
						}
						if (getParametros().containsKey("qty")) {
							qty = (Integer) getParametros().get("qty");
							getParametros().remove("qty");
						}
						if (getParametros().containsKey("count")) {
							palletCount = (Integer) getParametros().get("count");
							getParametros().remove("count");
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (truck) {
						Address trck = getRegSvc().getAddSvc().findById(UUID.fromString("7aa5bb90-97c3-11e9-815b-005056a19775"));

						for (Address baia : trck.getSiblings()) {
							Thing pallet = new Thing(prd_pallet.getName(), prd_pallet, prd_pallet.getModel().getPropertymodel(), getModel().getEstadoPara());
							DocumentThing dt = new DocumentThing(d, pallet, getModel().getEstadoPara());
							pallet.getProperties().addAll(getProperties(pallet, lot, man, exp, "1"));
							pallet.setAddress(baia);
							getRegSvc().getThSvc().persist(pallet);
							Thing t = new Thing(prd.getName(), prd, prd.getModel().getPropertymodel(), getModel().getEstadoPara());

							t.setCreatedAt(now);
							t.setUpdatedAt(now);
							t.getProperties().addAll(getProperties(t, lot, man, exp, String.valueOf(qty)));
							t.setParent(pallet);
							t.setAddress(baia);
							getRegSvc().getThSvc().persist(t);
							pallet.getSiblings().add(t);
							getRegSvc().getThSvc().persist(pallet);
							dt.setCreatedAt(now);
							dt.setUpdatedAt(now);
							d.getThings().add(dt);
						}
					} else {
						for (int i = 0; i < palletCount; i++) {
							Thing pallet = new Thing(prd_pallet.getName(), prd_pallet, prd_pallet.getModel().getPropertymodel(), getModel().getEstadoPara());
							DocumentThing dt = new DocumentThing(d, pallet, getModel().getEstadoPara());

							pallet.getProperties().addAll(getProperties(pallet, lot, man, exp, "1"));
							pallet.setAddress(linhaaddr);
							getRegSvc().getThSvc().persist(pallet);
							Thing t = new Thing(prd.getName(), prd, prd.getModel().getPropertymodel(), getModel().getEstadoPara());

							t.setCreatedAt(now);
							t.setUpdatedAt(now);
							t.getProperties().addAll(getProperties(t, lot, man, exp, String.valueOf(qty)));
							t.setParent(pallet);
							t.setAddress(linhaaddr);
							getRegSvc().getThSvc().persist(t);
							pallet.getSiblings().add(t);
							getRegSvc().getThSvc().persist(pallet);
							dt.setCreatedAt(now);
							dt.setUpdatedAt(now);
							d.getThings().add(dt);
						}
					}
					d.setCreatedAt(now);
					d.setUpdatedAt(now);
					getRegSvc().getDcSvc().persist(d);
					getParametros().put("doc", d);
					getParametros().remove("lot");
					getParametros().remove("truck");
					this.runSucess();
				} else {
					this.lockdown("Produto Não criado");
				}
			} else {
				getParametros().remove("noproc");
				this.runSucess();
			}
		} catch (Exception e) {
			logger.warn("Silent Exception timeout: " + e.getLocalizedMessage());
		}
	}

	@Override
	protected void processBefore(ComplexData rd) {
		try {
			if (rd.getType() == RawDataType.STATUS) {
				getParametros().put("noproc", "true");
			} else {
				prd = getRegSvc().getPrdSvc().findBySKU(rd.getTagId());
				if (rd.getType() == RawDataType.SENSOR) {
					SimPayload pl = new Gson().fromJson(rd.getPayload(), SimPayload.class);

					if (pl.getLot() != null) getParametros().put("lot", pl.getLot());
					if (pl.getFab() != null) getParametros().put("fab", pl.getFab());
					if (pl.getVal() != null) getParametros().put("val", pl.getVal());
					if (pl.getQty() > 0) getParametros().put("qty", pl.getQty());
					if (pl.getCount() > 0) getParametros().put("count", pl.getCount());
					getParametros().put("truck", pl.isTruck());

				}
				if (prd == null) {
					resend("PRODUTO NAO CADASTRADO " + rd.getTagId());
					this.lockdown("PRODUTO NAO CADASTRADO " + rd.getTagId());
				}
			}
		} catch (Exception e) {
			logger.warn("Silent Exception processBefore: " + e.getLocalizedMessage());
		}
	}

	@Override
	protected void processAfter(Thing rd) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void processUnknown(ComplexData rd) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void success() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void failure() {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	private Set<Property> getProperties(Thing t, String lot, Date man, Date exp, String qty) {
		Set<Property> ret = new HashSet<>();
		PropertyModel prm = t.getProduct().getModel().getPropertymodel();
		Supplier<Stream<PropertyModelField>> supPrmf = () -> prm.getFields().stream();
		PropertyModelField prmfSTRWGHT = supPrmf.get().filter(prmf -> prmf.getMetaname().equals("STARTING_WEIGHT")).findAny().orElse(null);
		PropertyModelField prmfACTWGHT = supPrmf.get().filter(prmf -> prmf.getMetaname().equals("ACTUAL_WEIGHT")).findAny().orElse(null);
		PropertyModelField prmfLOT = supPrmf.get().filter(prmf -> prmf.getMetaname().equals("LOT_ID")).findAny().orElse(null);
		PropertyModelField prmfEXP = supPrmf.get().filter(prmf -> prmf.getMetaname().equals("LOT_EXPIRE")).findAny().orElse(null);
		PropertyModelField prmfMAN = supPrmf.get().filter(prmf -> prmf.getMetaname().equals("MANUFACTURING_BATCH")).findAny().orElse(null);
		PropertyModelField prmfQTY = supPrmf.get().filter(prmf -> prmf.getMetaname().equals("QUANTITY")).findAny().orElse(null);
		Property plsw = new Property(t, prmfSTRWGHT, prd.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("GROSS_WEIGHT")).findFirst().get().getValue());
		Property plaw = new Property(t, prmfACTWGHT, prd.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("VAR_WEIGHT")).findFirst().get().getValue());
		Property pllot = new Property(t, prmfLOT, lot);
		Property plman = new Property(t, prmfMAN, DATE_FORMAT.format(man));
		Property plexp = new Property(t, prmfEXP, DATE_FORMAT.format(exp));
		Property plqty = new Property(t, prmfQTY, qty);

		plaw.setStatus("NOVO");
		plaw.setCreatedAt(man);
		plaw.setUpdatedAt(man);
		plsw.setStatus("NOVO");
		plsw.setCreatedAt(man);
		plsw.setUpdatedAt(man);
		pllot.setStatus("NOVO");
		pllot.setCreatedAt(man);
		pllot.setUpdatedAt(man);
		plman.setStatus("NOVO");
		plman.setCreatedAt(man);
		plman.setUpdatedAt(man);
		plexp.setStatus("NOVO");
		plexp.setCreatedAt(man);
		plexp.setUpdatedAt(man);
		plqty.setStatus("NOVO");
		plqty.setCreatedAt(man);
		plqty.setUpdatedAt(man);
		ret.add(plsw);
		ret.add(plaw);
		ret.add(pllot);
		ret.add(plman);
		ret.add(plexp);
		ret.add(plqty);
		return ret;
	}
}
