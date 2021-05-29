package com.gtp.hunter.process.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "product")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product extends UUIDAuditModel {

	@Expose
	@Basic(fetch = FetchType.EAGER)
	private String				sku;

	@Expose
	private String				barcode;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "productmodel_id")
	@Expose
	private ProductModel		model;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	@Expose
	private Product				parent;

	@OneToMany(mappedBy = "product", targetEntity = ProductField.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@Expose
	private Set<ProductField>	fields	= new HashSet<ProductField>();

	public Product() {
	}

	public Product(String name, ProductModel model, String sku, String status) {
		this.setName(name);
		this.setModel(model);
		this.setSku(sku);
		this.setStatus(status);
	}

	public Product getParent() {
		return parent;
	}

	public void setParent(Product parent) {
		this.parent = parent;
	}

	public ProductModel getModel() {
		return model;
	}

	public void setModel(ProductModel model) {
		this.model = model;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public Set<ProductField> getFields() {
		return fields;
	}

	public void setFields(Set<ProductField> fields) {
		this.fields = fields;
	}

}
