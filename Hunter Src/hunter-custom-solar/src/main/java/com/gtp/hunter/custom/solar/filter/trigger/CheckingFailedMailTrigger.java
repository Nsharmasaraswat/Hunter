package com.gtp.hunter.custom.solar.filter.trigger;

import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Asynchronous;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;

public class CheckingFailedMailTrigger extends BaseMailTrigger {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public CheckingFailedMailTrigger(SAPSolar solar, SAPService svc, IntegrationService is) {
		super(solar, svc, is);
	}

	public CheckingFailedMailTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	@Asynchronous
	public boolean execute(BaseModelEvent mdl) {
		Document ordConf = (Document) mdl.getModel();

		if (ordConf != null) {
			logger.info("CheckingFailed Trigger: " + ordConf.getCode());
			String confType = Documents.getStringField(ordConf, "CONF_TYPE", "");
			Document tmpPick = confType.equals("SPA") ? is.getRegSvc().getDcSvc().quickFindParentDoc(ordConf.getId()) : null;
			Document transport = confType.equals("SPA") ? is.getRegSvc().getDcSvc().findParent(tmpPick.getId()) : is.getRegSvc().getDcSvc().findParent(ordConf);
			Document dNF = transport.getSiblings().parallelStream().filter(ds -> ds.getModel().getMetaname().equals("NFSAIDA") || ds.getModel().getMetaname().equals("NFENTRADA")).findAny().orElse(null);
			String transpType = Documents.getStringField(transport, "SERVICE_TYPE");
			String load = transpType.equals("ROTA") ? Documents.getStringField(transport, "OBS") : transport.getCode();
			String tknum = transport.getSiblings().parallelStream()
							.filter(ds -> ds.getModel().getMetaname().equals("NFSAIDA") || ds.getModel().getMetaname().equals("NFENTRADA"))
							.flatMap(nf -> nf.getFields().parallelStream())
							.filter(df -> df.getField().getMetaname().equals("TICKET") || df.getField().getMetaname().equals("TRANSPORTE_SAP") && !df.getValue().isEmpty())
							.map(df -> df.getValue())
							.distinct()
							.collect(Collectors.joining(","));
			String truck = transport.getFields().parallelStream()
							.filter(df -> df.getField().getMetaname().equals("TRUCK_ID") && !df.getValue().isEmpty() && df.getValue().length() == 36)
							.map(df -> is.getRegSvc().getThSvc().findById(UUID.fromString(df.getValue())))
							.flatMap(tr -> is.getRegSvc().getThSvc().fillUnits(tr).getUnitModel().parallelStream())
							.filter(un -> un.getType() == UnitType.EXTERNAL_SYSTEM && un.getName() != null && un.getName().equals("RealPicking"))
							.map(un -> un.getTagId())
							.findAny()
							.orElse("");
			String date = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
			String setor = dNF.getItems().parallelStream().anyMatch(di -> di.getProduct().getModel().getMetaname().equals("MP")) ? "Almoxarifado" : "Logistica";
			String groupMeta = "Hunter_CNAT_" + setor + "_Supervisor";
			String mailProp = groupMeta.toLowerCase().concat("-mail");
			String subjectProp = ordConf.getStatus().equals("FALHA SAP") ? "sap-failed-checking-mail-subject" : "failed-checking-mail-subject";
			String bodyProp = ordConf.getStatus().equals("FALHA SAP") ? "sap-failed-checking-mail-body" : "failed-checking-mail-body";

			sendMail(groupMeta, tknum, load, ordConf.getCode(), truck, date, mailProp, subjectProp, bodyProp);
		} else
			logger.info("Should not be null: " + mdl.getClass().getCanonicalName());
		return true;
	}
}
