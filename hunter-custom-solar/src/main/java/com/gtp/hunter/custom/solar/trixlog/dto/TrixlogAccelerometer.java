package com.gtp.hunter.custom.solar.trixlog.dto;

import com.google.gson.annotations.Expose;

public class TrixlogAccelerometer {
	@Expose
	String	axis;//": "Y", 
	@Expose
	double	value;//": 0.3

	/**
	 * @return the axis
	 */
	public String getAxis() {
		return axis;
	}

	/**
	 * @param axis the axis to set
	 */
	public void setAxis(String axis) {
		this.axis = axis;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}
}
