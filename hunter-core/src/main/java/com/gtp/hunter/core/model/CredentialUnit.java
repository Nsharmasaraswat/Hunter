package com.gtp.hunter.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("UNT")
public class CredentialUnit extends Credential {

	@ManyToOne
	@JoinColumn(name="unit_id")
	private Unit unit;
	
	private static final String LOGINCLASS = "com.gtp.hunter.core.auth.CredentialPasswordHelper";
	private static final String PERMCLASS = "com.gtp.hunter.core.auth.InternalGroupHelper";

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	@Override
	public String getLoginClass() {
		return LOGINCLASS;
	}

	@Override
	public String getPermClass() {
		return PERMCLASS;
	}
	
	
}
