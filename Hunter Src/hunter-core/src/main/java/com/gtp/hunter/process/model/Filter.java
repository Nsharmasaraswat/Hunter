package com.gtp.hunter.process.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.core.model.UUIDAuditModel;

@Entity
@Table(name="filter")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Filter extends UUIDAuditModel{

	@Expose
	private String model;

	@Expose()
	private String status;
		
	@Expose()
	private String basefilter;
	
	@Expose()
	private String params;

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getBasefilter() {
		return basefilter;
	}

	public void setBasefilter(String basefilter) {
		this.basefilter = basefilter;
	}
	
}
