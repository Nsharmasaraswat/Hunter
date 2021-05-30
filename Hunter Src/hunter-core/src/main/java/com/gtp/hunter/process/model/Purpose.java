package com.gtp.hunter.process.model;

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
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "purpose")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Purpose extends UUIDAuditModel {

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinTable(name = "purposeorigin", joinColumns = @JoinColumn(name = "purpose_id"), inverseJoinColumns = @JoinColumn(name = "origin_id"))
	@Expose(serialize = false)
	@JsonIgnore
	@Fetch(FetchMode.SUBSELECT)
	private Set<Origin>		origins		= new HashSet<Origin>();

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinTable(name = "purposetaskdef", joinColumns = @JoinColumn(name = "purpose_id"), inverseJoinColumns = @JoinColumn(name = "taskdef_id"))
	@Expose(serialize = false)
	@JsonIgnore
	@Fetch(FetchMode.SUBSELECT)
	private Set<TaskDef>	tasks		= new HashSet<TaskDef>();

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinTable(name = "purposeprocess", joinColumns = @JoinColumn(name = "purpose_id"), inverseJoinColumns = @JoinColumn(name = "process_id"))
	@Expose(serialize = false)
	@JsonIgnore
	@Fetch(FetchMode.SUBSELECT)
	private Set<Process>	processes	= new HashSet<Process>();

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinTable(name = "purposeaddress", joinColumns = @JoinColumn(name = "purpose_id"), inverseJoinColumns = @JoinColumn(name = "address_id"))
	@Expose(serialize = false)
	@JsonIgnore
	@Fetch(FetchMode.SUBSELECT)
	private Set<Address>	addresses	= new HashSet<Address>();

	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinTable(name = "purposething", joinColumns = @JoinColumn(name = "purpose_id"), inverseJoinColumns = @JoinColumn(name = "thing_id"))
	@Expose(serialize = false)
	@JsonIgnore
	@Fetch(FetchMode.SUBSELECT)
	private Set<Thing>		things		= new HashSet<Thing>();

	public Purpose() {
	}

	public Purpose(String name, String metaname) {
		this.setName(name);
		this.setMetaname(metaname);
	}

	public Set<Origin> getOrigins() {
		return origins;
	}

	public void setOrigins(Set<Origin> origins) {
		this.origins = origins;
	}

	public Set<TaskDef> getTasks() {
		return tasks;
	}

	public void setTasks(Set<TaskDef> tasks) {
		this.tasks = tasks;
	}

	public Set<Process> getProcesses() {
		return processes;
	}

	public void setProcesses(Set<Process> processes) {
		this.processes = processes;
	}

	public Set<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(Set<Address> addresses) {
		this.addresses = addresses;
	}

	public Set<Thing> getThings() {
		return things;
	}

	public void setThings(Set<Thing> things) {
		this.things = things;
	}
}
