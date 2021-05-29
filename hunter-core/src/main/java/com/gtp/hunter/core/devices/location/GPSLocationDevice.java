package com.gtp.hunter.core.devices.location;

import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.devices.LocationDevice;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Source;

import io.reactivex.subjects.PublishSubject;

public abstract class GPSLocationDevice extends LocationDevice {

	public GPSLocationDevice(Source src, Device model, PublishSubject<Command> commands) {
		super(src, model, commands);
	}

	@Override
	public final String getCrs() {
		return getModel().getProperties().get("CRS");
	}
}
