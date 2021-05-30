package com.gtp.hunter.custom.solar.trixlog.dto;

import com.google.gson.annotations.Expose;

public class TrixlogStatistics {
	@Expose
	private double	min;//":14,
	@Expose
	private double	max;//":14,"
	@Expose
	private double	avg;//":14.0}
	/**
	 * @return the min
	 */
	public double getMin() {
		return min;
	}
	/**
	 * @param min the min to set
	 */
	public void setMin(double min) {
		this.min = min;
	}
	/**
	 * @return the max
	 */
	public double getMax() {
		return max;
	}
	/**
	 * @param max the max to set
	 */
	public void setMax(double max) {
		this.max = max;
	}
	/**
	 * @return the avg
	 */
	public double getAvg() {
		return avg;
	}
	/**
	 * @param avg the avg to set
	 */
	public void setAvg(double avg) {
		this.avg = avg;
	}
}
