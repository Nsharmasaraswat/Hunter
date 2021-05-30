package com.gtp.hunter.custom.solar.sap.worker;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.custom.solar.sap.SAPSolar;
import com.gtp.hunter.custom.solar.sap.dtos.ReadFieldsSap;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReadStartDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPReturnDTO;
import com.gtp.hunter.custom.solar.sap.dtos.SAPVehicleDTO;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.service.SAPService;
import com.gtp.hunter.custom.solar.util.Constants;
import com.gtp.hunter.custom.solar.util.ToJsonSAP;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductModel;
import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.PropertyModelField;
import com.gtp.hunter.process.model.Thing;
import com.sap.conn.jco.JCoException;

public class ZHWInformacaoVeiculoWorker extends BaseWorker {
	private static final boolean			logimmediately	= false;
	private transient static final Logger	logger			= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ZHWInformacaoVeiculoWorker(SAPService svc, SAPSolar solar, IntegrationService integrationService) {
		super(svc, solar, integrationService);
	}

	@Override
	public boolean work(SAPReadStartDTO rstart) {
		logger.info("================================================================" + Constants.RFC_VEICULOS + "===========================================================================");
		Profiler prof = new Profiler();
		ToJsonSAP jcoSonStart = new ToJsonSAP(getSolar().getFunc(Constants.RFC_VEICULOS));
		jcoSonStart.setParameters(new HashMap<String, Object>() {
			private static final long serialVersionUID = 6122767519535489994L;

			{
				put(Constants.I_CONTROLE, rstart.getControle());
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

		logJcoError(readFieldsSap, rstart.getControle(), Constants.RFC_VEICULOS);

		try {
			if (!readFieldsSap.getListaVeiculoDTOs().isEmpty()) {
				SAPVehicleDTO vehicle = readFieldsSap.getListaVeiculoDTOs().get(0);

				logger.info(vehicle.toString());
			} else {
				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, rstart.getControle(), Constants.RFC_VEICULOS, "Recebido Veículo Vazio!"));
				prof.done("Recebido veículo vazio!", logimmediately, false).forEach(logger::error);
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.ERROR, rstart.getControle(), Constants.RFC_VEICULOS, "Erro " + e.getLocalizedMessage()));
			prof.done(e.getLocalizedMessage(), logimmediately, true);
		}
		prof.done("Inventário SAP Importado", logimmediately, true);
		return false;
	}

