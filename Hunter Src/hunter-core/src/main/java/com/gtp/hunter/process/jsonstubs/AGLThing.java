package com.gtp.hunter.process.jsonstubs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AGLThing extends AGLBase implements Comparable<AGLThing> {

	@Expose
	private String				id;
	@Expose
	private String				status;
	@Expose
	private String				name;
	@Expose
	private String				metaname;
	@Expose
	private String				user_id;
	@Expose
	private String				parent_id;
	@Expose
	private String				address_id;
	@Expose
	private String				product_id;
	@Expose
	private Set<AGLUnit>		units		= new HashSet<>();
	@Expose
	private Set<AGLThing>		siblings	= new HashSet<>();
	@Expose
	private Map<String, String>	props		= new HashMap<>();

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getParent_id() {
		return parent_id;
	}

	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}

	public String getAddress_id() {
		return address_id;
	}

	public void setAddress_id(String address_id) {
		this.address_id = address_id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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
	 * @return the metaname
	 */
	public String getMetaname() {
		return metaname;
	}

	/**
	 * @param metaname the metaname to set
	 */
	public void setMetaname(String metaname) {
		this.metaname = metaname;
	}

	/**
	 * @return the siblings
	 */
	public Set<AGLThing> getSiblings() {
		return siblings;
	}

	/**
	 * @param siblings the siblings to set
	 */
	public void setSiblings(Set<AGLThing> siblings) {
		this.siblings = siblings;
	}

	/**
	 * @return the product_id
	 */
	public String getProduct_id() {
		return product_id;
	}

	/**
	 * @param product_id the product_id to set
	 */
	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}

	/**
	 * @return the props
	 */
	public Map<String, String> getProps() {
		return props;
	}

	/**
	 * @param props the props to set
	 */
	public void setProps(Map<String, String> props) {
		this.props = props;
	}

	/**
	 * @return the units
	 */
	public Set<AGLUnit> getUnits() {
		return units;
	}

	/**
	 * @param units the units to set
	 */
	public void setUnits(Set<AGLUnit> units) {
		this.units = units;
	}

	@Override
	public int compareTo(AGLThing arg0) {
		if (this.id == null || arg0.id == null)
			return this.name.compareTo(arg0.name);
		else
			return this.id.compareTo(arg0.id);
	}

}
