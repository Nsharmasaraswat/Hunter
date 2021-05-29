package com.gtp.hunter.process.jsonstubs;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AGLDocModelItem {

	@Expose
	private String		product_id;
	@Expose
	private BigDecimal	qty;
	@Expose
	private int			layer;
	@Expose
	private String		address_id;
	@Expose
	private String		unit_measure;
	@Expose
	private int			seq;

	public String getProduct_id() {
		return product_id;
	}

	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}

	public BigDecimal getQty() {
		return qty;
	}

	public void setQty(BigDecimal qty) {
		this.qty = qty;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public String getAddress_id() {
		return address_id;
	}

	public void setAddress_id(String address_id) {
		this.address_id = address_id;
	}

	public String getUnit_measure() {
		return unit_measure;
	}

	public void setUnit_measure(String unit_measure) {
		this.unit_measure = unit_measure;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

}
