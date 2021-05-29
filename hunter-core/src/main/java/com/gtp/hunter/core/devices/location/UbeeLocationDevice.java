package com.gtp.hunter.core.devices.location;

import java.util.ArrayList;

import com.gtp.hunter.common.devicedata.DeviceData;
import com.gtp.hunter.common.devicedata.UbeeData;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Source;

import io.reactivex.subjects.PublishSubject;

public class UbeeLocationDevice extends GPSLocationDevice {

	//Comando CMD_LCD ( 999 )
	//Comando CMD_LED ( 1 )
	//Comando CMD_BUZZER ( 2 )
	//Comando CMD_WRITE_ID ( 3 )
	//Comando CMD_WRITE_EEPROM ( 4 )
	//Comando CMD_CONFIG_RADIO ( 5 )

	public UbeeLocationDevice(Source src, Device model, PublishSubject<Command> commands) {
		super(src, model, commands);
	}

	@Override
	protected Command execute(DeviceData data) {
		Command cmd = super.getBaseCommand();
		UbeeData d = (UbeeData) data;
		StringBuilder deviceCommandString = new StringBuilder(d.getSlaveId() + ";" + d.getCmd() + ";");

		for (String p : d.getParameters()) {
			deviceCommandString.append(p + ";");
		}
		cmd.setPayload(deviceCommandString.toString());
		cmd.setMethod("sendCommand");
		return sendSyncCommand(cmd);
	}

	public Command configureId(int tempId, int newId) {
		Command ret = super.getBaseCommand();
		UbeeData data = new UbeeData();
		ArrayList<String> parms = new ArrayList<>();

		data.setSlaveId(tempId);
		data.setCmd(String.valueOf(newId));
		data.setParameters(parms);
		ret = execute(data);
		return ret;
	}

	public Command led(int id, boolean turnOn, int period) {
		Command ret = super.getBaseCommand();
		UbeeData data = new UbeeData();
		ArrayList<String> parms = new ArrayList<>();

		data.setSlaveId(id);
		data.setCmd("1");
		parms.add(turnOn ? "1" : "0");
		parms.add(String.valueOf(period));
		data.setParameters(parms);
		ret = execute(data);
		return ret;
	}

	public Command buzz(int id, boolean turnOn, int period) {
		Command ret = super.getBaseCommand();
		UbeeData data = new UbeeData();
		ArrayList<String> parms = new ArrayList<>();

		data.setSlaveId(id);
		data.setCmd("2");
		parms.add(turnOn ? "1" : "0");
		parms.add(String.valueOf(period));
		data.setParameters(parms);
		ret = execute(data);
		return ret;
	}

	public Command changeId(int id, int newId) {
		Command ret = super.getBaseCommand();
		UbeeData data = new UbeeData();
		ArrayList<String> parms = new ArrayList<>();

		data.setSlaveId(id);
		data.setCmd("3");
		parms.add(String.valueOf(newId));
		data.setParameters(parms);
		ret = execute(data);
		return ret;
	}

	public Command sendTextMessage(int id, String line1, String line2, int period) {
		Command ret = super.getBaseCommand();
		UbeeData data = new UbeeData();
		ArrayList<String> parms = new ArrayList<>();

		data.setSlaveId(id);
		data.setCmd("999");
		parms.add(line1);
		parms.add(line2);
		parms.add(String.valueOf(period));
		data.setParameters(parms);
		ret = execute(data);
		return ret;
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
