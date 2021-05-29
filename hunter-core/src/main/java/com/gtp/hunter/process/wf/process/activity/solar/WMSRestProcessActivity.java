package com.gtp.hunter.process.wf.process.activity.solar;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.BaseModel;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.process.BaseProcess;
import com.gtp.hunter.process.wf.process.activity.BaseProcessActivity;
import com.gtp.hunter.process.wf.process.activity.ProcessActivityExecuteReturn;

public class WMSRestProcessActivity extends BaseProcessActivity {

	private final Logger		logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private String				method;
	private String				verb;
	private Map<String, Object>	params;

	public WMSRestProcessActivity(ProcessActivity model, BaseOrigin origin) {
		super(model, origin);
		this.params = JsonUtil.jsonToMap(model.getParam());
		this.method = (String) params.get("webservice-method");
		this.verb = (String) params.get("http-method");
	}

	@Override
	public ProcessActivityExecuteReturn executePostConstruct() {
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn executePreTransform(Object arg) {
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn execute(BaseModel arg) {
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn execute(BaseProcess p) {
		Map<String, Object> procParams = p.getParametros();

		if (procParams.containsKey("doc")) {
			Document d = (Document) procParams.remove("doc");

			try {

				if (d.getModel().getMetaname().equals("TRANSPORT")) {
					boolean shouldConf = d.getSiblings().stream()
									.filter(sib -> (sib.getModel().getMetaname().equals("NFENTRADA") || sib.getModel().getMetaname().equals("NFSAIDA")) && Documents.getStringField(sib, "ZTRANS", "").equalsIgnoreCase("S"))
									.flatMap(nf -> nf.getItems().stream())
									.map(di -> di.getProduct().getModel())
									.anyMatch(pm -> pm.getProperties().containsKey("blind_conf") && pm.getProperties().get("blind_conf").equalsIgnoreCase("true"));
					//					boolean genConf = d.getSiblings().stream()
					//									.filter(sib -> sib.getModel().getMetaname().equals("NFSAIDA") && !Documents.getStringField(sib, "ZTRANS", "").equalsIgnoreCase("N"))
					//									.flatMap(nf -> nf.getItems().stream())
					//									.map(di -> di.getProduct().getModel())
					//									.anyMatch(pm -> pm.getProperties().containsKey("conf_out") && pm.getProperties().get("conf_out").equalsIgnoreCase("true"));
					//					Thing truck = p.getRegSvc().getThSvc().findByUnitId(d.getThings().stream()
					//									.filter(dt -> dt.getThing().getUnits().size() > 0)
					//									.findAny().get().getThing().getUnits().stream()
					//									.findAny().get());
					//					String plates = truck.getUnitModel().stream().filter(u -> u.getType() == UnitType.LICENSEPLATES).findFirst().get().getTagId();
					//					String docaChegada = d.getSiblings().stream()
					//									.filter(ds -> ds.getModel().getMetaname().equals("APODOCA"))
					//									.flatMap(ds -> ds.getFields().stream())
					//									.filter(df -> df.getField().getMetaname().equals("ARRIVAL_DOCK_ID"))
					//									.findAny()
					//									.get().getValue();
					//					if (params.containsKey(docaChegada)) {
					//						Address docaVirtual = p.getRegSvc().getAddSvc().findById(UUID.fromString((String) params.get(docaChegada)));
					//
					//						int left = Integer.parseInt(truck.getProperties().stream().filter(pr -> pr.getField().getMetaname().equalsIgnoreCase("LEFT_SIDE_QUANTITY")).findAny().get().getValue());
					//						int right = Integer.parseInt(truck.getProperties().stream().filter(pr -> pr.getField().getMetaname().equalsIgnoreCase("RIGHT_SIDE_QUANTITY")).findAny().get().getValue());
					//						List<Address> bays = docaVirtual.getSiblings()
					//										.stream()
					//										.filter(a -> {
					//											String sOrder = a.getFields().stream().filter(af -> af.getModel().getMetaname().equalsIgnoreCase("ROAD_SEQ")).findAny().get().getValue();
					//											int order = Integer.parseInt(sOrder);
					//
					//											return order <= (left + right);
					//										})
					//										.sorted((a1, a2) -> {
					//											String sOrder1 = a1.getFields().stream().filter(af -> af.getModel().getMetaname().equalsIgnoreCase("ROAD_SEQ")).findAny().get().getValue();
					//											String sOrder2 = a2.getFields().stream().filter(af -> af.getModel().getMetaname().equalsIgnoreCase("ROAD_SEQ")).findAny().get().getValue();
					//											int order1 = Integer.parseInt(sOrder1);
					//											int order2 = Integer.parseInt(sOrder2);
					//
					//											return order1 - order2;
					//										}).collect(Collectors.toList());
					//						for (Address baia : bays)
					//							p.getRegSvc().getWmsSvc().updateAddressCode(baia.getId(), plates);
					//						if (d.getSiblings().stream().anyMatch(ds -> ds.getModel().getMetaname().equals("NFSAIDA") && !Documents.getStringField(ds, "ZTRANS", "").equalsIgnoreCase("N"))) {
					//							Collections.reverse(bays);
					//							p.getRegSvc().getWmsSvc().generatePicking(d, bays);
					//						}
					//						if (genConf) {
					//							p.getRegSvc().getWmsSvc().createOutboundChecking(d);
					//						}
					//						p.getRegSvc().getAglSvc().sendDocToWMS(d, "PUT");
					if (!shouldConf)
						p.getRegSvc().getDcSvc().createChild(d, "CAMINHAO DESCARREGADO", "APODESCARGA", "NOVO", "DES", null, null, null, null);
					//					} else if (d.getSiblings().stream()
					//									.filter(ds -> ds.getModel().getMetaname().equals("NFENTRADA"))
					//									.flatMap(nf -> nf.getItems().stream())
					//									.map(di -> di.getProduct().getModel())
					//									.anyMatch(pm -> pm.getMetaname().equals("MP"))) {
					//						p.getRegSvc().getAglSvc().sendDocToWMS(d, "PUT");
					//					} else
					//						logger.error("Dock " + docaChegada + " Is not in processActivity Params");
				} else if (d.getModel().getMetaname().equals("ORDCRIACAO")) {
					p.getRegSvc().getAglSvc().sendDocToWMS(d, "POST");
				}
			} catch (Exception e) {
				logger.error("Attempt to send truck to exit failed");
				logger.trace(e.getLocalizedMessage(), e);
			}
		}
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public BaseModel executeUnknown(Object arg) {
		return null;
	}

	@Override
	public ProcessActivityExecuteReturn execute() {
		// TODO Auto-generated method stub
		return null;
	}
}