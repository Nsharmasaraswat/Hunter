package com.gtp.hunter.core.model;

import java.util.Date;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.google.gson.annotations.Expose;

@MappedSuperclass
public abstract class UUIDAuditModel extends UUIDBaseModel {

	@Expose()
	@Temporal(TemporalType.TIMESTAMP)
	private Date	createdAt;

	@Expose()
	@Temporal(TemporalType.TIMESTAMP)
	private Date	updatedAt;

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	@PrePersist
	protected void onInsert() {
		if (this.createdAt == null) this.createdAt = new Date();
		onUpdate();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = new Date();
	}

}
