package com.gtp.hunter.core.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;

@Entity
@Table(name = "prefix")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Prefix extends UUIDAuditModel {

	@Expose
	private String				prefix;

	@Expose
	private Long				count;

	@Expose
	@Transient
	private transient String	code;

	public Prefix() {
	}

	public Prefix(String prefix, Long count) {
		this.prefix = prefix;
		this.count = count;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
