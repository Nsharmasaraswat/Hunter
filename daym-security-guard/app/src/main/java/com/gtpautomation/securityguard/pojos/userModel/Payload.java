package com.gtpautomation.securityguard.pojos.userModel;

import com.google.gson.annotations.SerializedName;

public class Payload{

	@SerializedName("aud")
	private String aud;

	@SerializedName("sub")
	private String sub;

	@SerializedName("iss")
	private String iss;

	@SerializedName("exp")
	private int exp;

	@SerializedName("iat")
	private int iat;

	@SerializedName("jti")
	private String jti;

	public String getAud(){
		return aud;
	}

	public String getSub(){
		return sub;
	}

	public String getIss(){
		return iss;
	}

	public int getExp(){
		return exp;
	}

	public int getIat(){
		return iat;
	}

	public String getJti(){
		return jti;
	}
}