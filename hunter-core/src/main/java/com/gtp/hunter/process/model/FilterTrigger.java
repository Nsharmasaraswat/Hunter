package com.gtp.hunter.process.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name = "filtertrigger")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilterTrigger extends UUIDAuditModel {

	@ManyToOne
	@JoinColumn(name = "filter_id")
	@Expose()
	private Filter	filter;

	@Expose
	private String	classe;

	@Expose
	private String	params;

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public String getClasse() {
		return classe;
	}

	public void setClasse(String classe) {
		this.classe = classe;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

}
