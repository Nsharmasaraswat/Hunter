package com.gtp.hunter.common.devicedata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SensorData implements DeviceData {

	@Expose
	@SerializedName("cmd")
	private String cmd;

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
}
