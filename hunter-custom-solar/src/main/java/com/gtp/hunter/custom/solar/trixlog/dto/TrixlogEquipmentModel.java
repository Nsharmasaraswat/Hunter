package com.gtp.hunter.custom.solar.trixlog.dto;

import com.google.gson.annotations.Expose;

public class TrixlogEquipmentModel {
	@Expose
	private String	code;//": "MXT_150",

	@Expose
	private String	manufacturer;//": "MAXTRACK"

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the manufacturer
	 */
	public String getManufacturer() {
		return manufacturer;
	}

	/**
	 * @param manufacturer the manufacturer to set
	 */
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
}
