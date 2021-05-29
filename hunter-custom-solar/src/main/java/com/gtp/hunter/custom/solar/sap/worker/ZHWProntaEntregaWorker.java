package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPProntaEntregaDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.custom.solar.util.Constants;
import com.gtp.hunter.custom.solar.util.ToJsonSAP;
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

public class ZHWProntaEntregaWorker extends BaseWorker {

	private transient static final Logger	logger			= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final boolean			logimmediately	= false;

	public ZHWProntaEntregaWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		return false;
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	public boolean external(Object obj) {
		logger.info("================================================================" + Constants.RFC_PRONTA_ENTREGA + "===========================================================================");
		Profiler prof = new Profiler();
		Document transp = (Document) obj;
		final DecimalFormat DF = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.US));
		String tkNum = transp.getCode().replace("R", "000");
		JCoFunction func = getSolar().getFunc(Constants.RFC_PRONTA_ENTREGA);
		ToJsonSAP jcoSonStart = new ToJsonSAP(func);
		ReadFieldsSap readFieldsSap = null;

		try {
			jcoSonStart.setParameter(Constants.I_TKNUM, tkNum);
			readFieldsSap = getGson().fromJson(jcoSonStart.execute(getSolar().getDestination()), ReadFieldsSap.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JCoException e) {
			e.printStackTrace();
			this.callResult(Constants.RFC_PRONTA_ENTREGA, Constants.CODE_PRONTA_ENTREGA, tkNum, "ICW_0127", e.getLocalizedMessage());
		}
		logger.debug(prof.step("ReadFieldsSAP", logimmediately));
		logJcoError(readFieldsSap, tkNum, Constants.RFC_PRONTA_ENTREGA);
		try {
			if (!readFieldsSap.geteRetorno().equals("E")) {
				if (readFieldsSap.getProntaEntregaDTOs().size() > 0) {
					DocumentModel nfProntaEntrega = getISvc().getRegSvc().getDmSvc().findByMetaname("NFPENTREGA");
					DocumentModelField dmfSAP = DocumentModels.findField(nfProntaEntrega, "SAP");
					DocumentModelField dmfSAPOrig = DocumentModels.findField(nfProntaEntrega, "SAPORIG");
					List<Document> pEntregas = new ArrayList<>();

					for (SAPProntaEntregaDTO dto : readFieldsSap.getProntaEntregaDTOs()) {
						Product p = getISvc().getRegSvc().getPrdSvc().findBySKU(String.valueOf(Integer.parseInt(dto.getMatnr())));
						String mu = Products.getStringField(p, "GROUP_UM");
						Document pEntrega = pEntregas.parallelStream()
										.filter(d -> d.getCode().equals(dto.getTknumzvpe()))
										.findAny()
										.orElseGet(() -> {
											Document tmp = new Document(nfProntaEntrega, nfProntaEntrega.getName() + dto.getTknumzvpe(), dto.getTknumzvpe(), "INTEGRADO");

											tmp.setParent(transp);
											tmp.getFields().add(new DocumentField(tmp, dmfSAP, "NOVO", dto.getTknumzvpe()));
											tmp.getFields().add(new DocumentField(tmp, dmfSAPOrig, "NOVO", dto.getTknumzspe()));
											pEntregas.add(tmp);
											return tmp;
										});
						DocumentItem diVenda = new DocumentItem(pEntrega, p, dto.getKbmeng_zvpe(), "NOVO", mu);

						diVenda.getProperties().put("SAIDA", DF.format(dto.getKbmeng_zspe()));
						diVenda.getProperties().put("RETORNO", DF.format(dto.getKbmeng_zspe() - dto.getKbmeng_zvpe()));
						pEntrega.getItems().add(diVenda);
					}
					getISvc().getRegSvc().getDcSvc().multiPersist(pEntregas);
				}
				this.callResult(Constants.RFC_PRONTA_ENTREGA, Constants.CODE_PRONTA_ENTREGA, tkNum, "ICW_0000", "SUCESSO (" + readFieldsSap.getProntaEntregaDTOs().size());
			} else {
				this.callResult(Constants.RFC_PRONTA_ENTREGA, Constants.CODE_PRONTA_ENTREGA, tkNum, "ICW_0028", readFieldsSap.geteMensagem());
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			this.callResult(Constants.RFC_PRONTA_ENTREGA, Constants.CODE_PRONTA_ENTREGA, tkNum, "ICW_0027", e.getLocalizedMessage());
			prof.done(e.getLocalizedMessage(), logimmediately, false).forEach(logger::error);
		}
		return false;
	}
}
