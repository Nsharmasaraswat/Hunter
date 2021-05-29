package com.gtp.hunter.process.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;
import com.gtp.hunter.process.wf.process.activity.ProcessActivityPhase;

@Entity
@Table(name = "processactivity")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessActivity extends UUIDAuditModel {

	@ManyToOne
	@JoinColumn(name = "process_id")
	private Process					process;

	@Enumerated(EnumType.STRING)
	@Expose()
	private ProcessActivityPhase	phase;

	@Expose()
	private String					classe;

	@Expose
	@Column(columnDefinition = "LONGTEXT")
	private String					param;

	@Expose
	private int						ordem	= 0;

	public ProcessActivity() {
	}

	public ProcessActivity(String name, String metaname, Process proc, ProcessActivityPhase phase, String classe, String param) {
		this.setName(name);
		this.setMetaname(metaname);
		this.process = proc;
		this.phase = phase;
		this.classe = classe;
		this.param = param;
	}

	public String getClasse() {
		return classe;
	}

	public void setClasse(String classe) {
		this.classe = classe;
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	public ProcessActivityPhase getPhase() {
		return phase;
	}

	public void setPhase(ProcessActivityPhase phase) {
		this.phase = phase;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public int getOrdem() {
		return ordem;
	}

	public void setOrdem(int order) {
		this.ordem = order;
	}

}
