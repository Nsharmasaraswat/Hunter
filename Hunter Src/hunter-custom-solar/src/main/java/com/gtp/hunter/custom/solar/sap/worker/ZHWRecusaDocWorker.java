package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPRecusaDocDTO;
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
import com.gtp.hunter.process.model.util.DocumentModels;
import com.gtp.hunter.process.model.util.Products;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;

public class ZHWRecusaDocWorker extends BaseWorker {

	private transient static final Logger	logger			= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final boolean			logimmediately	= false;

	public ZHWRecusaDocWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	public boolean work(SAPReadStartDTO rstart) {
		logger.info("================================================================" + Constants.RFC_RECUSA_NF + "===========================================================================");
		Profiler prof = new Profiler();
		JCoFunction func = getSolar().getFunc(Constants.RFC_RECUSA_NF);
		ToJsonSAP jcoSonStart = new ToJsonSAP(func);
		jcoSonStart.setParameter("I_DATA", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
		jcoSonStart.setParameter("I_WERKS", ConfigUtil.get("hunter-custom-solar", "sap-plant", "CNAT"));
		logger.debug(prof.step("JcoSetParameters", logimmediately));
		ReadFieldsSap readFieldsSap = null;

		try {
			readFieldsSap = getGson().fromJson(jcoSonStart.execute(getSolar().getDestination()), ReadFieldsSap.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JCoException e) {
			e.printStackTrace();
			this.callResult(Constants.RFC_RECUSA_NF, Constants.CODE_RECUSA_NF, rstart.getControle(), "ICW_0127", e.getLocalizedMessage());
		}
		logger.debug(prof.step("ReadFieldsSAP", logimmediately));

		logJcoError(readFieldsSap, rstart.getControle(), Constants.RFC_RECUSA_NF);

		try {
			if (!readFieldsSap.geteRetorno().equals("E")) {
				DocumentModel recusaNF = getISvc().getRegSvc().getDmSvc().findByMetaname("RECUSANF");
				DocumentModel nfsModel = getISvc().getRegSvc().getDmSvc().findByMetaname("NFSAIDA");
				DocumentModelField dmfReason = DocumentModels.findField(recusaNF, "REASON");
				DocumentModelField dmfDesc = DocumentModels.findField(recusaNF, "DESCRIPTION");
				List<Document> pRecusas = new ArrayList<>();
				Set<String> skip = readFieldsSap.getRecusaNFDTOs()
								.parallelStream()
								.map(dto -> dto.getDocNum())
								.distinct()
								.collect(Collectors.toSet());

				skip.removeIf(c -> !getISvc().getRegSvc().getDcSvc().checkExistence(recusaNF, c));
				for (SAPRecusaDocDTO dto : readFieldsSap.getRecusaNFDTOs()) {
					String docCode = dto.getDocNum();

					if (skip.contains(docCode)) continue;

					Product p = getISvc().getRegSvc().getPrdSvc().findBySKU(String.valueOf(Integer.parseInt(dto.getMatnr())));
					if (p == null) {
						getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, rstart.getControle(), Constants.RFC_RECUSA_NF, "Documento " + docCode + " com produto inexistente " + dto.getMatnr()));
						continue;
					}

					String mu = Products.getStringField(p, "GROUP_UM", "CX");
					Document dRecusa = pRecusas.parallelStream()
									.filter(d -> d.getCode().equals(docCode))
									.findAny()
									.orElse(null);

					if (dRecusa == null) {
						logger.info("Creating Document " + docCode);
						String nfeCode = String.join("", Collections.nCopies(10 - dto.getNfeNum().length(), "0")) + dto.getNfeNum();
						Document nfs = getISvc().getRegSvc().getDcSvc().findByModelAndCodeAndPersonCode(nfsModel, nfeCode, dto.getKunnr());

						dRecusa = new Document(recusaNF, recusaNF.getName() + docCode, docCode, "INTEGRADO");

						if (nfs != null) {
							nfs.setStatus("RECUSADO");
							dRecusa.setParent(nfs);
						}
						dRecusa.getItems().add(new DocumentItem(dRecusa, p, dto.getMenge(), "NOVO", mu));
						dRecusa.getFields().add(new DocumentField(dRecusa, dmfReason, "NOVO", String.valueOf(dto.getReason())));
						dRecusa.getFields().add(new DocumentField(dRecusa, dmfDesc, "NOVO", dto.getDescription()));
						pRecusas.add(dRecusa);
					} else if (!dRecusa.getItems().parallelStream().anyMatch(di -> di.getProduct().getId().equals(p.getId()))) {
						logger.info("Adding Item to Document " + docCode);
						dRecusa.getItems().add(new DocumentItem(dRecusa, p, dto.getMenge(), "NOVO", mu));
					}
				}
				getISvc().getRegSvc().getDcSvc().multiPersist(pRecusas);
				//				getISvc().getRegSvc().getDcSvc().multiPersist(pRecusas.parallelStream()
				//								.map(r -> r.getParent())
				//								.collect(Collectors.toList()));
				this.callResult(Constants.RFC_RECUSA_NF, Constants.CODE_RECUSA_NF, rstart.getControle(), "ICW_0000", "SUCESSO");
			} else {
				this.callResult(Constants.RFC_RECUSA_NF, Constants.CODE_RECUSA_NF, rstart.getControle(), "ICW_0028", readFieldsSap.geteMensagem());
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			this.callResult(Constants.RFC_RECUSA_NF, Constants.CODE_RECUSA_NF, rstart.getControle(), "ICW_0027", e.getLocalizedMessage());
			prof.done(e.getLocalizedMessage(), logimmediately, false).forEach(logger::error);
		}
		return false;
	}

	@Override
	public boolean external(Object obj) {
		return true;
	}
}
