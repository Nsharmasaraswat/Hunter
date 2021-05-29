package com.gtp.hunter.core.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.gtp.hunter.common.enums.UnitType;

@Entity
@Table(name = "unit", indexes = {
		@Index(columnList = "tagId", name = "unit_tagid_idx")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Unit extends UUIDAuditModel {

	@Expose()
	private String		tagId;

	@Expose()
	@Enumerated(EnumType.STRING)
	private UnitType	type;

	public Unit() {
	}

	public Unit(String tagId, UnitType type) {
		setTagId(tagId);
		setType(type);
	}

	public Unit(String name, String tagId, UnitType type) {
		setName(name);
		setTagId(tagId);
		setType(type);
	}

	public String getTagId() {
		return tagId;
	}

	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	public UnitType getType() {
		return type;
	}

	public void setType(UnitType type) {
		this.type = type;
	}

}
