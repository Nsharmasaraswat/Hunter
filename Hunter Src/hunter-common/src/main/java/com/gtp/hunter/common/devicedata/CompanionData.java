package com.gtp.hunter.common.devicedata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CompanionData implements DeviceData {

	@Expose
	@SerializedName(value = "mission")
	private String mission;

	/**
	 * @return the mission
	 */
	public String getMission() {
		return mission;
	}

	/**
	 * @param mission the mission to set
	 */
	public void setMission(String mission) {
		this.mission = mission;
	}
}
