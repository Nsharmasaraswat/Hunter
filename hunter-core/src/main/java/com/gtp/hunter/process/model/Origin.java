package com.gtp.hunter.process.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "origin")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Origin extends UUIDAuditModel {

	private String			type;
	private String			params;

	@Expose(serialize = false)
	@JsonIgnore
	@OneToMany(mappedBy = "origin", targetEntity = Process.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	private Set<Process>	processes	= new HashSet<Process>();

	@Expose(serialize = false)
	@JsonIgnore
	@OneToMany(mappedBy = "origin", targetEntity = Feature.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	private Set<Feature>	features	= new HashSet<Feature>();

	@Expose(serialize = false)
	@JsonIgnore
	@ManyToMany(mappedBy = "origins", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	private Set<Purpose>	purposes	= new HashSet<Purpose>();

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "originpermission", joinColumns = @JoinColumn(name = "origin_id"))
	@Fetch(FetchMode.SUBSELECT)
	@Type(type = "uuid-char")
	@Column(name = "permission_id")
	private Set<UUID>		permissions	= new HashSet<UUID>();

	public Origin() {
	}

	public Origin(String name, String metaname, String type) {
		this.setName(name);
		this.setMetaname(metaname);
		this.setType(type);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public Set<Process> getProcesses() {
		return processes;
	}

	public void setProcesses(Set<Process> processes) {
		this.processes = processes;
	}

	public Set<Feature> getFeatures() {
		return features;
	}

	public void setFeatures(Set<Feature> features) {
		this.features = features;
	}

	public Set<Purpose> getPurposes() {
		return purposes;
	}

	public void setPurposes(Set<Purpose> purposes) {
		this.purposes = purposes;
	}

	public Set<UUID> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<UUID> permissions) {
		this.permissions = permissions;
	}

	@Override
	public boolean equals(Object obj) {
		return this.getId().equals(((Origin) obj).getId());
	}

}
