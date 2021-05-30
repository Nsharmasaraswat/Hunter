package com.gtp.hunter.common.payload;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SensorPayload extends BasePayload {

	@Expose
	@SerializedName(value = "name")
	private String	name;

	@Expose
	@SerializedName(value = "value")
	private double	value;

	@Expose
	@SerializedName(value = "unit")
	private String	unit;

	@Expose
	@SerializedName(value = "variance")
	private double	variance;

	public SensorPayload() {

	}

	public SensorPayload(String name, double value, String unit) {
		setName(name);
		setValue(value);
		setUnit(unit);
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getVariance() {
		return variance;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
