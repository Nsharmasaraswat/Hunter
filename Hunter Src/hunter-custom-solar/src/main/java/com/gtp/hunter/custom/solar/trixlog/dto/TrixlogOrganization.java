package com.gtp.hunter.custom.solar.trixlog.dto;

import com.google.gson.annotations.Expose;

public class TrixlogOrganization {
	@Expose
	int		id;//":40,

	@Expose
	String	name;//":"Demo Organization"

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
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

}
