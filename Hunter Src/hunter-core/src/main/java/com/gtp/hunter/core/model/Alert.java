package com.gtp.hunter.core.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.common.enums.AlertSeverity;
import com.gtp.hunter.common.enums.AlertType;

@Entity
@Table(name = "alert")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Alert extends UUIDAuditModel {

	@Expose
	private String			item;

	@Lob
	@Expose
	private String			msg;

	@Lob
	@Expose
	private String			description;

	@Expose
	@Enumerated(EnumType.STRING)
	private AlertType		type;

	@Expose
	@Enumerated(EnumType.STRING)
	private AlertSeverity	severity;

	public Alert() {
	}

	public Alert(AlertType type, AlertSeverity severity, String item, String msg, String description) {
		super();
		this.msg = msg;
		this.description = description;
		this.item = item;
		this.type = type;
		this.severity = severity;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public AlertType getType() {
		return type;
	}

	public void setType(AlertType type) {
		this.type = type;
	}

	public AlertSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(AlertSeverity severity) {
		this.severity = severity;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}
}
