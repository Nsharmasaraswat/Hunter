package com.gtp.hunter.common.devicedata;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UbeeData implements DeviceData {

	@Expose
	@SerializedName("slave-id")
	int							slaveId;

	@Expose
	@SerializedName("cmd")
	private String				cmd;

	@Expose
	@SerializedName("parameters")
	private ArrayList<String>	parameters;

	/**
	 * @return the slaveId
	 */
	public int getSlaveId() {
		return slaveId;
	}

	/**
	 * @param slaveId the slaveId to set
	 */
	public void setSlaveId(int slaveId) {
		this.slaveId = slaveId;
	}

	/**
	 * @return the cmd
	 */
	public String getCmd() {
		return cmd;
	}

	/**
	 * @param cmd the cmd to set
	 */
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	/**
	 * @return the parameters
	 */
	public ArrayList<String> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(ArrayList<String> parameters) {
		this.parameters = parameters;
	}
}
