package com.gtp.hunter.process.jsonstubs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AGLAddressProps implements Comparable<AGLAddressProps> {

	@Expose
	private String	id;

	@Expose
	private String	name;

	@Expose
	private String	metaname;

	@Expose
	private String	parent_id;

	public String getMetaname() {
		return metaname;
	}

	public void setMetaname(String metaname) {
		this.metaname = metaname;
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

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o != null)
			return ((AGLAddressProps) o).getId().equals(this.id);
		return false;
	}

	@Override
	public int compareTo(AGLAddressProps o) {
		if (o == null || o.getId() == null)
			return -1;
		if (this.id == null)
			return 1;
		return this.id.compareTo(o.getId());
	}

	/**
	 * @return the parent_id
	 */
	public String getParent_id() {
		return parent_id;
	}

	/**
	 * @param parent_id the parent_id to set
	 */
	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}

}
