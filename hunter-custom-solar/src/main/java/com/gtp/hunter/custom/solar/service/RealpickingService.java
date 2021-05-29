package com.gtp.hunter.custom.solar.service;

import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.custom.solar.realpicking.RPDeliveryDTO;
import com.gtp.hunter.custom.solar.realpicking.RPDeliveryRepository;
import com.gtp.hunter.custom.solar.realpicking.RPPickDTO;
import com.gtp.hunter.custom.solar.realpicking.RPPickItemDTO;
import com.gtp.hunter.custom.solar.realpicking.RPProductDTO;
import com.gtp.hunter.custom.solar.realpicking.RPProductRepository;
import com.gtp.hunter.custom.solar.realpicking.RPTripDTO;
import com.gtp.hunter.custom.solar.realpicking.RPVehicleDTO;
import com.gtp.hunter.custom.solar.realpicking.RPVehicleRepository;
import com.gtp.hunter.custom.solar.sap.worker.ZHWInformacaoVeiculoWorker;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.DocumentThing;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductField;
import com.gtp.hunter.process.model.ProductModel;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.Thing;

@Startup
@Singleton
public class RealpickingService {

	@Inject
	private Logger					logger;

	@Inject
	private RPProductRepository		rpPrdRep;

	@Inject
	private RPDeliveryRepository	rpDlRep;

	@Inject
	private RPVehicleRepository		rpVRep;

	@Inject
	private IntegrationService		iSvc;

