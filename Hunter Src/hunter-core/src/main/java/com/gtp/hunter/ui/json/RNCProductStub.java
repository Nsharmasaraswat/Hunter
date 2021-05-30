package com.gtp.hunter.ui.json;

import java.util.Date;

import com.google.gson.annotations.Expose;

public class RNCProductStub {

	@Expose
	private String	id;

	@Expose
	private String	sku;

	@Expose
	private String	name;

	@Expose
	private String	lot_id;

	@Expose
	private Date	manuf;

	@Expose
	private Date	exp;

	@Expose
	private String	rnc;

	@Expose
	private Double	qty;

	@Expose
	private String	status;

	@Expose
	private String	serial;

	@Expose
	private String	address;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLot_id() {
		return lot_id;
	}

	public void setLot_id(String lot_id) {
		this.lot_id = lot_id;
	}

	public Date getManuf() {
		return manuf;
	}

	public void setManuf(Date manuf) {
		this.manuf = manuf;
	}

	public Date getExp() {
		return exp;
	}

	public void setExp(Date exp) {
		this.exp = exp;
	}

	public String getRnc() {
		return rnc;
	}

	public void setRnc(String rnc) {
		this.rnc = rnc;
	}

	public Double getQty() {
		return qty;
	}

	public void setQty(Double qty) {
		this.qty = qty;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}
}