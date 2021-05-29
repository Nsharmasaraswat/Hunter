package com.gtp.hunter.process.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.core.model.UUIDAuditModel;
import com.gtp.hunter.core.model.Unit;

@Entity
@Table(name = "thing")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Thing extends UUIDAuditModel {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "propertymodel_id")
	@Expose(serialize = false)
	@JsonIgnore
	private PropertyModel	model;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id")
	@Expose(serialize = false)
	private Product			product;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	@Expose(serialize = false)
	@JsonIgnore
	private Thing			parent;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "address_id")
	@Expose
	private Address			address;

	@OneToMany(mappedBy = "thing", targetEntity = Property.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@Expose
	private Set<Property>	properties		= new HashSet<Property>();

	@OneToMany(mappedBy = "parent", targetEntity = Thing.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@Expose()
	private Set<Thing>		siblings		= new HashSet<Thing>();

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "thingunits", joinColumns = @JoinColumn(name = "thing_id"))
	@Fetch(FetchMode.SUBSELECT)
	@Type(type = "uuid-char")
	@Column(name = "unit_id")
	private Set<UUID>		units			= new HashSet<UUID>();

	@Transient
	@Expose
	private Set<Unit>		unitModel		= new HashSet<Unit>();

	@Transient
	@Expose
	private Set<String>		errors			= new HashSet<String>();

	@Transient
	@Expose
	private boolean			cancelProcess	= false;

	@Transient
	@Expose
	private UUID			document;

	@Transient
	@Expose
	private String			docCode;

	@Transient
	@Expose
	private UUID			address_id;

	@Transient
	@Expose
	@SerializedName("product_id")
	private UUID			product_id;

	@Transient
	@Expose
	@SerializedName("parent_id")
	private String			parent_id;

	@Transient
	@Expose
	private String			payload;

	public Thing() {
	}

	public Thing(String name, Product prd, PropertyModel model, String status) {
		this.setName(name);
		this.setModel(model);
		this.setStatus(status);
		this.setProduct(prd);
	}

	public Thing(String name, Product prd, PropertyModel model, String status, Thing parent) {
		this.setName(name);
		this.setModel(model);
		this.setStatus(status);
		this.setProduct(prd);
		this.setParent(parent);
	}

	public PropertyModel getModel() {
		return model;
	}

	public void setModel(PropertyModel model) {
		this.model = model;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
		this.product_id = product == null ? null : product.getId();
	}

	public Thing getParent() {
		return parent;
	}

	public void setParent(Thing parent) {
		this.parent = parent;
	}

	public Set<String> getErrors() {
		return errors;
	}

	public void setErrors(Set<String> errors) {
		this.errors = errors;
	}

	public Set<UUID> getUnits() {
		return units;
	}

	public void setUnits(Set<UUID> units) {
		this.units = units;
	}

	public Set<Unit> getUnitModel() {
		return unitModel;
	}

	public void setUnitModel(Set<Unit> unitModel) {
		this.unitModel = unitModel;
	}

	@Override
	public boolean equals(Object arg0) {
		return this.getId().equals(((Thing) arg0).getId());
	}

	public boolean isCancelProcess() {
		return cancelProcess;
	}

	public void setCancelProcess(boolean cancelProcess) {
		this.cancelProcess = cancelProcess;
	}

	public UUID getDocument() {
		return document;
	}

	public void setDocument(UUID document) {
		this.document = document;
	}

	public String getDocCode() {
		return docCode;
	}

	public void setDocCode(String docCode) {
		this.docCode = docCode;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
		this.address_id = address == null ? null : address.getId();
	}

	@JsonGetter("address_id")
	public UUID getAddress_id() {
		if (address != null)
			return address.getId();
		return address_id;
	}

	@JsonSetter("address_id")
	public void setAddress_id(UUID address_id) {
		this.address_id = address_id;
	}

	public Set<Thing> getSiblings() {
		return siblings;
	}

	public void setSiblings(Set<Thing> siblings) {
		this.siblings = siblings;
	}

	@JsonGetter("product_id")
	public UUID getProduct_id() {
		if (product != null) return product.getId();
		return product_id;
	}

	@JsonSetter("product_id")
	public void setProduct_id(UUID product_id) {
		this.product_id = product_id;
	}

	public Set<Property> getProperties() {
		return properties;
	}

	public void setProperties(Set<Property> properties) {
		this.properties = properties;
	}

	/**
	 * @return the payload
	 */
	@JsonGetter("payload")
	public String getPayload() {
		return payload;
	}

	/**
	 * @param payload the payload to set
	 */
	@JsonSetter("payload")
	public void setPayload(String payload) {
		this.payload = payload;
	}

	@JsonGetter("parent_id")
	public String getParent_id() {
		if (this.parent != null && this.parent.getId() != null) {
			return this.parent.getId().toString();
		} else {
			return parent_id;
		}
	}

	@JsonSetter("parent_id")
	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}
}
