package com.gtp.hunter.custom.descarpack.model;

import java.util.Map;
import java.util.UUID;

public class PrintTagOrder {

	private UUID device;
	private UUID document;
	private String docname;
	private UUID product;
	private String prodname;
	private String sku;
	private Map<String,String> properties;
	private Map<String,String> metadata;
	private int qty;
	private int printed;
	private String status;
	
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
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	
}
