package com.gtp.hunter.process.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
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
import com.gtp.hunter.core.model.UUIDAuditModel;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

@Entity
@Table(name = "location")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location extends UUIDAuditModel {

	@OneToMany(mappedBy = "parent", targetEntity = Location.class, fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@Expose(serialize = false)
	@JsonIgnore
	private Set<Location>	siblings	= new HashSet<Location>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	@Expose(serialize = false)
	@JsonIgnore
	private Location		parent;

	@Column(name = "crs")
	@Expose
	private String			crs;

	@Column(name = "center", nullable = false, columnDefinition = "GEOMETRY")
	@Expose(serialize = false)
	@JsonIgnore
	private Geometry		center;

	@Transient
	@Expose
	private String			wkt;

	@Column(name = "rotation")
	@Expose
	private int				rotation;

	@Expose
	private String			mapfile;

	@OneToMany(mappedBy = "location", targetEntity = Address.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@Expose(serialize = false)
	@JsonIgnore
	private Set<Address>	addresses	= new HashSet<Address>();

	public String getMapfile() {
		return mapfile;
	}

	public void setMapfile(String mapfile) {
		this.mapfile = mapfile;
	}

	/**
	 * @return the addresses
	 */
	public Set<Address> getAddresses() {
		return addresses;
	}

	/**
	 * @param addresses the addresses to set
	 */
	public void setAddresses(Set<Address> addresses) {
		this.addresses = addresses;
	}

	/**
	 * @return the siblings
	 */
	public Set<Location> getSiblings() {
		return siblings;
	}

	/**
	 * @param siblings the siblings to set
	 */
	public void setSiblings(Set<Location> siblings) {
		this.siblings = siblings;
	}

	/**
	 * @return the parent
	 */
	public Location getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Location parent) {
		this.parent = parent;
	}

	/**
	 * @return the crs
	 */
	public String getCrs() {
		return crs;
	}

	/**
	 * @param crs the crs to set
	 */
	public void setCrs(String crs) {
		this.crs = crs;
	}

	/**
	 * @return the center
	 */
	public Geometry getCenter() {
		return center;
	}

	/**
	 * @param center the center to set
	 */
	public void setCenter(Point center) {
		this.center = center;
	}

	/**
	 * @return the rotation
	 */
	public int getRotation() {
		return rotation;
	}

	/**
	 * @param rotation the rotation to set
	 */
	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	@JsonGetter("wkt")
	public String getWkt() {
		if (center != null)
			return center.toText();
		return wkt;
	}

	@JsonSetter("wkt")
	public void setWkt(String wkt) {
		WKTReader rdr = new WKTReader();

		try {
			this.center = (Point) rdr.read(wkt);
			this.wkt = wkt;
		} catch (ParseException e) {

		}
	}
}
