package com.gtp.hunter.core.devices;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.devicedata.DeviceData;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Source;

import io.reactivex.subjects.PublishSubject;

public class CompanionDevice extends BaseDevice {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public CompanionDevice(Source src, Device model, PublishSubject<Command> commands) {
		super(src, model, commands);
	}

	@Override
	protected Command execute(DeviceData data) {
		Command cmd = new Command();

		if (data != null)
			cmd.setPayload(String.valueOf(data));// PLACEHOLDER
		return sendSyncCommand(cmd);
	}

	public Command startMission() {
		Command cmd = getBaseCommand();

		cmd.setMethod("startMission");
		cmd.setPort(-1);
		long beforeSend = System.currentTimeMillis();
		cmd = sendSyncCommand(cmd);
		logger.debug("Sent in " + (System.currentTimeMillis() - beforeSend) + "ms");
		return cmd;
	}

	public Command uploadMission() {
		Command cmd = getBaseCommand();

		cmd.setMethod("uploadMission");
		cmd.setPort(-1);
		long beforeSend = System.currentTimeMillis();
		cmd = sendSyncCommand(cmd);
		logger.debug("Sent in " + (System.currentTimeMillis() - beforeSend) + "ms");
		return cmd;
	}

	public Command prepareForLaunch() {
		Command cmd = getBaseCommand();

		cmd.setMethod("prepareForLaunch");
		cmd.setPort(-1);
		long beforeSend = System.currentTimeMillis();
		cmd = sendSyncCommand(cmd);
		logger.debug("Sent in " + (System.currentTimeMillis() - beforeSend) + "ms");
		return cmd;
	}

}
