package com.gtp.hunter.process.jsonstubs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AGLCustomer extends AGLBase {

	@Expose
	private String				id;
	@Expose
	private String				status;
	@Expose
	private String				name;
	@Expose
	private String				metaname;
	@Expose
	private String				parent_id;
	@Expose
	private String				user_id;
	@Expose
	private Map<String, String>	props	= new HashMap<>();

	public String getParent_id() {
		return parent_id;
	}

	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public Map<String, String> getProps() {
		return props;
	}

	public void setProps(Map<String, String> props) {
		this.props = props;
	}

	@Override
	public int hashCode() {
		return UUID.fromString(id).hashCode();
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

}
