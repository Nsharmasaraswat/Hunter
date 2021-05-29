package com.gtp.hunter.core.model;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;

import com.google.gson.annotations.Expose;

@MappedSuperclass
public abstract class BaseModel<ID> {

	@Expose
	@Basic(fetch = FetchType.EAGER)
	private String	name;
	@Expose
	@Basic(fetch = FetchType.EAGER)
	private String	metaname;
	@Expose
	@Basic(fetch = FetchType.EAGER)
	private String	status;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMetaname() {
		return metaname;
	}

	public void setMetaname(String metaname) {
		this.metaname = metaname;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public abstract ID getId();

}
