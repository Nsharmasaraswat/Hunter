package com.gtp.hunter.core.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@Entity
@Table(name = "permission")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Permission extends UUIDAuditModel {

	@Expose
	private String				route;

	@Expose
	private String				icon;

	@Expose
	private String				app;

	@Expose
	private String				params;

	@Expose
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "permissioncategory_id")
	private PermissionCategory	category;

	@ManyToMany(cascade = { CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinTable(name = "userpermission", joinColumns = @JoinColumn(name = "permission_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
	@JsonIgnore
	private Set<User>			users		= new HashSet<User>();

	@ManyToMany(cascade = { CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinTable(name = "grouppermission", joinColumns = @JoinColumn(name = "permission_id"), inverseJoinColumns = @JoinColumn(name = "group_id"))
	@JsonIgnore
	private Set<Group>			groups		= new HashSet<Group>();

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "permissionproperty", joinColumns = @JoinColumn(name = "permission_id"))
	@Fetch(FetchMode.SUBSELECT)
	@MapKeyJoinColumn(name = "prop")
	@Column(name = "value")
	@Expose
	private Map<String, String>	properties	= new HashMap<String, String>();

	public Permission() {
	}

	public Permission(String name, String metaname, String route, String icon) {
		this.setName(name);
		this.setMetaname(metaname);
		this.setRoute(route);
		this.setIcon(icon);
	}

	public Permission(String name, String metaname, String route, String icon, Group defPerm) {
		this.setName(name);
		this.setMetaname(metaname);
		this.setRoute(route);
		this.setIcon(icon);
		this.getGroups().add(defPerm);
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public Set<Group> getGroups() {
		return groups;
	}

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	/**
	 * @return the params
	 */
	public String getParams() {
		return params;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(String params) {
		this.params = params;
	}

	/**
	 * @return the category
	 */
	public PermissionCategory getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(PermissionCategory category) {
		this.category = category;
	}

}
