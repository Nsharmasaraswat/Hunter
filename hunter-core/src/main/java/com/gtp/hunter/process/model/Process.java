package com.gtp.hunter.process.model;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "process")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Process extends UUIDAuditModel {

	@ManyToOne
	@JoinColumn(name = "wf_id")
	@Expose
	private Workflow workflow;

	@ManyToOne
	@JoinColumn(name = "origin_id")
	@Expose
	private Origin origin;

	@Expose(serialize = false)
	@JsonIgnore
	@OneToMany(mappedBy = "process", targetEntity = ProcessActivity.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Fetch(FetchMode.SUBSELECT)
	@OrderBy("ordem")
	private List<ProcessActivity> activities;

	@Expose(serialize = false)
	@ManyToMany(mappedBy = "processes", fetch = FetchType.EAGER)
	@Fetch(FetchMode.SUBSELECT)
	private Set<Purpose> purposes;

	@Expose
	private String classe;

	@Expose
	@Column(columnDefinition = "LONGTEXT")
	private String param;

	@Expose
	private String estadoDe;

	@Expose
	private String estadoPara;

	@Expose
	private boolean cancelable = false;
	
	@Expose
	private String urlRetorno;

	public Process() {
	}

	public Process(String name, String metaname, Workflow wf, Origin orig, String classe, String param, String estadoDe,
			String estadoPara, String urlRetorno) {
		this.setName(name);
		this.setMetaname(metaname);
		this.setWorkflow(wf);
		this.setOrigin(orig);
		this.setClasse(classe);
		this.setParam(param);
		this.setEstadoDe(estadoDe);
		this.setEstadoPara(estadoPara);
		this.setUrlRetorno(urlRetorno);
	}

	public Origin getOrigin() {
		return origin;
	}

	public void setOrigin(Origin origin) {
		this.origin = origin;
	}

	public String getClasse() {
		return classe;
	}

	public void setClasse(String classe) {
		this.classe = classe;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public String getEstadoDe() {
		return estadoDe;
	}

	public void setEstadoDe(String estadoDe) {
		this.estadoDe = estadoDe;
	}

	public String getEstadoPara() {
		return estadoPara;
	}

	public void setEstadoPara(String estadoPara) {
		this.estadoPara = estadoPara;
	}

	public List<ProcessActivity> getActivities() {
		return activities;
	}

	public void setActivities(List<ProcessActivity> activities) {
		this.activities = activities;
	}

	public boolean isCancelable() {
		return cancelable;
	}

	public void setCancelable(boolean cancelable) {
		this.cancelable = cancelable;
	}

	public Set<Purpose> getPurposes() {
		return purposes;
	}

	public void setPurposes(Set<Purpose> purposes) {
		this.purposes = purposes;
	}

	@Override
	public boolean equals(Object obj) {
		return this.getId().equals(((Process) obj).getId());
	}

	public String getUrlRetorno() {
		return urlRetorno;
	}

	public void setUrlRetorno(String urlRetorno) {
		this.urlRetorno = urlRetorno;
	}

}
