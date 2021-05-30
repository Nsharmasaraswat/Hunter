package com.gtp.hunter.core.devices;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.devicedata.DeviceData;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Source;

import io.reactivex.subjects.PublishSubject;

public class SensorDevice extends BaseDevice {
	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public SensorDevice(Source src, Device model, PublishSubject<Command> commands) {
		super(src, model, commands);
	}

	@Override
	protected Command execute(DeviceData data) {
		Command cmd = new Command();

		return sendSyncCommand(cmd);
	}

	public Command disable() {
		Command cmd = super.getBaseCommand();

		cmd.setPayload("");
		cmd.setMethod("disable");
		long beforeSend = System.currentTimeMillis();
		cmd = sendSyncCommand(cmd);
		logger.debug("Sent in " + (System.currentTimeMillis() - beforeSend) + "ms");
		return cmd;
	}

	public Command enable() {
		Command cmd = super.getBaseCommand();

		cmd.setPayload("");
		cmd.setMethod("enable");
		long beforeSend = System.currentTimeMillis();
		cmd = sendSyncCommand(cmd);
		logger.debug("Sent in " + (System.currentTimeMillis() - beforeSend) + "ms");
		return cmd;
	}
}
