package com.gtp.hunter.process.jsonstubs;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AGLProdModel extends AGLBase {

	@Expose
	private String				id;

	@Expose
	private String				name;

	@Expose
	private String				metaname;

	@Expose
	private String				status;

	@Expose
	private String				parent_id;

	@Expose
	private String				user_id;

	@Expose
	@SerializedName("properties")
	private Map<String, String>	props;

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMetaname() {
		return metaname;
	}

	public void setMetaname(String metaname) {
		this.metaname = metaname;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
