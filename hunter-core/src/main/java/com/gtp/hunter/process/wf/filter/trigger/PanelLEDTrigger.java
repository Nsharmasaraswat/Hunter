package com.gtp.hunter.process.wf.filter.trigger;

import java.lang.invoke.MethodHandles;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.devices.SensorDevice;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.origin.DeviceOrigin;

public class PanelLEDTrigger extends BaseTrigger {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private ScheduledExecutorService		exec	= Executors.newScheduledThreadPool(2);

	public PanelLEDTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		Document transport = (Document) mdl.getModel();
		String sDock = Documents.getStringField(transport, "DOCK");

		if (!sDock.isEmpty()) {
			UUID originId = UUID.fromString((String) getParams().get("origin-id"));
			UUID dockId = UUID.fromString(sDock);
			Thing truck = transport.getThings().parallelStream()
							.filter(dt -> dt.getThing().getProduct().getModel().getMetaname().equals("TRUCK"))
							.map(dt -> dt.getThing())
							.findAny()
							.orElse(null);

			if (truck != null) {
				Address dock = mdl.getRegSvc().getAddSvc().findById(dockId);
				DeviceOrigin o = (DeviceOrigin) mdl.getRsm().getOsm().getOrigin(originId);

				if (o.getDevices().containsKey("D" + dock.getMetaname())) {
					UUID deviceId = o.getDevices().get("D" + dock.getMetaname()).getModel().getId();
					SensorDevice dev = (SensorDevice) mdl.getRegSvc().getSrcSvc().getBaseDeviceByUUID(deviceId);
					Command cmd = new Command();
					String message = (String) getParams().get("message");
					String pfx = dev.getModel().getProperties().containsKey("prefix") ? dev.getModel().getProperties().get("prefix") : "";
					StringBuilder plates = new StringBuilder("");

					for (UUID uId : truck.getUnits()) {
						Unit u = mdl.getRegSvc().getUnSvc().findById(uId);

						if (u != null && u.getType() == UnitType.LICENSEPLATES) {
							plates.append(u.getTagId());
							break;
						}
					}
					String sCommand = "W_" + message.replace("%%prefix%%", pfx).replace("%%plates%%", plates.toString()) + ";";

					cmd.setSource(dev.getModel().getSource().getId());
					cmd.setDevice(deviceId);
					cmd.setPort(1);
					cmd.setMethod("sendCommand");
					cmd.setPayload(sCommand);
					Command ret = dev.sendSyncCommand(cmd);
					exec.schedule(() -> dev.sendSyncCommand(cmd), 1, TimeUnit.SECONDS);
					exec.schedule(() -> dev.sendSyncCommand(cmd), 5, TimeUnit.SECONDS);
					logger.info("Sending " + sCommand + " to " + dev.getModel().getMetaname() + " " + ret.getReturnValue());
				} else {
					logger.error("Cannot find a Device with Metaname: D" + dock.getMetaname());
				}
			} else {
				logger.warn("No Truck " + transport.getCode());
			}
		} else {
			logger.warn("Dock is Empty " + transport.getCode());
		}
		return true;
	}

}