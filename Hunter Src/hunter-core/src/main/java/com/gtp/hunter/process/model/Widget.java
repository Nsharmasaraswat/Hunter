package com.gtp.hunter.process.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.core.model.UUIDAuditModel;
import com.gtp.hunter.core.model.User;
import com.vladmihalcea.hibernate.type.json.JsonStringType;

@Entity
@Table(name = "widget")
@JsonIgnoreProperties(ignoreUnknown = true)
@TypeDef(typeClass = JsonStringType.class, defaultForType = JsonNode.class)
public class Widget extends UUIDAuditModel {

	@Expose
	@Column(columnDefinition = "json")
	@SerializedName("params")
	private JsonNode	params;

	@ManyToOne
	@Expose
	@JoinColumn(name = "user_id")
	@SerializedName("user")
	private User		user;

	/**
	 * @return the params
	 */
	public JsonNode getParams() {
		return params;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(JsonNode params) {
		this.params = params;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

}
