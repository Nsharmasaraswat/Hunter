package com.gtp.hunter.core.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@Entity
@Table(name = "permissioncategory")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionCategory extends UUIDAuditModel {

	@Expose
	private String				icon;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "parent_id")
	@Expose
	private PermissionCategory	parent;

	public PermissionCategory() {
	}

	public PermissionCategory(String name, String metaname, String status, String icon) {
		this.setName(name);
		this.setMetaname(metaname);
		this.setStatus(status);
		this.setIcon(icon);
	}

	/**
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}
}
