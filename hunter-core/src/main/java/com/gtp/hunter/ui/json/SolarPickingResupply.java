package com.gtp.hunter.ui.json;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.process.jsonstubs.BaseJSONStub;

public class SolarPickingResupply extends BaseJSONStub {

	@Expose
	@SerializedName("product_id")
	@JsonProperty("product_id")
	private UUID	product_id;

	@Expose
	@SerializedName("product_sku")
	@JsonProperty("product_sku")
	private String	product_sku;

	@Expose
	@SerializedName("product_name")
	@JsonProperty("product_name")
	private String	product_name;

	@Expose
	@SerializedName("ammount")
	@JsonProperty("ammount")
	private int		ammount;

	@Expose
	@SerializedName("min_expiry")
	@JsonProperty("min_expiry")
	private int		minExpiry;

	@Expose
	@SerializedName("prefix")
	@JsonProperty("prefix")
	private String	prefix;

	@Expose
	@SerializedName("status")
	@JsonProperty("status")
	private String	status;

	/**
	 * @return the product_id
	 */
	public UUID getProduct_id() {
		return product_id;
	}

	/**
	 * @param product_id the product_id to set
	 */
	public void setProduct_id(UUID product_id) {
		this.product_id = product_id;
	}

	/**
	 * @return the product_sku
	 */
	public String getProduct_sku() {
		return product_sku;
	}

	/**
	 * @param product_sku the product_sku to set
	 */
	public void setProduct_sku(String product_sku) {
		this.product_sku = product_sku;
	}

	/**
	 * @return the product_name
	 */
	public String getProduct_name() {
		return product_name;
	}

	/**
	 * @param product_name the product_name to set
	 */
	public void setProduct_name(String product_name) {
		this.product_name = product_name;
	}

	/**
	 * @return the ammuont
	 */
	public int getAmmount() {
		return ammount;
	}

	/**
	 * @param ammount the ammount to set
	 */
	public void setAmmount(int ammount) {
		this.ammount = ammount;
	}

	/**
	 * @return the minExpiry
	 */
	public int getMinExpiry() {
		return minExpiry;
	}

	/**
	 * @param minExpiry the minExpiry to set
	 */
	public void setMinExpiry(int minExpiry) {
		this.minExpiry = minExpiry;
	}

	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
}