	@PostConstruct
	public void init() {
		//update hunter products
		if (ConfigUtil.get("hunter-custom-solar", "list_rp_orders", "false").equalsIgnoreCase("true"))
			Executors.newSingleThreadScheduledExecutor().schedule(() -> {
				checkDelivery(Calendar.getInstance().getTime(), ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"));
			}, 10, TimeUnit.SECONDS);
		if (ConfigUtil.get("hunter-custom-solar", "update_rp_products", "true").equalsIgnoreCase("true"))
			Executors.newSingleThreadScheduledExecutor().schedule(() -> {
				updateProducts();
			}, 20, TimeUnit.SECONDS);
	}

	public RPDeliveryRepository getDataRepository() {
		return rpDlRep;
	}

	public IntegrationService getIntegrationService() {
		return iSvc;
	}

	public List<Document> checkTrips(Date dt, String plant) {
		final List<Document> transpList = new ArrayList<>();

		try {
			final DocumentModel trDocModel = iSvc.getRegSvc().getDmSvc().findByMetaname("TRANSPORT");
			final List<RPTripDTO> dataList = rpDlRep.getTripList(dt, plant);

			for (RPTripDTO dto : dataList) {
				String trip = dto.getTrip_number();
				String code = "R" + trip;
				Optional<Document> optTransp = transpList.stream().filter(doc -> doc.getCode().equals(code)).findAny();
				Document transp = optTransp.orElseGet(() -> {
					Document tmp = iSvc.getRegSvc().getDcSvc().quickFindByCodeAndModelMetaname(code, trDocModel.getMetaname());

					if (tmp != null && tmp.getId() != null)
						return iSvc.getRegSvc().getDcSvc().findById(tmp.getId());
					return null;
				});
				List<Document> nfList = iSvc.getRegSvc().getDcSvc().listByPropertyValue("TICKET", trip);

				if (transp == null) {
					String placa = dto.getVehicle_tag_number();
					Unit tmpIdRP = iSvc.getRegSvc().getUnSvc().findByTagId(dto.getVehicle_id());
					Unit idRP = tmpIdRP == null ? new Unit("Realpicking", dto.getVehicle_id(), UnitType.EXTERNAL_SYSTEM) : tmpIdRP;
					Thing truck = iSvc.getRegSvc().getThSvc().quickFindByUnitTagId(placa.isEmpty() ? dto.getVehicle_id() : placa);

					if (truck == null) {
						List<String> tknum = new ArrayList<>();
						ZHWInformacaoVeiculoWorker worker = new ZHWInformacaoVeiculoWorker(iSvc.getSap(), iSvc.getSolar(), iSvc);

						tknum.add(dto.getTrip_number());
						List<Thing> reg = worker.registerVehicles(tknum);

						if (!reg.isEmpty()) {
							truck = reg.get(0);
						}
					}
					transp = new Document(trDocModel, "Transporte Rota " + code, code, "INTEGRADO");
					if (truck != null) {
						Property prLeft = truck.getProperties().parallelStream().filter(pr -> pr.getField().getMetaname().equals("LEFT_SIDE_QUANTITY")).findAny().get();
						Property prRight = truck.getProperties().parallelStream().filter(pr -> pr.getField().getMetaname().equals("RIGHT_SIDE_QUANTITY")).findAny().get();

						if (prLeft.getValue().equals("UPDATE_ON_REALPICKING") || prRight.getValue().equals("UPDATE_ON_REALPICKING")) {
							RPVehicleDTO rpv = rpVRep.getVehicleCapacity(dto.getVehicle_id());

							iSvc.getRegSvc().getPrSvc().quickInsert(truck.getId(), prLeft.getField().getId(), String.valueOf((int) Math.ceil(rpv.getCapacity() / 2)));
							iSvc.getRegSvc().getPrSvc().quickInsert(truck.getId(), prRight.getField().getId(), String.valueOf((int) Math.floor(rpv.getCapacity() / 2)));
						}
						truck.getUnits().add(tmpIdRP == null ? iSvc.getRegSvc().getUnSvc().persist(idRP).getId() : idRP.getId());
						transp.getThings().add(new DocumentThing(transp, truck, ""));
					} else {
						logger.error("Caminhão Não Cadastrado!");
						iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, trip, "Veículo Indisponível", "Placa: " + placa + " ID: " + dto.getVehicle_tag_number()));
					}
					for (DocumentModelField dmf : trDocModel.getFields()) {
						DocumentField df = new DocumentField(transp, dmf, "NOVO", "");

						switch (dmf.getMetaname()) {
							case "DOCK":
								df.setValue(truck != null && truck.getAddress() != null ? truck.getAddress().getId().toString() : "");
								break;
							case "TRUCK_ID":
								df.setValue(truck != null ? truck.getId().toString() : "");
								break;
							case "DRIVER_ID":
								df.setValue("");
								break;
							case "CTE":
								df.setValue("");
								break;
							case "SERVICE_TYPE":
								df.setValue("ROTA");
								break;
							case "OBS":
								df.setValue("Carga: " + dto.getLoad_id());
								break;
							case "RULE_TEMPLATE":
								df.setValue("0");
								break;
						}
						transp.getFields().add(df);
					}
					iSvc.getRegSvc().getDcSvc().persist(transp);
					for (Document nfs : nfList) {
						nfs.setParent(transp);
					}
					listPicks(transp, plant);
					transpList.add(transp);
				} else {
					listPicks(transp, plant);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return transpList;
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public List<Document> listPicks(Document transport, String plant) {
		final String trip = transport.getCode().replace("R", "");

		try {
			final DocumentModel pkDocModel = iSvc.getRegSvc().getDmSvc().findByMetaname("PICKING");
			final List<RPPickDTO> dataList = rpDlRep.listPicksByTrip(trip, plant);

			for (RPPickDTO dto : dataList) {
				Document pick = null;
				int bayId = dto.getBay_id();
				String bayLabel = dto.getBay_label();
				String pckCode = trip + "." + bayLabel;
				Optional<Document> optPick = transport.getSiblings().stream()
								.filter(ds -> {
									if (ds.getModel().getId().equals(pkDocModel.getId())) {
										Optional<DocumentField> optBayDf = ds.getFields().stream().filter(df -> df.getField().getMetaname().equals("BAY_ID")).findAny();

										return optBayDf.isPresent() && optBayDf.get().getValue().equals(String.valueOf(bayId));
									}
									return false;
								})
								.findAny();
				if (optPick.isPresent())
					pick = optPick.get();
				else {
					pick = new Document(pkDocModel, pkDocModel.getName() + " " + trip + "." + bayLabel, pckCode, "ROMANEIO");
					pick.setParent(transport);
					transport.getSiblings().add(pick);
				}
				for (DocumentModelField dmf : pkDocModel.getFields()) {
					Optional<DocumentField> optDf = pick.getFields().parallelStream().filter(df -> df.getField().getId().equals(dmf.getId())).findAny();
					DocumentField df = optDf.isPresent() ? optDf.get() : new DocumentField(pick, dmf, "NOVO", "");

					switch (dmf.getMetaname()) {
						case "CASES_PHYSICAL":
							df.setValue(String.valueOf(dto.getCases_physical()));
							break;
						case "SKUS":
							df.setValue(String.valueOf(dto.getNumber_of_skus()));
							break;
						case "BAY_ID":
							df.setValue(String.valueOf(bayId));
							break;
						case "BAY_DESC":
							df.setValue(bayLabel);
							break;
						case "DELIVERY_DATE":
							df.setValue(dto.getDelivery_date());
							break;
						case "FULL":
							df.setValue(Boolean.toString(dto.is_full_pallet()));
							break;
						case "TICKET_MESSAGE":
							df.setValue(dto.getPicking_ticket_message());
							break;
						case "PLANT":
							df.setValue(dto.getLocation_id());
							break;
						case "CONTAINER_ID":
							df.setValue(dto.getConatiner_id() == null ? "" : dto.getConatiner_id());
							break;
						case "CONTAINER_LEVELS":
							df.setValue(String.valueOf(dto.getContainer_levels()));
							break;
						default:
							logger.warn("Mistyped " + dmf.getMetaname() + " property");
							break;
					}
					if (!optDf.isPresent())
						pick.getFields().add(df);
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return transport.getSiblings()
						.parallelStream()
						.filter(ds -> ds.getModel().getMetaname().equals("PICKING"))
						.collect(Collectors.toList());
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public Document listPickingItems(Document picking) {
		String trip = picking.getCode().split("\\.")[0];
		String bayLabel = picking.getCode().split("\\.")[1];
		String plant = picking.getFields().parallelStream()
						.filter(df -> df.getField().getMetaname().equals("PLANT"))
						.map(df -> df.getValue())
						.findAny()
						.orElse(ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"));

		picking.getItems().clear();
		try {
			final List<RPPickItemDTO> dataList = rpDlRep.listPickingItems(trip, plant, bayLabel);
			String lastLayer = "";
			DocumentItem lastDi = null;
			boolean prdChanged = false;
			boolean isFirst = false;

			for (RPPickItemDTO dto : dataList) {
				Product p = iSvc.getRegSvc().getPrdSvc().findBySKU(dto.getProduct_id());

				if (p != null) {
					prdChanged = lastDi != null && !lastDi.getProduct().getId().equals(p.getId());
					int qty = dto.getQuantity();
					Optional<ProductField> optGroupPF = p.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("GROUP_UM")).findFirst();
					String layerDesc = dto.getLayer_description().trim();

					if (layerDesc.isEmpty()) {
						isFirst = false;
						layerDesc = lastLayer.isEmpty() ? "1" : lastLayer;
					} else {
						isFirst = true;
						lastLayer = layerDesc;
					}
					int init = layerDesc.contains("-") ? Integer.valueOf(layerDesc.split("-")[0]) : Integer.valueOf(layerDesc);
					final String strLayer = Integer.toString(init);
					final String strSeq = Integer.toString(dto.getSequence_ticket());

					//					Optional<DocumentItem> optDi = picking.getItems()
					//									.parallelStream()
					//									.filter(di -> {
					//										boolean sameProduct = di.getProduct().getId().equals(p.getId());
					//										boolean sameLayer = di.getProperties().get("LAYER").equals(strLayer);
					//										boolean sameSeq = di.getProperties().get("SEQ").equals(strSeq);
					//
					//										return sameProduct && sameLayer && sameSeq;
					//									}).findAny();
					//					if (optDi.isPresent())
					//						continue;
					if (lastDi == null || prdChanged) {
						DocumentItem di = new DocumentItem(picking, p, qty, "NOVO");

						di.setMeasureUnit(optGroupPF.isPresent() ? optGroupPF.get().getValue() : "N/C");
						di.getProperties().put("LAYER", strLayer);
						di.getProperties().put("SEQ", strSeq);
						di.getProperties().put("PRODUCT_DESCRIPTION_LONG", dto.getProduct_description_long());
						di.getProperties().put("PRODUCT_DESCRIPTION_SHORT", dto.getProduct_description_short());
						di.getProperties().put("HIGHLIGHT", Boolean.toString(dto.isHighlight()));
						di.getProperties().put("IS_FIRST_ITEM", Boolean.toString(isFirst));
						di.getProperties().put("IS_LAST_ITEM", Boolean.toString(false));
						di.getProperties().put("SEPARATOR", Boolean.toString(dto.isSeparator_after_ticket()));
						di.getProperties().put("FULL_PALLET", Boolean.toString(dto.is_full_pallet()));
						picking.getItems().add(di);
						if (isFirst && lastDi != null)
							lastDi.getProperties().put("IS_LAST_ITEM", Boolean.toString(true));
						lastDi = di;
					} else {
						lastDi.setQty(lastDi.getQty() + qty);
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return picking;
	}

	public List<Document> checkDelivery(Date dt, String plant) {
		final List<Document> transpList = new ArrayList<>();

		try {
			final DocumentModel pkDocModel = iSvc.getRegSvc().getDmSvc().findByMetaname("PICKING");
			final DocumentModel trDocModel = iSvc.getRegSvc().getDmSvc().findByMetaname("TRANSPORT");
			final List<RPDeliveryDTO> dataList = rpDlRep.getDeliveryList(dt, plant);
			String lastLayer = "";
			String lastPckCode = "";
			DocumentItem lastDi = null;
			boolean prdChanged = false;
			boolean isFirst = false;

			for (RPDeliveryDTO dto : dataList) {
				String trip = dto.getTrip_number();
				int bayId = dto.getBay_id();
				String bayLabel = dto.getBay_label();
				String pckCode = trip + "." + bayLabel;
				String code = "R" + trip;
				Optional<Document> optTransp = transpList.stream().filter(doc -> doc.getCode().equals(code)).findAny();
				Document transp = optTransp.orElseGet(() -> {
					Document tmp = iSvc.getRegSvc().getDcSvc().quickFindByCodeAndModelMetaname(code, trDocModel.getMetaname());

					if (tmp != null && tmp.getId() != null)
						return iSvc.getRegSvc().getDcSvc().findById(tmp.getId());
					return null;
				});
				Document pick;

				if (!pckCode.equals(lastPckCode))
					lastDi = null;
				if (transp == null) {
					String placa = dto.getVehicle_tag_number();
					Unit tmpIdRP = iSvc.getRegSvc().getUnSvc().findByTagId(dto.getVehicle_id());
					Unit idRP = tmpIdRP == null ? new Unit("Realpicking", dto.getVehicle_id(), UnitType.EXTERNAL_SYSTEM) : tmpIdRP;
					Thing truck = iSvc.getRegSvc().getThSvc().quickFindByUnitTagId(placa.isEmpty() ? dto.getVehicle_id() : placa);

					if (truck == null) {
						List<String> tknum = new ArrayList<>();
						ZHWInformacaoVeiculoWorker worker = new ZHWInformacaoVeiculoWorker(iSvc.getSap(), iSvc.getSolar(), iSvc);

						tknum.add(dto.getTrip_number());
						List<Thing> reg = worker.registerVehicles(tknum);

						if (!reg.isEmpty()) {
							truck = reg.get(0);
						}
					}
					transp = new Document(trDocModel, "Transporte Rota " + code, code, "ROMANEIO");
					if (truck != null) {
						truck.getUnits().add(tmpIdRP == null ? iSvc.getRegSvc().getUnSvc().persist(idRP).getId() : idRP.getId());
						transp.getThings().add(new DocumentThing(transp, truck, ""));
					} else {
						logger.error("Caminhão Não Cadastrado!");
						iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, trip, "Veículo Indisponível", "Placa: " + placa + " ID: " + dto.getVehicle_tag_number()));
					}
					for (DocumentModelField dmf : trDocModel.getFields()) {
						DocumentField df = new DocumentField(transp, dmf, "NOVO", "");

						switch (dmf.getMetaname()) {
							case "DOCK":
								df.setValue("");
								break;
							case "TRUCK_ID":
								df.setValue("");
								break;
							case "DRIVER_ID":
								df.setValue("");
								break;
							case "CTE":
								df.setValue("");
								break;
							case "SERVICE_TYPE":
								df.setValue("ROTA");
								break;
							case "OBS":
								df.setValue("Carga: " + dto.getLoad_id());
								break;
							case "RULE_TEMPLATE":
								df.setValue("0");
								break;
						}
						transp.getFields().add(df);
					}
					transpList.add(transp);
				}
				Optional<Document> optPick = transp.getSiblings().stream()
								.filter(ds -> {
									if (ds.getModel().getId().equals(pkDocModel.getId())) {
										Optional<DocumentField> optBayDf = ds.getFields().stream().filter(df -> df.getField().getMetaname().equals("BAY_ID")).findAny();

										return optBayDf.isPresent() && optBayDf.get().getValue().equals(String.valueOf(bayId));
									}
									return false;
								})
								.findAny();
				if (optPick.isPresent())
					pick = optPick.get();
				else {
					pick = new Document(pkDocModel, pkDocModel.getName() + " " + trip + "." + bayLabel, pckCode, "ROMANEIO");
					for (DocumentModelField dmf : pkDocModel.getFields()) {
						DocumentField df = new DocumentField(pick, dmf, "NOVO", "");

						switch (dmf.getMetaname()) {
							case "CASES_PHYSICAL":
								df.setValue(String.valueOf(dto.getCases_physical()));
								break;
							case "SKUS":
								df.setValue(String.valueOf(dto.getNumber_of_skus()));
								break;
							case "BAY_ID":
								df.setValue(String.valueOf(bayId));
								break;
							case "BAY_DESC":
								df.setValue(bayLabel);
								break;
							case "DELIVERY_DATE":
								df.setValue(dto.getDelivery_date());
								break;
							case "FULL":
								df.setValue(Boolean.toString(dto.is_full_pallet()));
								break;
							case "TICKET_MESSAGE":
								df.setValue(dto.getPicking_ticket_message());
								break;
							case "PLANT":
								df.setValue(dto.getLocation_id());
								break;
							default:
								logger.warn("Mistyped " + dmf.getMetaname() + " property");
								break;
						}
						pick.getFields().add(df);
					}
					pick.setParent(transp);
					transp.getSiblings().add(pick);
				}
				Product p = iSvc.getRegSvc().getPrdSvc().findBySKU(dto.getProduct_id());

				if (p != null) {
					prdChanged = lastDi != null && !lastDi.getProduct().getId().equals(p.getId());
					int qty = dto.getQuantity();
					Optional<ProductField> optGroupPF = p.getFields().stream().filter(pf -> pf.getModel().getMetaname().equals("GROUP_UM")).findFirst();
					String layerDesc = dto.getLayer_description();

					if (layerDesc.isEmpty()) {
						isFirst = false;
						layerDesc = lastLayer;
					} else {
						isFirst = true;
						lastLayer = layerDesc;
					}
					int init = layerDesc.contains("-") ? Integer.valueOf(layerDesc.split("-")[0]) : Integer.valueOf(layerDesc);
					//					int cnt = layerDesc.contains("-") ? Integer.valueOf(layerDesc.split("-")[1]) : Integer.valueOf(layerDesc);
					//					int lyQt = cnt - init + 1;

					//					for (int layer = init; layer <= cnt; layer++) {
					//						final String strLayer = Integer.toString(layer);
					final String strLayer = Integer.toString(init);
					final String strSeq = Integer.toString(dto.getSequence_ticket());

					//					Optional<DocumentItem> optDi = pick.getItems()
					//									.parallelStream()
					//									.filter(di -> {
					//										boolean sameProduct = di.getProduct().getId().equals(p.getId());
					//										boolean sameLayer = di.getProperties().get("LAYER").equals(strLayer);
					//										boolean sameSeq = di.getProperties().get("SEQ").equals(strSeq);
					//
					//										return sameProduct && sameLayer && sameSeq;
					//									}).findAny();
					//					if (optDi.isPresent())
					//						continue;
					if (lastDi == null || prdChanged) {
						//					DocumentItem di = new DocumentItem(pick, p, qty / lyQt, "NOVO");
						DocumentItem di = new DocumentItem(pick, p, qty, "NOVO");

						di.setMeasureUnit(optGroupPF.isPresent() ? optGroupPF.get().getValue() : "N/C");
						di.getProperties().put("LAYER", strLayer);
						di.getProperties().put("SEQ", strSeq);
						di.getProperties().put("PRODUCT_DESCRIPTION_LONG", dto.getProduct_description_long());
						di.getProperties().put("PRODUCT_DESCRIPTION_SHORT", dto.getProduct_description_short());
						di.getProperties().put("HIGHLIGHT", Boolean.toString(dto.isHighlight()));
						di.getProperties().put("IS_FIRST_ITEM", Boolean.toString(isFirst));
						di.getProperties().put("IS_LAST_ITEM", Boolean.toString(false));
						di.getProperties().put("SEPARATOR", Boolean.toString(dto.isSeparator_after_ticket()));
						di.getProperties().put("FULL_PALLET", Boolean.toString(dto.is_full_pallet()));
						pick.getItems().add(di);
						if (isFirst && lastDi != null)
							lastDi.getProperties().put("IS_LAST_ITEM", Boolean.toString(true));
						lastDi = di;
					} else {
						lastDi.setQty(lastDi.getQty() + qty);
					}
					//					}//adicionados todos os itens
				} else {
					logger.error("Produto " + dto.getProduct_id() + " Não cadastrado!");
					iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.DOCUMENT, AlertSeverity.ERROR, code, "Produto não encontrado.", String.format("Produto: %s", dto.getProduct_id())));
				}
				lastPckCode = pckCode;
			}
			for (Document transp : transpList) {
				List<Document> nfList = iSvc.getRegSvc().getDcSvc().listByPropertyValue("TICKET", transp.getCode().substring(1));

				iSvc.getRegSvc().getDcSvc().persist(transp);
				for (Document nfs : nfList)
					nfs.setParent(transp);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return transpList;
	}

	public Product update(Product p) {
		if (p != null) {
			RPProductDTO prd = rpPrdRep.getBySku(p.getSku());

			if (prd != null)
				return update(prd, p);
			else {
				if (prd == null)
					logger.error("Product: " + p.getSku() + " Not found on RealPicking");
			}
		}
		return p;
	}

	public Product updateBySku(String sku) {
		RPProductDTO prd = rpPrdRep.getBySku(sku);
		Product ret = iSvc.getRegSvc().getPrdSvc().findBySKU(sku);

		if (prd != null && ret != null)
			return update(prd, ret);
		else {
			if (prd == null)
				logger.error("Product: " + sku + " Not found on RealPicking");
			else if (ret == null)
				logger.error("Product: " + sku + " Not found on hunter");
			return ret;
		}
	}

	public RPProductDTO findByPlantAndSku(String plant, String sku) {
		return rpPrdRep.getByPlantAndSku(plant, sku);
	}

	public void updateProducts() {
		List<RPProductDTO> prdList = rpPrdRep.listProducts();
		List<Product> pList = iSvc.getRegSvc().getPrdSvc().listAll();
		int rpCount = prdList.size();
		int updCount = 0;

		logger.info("Updating " + rpCount + " products with Realpicking information");
		for (RPProductDTO prd : prdList) {
			Optional<Product> optProduct = pList.stream().filter(p -> p.getSku().equals(prd.getProduct_id())).findAny();

			if (optProduct.isPresent()) {
				try {
					Product p = update(prd, optProduct.get());

					IntegrationReturn iRet = iSvc.getRegSvc().getAglSvc().sendProductToWMS(p, "PUT").get();

					if (iRet.isResult())
						logger.info(++updCount + ". Product " + p.getSku() + " Updated!");
					else
						logger.error("Product " + p.getSku() + " NOT Updated: " + iRet.getMessage());
				} catch (ExecutionException | InterruptedException ex) {
					logger.error(ex.getLocalizedMessage(), ex);
				}
			} else {
				logger.warn("Product " + prd.getProduct_id() + " not found on database");
			}
		}
		logger.info("Products Updated with Realpicking information");
		ConfigUtil.put("hunter-custom-solar", "update_rp_products", "false");
	}

	private Product update(RPProductDTO prd, Product p) {
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
		DecimalFormat decFormat = new DecimalFormat("#0.####", symbols);
		ProductModel pm = p.getModel();
		Optional<ProductField> optPFPalletBox = p.getFields().stream().filter(pf -> pf.getModel().getMetaname().equalsIgnoreCase("PALLET_BOX")).findFirst();
		Optional<ProductField> optPFPalletHeight = p.getFields().stream().filter(pf -> pf.getModel().getMetaname().equalsIgnoreCase("PALLET_HEIGHT")).findFirst();
		Optional<ProductField> optPFPalletLayer = p.getFields().stream().filter(pf -> pf.getModel().getMetaname().equalsIgnoreCase("PALLET_LAYER")).findFirst();
		Optional<ProductField> optPFBoxLayer = p.getFields().stream().filter(pf -> pf.getModel().getMetaname().equalsIgnoreCase("BOX_LAYER")).findFirst();
		Optional<ProductField> optPFUnitBox = p.getFields().stream().filter(pf -> pf.getModel().getMetaname().equalsIgnoreCase("UNIT_BOX")).findFirst();
		Double boxHeight = prd.getPackage_height();
		Double palletHeight = prd.getPallet_height();
		Double palletLayer = (double) Math.round(palletHeight / boxHeight);
		Integer palletBox = prd.getQuantity_standard();
		Double boxLayer = (double) Math.round(palletBox / palletLayer);
		Integer unitBox = prd.getNumber_of_subunits();

		decFormat.setRoundingMode(RoundingMode.CEILING);
		if (optPFPalletHeight.isPresent()) {
			ProductField pf = optPFPalletHeight.get();

			pf.setValue(decFormat.format(palletHeight));
			p.getFields().add(pf);
		} else {
			p.getFields().add(new ProductField(p, pm.getFields().stream().filter(pmf -> pmf.getMetaname().equals("PALLET_HEIGHT")).findFirst().get(), "NOVO", decFormat.format(palletHeight)));
			logger.warn("Product " + prd.getProduct_id() + " ( " + prd.getProduct_description_long() + ")" + " missing pallet_height productfield");
		}

		if (optPFPalletBox.isPresent()) {
			ProductField pf = optPFPalletBox.get();

			pf.setValue(String.valueOf(palletBox));
			p.getFields().add(pf);
		} else {
			p.getFields().add(new ProductField(p, pm.getFields().stream().filter(pmf -> pmf.getMetaname().equals("PALLET_BOX")).findFirst().get(), "NOVO", String.valueOf(palletBox)));
			logger.warn("Product " + prd.getProduct_id() + " ( " + prd.getProduct_description_long() + ")" + " missing pallet_box productfield");
		}

		if (optPFPalletLayer.isPresent()) {
			ProductField pf = optPFPalletLayer.get();

			pf.setValue(decFormat.format(palletLayer));
			p.getFields().add(pf);
		} else {
			p.getFields().add(new ProductField(p, pm.getFields().stream().filter(pmf -> pmf.getMetaname().equals("PALLET_LAYER")).findFirst().get(), "NOVO", decFormat.format(palletLayer)));
			logger.warn("Product " + prd.getProduct_id() + " ( " + prd.getProduct_description_long() + ")" + " missing pallet_layer productfield");
		}

		if (optPFBoxLayer.isPresent()) {
			ProductField pf = optPFBoxLayer.get();

			pf.setValue(decFormat.format(boxLayer));
			p.getFields().add(pf);
		} else {
			p.getFields().add(new ProductField(p, pm.getFields().stream().filter(pmf -> pmf.getMetaname().equals("BOX_LAYER")).findFirst().get(), "NOVO", String.valueOf(boxLayer)));
			logger.warn("Product " + prd.getProduct_id() + " ( " + prd.getProduct_description_long() + ")" + " missing box_layer productfield");
		}

		if (optPFUnitBox.isPresent()) {
			ProductField pf = optPFUnitBox.get();

			pf.setValue(decFormat.format(unitBox));
			p.getFields().add(pf);
		} else {
			p.getFields().add(new ProductField(p, pm.getFields().stream().filter(pmf -> pmf.getMetaname().equals("UNIT_BOX")).findFirst().get(), "NOVO", String.valueOf(unitBox)));
			logger.warn("Product " + prd.getProduct_id() + " ( " + prd.getProduct_description_long() + ")" + " missing unit_box productfield");
		}

		p.setUpdatedAt(Calendar.getInstance().getTime());
		iSvc.getRegSvc().getPrdSvc().persist(p);
		return p;
	}
}
