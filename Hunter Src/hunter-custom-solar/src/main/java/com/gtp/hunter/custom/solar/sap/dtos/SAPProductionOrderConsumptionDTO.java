package com.gtp.hunter.custom.solar.sap.dtos;

import com.google.gson.annotations.SerializedName;

public class SAPProductionOrderConsumptionDTO extends HeaderTableSapDTO {
	
	@SerializedName("ORDER_NUMBER")
	private String orderNum;
	
	@SerializedName("MATERIAL")
	private String material;
	
	@SerializedName("DESCRIPTION")
	private String description;
	
	@SerializedName("QUANTITY")
	private String quantity;
	
	@SerializedName("UNIT")
	private String unit;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	
}
