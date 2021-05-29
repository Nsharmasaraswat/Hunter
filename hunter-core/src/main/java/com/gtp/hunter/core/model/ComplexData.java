package com.gtp.hunter.core.model;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.model.RawData;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComplexData extends RawData {

	@Transient
	private Unit unit;

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	@Override
	public String toString() {
		return new GsonBuilder().serializeNulls().create().toJson(this);
	}

}
