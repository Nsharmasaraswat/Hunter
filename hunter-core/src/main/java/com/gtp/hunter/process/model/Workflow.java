package com.gtp.hunter.process.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "workflow")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Workflow extends UUIDAuditModel {

	@Expose(serialize = false)
	@JsonIgnore
	@OneToMany(mappedBy = "workflow", targetEntity = Process.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<Process>	processes;

	@Expose
	private String			statusFinal;

	public Workflow() {
	}

	public Workflow(String name, String metaname, String statusFinal) {
		this.setName(name);
		this.setMetaname(metaname);
		this.setStatusFinal(statusFinal);
	}

	public Set<Process> getProcesses() {
		return processes;
	}

	public void setProcesses(Set<Process> processes) {
		this.processes = processes;
	}

	public String getStatusFinal() {
		return statusFinal;
	}

	public void setStatusFinal(String statusFinal) {
		this.statusFinal = statusFinal;
	}

}
