package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPProductDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPProductPropertyDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPProductionOrderConsumptionDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPProductionOrderProductionDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.custom.solar.util.Constants;
import com.gtp.hunter.custom.solar.util.ToJsonSAP;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.model.Product;
import com.sap.conn.jco.JCoException;

public class ZHWOrdemProducaoWorker extends BaseWorker {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ZHWOrdemProducaoWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		ToJsonSAP jcoSonStart = new ToJsonSAP(getSolar().getFunc(Constants.RFC_OGC));
		int code = Integer.valueOf(rstart.getCode());
		jcoSonStart.setParameters(new HashMap<String, Object>() {
			private static final long serialVersionUID = 4186846436699556620L;

			{
				put(Constants.I_CODE, rstart.getCode());
				put(Constants.I_CONTROLE, rstart.getControle());
			}
		});

		ReadFieldsSap readFieldsSap = null;
		try {
			readFieldsSap = getGson().fromJson(jcoSonStart.execute(getSolar().getDestination()), ReadFieldsSap.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JCoException e) {
			e.printStackTrace();
		}

		try {
			logJcoError(readFieldsSap, rstart.getControle(), Constants.RFC_OGC);
			logger.info("Código da Ordem: " + rstart.getControle());
			registerProducts(readFieldsSap.getProductDTOs());

			if (readFieldsSap != null && readFieldsSap.getProdOrderPrdDTOs() != null && readFieldsSap.getProdOrderPrdDTOs().size() > 0) {
				if (readFieldsSap.getProdOrderPrdDTOs().get(0).getCentro().equalsIgnoreCase(ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"))) {
					Document d = getISvc().getRegSvc().getDcSvc().quickFindByCodeAndModelMetaname(rstart.getControle(), "PLANPROD");

					if (d == null) {
						DocumentModel dm = getISvc().getRegSvc().getDmSvc().findByMetaname("PLANPROD");
						Supplier<Stream<DocumentModelField>> sup = () -> dm.getFields().parallelStream();
						Document planprod = new Document(dm, dm.getName() + " - " + Integer.parseInt(rstart.getControle()), rstart.getControle(), "INTEGRADO");
						DocumentModelField dmfLinha = sup.get().filter(dmf -> dmf.getMetaname().equals("LINHA_PROD")).findAny().get();
						DocumentModelField dmfInicio = sup.get().filter(dmf -> dmf.getMetaname().equals("START_DATE")).findAny().get();
						DocumentModelField dmfReserva = sup.get().filter(dmf -> dmf.getMetaname().equals("RESERVATION_NUMBER")).findAny().get();

						registerProducts(readFieldsSap.getProductDTOs());
						for (SAPProductionOrderProductionDTO prd : readFieldsSap.getProdOrderPrdDTOs()) {
							Product p = getISvc().getRegSvc().getPrdSvc().findBySKU(String.valueOf(Integer.parseInt(prd.getMaterial())));

							if (p != null) {
								DocumentItem di = new DocumentItem();
								DocumentField dfLinha = new DocumentField();
								DocumentField dfInicio = new DocumentField();
								DocumentField dfReserva = new DocumentField();
								Optional<SAPProductDTO> optDTO = readFieldsSap.getProductDTOs().stream().filter(dto -> dto.getMaterial().equals(prd.getMaterial())).findFirst();

								di.setProduct(p);
								di.setDocument(planprod);
								if (optDTO.isPresent())
									di.setMeasureUnit(optDTO.get().getMeins());
								di.getProperties().put("PRODUCAO", "PRODUCAO");

								dfLinha.setDocument(planprod);
								dfLinha.setField(dmfLinha);
								dfLinha.setValue(prd.getResourceWork());

								dfInicio.setDocument(planprod);
								dfInicio.setField(dmfInicio);
								dfInicio.setValue(prd.getStartDate());

								dfReserva.setDocument(planprod);
								dfReserva.setField(dmfReserva);
								dfReserva.setValue(prd.getReservNum());

								planprod.getFields().add(dfLinha);
								planprod.getFields().add(dfInicio);
								planprod.getFields().add(dfReserva);
								planprod.getItems().add(di);
							} else {
								this.callResult(Constants.RFC_OGC, code, rstart.getControle(), "ICW_0010", "PRODUTO NÃO CADASTRADO " + prd.getMaterial());
							}
						}

						for (SAPProductionOrderConsumptionDTO con : readFieldsSap.getProdOrderConsDTOs()) {
							Product p = getISvc().getRegSvc().getPrdSvc().findBySKU(String.valueOf(Integer.parseInt(con.getMaterial())));

							if (p != null) {
								Optional<SAPProductPropertyDTO> optPrdProp = readFieldsSap.getProductPropertyDTOs().stream().filter(pp -> pp.getMatNr().equals(con.getMaterial()) && pp.getControle().equals(con.getControle())).findFirst();
								DocumentItem di = new DocumentItem();

								di.setProduct(p);
								di.setDocument(planprod);
								di.setMeasureUnit(con.getUnit());
								di.setQty(Double.parseDouble(con.getQuantity()));
								di.getProperties().put("PRODUCAO", "CONSUMO");

								if (optPrdProp.isPresent()) {
									SAPProductPropertyDTO prop = optPrdProp.get();

									di.getProperties().put("DENOMINADOR", prop.getUmRen());
									di.getProperties().put("FATOR_MULTIPLICATIVO", prop.getUmRez());
								} else {
									Alert alert = new Alert(AlertType.INTEGRATION, AlertSeverity.WARNING, rstart.getControle(), String.format("OGC SEM CONVERSÃO PARA PRODUTO %s", p.getSku()), String.format("CONTROLE: %s", rstart.getControle()));
									getISvc().getRegSvc().getAlertSvc().persist(alert);
								}

								planprod.getItems().add(di);
							} else {
								this.callResult(Constants.RFC_OGC, code, rstart.getControle(), "ICW_0010", "PRODUTO NÃO CADASTRADO " + con.getMaterial());
							}
						}

						getISvc().getRegSvc().getDcSvc().persist(planprod);
						this.callResult(Constants.RFC_OGC, code, rstart.getControle(), "ICW_0000", "SUCESSO");
					} else {
						this.callResult(Constants.RFC_OGC, code, rstart.getControle(), "ICW_0011", "SOLICITAÇÃO JÁ PROCESSADA");
						return Boolean.FALSE;
					}
				} else {
					logger.info("Planejamento de Produção " + rstart.getControle() + " de outro centro");
					this.callResult(Constants.RFC_OGC, code, rstart.getControle(), "ICW_0000", "PLANEJAMENTO DE OUTRO CENTRO " + readFieldsSap.getProdOrderPrdDTOs().get(0).getCentro());
					return Boolean.FALSE;
				}
			} else {
				logger.info("Planejamento de Produção " + rstart.getControle() + " sem Prd");
				this.callResult(Constants.RFC_OGC, code, rstart.getControle(), "ICW_0012", "PLANEJAMENTO SEM ITEM DE PRODUÇÃO");
				return Boolean.FALSE;
			}
			return Boolean.TRUE;
		} catch (Exception e) {
			e.printStackTrace();
			this.callResult(Constants.RFC_OGC, code, rstart.getControle(), "ICW_0099", e.getLocalizedMessage());
		}

		return Boolean.FALSE;
	}

	@Override
	public boolean external(Object obj) {
		return Boolean.FALSE;
	}

}
