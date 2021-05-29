package com.gtp.hunter.process.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "action")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Action extends UUIDAuditModel {

	@Expose
	private String		icon;

	@Expose
	private String		taskstatus;

	@ManyToOne(targetEntity = TaskDef.class, fetch = FetchType.LAZY)
	@JsonIgnore
	@JoinColumn(name = "taskdef_id")
	@Expose(serialize = false)
	private TaskDef		taskdef;

	@Expose
	private String		actionDef;

	@Expose
	private String		classe;

	@Expose
	private String		route;

	@Expose
	@Column(columnDefinition = "LONGTEXT")
	private String		params;

	@Expose
	@Column(columnDefinition = "LONGTEXT")
	private String		defparams;

	@JsonIgnore
	@Expose(serialize = false)
	@Column(columnDefinition = "LONGTEXT")
	private String		srvparams;

	@Transient
	private Document	document;

	public Action() {
	}

	public Action(Action a) {
		if (a != null) {
			setId(a.getId());
			setMetaname(a.getMetaname());
			setName(a.getName());
			setStatus(a.getStatus());
			setCreatedAt(a.getCreatedAt());
			setUpdatedAt(a.getUpdatedAt());
			setIcon(a.getIcon());
			setTaskstatus(a.getTaskstatus());
			setTaskdef(a.getTaskdef());
			setActionDef(a.getActionDef());
			setClasse(a.getClasse());
			setRoute(a.getRoute());
			setParams(a.getParams());
			setDefparams(a.getDefparams());
			setSrvparams(a.getSrvparams());
			setDocument(a.getDocument());
		}
	}

	public Action(String name, String metaname, TaskDef taskDef, String classe, String actionDef, String route, String params) {
		this.setName(name);
		this.setMetaname(metaname);
		this.setTaskdef(taskDef);
		this.setClasse(classe);
		this.setRoute(route);
		this.setParams(params);
		this.setActionDef(actionDef);
	}

	public Action(UUID id, String name, String metaname, TaskDef taskDef, String classe, String actionDef, String route, String params) {
		this.setId(id);
		this.setName(name);
		this.setMetaname(metaname);
		this.setTaskdef(taskDef);
		this.setClasse(classe);
		this.setRoute(route);
		this.setParams(params);
		this.setActionDef(actionDef);
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getClasse() {
		return classe;
	}

	public void setClasse(String classe) {
		this.classe = classe;
	}

	public TaskDef getTaskdef() {
		return taskdef;
	}

	public void setTaskdef(TaskDef taskdef) {
		this.taskdef = taskdef;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getTaskstatus() {
		return taskstatus;
	}

	public void setTaskstatus(String taskstatus) {
		this.taskstatus = taskstatus;
	}

	public String getActionDef() {
		return actionDef;
	}

	public void setActionDef(String actionDef) {
		this.actionDef = actionDef;
	}

	public String getDefparams() {
		return defparams;
	}

	public void setDefparams(String defparams) {
		this.defparams = defparams;
	}

	public String getSrvparams() {
		return srvparams;
	}

	public void setSrvparams(String srvparams) {
		this.srvparams = srvparams;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
}
