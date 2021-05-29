package com.gtp.hunter.process.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "productmodel")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductModel extends UUIDAuditModel {

	@OneToMany(mappedBy = "model", targetEntity = ProductModelField.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@Expose()
	private Set<ProductModelField>	fields;

	@Transient
	private Set<ProductModel>		siblings;

	@ManyToOne
	@JoinColumn(name = "propertymodel_id")
	@Expose
	private PropertyModel			propertymodel;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	@Expose(serialize = false)
	@JsonIgnore
	private ProductModel			parent;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "productmodelproperty", joinColumns = @JoinColumn(name = "productmodel_id"))
	@Fetch(FetchMode.SUBSELECT)
	@MapKeyJoinColumn(name = "prop")
	@Column(name = "value")
	@Expose
	private Map<String, String>		properties	= new HashMap<String, String>();
	
	@Transient
	@Expose
	@SerializedName("parent_id")
	private String				parent_id;

	public ProductModel() {
	}

	public ProductModel(String name) {
		this.setName(name);
	}

	public ProductModel(String name, String metaname, PropertyModel prop, String status) {
		this.setName(name);
		this.setMetaname(metaname);
		this.setPropertymodel(prop);
		this.setStatus(status);
	}

	public Set<ProductModelField> getFields() {
		return fields;
	}

	public void setFields(Set<ProductModelField> fields) {
		this.fields = fields;
	}

	public PropertyModel getPropertymodel() {
		return propertymodel;
	}

	public void setPropertymodel(PropertyModel propertymodel) {
		this.propertymodel = propertymodel;
	}

	public ProductModel getParent() {
		return parent;
	}

	public void setParent(ProductModel parent) {
		this.parent = parent;
	}

	public Set<ProductModel> getSiblings() {
		return siblings;
	}

	public void setSiblings(Set<ProductModel> siblings) {
		this.siblings = siblings;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public String getParent_id() {
		if (this.parent != null && this.parent.getId() != null) {
			return this.parent.getId().toString();
		} else {
			return null;
		}
	}
}
