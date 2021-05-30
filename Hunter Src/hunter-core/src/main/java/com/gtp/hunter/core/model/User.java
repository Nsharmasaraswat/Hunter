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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.gson.annotations.Expose;

@Entity
@Table(name = "user")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends UUIDAuditModel {

	@ManyToOne
	@JoinColumn(name = "device_id")
	private Device				device;

	@JsonIgnore
	@Expose(serialize = false)
	@OneToMany(mappedBy = "user", targetEntity = Credential.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	private Set<Credential>		credentials	= new HashSet<Credential>();

	@JsonIgnore
	@Expose(serialize = false)
	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinTable(name = "groupuser", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "group_id"))
	@Fetch(FetchMode.SUBSELECT)
	private Set<Group>			groups		= new HashSet<Group>();

	@JsonIgnore
	@Expose(serialize = false)
	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinTable(name = "userpermission", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
	@Fetch(FetchMode.SUBSELECT)
	private Set<Permission>		permissions	= new HashSet<Permission>();

	@Expose
	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "userproperty", joinColumns = @JoinColumn(name = "user_id"))
	@Fetch(FetchMode.SUBSELECT)
	@MapKeyJoinColumn(name = "property")
	@Column(name = "value")
	private Map<String, String>	properties	= new HashMap<String, String>();

	@Transient
	private transient String	unit;

	@Transient
	private transient String	salt;

	@Transient
	@Expose(serialize = false)
	private String				login;

	@Transient
	@Expose(serialize = false)
	private String				pwd;

	@Transient
	@Expose(serialize = false)
	private String				grpId;

	public User() {
	}

	public User(String name) {
		this.setName(name);
	}

	public Set<Credential> getCredentials() {
		return credentials;
	}

	public void setCredentials(Set<Credential> credentials) {
		this.credentials = credentials;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public Set<Group> getGroups() {
		return groups;
	}

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}

	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getLogin() {
		return login;
	}

	@JsonSetter("login")
	public void setLogin(String login) {
		this.login = login;
	}

	public String getPwd() {
		return pwd;
	}

	@JsonSetter("pwd")
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getGrpId() {
		return grpId;
	}

	public void setGrpId(String grpId) {
		this.grpId = grpId;
	}

}
