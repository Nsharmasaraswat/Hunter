package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPCustomerDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPDocumentDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPSupplierDTO;
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
import com.gtp.hunter.process.model.Person;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;

public class ZHWInformacaoChegadaWorker extends BaseWorker {
	private static final boolean			logimmediately	= false;
	private transient static final Logger	logger			= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ZHWInformacaoChegadaWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		logger.info("================================================================" + Constants.RFC_INFORMACAO + "===========================================================================");
		Profiler prof = new Profiler();
		JCoFunction func = getSolar().getFunc(Constants.RFC_INFORMACAO);
		ToJsonSAP jcoSonStart = new ToJsonSAP(func);
		jcoSonStart.setParameters(new HashMap<String, Object>() {
			private static final long serialVersionUID = 6122767519535489994L;

			{
				put(Constants.I_CODE, rstart.getCode());
				put(Constants.I_CONTROLE, rstart.getControle());
			}
		});
		//		logger.info(func.toString());
		//		func.getTableParameterList().forEach(f -> {
		//			JCoFieldIterator it = f.getTable().getFieldIterator();
		//			while (it.hasNextField()) {
		//				JCoField field = it.nextField();
		//				logger.info(field.getName());
		//			}
		//		});
		logger.debug(prof.step("JcoSetParameters", logimmediately));
		ReadFieldsSap readFieldsSap = null;
		try {
			readFieldsSap = getGson().fromJson(jcoSonStart.execute(getSolar().getDestination()), ReadFieldsSap.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JCoException e) {
			e.printStackTrace();
		}
		logger.debug(prof.step("ReadFieldsSAP", logimmediately));

		logJcoError(readFieldsSap, rstart.getControle(), Constants.RFC_INFORMACAO);

		try {
			registerProducts(readFieldsSap.getProductDTOs());
			logger.debug(prof.step("ForProdutoDTO", logimmediately));
			if (readFieldsSap.getDocumentItemDTOs().size() > 0) {
				if (!readFieldsSap.getDocumentDTOs().get(0).getDirecaoNf().isEmpty()) {
					int direcao = Integer.parseInt(readFieldsSap.getDocumentDTOs().get(0).getDirecaoNf());
					String tipoNF = readFieldsSap.getDocumentDTOs().get(0).getCategNf();

					if (tipoNF != null && !tipoNF.isEmpty())
						direcao = 5;

					switch (direcao) {
						case Constants.NF_DIR_ENTRADA:
							registerNFEntrada(readFieldsSap, Integer.parseInt(rstart.getCode()), rstart.getControle(), prof);
							break;
						case Constants.NF_DIR_SAIDA:
							registerNFSaida(readFieldsSap, Integer.parseInt(rstart.getCode()), rstart.getControle(), prof);
							break;
						case Constants.NF_DIR_ENTRADADEV:
							registerNFSaida(readFieldsSap, Integer.parseInt(rstart.getCode()), rstart.getControle(), prof);
							break;
						case Constants.NF_DIR_SAIDADEV:
							registerNFEntrada(readFieldsSap, Integer.parseInt(rstart.getCode()), rstart.getControle(), prof);
							break;
						case Constants.NF_DIR_DEVSAIDA:
							registerNFEntrada(readFieldsSap, Integer.parseInt(rstart.getCode()), rstart.getControle(), prof);
							break;
					}
				} else {
					String message = "NF_DIRECAO Em Branco";

					this.callResult(Constants.RFC_INFORMACAO, Integer.valueOf(rstart.getCode()), rstart.getControle(), "ICW_0007", message);
					prof.done(message, logimmediately, false).forEach(logger::info);
					return false;
				}
			} else {
				String message = "T_ZWH_NFLIN Vazia";

				this.callResult(Constants.RFC_INFORMACAO, Integer.valueOf(rstart.getCode()), rstart.getControle(), "ICW_0006", message);
				prof.done(message, logimmediately, false).forEach(logger::info);
				return false;
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.SEVERE, rstart.getControle(), Constants.RFC_INFORMACAO, "Erro " + e.getLocalizedMessage()));
			prof.done(e.getLocalizedMessage(), logimmediately, false).forEach(logger::error);
		}
		return false;
	}

	@Override
	public boolean external(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean registerNFEntrada(ReadFieldsSap readFieldsSap, int code, String controle, Profiler prof) {
		String centroInstancia = ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT");
		String centroExterno = ConfigUtil.get("hunter-custom-solar", "external-plant", "EXTR");
		Person ps = null;

		if (readFieldsSap.getSupplierDTOs().size() > 0) {
			SAPSupplierDTO fornecedor = readFieldsSap.getSupplierDTOs().get(0);

			if (fornecedor.getStcd1() != null && !fornecedor.getStcd1().isEmpty()) {
				// Carrega Fornecedor
				ps = registerSupplier(fornecedor);
			} else {
				this.callResult(Constants.RFC_INFORMACAO, code, controle, "ICW_0015", "FORNECEDOR COM CODIGO VAZIO");
				logger.info(prof.step("Fornecedor com CODIGO vazio", logimmediately));
				return false;
			}
			logger.info(prof.step("SavePerson", logimmediately));
		} else {
			this.callResult(Constants.RFC_INFORMACAO, code, controle, "ICW_0001", "NF SEM FORNECEDOR");
			logger.info(prof.step("NF Sem Fornecedor", logimmediately));
			return false;
		}
		logger.info(prof.step("SavePerson", logimmediately));

		DocumentModel dmnf = getISvc().getRegSvc().getDmSvc().findByMetaname("NFENTRADA");
		Supplier<Stream<DocumentModelField>> fields = () -> dmnf.getFields().stream();

		DocumentModelField cmpSerieNf = fields.get().filter(dmf -> dmf.getMetaname().equals("SERIE_NF")).findFirst().get();
		DocumentModelField cmpDataNf = fields.get().filter(dmf -> dmf.getMetaname().equals("DATA_NF")).findFirst().get();
		DocumentModelField cmpChave = fields.get().filter(dmf -> dmf.getMetaname().equals("CHAVE_NF")).findFirst().get();
		DocumentModelField cmpCsc = fields.get().filter(dmf -> dmf.getMetaname().equals("STATUSCSC")).findFirst().get();
		DocumentModelField cmpOrigin = fields.get().filter(dmf -> dmf.getMetaname().equals("ORIGIN")).findFirst().get();
		DocumentModelField cmpCat = fields.get().filter(dmf -> dmf.getMetaname().equals("CAT_NF")).findFirst().get();
		DocumentModelField cmpSap = fields.get().filter(dmf -> dmf.getMetaname().equals("TRANSPORTE_SAP")).findFirst().get();
		DocumentModelField cmpTpTr = fields.get().filter(dmf -> dmf.getMetaname().equals("TIPO_TRANSPORTE")).findFirst().get();
		DocumentModelField cmpZtr = fields.get().filter(dmf -> dmf.getMetaname().equals("ZTRANS")).findFirst().get();

		for (SAPDocumentDTO doc : readFieldsSap.getDocumentDTOs()) {
			String centro = readFieldsSap.getDocumentItemDTOs() == null || readFieldsSap.getDocumentItemDTOs().size() == 0 ? "" : readFieldsSap.getDocumentItemDTOs().get(0).getCentro();

			if (!centro.equalsIgnoreCase(centroInstancia)) {
				String message = String.format("NFENTRADA: %s DE OUTRO CENTRO %s (REQUERIDO=%s)", doc.getNumeroNf(), centro, centroInstancia);
				this.callResult(Constants.RFC_INFORMACAO, code, controle, "ICW_0006", message);
				continue;
			}
			UUID tmpD = getISvc().getRegSvc().getDcSvc().quickFindIDByCodeAndModelMetanameAndPerson(doc.getNumeroNf(), "NFENTRADA", ps.getId().toString());
			Set<DocumentItem> diSet = getDocumentItems(readFieldsSap.getDocumentItemDTOs(), readFieldsSap.getProductPropertyDTOs(), doc, code, controle);

			if (tmpD != null) {// Ja existe
				updateNF(tmpD, diSet, cmpCsc, doc.getVbeLn(), cmpSap, doc.getTkNum());
				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.INFO, controle, Constants.RFC_INFORMACAO, String.format("Número NFENTRADA: %s - Atualizada com %d itens", doc.getNumeroNf(), readFieldsSap.getDocumentItemDTOs().size())));
				logger.info(prof.step("NF Updated " + doc.getNumeroNf() + " Itens: " + diSet.size(), logimmediately));
			} else {//Novo
				Document document = new Document(dmnf, "NF Entrada - " + doc.getNumeroNf(), doc.getNumeroNf(), "INTEGRADO");
				// document.setParent(transp);
				document.getFields().add(new DocumentField(document, cmpSerieNf, "NOVO", doc.getSerieNf()));
				document.getFields().add(new DocumentField(document, cmpDataNf, "NOVO", doc.getDataNf()));
				document.getFields().add(new DocumentField(document, cmpChave, "NOVO", doc.getChaveNfe()));
				document.getFields().add(new DocumentField(document, cmpCsc, "NOVO", doc.getVbeLn()));
				document.getFields().add(new DocumentField(document, cmpOrigin, "NOVO", doc.getVsTel().isEmpty() ? centroExterno : doc.getVsTel()));
				document.getFields().add(new DocumentField(document, cmpCat, "NOVO", doc.getCategNf()));
				document.getFields().add(new DocumentField(document, cmpSap, "NOVO", doc.getTkNum() == null || doc.getTkNum().isEmpty() ? "" : String.valueOf(Integer.parseInt(doc.getTkNum()))));
				document.getFields().add(new DocumentField(document, cmpTpTr, "NOVO", doc.getShtyp()));
				document.getFields().add(new DocumentField(document, cmpZtr, "NOVO", doc.getZtrans()));

				diSet.forEach(di -> di.setDocument(document));
				document.getItems().addAll(diSet);
				getISvc().getRegSvc().getDcSvc().persist(document);
				//FUCKING DETACHED ENTITY MY EGGS
				document.setPerson(getISvc().getRegSvc().getPsSvc().findById(ps.getId()));
				getISvc().getRegSvc().getDcSvc().persist(document);
			}
			this.callResult(Constants.RFC_INFORMACAO, code, controle, "ICW_0000", "SUCESSO");
		}
		prof.done("Final", logimmediately, true).forEach(logger::info);

		return true;
	}

	private boolean registerNFSaida(ReadFieldsSap readFieldsSap, int code, String controle, Profiler prof) {
		String centroExterno = ConfigUtil.get("hunter-custom-solar", "external-plant", "EXTR");
		Person ps = null;

		if (readFieldsSap.getCustomerDTOs().size() > 0) {
			SAPCustomerDTO cliente = readFieldsSap.getCustomerDTOs().get(0);

			if (cliente.getKunnr() != null && !cliente.getKunnr().isEmpty()) {
				ps = registerCustomer(cliente);
			} else {
				this.callResult(Constants.RFC_INFORMACAO, code, controle, "ICW_0001", "NF CLIENTE COM CODIGO VAZIO");
				logger.info(prof.step("NF Cliente com CODIGO vazio", logimmediately));
				return false;
			}
			logger.info(prof.step("SavePerson", logimmediately));
		} else {
			this.callResult(Constants.RFC_INFORMACAO, code, controle, "ICW_0001", "NF SEM CLIENTE");
			logger.info(prof.step("NF Sem Cliente", logimmediately));
			return false;
		}
		DocumentModel dmnf = getISvc().getRegSvc().getDmSvc().findByMetaname("NFSAIDA");
		DocumentModel dmtr = getISvc().getRegSvc().getDmSvc().findByMetaname("TRANSPORT");
		Supplier<Stream<DocumentModelField>> fields = () -> dmnf.getFields().stream();
		DocumentModelField cmpSerieNf = fields.get().filter(dmf -> dmf.getMetaname().equals("SERIE_NF")).findFirst().get();
		DocumentModelField cmpDataNf = fields.get().filter(dmf -> dmf.getMetaname().equals("DATA_NF")).findFirst().get();
		DocumentModelField cmpChave = fields.get().filter(dmf -> dmf.getMetaname().equals("CHAVE_NF")).findFirst().get();
		DocumentModelField cmpCsc = fields.get().filter(dmf -> dmf.getMetaname().equals("STATUSCSC")).findFirst().get();
		DocumentModelField cmpDestination = fields.get().filter(dmf -> dmf.getMetaname().equals("DESTINATION")).findFirst().get();
		DocumentModelField cmpTkNum = fields.get().filter(dmf -> dmf.getMetaname().equals("TICKET")).findFirst().get();
		DocumentModelField cmpTpTr = fields.get().filter(dmf -> dmf.getMetaname().equals("TIPO_TRANSPORTE")).findFirst().get();
		DocumentModelField cmpZtr = fields.get().filter(dmf -> dmf.getMetaname().equals("ZTRANS")).findFirst().get();

		for (SAPDocumentDTO doc : readFieldsSap.getDocumentDTOs()) {
			UUID tmpD = getISvc().getRegSvc().getDcSvc().quickFindIDByCodeAndModelMetanameAndPerson(doc.getNumeroNf(), "NFSAIDA", ps.getId().toString());
			Set<DocumentItem> diSet = getDocumentItems(readFieldsSap.getDocumentItemDTOs(), readFieldsSap.getProductPropertyDTOs(), doc, code, controle);

			if (tmpD != null) {// Ja existe

				updateNF(tmpD, diSet, cmpCsc, doc.getVbeLn(), cmpTkNum, doc.getTkNum());

				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.INFO, controle, Constants.RFC_INFORMACAO, String.format("Número NFSAIDA: %s - Atualizada com %d itens", doc.getNumeroNf(), readFieldsSap.getDocumentItemDTOs().size())));
				logger.info(prof.step("NF Updated " + doc.getNumeroNf() + " Itens: " + diSet.size(), logimmediately));
			} else {//Novo
				String tkNum = doc.getTkNum() == null || doc.getTkNum().isEmpty() ? "" : String.valueOf(Integer.parseInt(doc.getTkNum()));
				Document document = new Document(dmnf, "NF Saída - " + doc.getNumeroNf(), doc.getNumeroNf(), "INTEGRADO");
				Document transp = doc.getTkNum() == null || doc.getTkNum().isEmpty() ? null : getISvc().getRegSvc().getDcSvc().findByModelAndCode(dmtr, "R" + tkNum);
				// document.setParent(transp);
				document.getFields().add(new DocumentField(document, cmpSerieNf, "NOVO", doc.getSerieNf()));
				document.getFields().add(new DocumentField(document, cmpDataNf, "NOVO", doc.getDataNf()));
				document.getFields().add(new DocumentField(document, cmpChave, "NOVO", doc.getChaveNfe()));
				document.getFields().add(new DocumentField(document, cmpCsc, "NOVO", doc.getVbeLn()));
				document.getFields().add(new DocumentField(document, cmpDestination, "NOVO", doc.getVsTel().isEmpty() ? centroExterno : doc.getVsTel()));
				document.getFields().add(new DocumentField(document, cmpTkNum, "NOVO", tkNum));
				document.getFields().add(new DocumentField(document, cmpTpTr, "NOVO", doc.getShtyp()));
				document.getFields().add(new DocumentField(document, cmpZtr, "NOVO", doc.getZtrans()));

				diSet.forEach(di -> di.setDocument(document));
				document.getItems().addAll(diSet);
				getISvc().getRegSvc().getDcSvc().persist(document);
				//FUCKING DETACHED ENTITY MY EGGS
				document.setPerson(getISvc().getRegSvc().getPsSvc().findById(ps.getId()));
				document.setParent(transp);
				if (transp != null && transp.getThings().parallelStream().noneMatch(dt -> dt.getThing().getProduct().getModel().getMetaname().equals("TRUCK")))
					Executors.newSingleThreadScheduledExecutor().schedule(() -> logger.info("UPDATE TRNASPORT TRUCK"), 5, TimeUnit.SECONDS);
				getISvc().getRegSvc().getDcSvc().persist(document);
			}
			this.callResult(Constants.RFC_INFORMACAO, code, controle, "ICW_0000", "SUCESSO");
		}
		prof.done("Final", logimmediately, false).forEach(logger::info);

		return true;
	}

	private void updateNF(UUID id, Set<DocumentItem> diSet, DocumentModelField cmpCsc, String statusCSC, DocumentModelField cmpSap, String trSap) {
		Document d = getISvc().getRegSvc().getDcSvc().findById(id);

		d.getFields().stream().filter(df -> df.getField().getId().equals(cmpCsc.getId())).findAny().get().setValue(statusCSC);
		d.getFields().stream().filter(df -> df.getField().getId().equals(cmpSap.getId())).findAny().get().setValue(trSap);
		//		for (DocumentItem di : d.getItems()) {
		//			Optional<DocumentItem> optDiUpd = diSet.stream().filter(diN -> diN.getProduct().getId().equals(di.getProduct().getId())).findFirst();
		//
		//			if (optDiUpd.isPresent()) {
		//				DocumentItem diN = optDiUpd.get();
		//
		//				if (di.getQty() != diN.getQty())
		//					di.setQty(diN.getQty());
		//				if (diN.getProperties().containsKey("DOC_COMPRAS") && !diN.getProperties().get("DOC_COMPRAS").isEmpty())
		//					di.getProperties().put("DOC_COMPRAS", diN.getProperties().get("DOC_COMPRAS"));
		//				diSet.remove(diN);
		//			}
		//		}
		d.getItems().stream()
						.filter(di -> di.getId() != null)
						.forEach(di -> getISvc().getRegSvc().getDiSvc().remove(di));
		d.getItems().clear();
		for (DocumentItem di : diSet) {
			di.setDocument(d);
		}
		d.getItems().addAll(diSet);
		getISvc().getRegSvc().getDcSvc().persist(d);
	}
}
