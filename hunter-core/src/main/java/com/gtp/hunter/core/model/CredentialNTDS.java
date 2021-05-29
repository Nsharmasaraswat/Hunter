package com.gtp.hunter.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("NTDS")
public class CredentialNTDS extends Credential {

	private static final String LOGINCLASS = "com.gtp.hunter.core.auth.CredentialNTDSHelper";
	private static final String PERMCLASS = "com.gtp.hunter.core.auth.NTDSGroupHelper";
	
	private String sid;
	
	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
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
