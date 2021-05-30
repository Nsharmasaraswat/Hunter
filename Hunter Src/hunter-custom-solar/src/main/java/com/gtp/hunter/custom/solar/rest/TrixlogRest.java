package com.gtp.hunter.custom.solar.rest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;
import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.common.model.RawData.RawDataType;
import com.gtp.hunter.common.payload.LocatePayload;
import com.gtp.hunter.core.model.Alert;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Source;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.custom.solar.service.IntegrationService;
import com.gtp.hunter.custom.solar.trixlog.dto.TrixlogAlert;
import com.gtp.hunter.custom.solar.trixlog.dto.TrixlogCoordinate;
import com.gtp.hunter.custom.solar.trixlog.dto.TrixlogPosition;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Thing;

@RequestScoped
@Path("/trixlog")
public class TrixlogRest {

	private static final Gson	gson	= new GsonBuilder()
					.setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
					.serializeNulls()
					.enableComplexMapKeySerialization()
					.excludeFieldsWithoutExposeAnnotation()
					.create();

	@Inject
	private transient Logger	logger;

	@Inject
	private IntegrationService	iSvc;

	@POST
	@Path("/coordinate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public IntegrationReturn postCoordinates(String body) {
		logger.debug(body);

		if (body.contains("openedPosition") && body.contains("closedPosition")) {
			parseAlerts(body);
		} else if (body.contains("vehiclePlate") && body.contains("position")) {
			parseCoordinates(body);
		}

		return IntegrationReturn.OK;
	}

	private void parseAlerts(String alerts) {
		Type tp = new TypeToken<ArrayList<TrixlogAlert>>() {
		}.getType();
		List<TrixlogAlert> alertList = gson.fromJson(alerts, tp);

		for (TrixlogAlert alert : alertList) {
			iSvc.getRegSvc().getAlertSvc().persist(new Alert(AlertType.INTEGRATION, AlertSeverity.INFO, alert.getVehicleCode() + " - " + alert.getVehiclePlate(), alert.getMessage(), "TRIXLOG: " + alert.getEventName()));
			generateRawdataEvent(alert.getVehiclePlate(), alert.getVehicleCode(), alert.getOpenedPosition());
		}
	}

	private void parseCoordinates(String coords) {
		Type tp = new TypeToken<ArrayList<TrixlogCoordinate>>() {
		}.getType();
		List<TrixlogCoordinate> coordinates = gson.fromJson(coords, tp);

		for (TrixlogCoordinate coord : coordinates) {
			generateRawdataEvent(coord.getVehiclePlate(), coord.getVehicleCode(), coord.getPosition());
		}
	}

	private void generateRawdataEvent(String vPlate, String vCode, TrixlogPosition position) {
		if (!vPlate.contains("-")) vPlate = vPlate.substring(0, 3) + "-" + vPlate.substring(3);
		Unit plates = iSvc.getRegSvc().getUnSvc().findByTagId(vPlate);

		if (plates != null) {
			Thing truck = iSvc.getRegSvc().getThSvc().findByUnit(plates);

			if (truck != null) {
				iSvc.getRegSvc().getThSvc().fillUnits(truck);
				Unit gps = truck.getUnitModel()
								.parallelStream()
								.filter(u -> u.getType() == UnitType.RTLS)
								.findAny()
								.orElse(null);
				if (gps == null) {
					String tagId = String.join("", Collections.nCopies(6 - vCode.length(), "0")) + vCode;
					gps = new Unit("Trixlog " + tagId, "TXL" + tagId, UnitType.RTLS);
					iSvc.getRegSvc().getUnSvc().persist(gps);
					truck.getUnits().add(gps.getId());
					iSvc.getRegSvc().getThSvc().persist(truck);
				}
				Source source = iSvc.getRegSvc().getSrcSvc().findByMetaname("SLOCATION");
				Device device = iSvc.getRegSvc().getDevSvc().findByMetaname(source.getId(), "DLOCATE1");
				int port = 1;
				ComplexData cd = new ComplexData();
				LocatePayload locData = new LocatePayload();

				locData.setLatitude(position.getLatitude());
				locData.setLongitude(position.getLongitude());
				cd.setUnit(gps);
				cd.setType(RawDataType.LOCATION);
				cd.setTs(new Date().getTime());
				cd.setTagId(gps.getTagId());
				cd.setSource(source.getId());
				cd.setDevice(device.getId());
				cd.setPort(port);
				cd.setPayload(locData.toString());
				iSvc.getRegSvc().getRdSvc().processRawData(cd);
			} else
				logger.warn("Truck not registered: " + plates.getId() + " - " + plates.getTagId());
		} else
			logger.error("Unit not found: " + vPlate);
	}
}
