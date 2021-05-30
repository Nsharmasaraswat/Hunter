package com.gtpautomation.securityguard.pojos.userModel;

import com.google.gson.annotations.SerializedName;

public class ValuesItem{

	@SerializedName("_id")
	private String id;

	@SerializedName("label")
	private String label;

	@SerializedName("value")
	private String value;

	public String getId(){
		return id;
	}

	public String getLabel(){
		return label;
	}

	public String getValue(){
		return value;
	}
}