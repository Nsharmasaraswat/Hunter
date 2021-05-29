package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.SerializedName;

public class SAPProductionOrderProductionDTO extends HeaderTableSapDTO {

	@SerializedName("ORDER_NUMBER")
	private String	orderNum;

	@SerializedName("MATERIAL")
	private String	material;

	@SerializedName("PLANT")
	private String	centro;

	@SerializedName("START_DATE")
	private String	startDate;

	@SerializedName("RESOURCE_WORK")
	private String	resourceWork;

	@SerializedName("RESERVATION_NUMBER")
	private String	reservNum;

	public String getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(String orderNum) {
		this.orderNum = orderNum;
	}

	public String getMaterial() {
		return material;
	}

	public void setMaterial(String material) {
		this.material = material;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getResourceWork() {
		return resourceWork;
	}

	public void setResourceWork(String resourceWork) {
		this.resourceWork = resourceWork;
	}

	public String getReservNum() {
		return reservNum;
	}

	public void setReservNum(String reservNum) {
		this.reservNum = reservNum;
	}

	public String getCentro() {
		return centro;
	}

	public void setCentro(String centro) {
		this.centro = centro;
	}
}
