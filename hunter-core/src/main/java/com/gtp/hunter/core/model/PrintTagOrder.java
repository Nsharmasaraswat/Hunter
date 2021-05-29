package com.gtp.hunter.core.model;

import java.util.Map;
import java.util.UUID;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class PrintTagOrder {

	@Expose
	private UUID				device;

	@Expose
	private UUID				document;

	@Expose
	private String				docname;

	@Expose
	private UUID				product;

	@Expose
	private UUID				thing;

	@Expose
	private String				prodname;

	@Expose
	private String				sku;

	@Expose
	private Map<String, String>	properties;

	@Expose
	private Map<String, String>	metadata;

	@Expose
	private int					qty;

	@Expose
	private int					printed;

	@Expose
	private String				status;

	public UUID getProduct() {
		return product;
	}

	public void setProduct(UUID product) {
		this.product = product;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public UUID getDocument() {
		return document;
	}

	public void setDocument(UUID document) {
		this.document = document;
	}

	public int getPrinted() {
		return printed;
	}

	public void setPrinted(int printed) {
		this.printed = printed;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public String getDocname() {
		return docname;
	}

	public void setDocname(String docname) {
		this.docname = docname;
	}

	public String getProdname() {
		return prodname;
	}

	public void setProdname(String prodname) {
		this.prodname = prodname;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public UUID getDevice() {
		return device;
	}

	public void setDevice(UUID device) {
		this.device = device;
	}

	public UUID getThing() {
		return thing;
	}

	public void setThing(UUID thing) {
		this.thing = thing;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	@Override
	public String toString() {
		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().toJson(this);
	}
}
