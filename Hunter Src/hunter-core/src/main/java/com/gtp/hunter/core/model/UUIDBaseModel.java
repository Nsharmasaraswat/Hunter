package com.gtp.hunter.core.model;

import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import com.google.gson.annotations.Expose;

@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class UUIDBaseModel extends BaseModel<UUID> {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@Basic(optional = false)
	@Type(type = "uuid-char")
	@Column(name = "id", nullable = false, columnDefinition = "CHAR(36)")
	@Expose()
	private UUID id;

	@Override
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}
}
