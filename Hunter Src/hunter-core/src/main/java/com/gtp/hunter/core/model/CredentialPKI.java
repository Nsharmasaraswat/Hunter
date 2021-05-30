package com.gtp.hunter.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("PUB")
public class CredentialPKI extends Credential {

	private String pubKey;
	
	private static final String LOGINCLASS = "com.gtp.hunter.core.auth.CredentialPasswordHelper";
	private static final String PERMCLASS = "com.gtp.hunter.core.auth.InternalGroupHelper";

	@Override
	public String getLoginClass() {
		return LOGINCLASS;
	}

	@Override
	public String getPermClass() {
		return PERMCLASS;
	}
	
}
