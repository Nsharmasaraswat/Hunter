package com.gtp.hunter.ui.json;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.process.jsonstubs.BaseJSONStub;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SolarCreatePallet extends BaseJSONStub {

	@Expose
	@SerializedName("id")
	@JsonProperty("id")
	private UUID	id;

	@Expose
	@SerializedName("address-id")
	@JsonProperty("address-id")
	private UUID	address_id;

	@Expose
	@SerializedName("product-id")
	@JsonProperty("product-id")
	private UUID	product_id;

	@Expose
	@SerializedName("lot-prefix")
	@JsonProperty("lot-prefix")
	private String	lot_prefix;

	@Expose
	@SerializedName("quantity")
	@JsonProperty("quantity")
	private double	quantity;

	@Expose
	@SerializedName("volumes")
	@JsonProperty("volumes")
	private int		volumes;

	@Expose
	@SerializedName("manufacture")
	@JsonProperty("manufacture")
	private Date	manufacture;

	@Expose
	@SerializedName("expire")
	@JsonProperty("expire")
	private Date	expire;

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
	 * @return the quantity
	 */
	public double getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the volumes
	 */
	public int getVolumes() {
		return volumes;
	}

	/**
	 * @param volumes the volumes to set
	 */
	public void setVolumes(int volumes) {
		this.volumes = volumes;
	}

	/**
	 * @return the manufacture
	 */
	public Date getManufacture() {
		return manufacture;
	}

	/**
	 * @param manufacture the manufacture to set
	 */
	public void setManufacture(Date manufacture) {
		this.manufacture = manufacture;
	}

	/**
	 * @return the expire
	 */
	public Date getExpire() {
		return expire;
	}

	/**
	 * @param expire the expire to set
	 */
	public void setExpire(Date expire) {
		this.expire = expire;
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

	/**
	 * @return the lot_prefix
	 */
	public String getLot_prefix() {
		return lot_prefix;
	}

	/**
	 * @param lot_prefix the lot_prefix to set
	 */
	public void setLot_prefix(String lot_prefix) {
		this.lot_prefix = lot_prefix;
	}

	/**
	 * @return the address_id
	 */
	public UUID getAddress_id() {
		return address_id;
	}

	/**
	 * @param address_id the address_id to set
	 */
	public void setAddress_id(UUID address_id) {
		this.address_id = address_id;
	}

	/**
	 * @return the id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(UUID id) {
		this.id = id;
	}
}
