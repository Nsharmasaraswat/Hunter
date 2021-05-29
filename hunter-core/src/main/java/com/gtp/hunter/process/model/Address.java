package com.gtp.hunter.process.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.core.model.UUIDAuditModel;
import com.gtp.hunter.core.model.Unit;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

@Entity
@Table(name = "address")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address extends UUIDAuditModel {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	@Expose(serialize = false)
	@JsonIgnore
	private Address				parent;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "addressmodel_id")
	@Expose
	private AddressModel		model;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "location_id", nullable = false)
	@Expose(serialize = false)
	@JsonIgnore
	private Location			location;

	@OneToMany(mappedBy = "address", targetEntity = AddressField.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@Expose
	private Set<AddressField>	fields		= new HashSet<AddressField>();

	@OneToMany(mappedBy = "parent", targetEntity = Address.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@Expose
	private Set<Address>		siblings	= new HashSet<Address>();

	@Column(name = "region", nullable = false, columnDefinition = "GEOMETRY")
	@Expose(serialize = false)
	@JsonIgnore
	private Geometry			region;

	@Transient
	@Expose
	@SerializedName("wkt")
	private String				wkt;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "addressunits", joinColumns = @JoinColumn(name = "address_id"), inverseJoinColumns = @JoinColumn(name = "unit_id"))
	@Expose
	@Fetch(FetchMode.SUBSELECT)
	private Set<Unit>			units		= new HashSet<Unit>();

	@Transient
	@Expose
	@SerializedName("parent_id")
	private String				parent_id;

	@Transient
	@Expose
	@SerializedName("location_id")
	private String				location_id;

	@Transient
	@JsonIgnore
	private transient boolean	occupied;

	@Transient
	@JsonIgnore
	private Product				product;

	public Set<Address> getSiblings() {
		return siblings;
	}

	public void setSiblings(Set<Address> siblings) {
		this.siblings = siblings;
	}

	public Geometry getRegion() {
		return region;
	}

	public Set<Unit> getUnits() {
		return units;
	}

	public void setUnits(Set<Unit> units) {
		this.units = units;
	}

	public void setRegion(Geometry region) {
		this.region = region;
	}

	public Address getParent() {
		return parent;
	}

	public void setParent(Address parent) {
		this.parent = parent;
	}

	public AddressModel getModel() {
		return model;
	}

	public void setModel(AddressModel model) {
		this.model = model;
	}

	public Set<AddressField> getFields() {
		return fields;
	}

	public void setFields(Set<AddressField> fields) {
		this.fields = fields;
	}

	@JsonGetter("wkt")
	public String getWkt() {
		if (wkt == null)
			wkt = region.toText();
		return wkt;
	}

	@JsonSetter("wkt")
	public void setWkt(String wkt) {
		WKTReader rdr = new WKTReader();
		try {
			this.region = rdr.read(wkt);
			this.wkt = wkt;
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
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

	@JsonSetter("location_id")
	public void setLocation_id(String locationId) {
		this.location_id = locationId;
	}

	@JsonGetter("location_id")
	public String getLocation_id() {
		if (this.location != null && this.location.getId() != null) {
			return this.location.getId().toString();
		} else {
			return location_id;
		}
	}

	public boolean isOccupied() {
		return this.occupied;
	}

	public void setOccupied(boolean val) {
		this.occupied = val;
	}

	/**
	 * @return the product
	 */
	public Product getProduct() {
		return product;
	}

	/**
	 * @param product the product to set
	 */
	public void setProduct(Product product) {
		this.product = product;
	}
}