	@Override
	public boolean external(Object obj) {
		logger.info("================================================================" + Constants.RFC_VEICULOS + "===========================================================================");
		logger.info("================================================================NÃO IMPLEMENTADO===========================================================================");
		return true;
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public List<Thing> registerVehicles(List<String> transpList) {
		logger.info("================================================================" + Constants.RFC_VEICULOS + "===========================================================================");
		List<Thing> ret = new ArrayList<>();
		Profiler prof = new Profiler();
		ReadFieldsSap readFieldsSap = null;
		ToJsonSAP jcoSonStart = new ToJsonSAP(getSolar().getFunc(Constants.RFC_VEICULOS));
		LinkedList<LinkedHashMap<String, Object>> tblValues = new LinkedList<>();

		for (String tknum : transpList) {
			LinkedHashMap<String, Object> val = new LinkedHashMap<>();

			val.put("TKNUM_NUM", tknum);
			tblValues.add(val);
		}
		jcoSonStart.setParameter(Constants.I_FLUXO, "L");
		jcoSonStart.setParameter(Constants.I_TKNUM, tblValues);
		logger.info(prof.step("JcoSetParameters", logimmediately));

		try {
			readFieldsSap = getGson().fromJson(jcoSonStart.execute(getSolar().getDestination()), ReadFieldsSap.class);
			List<SAPVehicleDTO> dtoList = readFieldsSap.getListaVeiculoDTOs();
			ProductModel pmTruck = getISvc().getRegSvc().getPmSvc().findByMetaname("TRUCK");
			List<Product> pList = getISvc().getRegSvc().getPrdSvc().listByModelMetaname("TRUCK");

			for (SAPVehicleDTO dto : dtoList) {
				Optional<Product> optPrd = pList.parallelStream().filter(p -> p.getSku().equals(dto.getTipoVeiculo())).findAny();
				Product pTruck = optPrd.isPresent() ? optPrd.get() : new Product(dto.getTipo_rodado() + " - " + dto.getCarroceria(), pmTruck, dto.getTipoVeiculo(), "INTEGRADO");
				Thing tmp = getISvc().getRegSvc().getThSvc().quickFindByUnitTagId(dto.getPlaca());
				Thing t = tmp == null ? new Thing(pTruck.getName(), pTruck, pmTruck.getPropertymodel(), "NOVO") : tmp;
				Unit tmpPlaca = getISvc().getRegSvc().getUnSvc().findByTagId(dto.getPlaca());
				Unit tmpIdSap = getISvc().getRegSvc().getUnSvc().findByTagId(dto.getCodigo());
				Unit placa = tmpPlaca == null ? new Unit("PLACA", dto.getPlaca(), UnitType.LICENSEPLATES) : tmpPlaca;
				Unit idSap = tmpIdSap == null ? new Unit("SAP", dto.getCodigo(), UnitType.EXTERNAL_SYSTEM) : tmpIdSap;

				for (PropertyModelField prmf : pmTruck.getPropertymodel().getFields()) {
					Optional<Property> optPr = t.getProperties().stream().filter(pr -> pr.getField().getId().equals(prmf.getId())).findAny();
					Property pr = optPr.isPresent() ? optPr.get() : new Property(t, prmf, "", "NOVO");
					String value = "";

					switch (prmf.getMetaname()) {
						case "TRUCK_CAPACITY":
							value = String.valueOf(dto.getCapacidade());
							break;
						case "FORKLIFT_TYPE":
							value = "SIMPLES";
							break;
						case "SERVICE_TYPE":
							value = "ROTA";
							break;
						case "CARRIER":
							value = "SOLAR";
							break;
						case "CODE":
							value = dto.getPlaca();
							break;
						case "BRAND":
							value = dto.getTipo_rodado();
							break;
						case "MODEL":
							value = dto.getCarroceria();
							break;
						case "RIGHT_SIDE_QUANTITY":
							value = "UPDATE_ON_REALPICKING";
							break;
						case "LEFT_SIDE_QUANTITY":
							value = "UPDATE_ON_REALPICKING";
							break;
					}
					pr.setValue(value);

					if (t.getId() != null)
						getISvc().getRegSvc().getPrSvc().quickInsert(t.getId(), prmf.getId(), value);
					else
						t.getProperties().add(pr);
				}
				t.getUnitModel().add(idSap);
				t.getUnitModel().add(placa);
				t.getUnits().add(tmpIdSap == null ? getISvc().getRegSvc().getUnSvc().persist(idSap).getId() : idSap.getId());
				t.getUnits().add(tmpPlaca == null ? getISvc().getRegSvc().getUnSvc().persist(placa).getId() : placa.getId());
				if (!dto.getStatus().isEmpty()) t.setStatus(dto.getStatus());
				if (!optPrd.isPresent()) {
					getISvc().getRegSvc().getPrdSvc().persist(pTruck);
					pList.add(pTruck);
				}
				getISvc().getRegSvc().getThSvc().persist(t);
				ret.add(t);
			}
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JCoException e) {
			e.printStackTrace();
		}
		if (readFieldsSap != null) {
			List<SAPReturnDTO> returnList = readFieldsSap.getReturnDTOs()
							.stream()
							.sorted((ret1, ret2) -> {
								return Integer.valueOf(ret1.getSeq().replace("*", "")) - Integer.valueOf(ret2.getSeq().replace("*", ""));
							}).collect(Collectors.toList());

			for (SAPReturnDTO msg : returnList) {
				AlertSeverity sev = AlertSeverity.INFO;

				if (msg.getTipo().equals("E"))
					sev = AlertSeverity.ERROR;
				else if (msg.getTipo().equals("W"))
					sev = AlertSeverity.WARNING;
				getISvc().getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, sev, "LISTA", msg.getMensagem(), msg.getSeq()));
			}
		}
		getISvc().getRegSvc().getThSvc().flush();
		return ret;
	}
}
