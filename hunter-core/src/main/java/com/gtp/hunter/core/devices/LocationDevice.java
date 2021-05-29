package com.gtp.hunter.core.devices;

import com.gtp.hunter.common.devicedata.DeviceData;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Source;

import io.reactivex.subjects.PublishSubject;

public abstract class LocationDevice extends BaseDevice {

	public LocationDevice(Source src, Device model, PublishSubject<Command> commands) {
		super(src, model, commands);
	}

	@Override
	protected Command execute(DeviceData data) {
		Command cmd = new Command();

		cmd.setPayload(String.valueOf(cmd));// PLACEHOLDER
		return sendSyncCommand(cmd);
	}

	public abstract boolean enable();

	public abstract boolean disable();

	public abstract String getCrs();

}
