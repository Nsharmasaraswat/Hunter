package com.gtp.hunter.core.devices.location;

import com.gtp.hunter.common.devicedata.DeviceData;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Source;

import io.reactivex.subjects.PublishSubject;

public class DartLocationDevice extends GPSLocationDevice {

	public DartLocationDevice(Source src, Device model, PublishSubject<Command> commands) {
		super(src, model, commands);
	}

	@Override
	protected Command execute(DeviceData data) {
		Command cmd = super.getBaseCommand();
		cmd.setReturnValue("true");
		return sendSyncCommand(cmd);
	}

	@Override
	public boolean enable() {
		Command cmd = super.getBaseCommand();

		cmd.setPayload("");
		cmd.setMethod("enable");
		long beforeSend = System.currentTimeMillis();
		cmd = sendSyncCommand(cmd);
		logger.debug("Sent in " + (System.currentTimeMillis() - beforeSend) + "ms");
		return cmd.getReturnValue().equalsIgnoreCase("true");
	}

	@Override
	public boolean disable() {
		Command cmd = super.getBaseCommand();

		cmd.setPayload("");
		cmd.setMethod("disable");
		long beforeSend = System.currentTimeMillis();
		cmd = sendSyncCommand(cmd);
		logger.debug("Sent in " + (System.currentTimeMillis() - beforeSend) + "ms");
		return cmd.getReturnValue().equalsIgnoreCase("true");
	}
}
