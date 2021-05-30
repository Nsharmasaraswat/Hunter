package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
import com.gtp.hunter.custom.solar.sap.dtos.SAPItemListInventarioDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReturnDTO;
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

public class ZHWInformacaoInventarioWorker extends BaseWorker {
	private static final int				CODE			= 7;
	private static final boolean			logimmediately	= false;
	private transient static final Logger	logger			= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ZHWInformacaoInventarioWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		logger.info("================================================================" + Constants.RFC_INVENTARIO + "===========================================================================");
		Profiler prof = new Profiler();
		ToJsonSAP jcoSonStart = new ToJsonSAP(getSolar().getFunc(Constants.RFC_INVENTARIO));
		jcoSonStart.setParameters(new HashMap<String, Object>() {
			private static final long serialVersionUID = 6122767519535489994L;

			{
				put(Constants.I_CONTROLE, rstart.getControle());
				put(Constants.I_FLUXO, "L");
			}
		});
		logger.info(prof.step("JcoSetParameters", logimmediately));
		ReadFieldsSap readFieldsSap = null;
		try {
			readFieldsSap = getGson().fromJson(jcoSonStart.execute(getSolar().getDestination()), ReadFieldsSap.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JCoException e) {
			e.printStackTrace();
		}
		logger.info(prof.step("ReadFieldsSAP", logimmediately));
		try {
			if (!readFieldsSap.getListaInventarioDTOs().isEmpty()) {
				SAPItemListInventarioDTO firstDTO = readFieldsSap.getListaInventarioDTOs().get(0);
				logger.info(prof.step("List Warehouses", logimmediately));
				String code = firstDTO.getDocSap();
				String centro = firstDTO.getCentro();
				String dep = firstDTO.getDeposito();
				int ano = firstDTO.getAno();
				DocumentModel dmInv = getISvc().getRegSvc().getDmSvc().findByMetaname("SAPINVENTORY");
				logger.info(prof.step("Find DocumentModel SAPINVENTORY", logimmediately));
				Document tmpInv = getISvc().getRegSvc().getDcSvc().findByModelAndCode(dmInv, code);
				logger.info(prof.step("Find Document INV", logimmediately));
				final Document dInv = tmpInv == null ? new Document(dmInv, dmInv.getName() + " - " + code, code, "ATIVO") : tmpInv;
				Supplier<Stream<DocumentField>> supDfs = () -> dInv.getFields().stream();
				Supplier<Stream<DocumentModelField>> supDmfs = () -> dmInv.getFields().stream();
				DocumentField dfCentro = supDfs.get()
								.filter(df -> df.getField().getMetaname().equals("CENTRO"))
								.findAny()
								.orElseGet(() -> {
									DocumentField tmp = new DocumentField(dInv, supDmfs.get().filter(dmf -> dmf.getMetaname().equals("CENTRO")).findAny().get(), "NOVO", centro);

									dInv.getFields().add(tmp);
									return tmp;
								});
				logger.info(prof.step("Find DocumentField CENTRO", logimmediately));
				DocumentField dfDep = supDfs.get()
								.filter(df -> df.getField().getMetaname().equals("DEPOSITO"))
								.findAny()
								.orElseGet(() -> {
									DocumentField tmp = new DocumentField(dInv, supDmfs.get().filter(dmf -> dmf.getMetaname().equals("DEPOSITO")).findAny().get(), "NOVO", dep);

									dInv.getFields().add(tmp);
									return tmp;
								});
				logger.info(prof.step("Find DocumentField DEPOSITO", logimmediately));
				DocumentField dfAno = supDfs.get()
								.filter(df -> df.getField().getMetaname().equals("ANO"))
								.findAny()
								.orElseGet(() -> {
									DocumentField tmp = new DocumentField(dInv, supDmfs.get().filter(dmf -> dmf.getMetaname().equals("ANO")).findAny().get(), "NOVO", String.valueOf(ano));

									dInv.getFields().add(tmp);
									return tmp;
								});
				logger.info(prof.step("Find DocumentField ANO", logimmediately));
				DocumentField dfTipo = supDfs.get()
								.filter(df -> df.getField().getMetaname().equals("INV_TYPE"))
								.findAny()
								.orElseGet(() -> {
									DocumentField tmp = new DocumentField(dInv, supDmfs.get().filter(dmf -> dmf.getMetaname().equals("INV_TYPE")).findAny().get(), "NOVO", "INV" + dep.substring(0, 2));

									dInv.getFields().add(tmp);
									return tmp;
								});
				logger.info(prof.step("Find DocumentField INV_TYPE", logimmediately));
				dfCentro.setValue(centro);
				dfDep.setValue(dep);
				dfAno.setValue(String.valueOf(ano));
				dfTipo.setValue("INV" + dep.substring(0, 2));
				for (SAPItemListInventarioDTO invItemDTO : readFieldsSap.getListaInventarioDTOs()) {
					String sku = String.valueOf(Integer.parseInt(invItemDTO.getSku()));

					if (!dInv.getItems().stream().anyMatch(di -> di.getProduct().getSku().equals(sku))) {
						String measureUnit = invItemDTO.getMeasureUnit();
						String linha = invItemDTO.getLinha();
						String desc = invItemDTO.getPrdDesc();
						String elim = invItemDTO.getItemEliminado();
						double qty = invItemDTO.getQtd();
						Product p = getISvc().getRegSvc().getPrdSvc().findBySKU(sku);
						DocumentItem di = new DocumentItem(dInv, p, qty, "NOVO");

						if (p == null) {
							this.callResult(Constants.RFC_INVENTARIO, CODE, dInv.getCode(), "ICW_0010", "PRODUTO NÃO CADASTRADO " + invItemDTO.getSku());
							continue;
						}

						di.setMeasureUnit(measureUnit);
						di.getProperties().put("LINHA", linha);
						di.getProperties().put("DESC", desc);
						di.getProperties().put("ELIMINADO", elim);
						dInv.getItems().add(di);
						logger.info(prof.step("Inventory Item: " + getGson().toJson(invItemDTO), logimmediately));
					}
				}
				if (dInv.getItems().size() == 0) {
					this.callResult(Constants.RFC_INVENTARIO, CODE, dInv.getCode(), "ICW_0021", "INVENTÁRIO SEM PRODUTOS VÁLIDOS");
					return false;
				}
				getISvc().getRegSvc().getDcSvc().persist(dInv);
				this.callResult(Constants.RFC_INVENTARIO, CODE, dInv.getCode(), "ICW_0000", "INVENTÁRIO IMPORTADO COM SUCESSO");
			} else {
				prof.done("Recebido inventário vazio!", logimmediately, false).forEach(logger::error);
				this.callResult(Constants.RFC_INVENTARIO, CODE, rstart.getControle(), "ICW_0022", "RECEBIDO INVENTÁRIO VAZIO");
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, rstart.getControle(), Constants.RFC_INVENTARIO, "Erro " + e.getLocalizedMessage()));
			prof.done(e.getLocalizedMessage(), logimmediately, true);
		}
		prof.done("Inventário SAP Importado", logimmediately, true);
		return false;
	}

	@Override
	public boolean external(Object obj) {
		logger.info("================================================================" + Constants.RFC_INVENTARIO + "===========================================================================");
		LinkedList<LinkedHashMap<String, Object>> ret = new LinkedList<LinkedHashMap<String, Object>>();
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
		final DecimalFormat DF = new DecimalFormat("#0.0000", symbols);
		Document inventory = (Document) obj;
		Supplier<Stream<DocumentField>> supDfs = () -> inventory.getFields().stream();
		DocumentField dfCentro = supDfs.get()
						.filter(df -> df.getField().getMetaname().equals("CENTRO"))
						.findAny()
						.get();
		DocumentField dfDep = supDfs.get()
						.filter(df -> df.getField().getMetaname().equals("DEPOSITO"))
						.findAny()
						.get();
		DocumentField dfAno = supDfs.get()
						.filter(df -> df.getField().getMetaname().equals("ANO"))
						.findAny()
						.get();
		Map<Product, List<DocumentItem>> diMap = inventory.getItems().stream()
						.collect(Collectors.groupingBy(DocumentItem::getProduct));
		for (Product p : diMap.keySet()) {
			LinkedHashMap<String, Object> item = new LinkedHashMap<String, Object>();
			DocumentItem di = diMap.get(p).stream().reduce((di1, di2) -> {
				di1.setQty(di1.getQty() + di2.getQty());
				return di1;
			}).get();

			logger.info("********************************************************************");
			logger.info("CODE: " + inventory.getCode() + " Product: " + di.getProduct().getSku() + " QTY: " + DF.format(di.getQty()));
			logger.info("********************************************************************");
			item.put("CENTRO", dfCentro.getValue());
			item.put("DEPOSITO", dfDep.getValue());
			item.put("NUMDOC", inventory.getCode());
			item.put("EXERCICIO", dfAno.getValue());
			item.put("DATA", inventory.getCreatedAt());
			item.put("ITEM", di.getProperties().get("LINHA"));
			item.put("MATERIAL", String.join("", Collections.nCopies(18 - di.getProduct().getSku().length(), "0")) + di.getProduct().getSku());
			item.put("QUANTIDADE", di.getQty());
			item.put("UND", di.getMeasureUnit());
			item.put("PAC", "");
			ret.add(item);
		}
		inventory.setStatus("ENVIANDO SAP");
		getISvc().getRegSvc().getDcSvc().persist(inventory);
		sendToSAP(ret, inventory);
		return true;
	}

	private void sendToSAP(LinkedList<LinkedHashMap<String, Object>> ret, Document inventory) {
		boolean hasErrors = false;

		if (ConfigUtil.get("hunter-custom-solar", "integration_sap_enabled", "true").equalsIgnoreCase("true")) {
			ToJsonSAP jcoSonStart = new ToJsonSAP(getSolar().getFunc(Constants.RFC_INVENTARIO));

			jcoSonStart.setParameters(new HashMap<String, Object>() {
				private static final long serialVersionUID = 6122767519535489994L;

				{
					put(Constants.I_CONTROLE, inventory.getCode());
					put(Constants.I_FLUXO, "H");
					put(Constants.I_REGISTRO, ret);
				}
			});

			ReadFieldsSap readFieldsSap = null;
			try {
				logger.info("************************** " + Constants.RFC_INVENTARIO + " Enviando " + inventory.getCode() + " para SAP ********************************");
				String funcRet = jcoSonStart.execute(getSolar().getDestination());

				readFieldsSap = getGson().fromJson(funcRet, ReadFieldsSap.class);
			} catch (JCoException e) {
				RuntimeException t = new RuntimeException();
				t.setStackTrace(e.getStackTrace());
				t.initCause(e.getCause());
				throw t;
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
				logger.trace(e.getLocalizedMessage(), e);
				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, inventory.getCode(), Constants.RFC_INVENTARIO, e.getCause().getLocalizedMessage()));
			}

			if (readFieldsSap.getJcoError() != null && !readFieldsSap.getJcoError().isEmpty()) {
				hasErrors = true;
				logJcoError(readFieldsSap, inventory.getCode(), Constants.RFC_INVENTARIO);
			}

			for (SAPReturnDTO msg : readFieldsSap.getReturnDTOs()) {
				AlertSeverity sev = AlertSeverity.INFO;

				if (msg.getTipo().equals("E")) {
					sev = AlertSeverity.ERROR;
					hasErrors = true;
				} else if (msg.getTipo().equals("W"))
					sev = AlertSeverity.WARNING;
				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, inventory.getCode(), Constants.RFC_INVENTARIO, msg.getSeq() + " - " + msg.getMensagem()));
			}
		} else
			logger.warn("SAP Integration disabled in config file");
		inventory.setStatus(hasErrors ? "FALHA SAP" : "CONCLUIDO");
		getISvc().getRegSvc().getDcSvc().persist(inventory);
	}
}
