package com.gtp.hunter.common.devicedata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GPIOData implements DeviceData {

	transient int	pin;

	@Expose
	@SerializedName(value = "gpio-state")
	int				state;

	/**
	 * @return the pin
	 */
	public int getPin() {
		return pin;
	}

	/**
	 * @param pin
	 *            the pin to set
	 */
	public void setPin(int pin) {
		this.pin = pin;
	}

	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(int state) {
		this.state = state;
	}
}
