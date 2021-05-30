package com.gtp.hunter.process.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "productfield", uniqueConstraints = {
		@UniqueConstraint(columnNames = {
				"product_id",
				"productmodelfield_id"
		})
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductField extends UUIDAuditModel {

	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	@Expose(serialize = false)
	@JsonIgnore
	private Product				product;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "productmodelfield_id", nullable = false)
	private ProductModelField	model;

	@Transient
	@Expose
	@SerializedName("modelfield_id")
	private String				modelfield_id;

	@Expose
	private String				value;

	public ProductField() {
	}

	public ProductField(Product p, ProductModelField pmf, String status, String value) {
		setProduct(p);
		setModel(pmf);
		setStatus(status);
		setValue(value);
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ProductModelField getModel() {
		return model;
	}

	public void setModel(ProductModelField model) {
		this.model = model;
	}

	@JsonGetter("modelfield_id")
	public String getModelfield_id() {
		if (this.model != null && this.model.getId() != null) {
			return this.model.getId().toString();
		} else {
			return modelfield_id;
		}
	}

	@JsonSetter("modelfield_id")
	public void setModelfield_id(String modelfieldid) {
		this.modelfield_id = modelfieldid;
	}
}
