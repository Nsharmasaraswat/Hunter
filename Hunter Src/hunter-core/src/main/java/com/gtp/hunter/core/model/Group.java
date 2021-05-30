package com.gtp.hunter.core.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@Entity
@Table(name = "groups")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group extends UUIDAuditModel {

	@ManyToMany(cascade = { CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinTable(name = "groupuser", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
	@Expose(serialize=false)
	@JsonIgnore
	private Set<User> users = new HashSet<User>();
	
	@ManyToMany(cascade = { CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinTable(name = "groupjoin", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "inside_id"))
	@Expose(serialize=false)
	@JsonIgnore
	private Set<Group> groups = new HashSet<Group>();
	
	@ManyToMany(cascade = { CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinTable(name = "groupjoin", joinColumns = @JoinColumn(name = "inside_id"), inverseJoinColumns = @JoinColumn(name = "group_id"))
	@Expose(serialize=false)
	@JsonIgnore
	private Set<Group> insides = new HashSet<Group>();

	@ManyToMany(cascade = { CascadeType.MERGE }, fetch = FetchType.EAGER)
	@JoinTable(name = "grouppermission", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
	@Expose(serialize=false)
	@JsonIgnore
	@Fetch(FetchMode.SUBSELECT)
	private Set<Permission> permissions = new HashSet<Permission>();
	
	public Group() {   }
	
	public Group(String name, String metaname) {
		this.setName(name);
		this.setMetaname(metaname);
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

	public Set<Group> getInsides() {
		return insides;
	}

	public void setInsides(Set<Group> insides) {
		this.insides = insides;
	}

	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}
	
	public void addMultiPermissions(Permission... permissions ) {
		for(Permission p : permissions) {
			this.getPermissions().add(p);
		}
	}
	

}
