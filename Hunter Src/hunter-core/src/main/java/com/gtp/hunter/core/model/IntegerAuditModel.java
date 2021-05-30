package com.gtp.hunter.core.model;

import java.util.Date;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@MappedSuperclass
public abstract class IntegerAuditModel extends IntegerBaseModel {

	private Date	createdAt;
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
	private void onInsert() {
		if (this.createdAt == null) this.createdAt = new Date();
	}

	@PreUpdate
	private void onUpdate() {
		this.updatedAt = new Date();
	}

}
