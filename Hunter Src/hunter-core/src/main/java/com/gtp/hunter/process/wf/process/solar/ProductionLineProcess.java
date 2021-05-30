/**
 * 
 */
package com.gtp.hunter.process.wf.process.solar;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.model.RawData.RawDataType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.model.Prefix;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.DocumentTransport;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductField;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModel;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Products;
import com.gtp.hunter.process.wf.process.ContinuousProcess;
import com.gtp.hunter.ui.json.process.BaseProcessMessage;
import com.gtp.hunter.ui.json.process.ProcessNotification;
import com.gtp.hunter.ui.json.process.drone.ProcessMessage;

/**
 * @author Mateus Tormin
 *
 */
public class ProductionLineProcess extends ContinuousProcess implements ProductionLineInterface {

	private transient static final Logger	logger					= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String				PLANT_PRODUCTION_CODE	= ConfigUtil.get("hunter-custom-solar", "plant_production_code", "CNAT");
	private static final SimpleDateFormat	DATE_FORMAT				= new SimpleDateFormat("dd/MM/yyyy");
	private static final SimpleDateFormat	LOT_FORMAT				= new SimpleDateFormat("ddMMyy");
	private static final DecimalFormat		DF						= new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.US));

	private static final UUID				PD_ADDRESS_ID			= UUID.fromString("b5044db5-cdd5-11e9-90f5-005056a19775");
	private static final UUID				EC_ADDRESS_ID			= UUID.fromString("9b264265-d01b-11e9-90f5-005056a19775");
	private static final UUID				LO_ADDRESS_ID			= UUID.fromString("7c38cf65-da11-11e9-90f5-005056a19775");

	private DocumentModel					ordprodModel;
	private Document						prodOrder;
	private int								packCount;
	private int								palletCount;
	private DocumentModel					ordCriacaoModel;
	private static Product					prd_pallet;
	private static PropertyModelField		prmfPalletSTRWGHT;
	private static PropertyModelField		prmfPalletACTWGHT;
	private static PropertyModelField		prmfPalletEXP;
	private static PropertyModelField		prmfPalletMAN;
	private static PropertyModelField		prmfPalletQTY;
	private static PropertyModelField		prmfPalletLOT;
	private static Product					prd_eucatex;
	private static Product					prd_pallet_leftover;
	private DocumentField					dfPltCnt;
	private DocumentField					dfBxCnt;
	private Product							prd;
	private List<Integer>					boxCountIds;
	private Map<Integer, Address>			dividerStockMap;
	private Map<Integer, Address>			palletStockMap;
	private Map<Integer, Address>			inboundMPMap;
	private Map<Integer, Address>			leftoverMap;
	private Map<Integer, Address>			lineEndMap;
	private Map<Integer, Integer>			statusMap;
	private boolean							verbose					= false;

	public ProductionLineProcess() {

	}

	@Override
	protected void checkParams() throws Exception {
		if (!getParametros().containsKey("ordprod-meta")) throw new Exception("Parâmetro 'ordprod-meta' não encontrado.");
		if (!getParametros().containsKey("ordprod-status")) throw new Exception("Parâmetro 'ordprod-status' não encontrado.");
		if (!getParametros().containsKey("document-meta")) throw new Exception("Parâmetro 'document-meta' não encontrado.");
		if (!getParametros().containsKey("pallet-prd-id")) throw new Exception("Parâmetro 'pallet-prd-id' não encontrado.");
		if (!getParametros().containsKey("eucatex-prd-id")) throw new Exception("Parâmetro 'eucatex-prd-id' não encontrado.");
		if (!getParametros().containsKey("leftover-prd-id")) throw new Exception("Parâmetro 'pallet-leftover-prd-id' não encontrado.");
		if (!getParametros().containsKey("starting-weight-property-meta")) throw new Exception("Parâmetro 'starting-weight-property-meta' não encontrado.");
		if (!getParametros().containsKey("actual-weight-property-meta")) throw new Exception("Parâmetro 'actual-weight-property-meta' não encontrado.");
		if (!getParametros().containsKey("line-number")) throw new Exception("Parâmetro 'line-number' não encontrado.");
		if (!getParametros().containsKey("in-mp-mapping")) throw new Exception("Parâmetro 'in-mp-mapping' não encontrado.");
		if (!getParametros().containsKey("pallet-stock-mapping")) throw new Exception("Parâmetro 'pallet-stock-mapping' não encontrado.");
		if (!getParametros().containsKey("divider-stock-mapping")) throw new Exception("Parâmetro 'divider-stock-mapping' não encontrado.");
		if (!getParametros().containsKey("line-end-mapping")) throw new Exception("Parâmetro 'line-end-mapping' não encontrado.");
		if (!getParametros().containsKey("box-count-ids")) throw new Exception("Parâmetro 'box-count-ids' não encontrado.");
		if (!getParametros().containsKey("status-thing-to")) throw new Exception("Parâmetro 'status-thing-to' não encontrado");
		if (!getParametros().containsKey("prodln-field-meta")) throw new Exception("Parâmetro 'prodln-field-meta' não encontrado");
		if (!getParametros().containsKey("prodln-field-value")) throw new Exception("Parâmetro 'prodln-field-value' não encontrado");
	}

	private Map<Integer, Address> extractMap(Map<String, Object> param) {
		return param.entrySet().stream()
						.collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), e -> getRegSvc().getAddSvc().findById(UUID.fromString(((String) e.getValue())))));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onInit() {
		statusMap = new HashMap<>();
		ordprodModel = getRegSvc().getDmSvc().findByMetaname((String) getParametros().get("ordprod-meta"));
		ordCriacaoModel = getRegSvc().getDmSvc().findByMetaname((String) getParametros().get("document-meta"));
		boxCountIds = ((List<Object>) getParametros().get("box-count-ids")).stream().map(o -> ((JsonNumber) o).intValue()).collect(Collectors.toList());
		inboundMPMap = extractMap((Map<String, Object>) getParametros().get("in-mp-mapping"));
		palletStockMap = extractMap((Map<String, Object>) getParametros().get("pallet-stock-mapping"));
		dividerStockMap = extractMap((Map<String, Object>) getParametros().get("divider-stock-mapping"));
		leftoverMap = extractMap((Map<String, Object>) getParametros().get("leftover-mapping"));
		lineEndMap = extractMap((Map<String, Object>) getParametros().get("line-end-mapping"));
		if (prd_pallet == null) {
			prd_pallet = getRegSvc().getPrdSvc().findById(UUID.fromString((String) getParametros().get("pallet-prd-id")));
			PropertyModel prm_pallet = prd_pallet.getModel().getPropertymodel();
			prmfPalletSTRWGHT = prm_pallet.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("STARTING_WEIGHT")).findAny().get();
			prmfPalletACTWGHT = prm_pallet.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("ACTUAL_WEIGHT")).findAny().get();
			prmfPalletEXP = prm_pallet.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("LOT_EXPIRE")).findAny().get();
			prmfPalletMAN = prm_pallet.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("MANUFACTURING_BATCH")).findAny().get();
			prmfPalletQTY = prm_pallet.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("QUANTITY")).findAny().get();
			prmfPalletLOT = prm_pallet.getFields().parallelStream().filter(prmf -> prmf.getMetaname().equals("LOT_ID")).findAny().get();
		}
		if (prd_eucatex == null) {
			prd_eucatex = getRegSvc().getPrdSvc().findById(UUID.fromString((String) getParametros().get("eucatex-prd-id")));
		}
		if (prd_pallet_leftover == null) {
			prd_pallet_leftover = getRegSvc().getPrdSvc().findById(UUID.fromString((String) getParametros().get("leftover-prd-id")));
		}
		startProduction(getRegSvc().getDcSvc().findLastByTypeAndStatusAndFieldValue(ordprodModel, "ATIVO", (String) getParametros().get("prodln-field-meta"), (String) getParametros().get("prodln-field-value")));
		logger.info("Process Initialized!");
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#connect()
	 */
	@Override
	protected void connect() {
		logger.info("Connected on " + (prodOrder != null ? prodOrder.getCode() : getModel().getName()));
		updateProdCounters();
		List<Integer> allIds = new ArrayList<Integer>(boxCountIds);

		allIds.addAll(lineEndMap.keySet());
		allIds.addAll(leftoverMap.keySet());
		allIds.addAll(inboundMPMap.keySet());
		allIds.addAll(palletStockMap.keySet());
		allIds.addAll(dividerStockMap.keySet());
		this.getParametros().put("request", allIds);
		this.getParametros().put("read", allIds);
		this.start();
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#timeout(java.util.Map)
	 */
	@Override
	public void timeout(Map<String, Thing> itens) {
		try {
			if (itens != null) {
				if (prodOrder != null) {
					if (!itens.keySet().isEmpty()) logger.info(prodOrder.getName() + " - timeout: " + itens.keySet().size());
					for (String tagId : itens.keySet()) {
						Thing t = itens.get(tagId);

						if (this.prodOrder.getItems().stream().anyMatch(di -> di.getProduct().getId().equals(t.getProduct().getId()))) {
							if (!this.prodOrder.getThings().stream().anyMatch(dt -> dt.getThing().getId().equals(t.getId()))) createDocThing(t);
							t.setDocument(prodOrder.getId());
							resend(t);
						} else {
							logger.info("Produto não pertencente à " + this.prodOrder.getCode());
						}
					}
				}
				this.runSucess();
			}
			this.unlock();
		} catch (Exception ex) {
			this.lockdown(ex.getLocalizedMessage());
			logger.error(ex.getLocalizedMessage(), ex);
		}
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#processBefore(com.gtp.hunter.core.model.ComplexData)
	 */
	@Override
	protected void processBefore(ComplexData rd) {
		verbose = ConfigUtil.get("hunter-process", "verbose-process", "true").equalsIgnoreCase("true");
		try {
			if (prodOrder != null) {
				if (rd.getType() != RawDataType.IDENT) {
					Map<String, Object> props = new LinkedHashMap<>();
					JsonReader jsonReader = Json.createReader(new StringReader(rd.getPayload()));
					JsonObject payload = jsonReader.readObject();
					int status = payload.getInt("status", 0);
					Thing t = new Thing();

					jsonReader.close();
					if (rd.getType() == RawDataType.STATUS) {
						props.put("type", "STATUS");
						if (palletStockMap.containsKey(rd.getPort())) {
							if (verbose) logger.info(logPrefix() + "PalletStock: " + status);
							if (status == 1)
								props.put("message", "Estoque Pallet OK");
							else if (status == 0) {
								if (statusMap.containsKey(rd.getPort()) && statusMap.get(rd.getPort()) != status) {
									Address orig = getRegSvc().getAddSvc().findById(PD_ADDRESS_ID);

									restock(prd_pallet, orig, palletStockMap.get(rd.getPort()), "RST", "RESTOCK");
								}
								props.put("message", "Faltando Pallet");
							}
						} else if (dividerStockMap.containsKey(rd.getPort())) {
							if (verbose) logger.info(logPrefix() + "DividerStock: " + status);
							if (status == 1)
								props.put("message", "Estoque Eucatex OK");
							else if (status == 0) {
								if (statusMap.containsKey(rd.getPort()) && statusMap.get(rd.getPort()) != status) {
									Address orig = getRegSvc().getAddSvc().findById(PD_ADDRESS_ID);

									restock(prd_eucatex, orig, dividerStockMap.get(rd.getPort()), "RST", "RESTOCK");
								}
								props.put("message", "Faltando Eucatex");
							}
						} else if (leftoverMap.containsKey(rd.getPort())) {
							if (verbose) logger.info(logPrefix() + "LeftoverStock: " + status);
							if (status == 0)
								props.put("message", "Estoque Sobras OK");
							else if (status == 1) {
								if (statusMap.containsKey(rd.getPort()) && statusMap.get(rd.getPort()) != status) {
									Address dest = getRegSvc().getAddSvc().findById(LO_ADDRESS_ID);

									restock(prd_pallet_leftover, leftoverMap.get(rd.getPort()), dest, "RST", "RESTOCK");
								}
								props.put("message", "Estoque Sobras Cheio");
							}
						} else if (inboundMPMap.containsKey(rd.getPort())) {
							if (verbose) logger.info(logPrefix() + "PMStock: " + status);
							if (status == 1)
								props.put("message", "Estoque Matéria Prima OK");
							else if (status == 0) {
								if (statusMap.containsKey(rd.getPort()) && statusMap.get(rd.getPort()) != status) {
									Address orig = getRegSvc().getAddSvc().findById(EC_ADDRESS_ID);
									Optional<Product> optPrd = this.prodOrder.getItems().stream()
													.filter(di -> !di.getProduct().getId().equals(prd.getId()))//Item em producao
													.filter(di -> di.getProduct().getName().contains(" LT ") || di.getProduct().getName().contains(" LATA ") || di.getProduct().getName().startsWith("LT ") || di.getProduct().getName().startsWith("LATA "))//Comeca ou contem lata
													.filter(di -> !(di.getProduct().getName().startsWith("TAMPA ") || di.getProduct().getName().startsWith("TP ")))//Nao comeca com TP ou TAMPA
													.map(di -> di.getProduct())
													.findFirst();

									if (optPrd.isPresent()) {
										restock(optPrd.get(), orig, inboundMPMap.get(rd.getPort()), "RST", "RESTOCK");
									}
								}
								props.put("message", "Faltando Matéria Prima");
							}
						} else if (lineEndMap.containsKey(rd.getPort())) {
							if (statusMap.containsKey(rd.getPort())) {
								//GenerateOrdCriacao
								if (status == 1 && statusMap.get(rd.getPort()) != status) {
									Address orig = lineEndMap.get(rd.getPort());
									Thing t1 = createPallet(prd, orig);
									Thing tPrd = t1.getSiblings().stream().findFirst().get();

									if (prodOrder.getSiblings().parallelStream().anyMatch(ds -> ds.getModel().getMetaname().equals("ORDCRIACAO")))
										recordProduction(prd, Products.getIntegerField(prd, "PALLET_BOX", 1), false);
									t1.setDocument(this.prodOrder.getId());
									tPrd.setDocument(this.prodOrder.getId());
									resend(tPrd);
									this.prodOrder.getSiblings().add(getRegSvc().getDcSvc().createDocumentWithThing(this.prodOrder, ordCriacaoModel, "NOVO", orig, getRegSvc().getPfxSvc().findNext("OCR", 9), t1, getModel().getEstadoPara()));
								}
							}
						}
						if (verbose) logger.info(logPrefix() + rd.getPort() + " Status: " + status + " Last: " + (statusMap.containsKey(rd.getPort()) ? statusMap.get(rd.getPort()) : "NULL"));
						statusMap.put(rd.getPort(), status);
					} else if (rd.getType() == RawDataType.SENSOR) {
						props.put("type", "SENSOR");
						props.put("count", status);
						if (boxCountIds.contains(rd.getPort())) {
							props.put("message", "Qtd Caixa: " + status);
							this.packCount = status;
							if (verbose) logger.info(logPrefix() + "Box Count: " + status);
						} else if (lineEndMap.containsKey(rd.getPort())) {
							props.put("message", "Qtd Pallet: " + status);
							this.palletCount = status;
							statusMap.put(rd.getPort(), 0);
							if (verbose) logger.info(logPrefix() + "Pallet Count: " + status);
						} else if (verbose) logger.warn(logPrefix() + "Not Mapped: " + rd.getPort());
					}
					props.put("sensor-id", rd.getPort());
					props.put("sensor-value", status);
					String pl = new GsonBuilder().enableComplexMapKeySerialization().create().toJson(props);

					t.setPayload(pl);
					t.setDocument(prodOrder.getId());
					resend(t);
				}
			} else if (verbose) logger.warn(logPrefix() + "Production Stopped");
		} catch (Exception e) {
			this.lockdown(e.getLocalizedMessage());
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#processAfter(com.gtp.hunter.process.model.Thing)
	 */
	@Override
	protected void processAfter(Thing rd) {
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#processUnknown(com.gtp.hunter.core.model.ComplexData)
	 */
	@Override
	protected void processUnknown(ComplexData rd) {
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#success()
	 */
	@Override
	protected void success() {
		logger.info("SUCCESS");
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.ContinuousProcess#failure()
	 */
	@Override
	protected void failure() {
		logger.warn("FAILURE");
	}

	/* (non-Javadoc)
	 * @see com.gtp.hunter.process.wf.process.BaseProcess#cancel()
	 */
	@Override
	public void cancel() {
		logger.warn("CANCEL");
	}

	@Override
	public boolean resetCounters() {
		List<Integer> resetIds = new ArrayList<Integer>(boxCountIds);

		resetIds.addAll(lineEndMap.keySet());
		this.palletCount = 0;
		this.packCount = 0;
		this.getParametros().put("reset", resetIds);
		this.start();
		return true;
	}

	@Override
	public boolean startProduction(Document tmpProdOrder) {
		if (tmpProdOrder != null) {
			prodOrder = tmpProdOrder;
			logger.info("ProdOrder Set to " + prodOrder.getId().toString());
			prodOrder.getFields().addAll(getRegSvc().getDfSvc().listByDocumentId(prodOrder.getId()));
			prd = prodOrder.getItems().stream().filter(di -> di.getProperties().get("PRODUCAO").equalsIgnoreCase("PRODUCAO")).findAny().get().getProduct();
			this.resetCounters();
		}
		logger.info("Document Reloaded: " + (prodOrder == null ? "Production Stopped" : prodOrder.getName()));
		return true;
	}

	@Override
	public boolean stopProduction() {
		if (prodOrder != null) {
			if (fixLeftover()) {
				this.palletCount = 0;
				this.packCount = 0;
				logger.info("Releasing prodOrder " + prodOrder.getId().toString());
				this.prodOrder = null;
				this.prd = null;
			} else
				throw new RuntimeException("Error fixing last pallet");
		}
		return true;
	}

	@Override
	public Document getProductionOrder() {
		return prodOrder;
	}

	private Document restock(Product prd, Address orig, Address dest, String pfx, String status) {
		DocumentModel dmOrdMov = getRegSvc().getDmSvc().findByMetaname("ORDMOV");
		DocumentModelField dmPri = dmOrdMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("PRIORITY")).findAny().get();
		DocumentModelField dmType = dmOrdMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TYPE")).findAny().get();
		DocumentModelField dmTitle = dmOrdMov.getFields().parallelStream().filter(dmf -> dmf.getMetaname().equals("MOV_TITLE")).findAny().get();

		Prefix prefix = getRegSvc().getPfxSvc().findNext(pfx, 9);

		Document ordMov = new Document(dmOrdMov, dmOrdMov.getName() + " " + prefix.getCode(), prefix.getPrefix() + prefix.getCode(), status);
		Thing t = createPallet(prd, orig);

		ordMov.setParent(this.prodOrder);
		ordMov.getItems().add(new DocumentItem(ordMov, prd, 1d, "NOVO", "UN"));
		ordMov.getThings().add(new DocumentThing(ordMov, t, "NOVO"));
		ordMov.getFields().add(new DocumentField(ordMov, dmPri, "NOVO", "4"));
		ordMov.getFields().add(new DocumentField(ordMov, dmType, "NOVO", "RESTOCK"));
		ordMov.getFields().add(new DocumentField(ordMov, dmTitle, "NOVO", "LINHA: REABASTECIMENTO"));
		ordMov.getTransports().add(new DocumentTransport(ordMov, 1, t, dest));
		getRegSvc().getDcSvc().persist(ordMov);
		return ordMov;
	}

	private Thing createPallet(Product prd, Address linhaaddr) {
		Calendar expCal = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		Thing pallet = new Thing(prd_pallet.getName(), prd_pallet, prd_pallet.getModel().getPropertymodel(), getModel().getEstadoPara());
		Thing t = new Thing(prd.getName(), prd, prd.getModel().getPropertymodel(), getModel().getEstadoPara());
		PropertyModel prm = prd.getModel().getPropertymodel();
		Supplier<Stream<PropertyModelField>> supPrm = () -> prm.getFields().stream();
		Supplier<Stream<ProductField>> supFields = () -> prd.getFields().stream();
		String lot = "PROD" + LOT_FORMAT.format(cal.getTime()) + Integer.parseInt(prd.getSku());
		String palletStrtWeight = prd_pallet.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("GROSS_WEIGHT")).findFirst().get().getValue();
		String palletActualWeight = prd_pallet.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("VAR_WEIGHT")).findFirst().get().getValue();
		String prdStrtWeight = supFields.get().filter(pf -> pf.getModel().getMetaname().equals("GROSS_WEIGHT")).findFirst().get().getValue();
		String prdActualWeight = supFields.get().filter(pf -> pf.getModel().getMetaname().equals("VAR_WEIGHT")).findFirst().get().getValue();
		String prdPalletBox = supFields.get().filter(pf -> pf.getModel().getMetaname().equals("PALLET_BOX")).findFirst().get().getValue();
		String shelfLife = supFields.get().filter(pf -> pf.getModel().getMetaname().equals("SHELFLIFE")).findFirst().get().getValue();
		PropertyModelField prmfSTRWGHT = supPrm.get().filter(prmf -> prmf.getMetaname().equals("STARTING_WEIGHT")).findFirst().get();
		PropertyModelField prmfACTWGHT = supPrm.get().filter(prmf -> prmf.getMetaname().equals("ACTUAL_WEIGHT")).findFirst().get();
		PropertyModelField prmfMAN = supPrm.get().filter(prmf -> prmf.getMetaname().equals("MANUFACTURING_BATCH")).findFirst().get();
		PropertyModelField prmfEXP = supPrm.get().filter(prmf -> prmf.getMetaname().equals("LOT_EXPIRE")).findFirst().get();
		PropertyModelField prmfQTY = supPrm.get().filter(prmf -> prmf.getMetaname().equals("QUANTITY")).findFirst().get();
		PropertyModelField prmfLOT = supPrm.get().filter(prmf -> prmf.getMetaname().equals("LOT_ID")).findFirst().get();

		try {
			expCal.add(Calendar.DAY_OF_MONTH, Integer.parseInt(shelfLife));
		} catch (NumberFormatException nfe) {
			expCal.add(Calendar.DAY_OF_MONTH, 60);
		}
		pallet.getProperties().addAll(getProperties(pallet, prmfPalletSTRWGHT, palletStrtWeight, prmfPalletACTWGHT, palletActualWeight, prmfPalletLOT, lot, prmfPalletMAN, cal.getTime(), prmfPalletEXP, expCal.getTime(), prmfPalletQTY, "1"));
		pallet.setAddress(linhaaddr);
		//getRegSvc().getThSvc().persist(pallet);
		t.setCreatedAt(cal.getTime());
		t.setUpdatedAt(cal.getTime());
		t.getProperties().addAll(getProperties(t, prmfSTRWGHT, prdStrtWeight, prmfACTWGHT, prdActualWeight, prmfLOT, lot, prmfMAN, cal.getTime(), prmfEXP, expCal.getTime(), prmfQTY, prdPalletBox == null || prdPalletBox.isEmpty() ? "1" : prdPalletBox));
		t.setParent(pallet);
		t.setAddress(linhaaddr);
		pallet.getSiblings().add(t);
		//getRegSvc().getThSvc().persist(t);
		getRegSvc().getThSvc().persist(pallet);
		if (dfPltCnt == null) updateProdCounters();
		dfPltCnt.setValue(String.valueOf(Integer.parseInt(dfPltCnt.getValue()) + 1));
		dfBxCnt.setValue(String.valueOf(Integer.parseInt(dfBxCnt.getValue()) + prd.getFields().stream()
						.filter(pf -> pf.getModel().getMetaname().equals("PALLET_BOX") && !pf.getValue().isEmpty())
						.map(pf -> Integer.parseInt(pf.getValue()))
						.findFirst()
						.orElse(0)));
		getRegSvc().getDfSvc().quickUpdateValue(dfPltCnt);
		getRegSvc().getDfSvc().quickUpdateValue(dfBxCnt);
		return pallet;
	}

	private Set<Property> getProperties(Thing t, PropertyModelField prmfSTRWGHT, String strtWeight, PropertyModelField prmfACTWGHT, String actualWeight, PropertyModelField prmfLOT, String lot, PropertyModelField prmfMAN, Date man, PropertyModelField prmfEXP, Date exp, PropertyModelField prmfQTY, String qty) {
		Set<Property> ret = new HashSet<>();
		Property plsw = new Property(t, prmfSTRWGHT, strtWeight);
		Property plaw = new Property(t, prmfACTWGHT, actualWeight);
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

	private void createDocThing(Thing t) {
		DocumentThing dt = new DocumentThing(this.prodOrder, t, (String) getParametros().get("status-thing-to"));

		t.setStatus((String) getParametros().get("status-thing-to"));
		dt.setCreatedAt(Calendar.getInstance().getTime());
		dt.setUpdatedAt(Calendar.getInstance().getTime());
		getRegSvc().getDtSvc().persist(dt);
		getRegSvc().getThSvc().persist(t);
		logger.info("Thing " + t.getName() + " adicionado a " + prodOrder.getFields().stream().filter(df -> df.getField().getMetaname().equalsIgnoreCase("LINHA_PROD")).findFirst().get().getValue());
		this.prodOrder.getThings().add(dt);
	}

	private void updateProdCounters() {
		if (prodOrder != null) {
			dfPltCnt = prodOrder.getFields().parallelStream()
							.filter(df -> df.getField().getMetaname().equals("PALLET_COUNT"))
							.findAny()
							.orElse(null);
			dfBxCnt = prodOrder.getFields().parallelStream()
							.filter(df -> df.getField().getMetaname().equals("BOX_COUNT"))
							.findAny()
							.orElse(null);
			long pltCnt = prodOrder.getSiblings().stream().filter(ds -> ds.getModel().getMetaname().equals("ORDCRIACAO")).count();
			long bxCnt = pltCnt * Products.getIntegerField(prd, "PALLET_BOX", 0);

			if (dfPltCnt == null) {
				DocumentModelField dmf = getRegSvc().getDmfSvc().findByModelAndMetaname(prodOrder.getModel(), "PALLET_COUNT");

				dfPltCnt = new DocumentField(prodOrder, dmf, "NOVO", "" + pltCnt);
				getRegSvc().getDfSvc().persist(dfPltCnt);
			} else if (!dfPltCnt.getValue().equals("" + pltCnt)) {
				dfPltCnt.setValue("" + pltCnt);
				getRegSvc().getDfSvc().quickUpdateValue(dfPltCnt);
			}
			if (dfBxCnt == null) {
				DocumentModelField dmf = getRegSvc().getDmfSvc().findByModelAndMetaname(prodOrder.getModel(), "BOX_COUNT");

				dfBxCnt = new DocumentField(prodOrder, dmf, "NOVO", "" + bxCnt);
				getRegSvc().getDfSvc().persist(dfBxCnt);
			} else if (!dfBxCnt.getValue().equals("" + bxCnt)) {
				dfBxCnt.setValue("" + bxCnt);
				getRegSvc().getDfSvc().quickUpdateValue(dfBxCnt);
			}
		}
	}

	@Override
	public void message(BaseProcessMessage msg) {
		super.message(msg);
		switch (msg.getCommand()) {
			case "GET_PACK_COUNT":
				ProcessMessage pmPk = new ProcessMessage("PACK_COUNT");

				pmPk.setData(this.packCount);
				resend(pmPk);
				break;
			case "GET_PALLET_COUNT":
				ProcessMessage pmPl = new ProcessMessage("PALLET_COUNT");

				pmPl.setData(this.palletCount);
				resend(pmPl);
				break;
			case "LAST_PALLET":
				fixLeftover();
				break;
			case "RESET_COUNTERS":
				resetCounters();
				break;
			case "RELOAD_DOCUMENT":
				ProcessNotification pn = new ProcessNotification();

				if (startProduction(getRegSvc().getDcSvc().findLastByTypeAndStatusAndFieldValue(ordprodModel, "ATIVO", (String) getParametros().get("prodln-field-meta"), (String) getParametros().get("prodln-field-value"))))
					pn.setData(this.prodOrder == null ? "NENHUMA ORDEM ABERTA" : this.prodOrder.getName());
				else
					pn.setData("Não foi possível reiniciar o processo");
				resend(pn);
				break;
			case "RECORD_PRODUCTION":
				ProcessNotification pnRecord = new ProcessNotification();
				Document record = recordProduction(prd, (double) msg.getData(), true);

				if (record != null) {
					pnRecord.setData(record.getName() + " Criado com Sucesso!");
				} else {
					pnRecord.setData("Não foi possível registrar a produção");
				}
				resend(pnRecord);
				break;
			default:
				logger.info("Message Received: " + msg.toString());
		}
	}

	@Override
	public Document recordProduction(Product prd, double quantity, boolean lastPallet) {
		if (this.prodOrder != null) {
			DocumentModel prodRecordMod = getRegSvc().getDmSvc().findByMetaname("APOPRODUCAO");
			Prefix recordPfx = getRegSvc().getPfxSvc().findNext("APRD", 10);
			Document dRecord = new Document(prodRecordMod, prodRecordMod.getName() + " - " + recordPfx.getCode(), recordPfx.getCode(), "NOVO");
			DocumentField dfBoxCount = this.prodOrder.getFields().parallelStream()
							.filter(df -> df.getField().getMetaname().equals(""))
							.findAny()
							.orElse(null);
			if (lastPallet) {
				Thing pl = this.prodOrder.getSiblings().parallelStream()
								.filter(ds -> ds.getModel().getMetaname().equals("ORDCRIACAO"))
								.sorted((d1, d2) -> d2.getCreatedAt().compareTo(d1.getCreatedAt()))
								.flatMap(d -> d.getThings().stream().map(dt -> dt.getThing()))
								.findFirst()
								.orElse(null);

				if (pl != null) {
					Property prQty = pl.getSiblings().parallelStream()
									.flatMap(ts -> ts.getProperties().parallelStream())
									.filter(pr -> pr.getField().getMetaname().equals("QUANTITY"))
									.findAny()
									.get();

					prQty.setValue(DF.format(quantity));
					getRegSvc().getPrSvc().quickUpdateValue(prQty);
					getRegSvc().getAglSvc().sendThingToWMS(pl, "PUT");
				}
			}
			if (dfBoxCount != null) {
				Integer currVal = Integer.parseInt(dfBoxCount.getValue());
				Integer palletBox = Products.getIntegerField(prd, "PALLET_BOX", 1);

				dfBoxCount.setValue(DF.format(currVal - palletBox + quantity));
				getRegSvc().getDfSvc().quickUpdateValue(dfBoxCount);
			}
			dRecord.setParent(this.prodOrder);
			dRecord.getItems().add(new DocumentItem(dRecord, prd, quantity, "NOVO"));
			return getRegSvc().getDcSvc().persist(dRecord);
		}
		return null;
	}

	private boolean fixLeftover() {
		if (this.prodOrder != null) {
			if (ConfigUtil.get("hunter-custom-solar", "fix-last-pallet", "TRUE").equalsIgnoreCase("TRUE")) {
				int palletBox = Products.getIntegerField(prd, "PALLET_BOX", 1);
				Document po = getRegSvc().getDcSvc().findById(this.prodOrder.getId());

				long countedPallets = this.palletCount;
				long createdPallets = po.getSiblings().parallelStream().filter(ds -> ds.getModel().getMetaname().equals("ORDCRIACAO")).count();
				long countedPacks = this.packCount;
				long createdPacks = po.getSiblings().parallelStream()
								.filter(ds -> ds.getModel().getMetaname().equals("ORDCRIACAO"))
								.flatMap(oc -> oc.getThings().parallelStream())
								.flatMap(dt -> dt.getThing().getSiblings().parallelStream())
								.flatMap(ts -> ts.getProperties().parallelStream())
								.filter(pr -> pr.getField().getMetaname().equals("QUANTITY") && !pr.getValue().isEmpty())
								.mapToLong(prq -> new Double(Double.parseDouble(prq.getValue())).longValue())
								.sum();

				logger.info("countedPallets: " + countedPallets + " createdPallets: " + createdPallets + " countedPacks " + countedPacks + " createdPacks " + createdPacks);
				if (createdPacks != countedPacks) {
					long expectedCountedPacks = countedPallets * palletBox;
					long countedFullPallets = Math.floorDiv(countedPacks, palletBox);
					long leftoverCountedPacks = countedPacks - (countedFullPallets * palletBox);

					logger.info("expectedCountedPacks " + expectedCountedPacks + " countedFullPalets " + countedFullPallets + " leftoverCountedPacks " + leftoverCountedPacks);

					if (expectedCountedPacks < countedPacks) { //Modifica ultimo palete
						logger.info("Last Pallet was created and is incomplete with " + leftoverCountedPacks + " packs");
						Thing pl = po.getSiblings().stream()
										.filter(ds -> ds.getModel().getMetaname().equals("ORDCRIACAO"))
										.sorted((d1, d2) -> d2.getCreatedAt().compareTo(d1.getCreatedAt()))
										.flatMap(d -> d.getThings().stream())
										.map(dt -> dt.getThing())
										.findAny()
										.orElse(null);
						if (pl != null) {
							Property prQty = pl.getSiblings().stream()
											.flatMap(ts -> ts.getProperties().stream())
											.filter(pr -> pr.getField().getMetaname().equals("QUANTITY"))
											.findAny()
											.get();
							prQty.setValue(DF.format(leftoverCountedPacks));
							getRegSvc().getPrSvc().quickUpdateValue(prQty);
							getRegSvc().getAglSvc().sendThingToWMS(pl, "PUT");
						} else
							getRegSvc().getAlertSvc().persist(new Alert(AlertType.PROCESS, AlertSeverity.WARNING, this.prodOrder.getCode(), "Contagens Inválidas", "OP Sem paletes mas com contagens: " + countedPacks));
					} else if (expectedCountedPacks > countedPacks) {//Cria novo palete
						logger.info("Create Last Pallet with " + leftoverCountedPacks + " packs");
						Address orig = lineEndMap.values().iterator().next();
						Thing t1 = createPallet(prd, orig);
						Thing tPrd = t1.getSiblings().stream().findAny().get();

						t1.setDocument(this.prodOrder.getId());
						tPrd.setDocument(this.prodOrder.getId());
						Property prQty = tPrd.getProperties().stream()
										.filter(pr -> pr.getField().getMetaname().equals("QUANTITY"))
										.findAny()
										.get();
						prQty.setValue(DF.format(leftoverCountedPacks));
						resend(tPrd);
						getRegSvc().getPrSvc().quickUpdateValue(prQty);
						getRegSvc().getDcSvc().createDocumentWithThing(this.prodOrder, ordCriacaoModel, "NOVO", orig, getRegSvc().getPfxSvc().findNext("OCR", 9), t1, getModel().getEstadoPara());
					}
				} else
					logger.info(String.format("All %d pallets are full. All good! Nothing to do!", countedPallets));
			} else
				logger.warn("FixLstPallet disabled on hunter-custom-solar.properties");
		} else
			logger.warn("Production Stopped!!!");
		return true;
	}
}
