package com.gtp.hunter.custom.solar.sap.worker;

import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonSyntaxException;
import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPProductDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPProductionOrderConsumptionDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPProductionOrderProductionDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.custom.solar.util.Constants;
import com.gtp.hunter.custom.solar.util.ToJsonSAP;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentItem;
import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductModel;
import com.sap.conn.jco.JCoException;

public class ZHWSolicitacaoReservaWorker extends BaseWorker {

	public ZHWSolicitacaoReservaWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {

		ToJsonSAP jcoSonStart = new ToJsonSAP(getSolar().getFunc(Constants.RFC_RESERVA));
		jcoSonStart.setParameters(new HashMap<String, Object>() {
			private static final long serialVersionUID = -4097609739646268844L;

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
			logJcoError(readFieldsSap, rstart.getControle(), Constants.RFC_RECUSA_NF);

			System.out.println("Código da Ordem: " + rstart.getControle());
			List<ProductModel> modelBase = getISvc().getRegSvc().getPmSvc().listAll();
			HashMap<String, ProductModel> chaves = new HashMap<>();

			for (ProductModel modelsNaBase : modelBase) {
				chaves.put(modelsNaBase.getMetaname(), modelsNaBase);
			}

			for (SAPProductDTO prod : readFieldsSap.getProductDTOs()) {
				if (prod.getMvGr1() != null) {
					String mvGr1 = prod.getMvGr1();
					ProductModel productModel = null;

					if (!chaves.containsKey(mvGr1)) {
						productModel = new ProductModel(mvGr1, mvGr1, null, "INTEGRADO");
						getISvc().getRegSvc().getPmSvc().persist(productModel);
						productModel = getISvc().getRegSvc().getPmSvc().findByMetaname(mvGr1);
						chaves.put(mvGr1, productModel);
					} else {
						//productModel = getIntegrationService().getrSvc().getPmRep().findByMetaname(mvGr1);
						productModel = chaves.get(mvGr1);
					}

					Product prodCreat = null;
					try {
						prodCreat = getISvc().getRegSvc().getPrdSvc().findBySKU(String.valueOf(Integer.parseInt(prod.getMaterial())));
					} catch (NumberFormatException e) {
						//this.callResult(Integer.valueOf(rstart.getCode()), rstart.getControle(), "OGC_0001","OGC SEM PRODUTO");
						Alert alert = new Alert(AlertType.DOCUMENT, AlertSeverity.ERROR, rstart.getControle(), "OGC SEM PRODUTO", String.format("CONTROLE: %s", rstart.getControle()));
						getISvc().getRegSvc().getAlertSvc().persist(alert);
						return false;
					}

					if (prodCreat == null) {
						Product prd = new Product(prod.getMakTx(), productModel, String.valueOf(Integer.parseInt(prod.getMaterial())), "INTEGRADO");
						getISvc().getRegSvc().getPrdSvc().persist(prd);
					}

				}
			}
			Document d = getISvc().getRegSvc().getDcSvc().quickFindByCodeAndModelMetaname(rstart.getControle(), "PLANPROD");
			if (d == null) {
				DocumentModel dm = getISvc().getRegSvc().getDmSvc().findByMetaname("PLANPROD");
				Document planprod = new Document(dm, "Ordem de Produção - " + rstart.getControle(), rstart.getControle(), "INTEGRADO");

				for (SAPProductionOrderConsumptionDTO con : readFieldsSap.getProdOrderConsDTOs()) {
					Product p = getISvc().getRegSvc().getPrdSvc().findBySKU(String.valueOf(Integer.parseInt(con.getMaterial())));
					if (p != null) {
						DocumentItem di = new DocumentItem();
						di.setProduct(p);
						di.setDocument(planprod);
						//						di.setMeasureUnit(con.getUnit());
						di.getProperties().put("PRODUCAO", "CONSUMO");
						planprod.getItems().add(di);
					} else {
						System.out.println("Produto não encontrado: " + con.getMaterial());
					}
				}

				for (SAPProductionOrderProductionDTO prd : readFieldsSap.getProdOrderPrdDTOs()) {
					Product p = getISvc().getRegSvc().getPrdSvc().findBySKU(String.valueOf(Integer.parseInt(prd.getMaterial())));
					if (p != null) {
						DocumentItem di = new DocumentItem();
						di.setProduct(p);
						di.setDocument(planprod);
						//						di.setMeasureUnit("UN");
						di.getProperties().put("PRODUCAO", "PRODUCAO");
						planprod.getItems().add(di);
					} else {
						System.out.println("Produto não encontrado: " + prd.getMaterial());
					}
				}

				getISvc().getRegSvc().getDcSvc().persist(planprod);
			} else {
				System.out.println("solicitação já processada");
				return Boolean.FALSE;
			}
			return Boolean.TRUE;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Boolean.FALSE;
	}

	@Override
	public boolean external(Object obj) {
		return Boolean.FALSE;
	}

}
