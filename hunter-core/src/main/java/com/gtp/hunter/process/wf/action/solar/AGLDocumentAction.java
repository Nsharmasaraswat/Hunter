package com.gtp.hunter.process.wf.action.solar;

import java.lang.invoke.MethodHandles;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.jsonstubs.AGLDocModelField;
import com.gtp.hunter.process.jsonstubs.AGLDocModelForm;
import com.gtp.hunter.process.jsonstubs.AGLDocModelProps;
import com.gtp.hunter.process.jsonstubs.AGLDocTransport;
import com.gtp.hunter.process.jsonstubs.AGLThing;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.DocumentTransport;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.DocumentAction;

public abstract class AGLDocumentAction extends DocumentAction {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private AGLDocModelForm					translate;
	private AGLDocModelProps				transpop;

	public AGLDocumentAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
		translate = regSvc.getAglSvc().convertDocToAgl(getDoc());
		transpop = regSvc.getAglSvc().convertPropDocToAgl(getDoc());
	}

	public AGLDocModelForm getTranslate() {
		return translate;
	}

	public void setTranslate(AGLDocModelForm translate) {
		this.translate = translate;
	}

	@Transactional(value = TxType.REQUIRED)
	protected Document salvadoc(String msg) {
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		AGLDocModelForm obj = gson.fromJson(msg, AGLDocModelForm.class);

		if (obj.getId() != null && !obj.getId().isEmpty()) {
			logger.info("Document " + obj.getMetaname() + " " + obj.getCode());
			List<Thing> dirttList = new ArrayList<>();
			Document d = getRegSvc().getDcSvc().findById(UUID.fromString(obj.getId()));

			if (d == null) {
				logger.info("Procurando dm " + obj.getMetaname());
				DocumentModel dm = getRegSvc().getDmSvc().findByMetaname(obj.getMetaname().toUpperCase());

				logger.info("DocumentModel " + dm.getMetaname());
				d = new Document(dm, dm.getName(), obj.getCode(), obj.getStatus());
				d.setId(UUID.fromString(obj.getId()));
				getRegSvc().getDcSvc().dirtyFullInsert(d, false);
				d = getRegSvc().getDcSvc().findById(UUID.fromString(obj.getId()));
			}
			//Fucking Trigger
			if (obj.getMetaname().equals("ORDCONF") && obj.getStatus().equals("SUCESSO"))
				d.setStatus("TEMPSUCESSO");
			else
				d.setStatus(obj.getStatus());
			if (obj.getProps() != null && obj.getProps().size() > 0) {
				for (String key : obj.getProps().keySet()) {
					String value = obj.getProps().get(key);
					Optional<DocumentModelField> optDMF = d.getModel().getFields().stream().filter(dmf -> dmf.getMetaname().equalsIgnoreCase(key)).findFirst();

					if (optDMF.isPresent()) {
						DocumentModelField dmf = optDMF.get();
						Optional<DocumentField> optDF = d.getFields().stream().filter(df -> df.getField().getId().equals(dmf.getId())).findFirst();

						if (optDF.isPresent()) {
							DocumentField df = optDF.get();

							df.setValue(value);
							getRegSvc().getDfSvc().persist(df);
						} else {
							DocumentField df = new DocumentField();
							df.setDocument(d);
							df.setField(dmf);
							df.setValue(value);
							getRegSvc().getDfSvc().persist(df);
						}
					}
				}
			}
			for (DocumentModelField dmf : d.getModel().getFields()) {
				logger.info("DocumentModelField " + dmf.getMetaname());
				for (AGLDocModelField admf : obj.getModel()) {
					if (admf.getAttrib().equals(dmf.getMetaname())) {
						boolean insert = true;
						for (DocumentField df : d.getFields()) {
							if (df.getField().equals(dmf)) {
								df.setValue(admf.getValue());
								getRegSvc().getDfSvc().persist(df);
								insert = false;
								break;
							}
						}
						if (insert) {
							DocumentField df = new DocumentField();
							df.setDocument(d);
							df.setField(dmf);
							df.setValue(admf.getValue());
							getRegSvc().getDfSvc().persist(df);
						}
						break;
					}
				}
			}
			getRegSvc().getDcSvc().dirtyFullInsert(d, false);
			for (AGLDocModelForm sib : obj.getSiblings()) {
				Document dSib = salvadoc(gson.toJson(sib));

				dSib.setStatus(sib.getStatus());
				dSib.setParent(d);
				dSib.setUser(getUser());
				if (sib.getMetaname().equalsIgnoreCase("RETORDCONF")) {//TODO: this is crazy man!!!
					SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
					SimpleDateFormat LOT_FORMAT = new SimpleDateFormat("ddMMyy");
					Document transp = getRegSvc().getDcSvc().findById(getRegSvc().getDcSvc().quickFindParentDoc(obj.getId()).getId());
					List<Document> nfents = transp.getSiblings().stream().filter(ds -> ds.getModel().getMetaname().equals("NFENTRADA")).collect(Collectors.toList());
					Optional<DocumentField> optOrigin = nfents.stream().flatMap(nfe -> nfe.getFields().stream()).filter(df -> df.getField().getMetaname().equals("ORIGIN")).findFirst();
					String pfx = optOrigin.isPresent() ? optOrigin.get().getValue() : ConfigUtil.get("hunter-custom-solar", "external-plant", "EXTR");
					boolean ePAPD = d.getFields().stream().filter(df -> df.getField().getMetaname().equals("CONF_TYPE")).anyMatch(df -> df.getValue().equals("EPAPD"));
					Address a = getRegSvc().getAddSvc().findByName("ALMOXARIFADO");
					DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
					DecimalFormat DF = new DecimalFormat("0.0000", symbols);
					Set<ConfProduct> conference = new HashSet<>();

					DF.setRoundingMode(RoundingMode.FLOOR);
					for (AGLThing t : sib.getThings()) {
						String lotId = "";
						Product lastProduct = null;
						Address lastAddress = a;
						if (ePAPD && d.getStatus().equals("TEMPSUCESSO") && dSib.getStatus().equals("SUCESSO"))
							dirttList.add(getRegSvc().getAglSvc().convertAGLThingToThing(t));

						for (AGLThing ts : t.getSiblings()) {
							Product p = lastProduct;
							Address as = lastAddress;
							if (p == null || !p.getId().equals(UUID.fromString(ts.getProduct_id()))) p = getRegSvc().getPrdSvc().findById(UUID.fromString(ts.getProduct_id()));
							if (as == null || !as.getId().equals(UUID.fromString(ts.getAddress_id()))) as = getRegSvc().getAddSvc().findById(UUID.fromString(ts.getAddress_id()));
							if (p != null && ts != null && t != null && t.getProps() != null && ts.getProps() != null && t.getProps().containsKey("lot_id") && t.getProps().containsKey("manufacturing_batch") && ts.getProps().containsKey("manufacturing_batch")) {
								String mfgLot = ts.getProps().get("manufacturing_batch");
								try {
									lotId = pfx + LOT_FORMAT.format(DATE_FORMAT.parse(mfgLot)) + p.getSku();
									if (t.getProps().get("lot_id").equals("TEMPORARIO")) {
										t.getProps().put("lot_id", lotId);
										logger.debug("Changing LOT ID to " + lotId + " for AGLthing " + t.getId());
									}
									if (ts.getProps().get("lot_id").equals("TEMPORARIO")) {
										ts.getProps().put("lot_id", lotId);
										logger.debug("Changing LOT ID to " + lotId + " for AGLthing sib " + ts.getId());
									}
								} catch (ParseException pe) {
									logger.error("Wrong Manufacturing Date " + mfgLot);
								} catch (NullPointerException npe) {
									logger.error("Can't change lot of thing " + t.getId() + " NPE");
									for (StackTraceElement ste : npe.getStackTrace())
										logger.error(ste.getFileName() + ":" + ste.getLineNumber());
								}
							}
							ConfProduct cProd = new ConfProduct(p, as, 0d, ts.getProps());
							Double countQty = Double.valueOf(ts.getProps().get("quantity").replace(",", "."));

							if ((d.getStatus().equals("TEMPSUCESSO") || d.getStatus().equals("SUCESSO")) && dSib.getStatus().equals("SUCESSO")) {
								if (ts.getAddress_id() == null || ts.getAddress_id().isEmpty()) {
									t.setAddress_id(a.getId().toString());
									ts.setAddress_id(a.getId().toString());
								}
								if (!ePAPD)
									dirttList.add(getRegSvc().getAglSvc().convertAGLThingToThing(ts));
							}

							lastProduct = p;
							lastAddress = as;
							cProd.addConferenceCount(countQty);
							conference.add(cProd);
						}
						if (ePAPD) {
							Thing st = getRegSvc().getThSvc().findById(UUID.fromString(t.getId()));
							final String lot = lotId;

							Stream<Property> prStream = Stream.concat(st.getProperties()
											.stream()
											.filter(pr -> pr.getField().getMetaname().equals("LOT_ID")),
											st.getSiblings()
															.stream()
															.flatMap(sts -> sts.getProperties()
																			.stream()
																			.filter(pr -> pr.getField().getMetaname().equals("LOT_ID"))));
							prStream.forEach(pr -> {
								pr.setValue(lot);
								getRegSvc().getPrSvc().quickUpdateValue(pr);
							});
						}
					}
					for (Thing dirty_ht : dirttList) {
						Thing ht = getRegSvc().getThSvc().findById(dirty_ht.getId());
						DocumentThing tmpDt = getRegSvc().getDtSvc().findByDocumentAndThing(dSib, ht);

						if (tmpDt == null) {
							DocumentThing dt = new DocumentThing(dSib, ht, dSib.getStatus());

							dt.setName(ht.getName());
							dt.setCreatedAt(Calendar.getInstance().getTime());
							dt.setUpdatedAt(Calendar.getInstance().getTime());
							dt.setDocument(dSib);
							getRegSvc().getDtSvc().persist(dt);
						} else {
							tmpDt.setStatus(ht.getStatus());
							getRegSvc().getDtSvc().persist(tmpDt);
						}
					}
					dSib.setThings(new HashSet<>(getRegSvc().getDtSvc().listByDocumentId(dSib.getId())));

					for (ConfProduct cPrd : conference) {
						DocumentItem di = new DocumentItem();
						Product prd = cPrd.getProduct();
						Address add = cPrd.getAddress();
						Map<String, String> diProps = cPrd.getProperties();

						sib.getThings().stream().flatMap(ct -> ct.getSiblings().stream()).forEach(at -> {
							ConfProduct tmp = new ConfProduct(prd, add, 0d, at.getProps());

							if (cPrd.equals(tmp)) {
								cPrd.setVolumeCount(cPrd.getVolumeCount() + 1);
							}
						});
						diProps.put("volumes", String.valueOf(cPrd.getVolumeCount()));
						di.setProduct(prd);
						di.setQty(Double.parseDouble(DF.format(cPrd.getConferenceCount() * cPrd.getVolumeCount()).replace(",", ".")));
						di.setDocument(dSib);
						di.setProperties(diProps);
						dSib.getItems().add(di);
						getRegSvc().getDiSvc().persist(di);
					}
				}
				logger.info("Sibling: " + sib.getMetaname() + " " + sib.getName() + " " + sib.getCode());
				dSib = getRegSvc().getDcSvc().dirtyFullInsert(dSib, false);
			}
			for (AGLDocTransport agldTr : obj.getTransport()) {
				DocumentTransport dtr = getRegSvc().getAglSvc().convertAGLTransportToTransport(agldTr, d.getId());
				Optional<DocumentTransport> optDtr = d.getTransports().stream().filter(dtrr -> dtrr.getId().equals(dtr.getId())).findAny();

				if (optDtr.isPresent()) d.setTransports(d.getTransports().stream().filter(dtrr -> !dtrr.getId().equals(dtr.getId())).collect(Collectors.toSet()));
				d.getTransports().add(getRegSvc().getDtrSvc().findById(dtr.getId()));
			}
			for (AGLThing t : obj.getThings()) {
				if (!dirttList.stream().anyMatch(dirty -> dirty.getId() != null && dirty.getId().toString().equals(t.getId()))) {
					if (t.getName() == null || !t.getName().equals("CONTAINER")) {
						Thing dirty_ht = getRegSvc().getAglSvc().convertAGLThingToThing(t);
						Thing ht = getRegSvc().getThSvc().refresh(dirty_ht);

						if (ht.getId() != null && d.getId() != null) {
							final UUID dId = d.getId();
							Optional<DocumentThing> optDT = d.getThings().stream().filter(dtt -> dtt.getThing().getId().equals(ht.getId()) && dtt.getDocument().getId().equals(dId)).findFirst();

							if (!optDT.isPresent()) {
								DocumentThing dt = getRegSvc().getDcSvc().createDocumentThing(d, ht);

								d.getThings().add(dt);
							}
						}
					}
				}
			}
			return getRegSvc().getDcSvc().persist(d);
		}
		return null;
	}

	private class ConfProduct {
		private Product				p;
		private double				confCount;
		private long				volumeCount;
		private Address				a;
		private Map<String, String>	props;

		public ConfProduct(Product prd, Address a, Double cnt, Map<String, String> properties) {
			this.a = a;
			this.p = prd;
			this.confCount = cnt;
			this.props = properties;
			this.volumeCount = 0;
		}

		@Override
		public int hashCode() {
			int idRet = p == null ? 0 : (p.getId() == null ? 1 : p.getId().hashCode());

			idRet += a == null ? 0 : (a.getId() == null ? 1 : a.getId().hashCode());
			for (String key : this.props.keySet()) {
				idRet += key.hashCode();
				idRet += this.props.get(key).hashCode();
			}
			return idRet;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof ConfProduct) {
				ConfProduct other = (ConfProduct) o;
				boolean sameProduct = this.getProduct().getId().equals(other.getProduct().getId());
				boolean samePropertyKeys = other.getProperties().equals(this.getProperties());
				boolean sameProperties = samePropertyKeys && this.getProperties().keySet().stream().allMatch(k -> other.getProperties().get(k).equals(this.getProperties().get(k)));
				boolean sameAddress = (this.getAddress() != null && other.getAddress() != null && this.getAddress().getId().equals(other.getAddress().getId())) || (this.getAddress() == null && other.getAddress() == null);

				return sameProduct && sameProperties && sameAddress;
			}
			return false;
		}

		/**
		 * @return the p
		 */
		public Product getProduct() {
			return p;
		}

		/**
		 * @return the confCount
		 */
		public double getConferenceCount() {
			return confCount;
		}

		/**
		 * @return the volumes
		 */
		public long getVolumeCount() {
			return volumeCount;
		}

		/**
		 * add a volume
		 */
		public void setVolumeCount(long volumeCount) {
			this.volumeCount = volumeCount;
		}

		/**
		 * @param confCount the confCount to add
		 */
		public void addConferenceCount(double confCount) {
			this.confCount += confCount;
		}

		/**
		 * @return the props
		 */
		public Map<String, String> getProperties() {
			return props;
		}

		/**
		 * @return the a
		 */
		public Address getAddress() {
			return a;
		}

		@Override
		public String toString() {
			String sku = p == null ? "-" : p.getSku();
			String name = p == null ? "-" : p.getName();
			String loc = a == null ? "..." : a.getName();
			return sku + name + " on " + loc + confCount + volumeCount;
		}
	}

	protected void salvapop(String msg) throws Exception {

		AGLDocModelProps obj = new Gson().fromJson(msg, AGLDocModelProps.class);

		Document d = getRegSvc().getDcSvc().findById(UUID.fromString(obj.getId()));

		for (DocumentModelField dmf : d.getModel().getFields()) {
			for (AGLDocModelField admf : obj.getProps()) {
				if (admf.getAttrib().equalsIgnoreCase(dmf.getMetaname())) {
					boolean insert = true;

					for (DocumentField df : d.getFields()) {
						if (df.getField().equals(dmf)) {
							df.setValue(admf.getValue());
							getRegSvc().getDfSvc().persist(df);
							insert = false;
							break;
						}
					}
					if (insert) {
						DocumentField df = new DocumentField(d, dmf, "NOVO", admf.getValue());

						getRegSvc().getDfSvc().persist(df);
					}
					break;
				}
			}
		}
	}

	public AGLDocModelProps getTranspop() {
		return transpop;
	}

	public void setTranspop(AGLDocModelProps transpop) {
		this.transpop = transpop;
	}

}
