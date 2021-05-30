package com.gtp.hunter.core.devices;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.devicedata.DeviceData;
import com.gtp.hunter.common.devicedata.GPIOData;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Source;

import io.reactivex.subjects.PublishSubject;

public class GPIODevice extends BaseDevice {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public GPIODevice(Source src, Device model, PublishSubject<Command> commands) {
		super(src, model, commands);
	}

	@Override
	protected Command execute(DeviceData data) {
		Command cmd = new Command();

		cmd.setPayload(String.valueOf(cmd));// PLACEHOLDER
		return sendSyncCommand(cmd, 7);
	}

	public Command setState(DeviceData dt) {
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		Command cmd = getBaseCommand();
		GPIOData data = (GPIOData) dt;

		cmd.setMethod("setState");
		cmd.setPayload(gson.toJson(data));
		cmd.setPort(data.getPin());
		long beforeSend = System.currentTimeMillis();
		cmd = sendSyncCommand(cmd);
		logger.debug("Sent in " + (System.currentTimeMillis() - beforeSend) + "ms");
		return cmd;
	}

}
