package com.gtp.hunter.core.devices;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.gtp.hunter.common.devicedata.DeviceData;
import com.gtp.hunter.common.devicedata.UbeeData;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Source;

import io.reactivex.subjects.PublishSubject;

public class UbeeSensorDevice extends SensorDevice {

	public UbeeSensorDevice(Source src, Device model, PublishSubject<Command> commands) {
		super(src, model, commands);
	}

	@Override
	protected Command execute(DeviceData data) {
		DecimalFormat df = new DecimalFormat("00");
		Command cmd = super.getBaseCommand();
		UbeeData d = (UbeeData) data;
		StringBuilder deviceCommandString = new StringBuilder(df.format(d.getSlaveId()));

		deviceCommandString.append("_").append(d.getCmd());
		for (String p : d.getParameters()) {
			deviceCommandString.append(p);
		}
		cmd.setPort(d.getSlaveId());
		cmd.setPayload(deviceCommandString.append("\n").toString());
		cmd.setMethod("sendCommand");
		return sendSyncCommand(cmd);
	}

	public Command reset(int id) {
		UbeeData data = new UbeeData();
		ArrayList<String> parms = new ArrayList<>();

		data.setSlaveId(id);
		data.setCmd("RESET");
		data.setParameters(parms);
		return execute(data);
	}

	public Command request(int id) {
		UbeeData data = new UbeeData();
		ArrayList<String> parms = new ArrayList<>();

		data.setSlaveId(id);
		data.setCmd("REQUEST");
		data.setParameters(parms);
		return execute(data);
	}

	public Command read(int id) {
		UbeeData data = new UbeeData();
		ArrayList<String> parms = new ArrayList<>();

		data.setSlaveId(id);
		data.setCmd("READ");
		data.setParameters(parms);
		return execute(data);
	}
}
