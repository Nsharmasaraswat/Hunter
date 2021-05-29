package com.gtp.hunter.process.wf.action.solar;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Location;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.process.wf.action.BaseAction;

public class AGLCarregamentoRotaAction extends BaseAction {

	private transient static final Logger	logger		= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static int						lastDock	= 0;
	private Map<String, Object>				params		= new HashMap<String, Object>();

	public AGLCarregamentoRotaAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
		this.params = JsonUtil.jsonToMap(action.getParams());
	}

	@Override
	public String execute(Action t) throws Exception {
		String masterId = (String) params.get("master-id");
		String masterStatus = (String) params.get("master-status");
		String childModelMeta = (String) params.get("child-model-meta");
		String childStatus = (String) params.get("child-status");
		String childCodePrefix = (String) params.get("child-code-prefix");
		Document dMaster = getRegSvc().getDcSvc().findById(UUID.fromString(masterId));
		logger.info("RODANDO ACTION DE CARREGAENTO DE CAMINHAO DE ROTA " + dMaster.getCode());

		if (!getRsm().getTsm().isTaskLocked(dMaster.getId()) || getRsm().getTsm().isTaskBoundToUser(getUser().getId(), dMaster.getId())) {
			getRsm().getTsm().cancelTask(getUser().getId(), dMaster);
			Thing truck = dMaster.getThings().parallelStream()
							.filter(dt -> dt.getThing().getProduct().getModel().getMetaname().equals("TRUCK"))
							.map(dt -> dt.getThing())
							.findAny()
							.orElse(null);
			if (truck != null) {
				Location loc = getRegSvc().getLocSvc().findByMetaname("WMS");
				List<Address> docks = loc.getAddresses().parallelStream()
								.filter(a -> a.getMetaname().startsWith("VIRTUALDOCK"))
								.sorted((a1, a2) -> a1.getMetaname().compareTo(a2.getMetaname()))
								.collect(Collectors.toList());
				if (lastDock >= docks.size())
					lastDock = 0;
				Address dock = docks.parallelStream()
								.filter(a -> Integer.parseInt(a.getMetaname().replace("VIRTUALDOCK", "")) > lastDock)
								//.filter(a -> getRegSvc().getThSvc().listByAddressId(a.getId()).size() == 0)//Consertar Processo e docas antes de ativar, por enquanto pega sempre a proxima
								.findFirst()
								.orElse(null);
				if (dock != null) {
					lastDock = Integer.parseInt(dock.getMetaname().replace("VIRTUALDOCK", ""));
					dMaster.getFields().parallelStream().filter(df -> df.getField().getMetaname().equals("DOCK")).findAny().get().setValue(dock.getId().toString());
					truck.setAddress(dock);
				} else
					getRegSvc().getAlertSvc().persist(new Alert(AlertType.WMS, AlertSeverity.WARNING, dMaster.getCode(), "DOCAS OCUPADAS", "Não há docas disponíveis para o transporte"));
			} else
				getRegSvc().getAlertSvc().persist(new Alert(AlertType.WMS, AlertSeverity.WARNING, dMaster.getCode(), "TRANSPORTE SEM VEÍCULO", "Não há veículo associado ao transporte"));

			getRegSvc().getDcSvc().createChild(dMaster, masterStatus, childModelMeta, childStatus, childCodePrefix, null, null, null, getUser());
			//TODO: Integracao WMS
			//getRegSvc().getAglSvc().sendDocToWMS(dMaster, "POST");
			getRsm().getTsm().unlockTask(dMaster);
		}
		return t.getRoute();
	}
}
