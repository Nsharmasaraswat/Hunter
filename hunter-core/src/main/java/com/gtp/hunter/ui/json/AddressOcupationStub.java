package com.gtp.hunter.ui.json;

import java.util.UUID;

import com.google.gson.annotations.Expose;

public class AddressOcupationStub {

	@Expose
	private UUID	address_id;

	@Expose
	private String	name;

	@Expose
	private String	status;

	@Expose
	private String	productStatus;

	@Expose
	private String	products;

	@Expose
	private int		capacity;

	@Expose
	private int		free;

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the products
	 */
	public String getProducts() {
		return products;
	}

	/**
	 * @param products the products to set
	 */
	public void setProducts(String products) {
		this.products = products;
	}

	/**
	 * @return the capacity
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * @param capacity the capacity to set
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * @return the free
	 */
	public int getFree() {
		return free;
	}

	/**
	 * @param free the free to set
	 */
	public void setFree(int free) {
		this.free = free;
	}

	/**
	 * @return the productStatus
	 */
	public String getProductStatus() {
		return productStatus;
	}

	/**
	 * @param productStatus the productStatus to set
	 */
	public void setProductStatus(String productStatus) {
		this.productStatus = productStatus;
	}

}