package com.gtp.hunter.process.jsonstubs;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AGLDocModelProps extends AGLBase {

	@Expose
	private String					id;
	@Expose
	private String					name;
	@Expose
	private String					metaname;
	@Expose
	private String					status;
	@Expose
	private String					user_id;
	@Expose
	private String					code;
	@Expose
	private String					parent_id;
	@Expose
	private List<AGLDocModelField>	props		= new LinkedList<AGLDocModelField>();
	@Expose
	private List<AGLDocModelItem>	items		= new LinkedList<AGLDocModelItem>();
	@Expose
	private List<AGLDocModelProps>	siblings	= new LinkedList<AGLDocModelProps>();

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getParent_id() {
		return parent_id;
	}

	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}

	public List<AGLDocModelItem> getItems() {
		return items;
	}

	public void setItems(List<AGLDocModelItem> items) {
		this.items = items;
	}

	public List<AGLDocModelProps> getSiblings() {
		return siblings;
	}

	public void setSiblings(List<AGLDocModelProps> siblings) {
		this.siblings = siblings;
	}

	public List<AGLDocModelField> getProps() {
		return props;
	}

	public void setProps(List<AGLDocModelField> props) {
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
