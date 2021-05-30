package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.SerializedName;

public class SAPSupplierDTO extends HeaderTableSapDTO {
	
	@SerializedName("LIFNR")
	private String lifnr;
	
	@SerializedName("NAME1")
	private String name1;
	
	@SerializedName("STCD1")
	private String stcd1;
	
	@SerializedName("REGIO")
	private String regio;
	
	@SerializedName("CITY1")
	private String city1;
	
	@SerializedName("KTOKK")
	private String ktokk;

	public String getLifnr() {
		return lifnr;
	}

	public void setLifnr(String lifnr) {
		this.lifnr = lifnr;
	}

	public String getName1() {
		return name1;
	}

	public void setName1(String name1) {
		this.name1 = name1;
	}

	public String getStcd1() {
		return stcd1;
	}

	public void setStcd1(String stcd1) {
		this.stcd1 = stcd1;
	}

	public String getRegio() {
		return regio;
	}

	public void setRegio(String regio) {
		this.regio = regio;
	}

	public String getCity1() {
		return city1;
	}

	public void setCity1(String city1) {
		this.city1 = city1;
	}

	public String getKtokk() {
		return ktokk;
	}

	public void setKtokk(String ktokk) {
		this.ktokk = ktokk;
	}
	
}
