package com.gtp.hunter.common.payload;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SimPayload {
	@Expose
	@SerializedName(value = "lot")
	private String	lot;

	@Expose
	@SerializedName(value = "fab")
	private String	fab;

	@Expose
	@SerializedName(value = "val")
	private String	val;

	@Expose
	@SerializedName(value = "qty")
	private int		qty;

	@Expose
	@SerializedName(value = "count")
	private int		count;

	@Expose
	@SerializedName(value = "truck")
	private boolean	truck;

	@Expose
	@SerializedName(value = "transp")
	private String	transp;

	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}

	public String getFab() {
		return fab;
	}

	public void setFab(String fab) {
		this.fab = fab;
	}

	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public boolean isTruck() {
		return truck;
	}

	public void setTruck(boolean truck) {
		this.truck = truck;
	}

	/**
	 * @return the transp
	 */
	public String getTransp() {
		return transp;
	}

	/**
	 * @param transp the transp to set
	 */
	public void setTransp(String transp) {
		this.transp = transp;
	}
}
