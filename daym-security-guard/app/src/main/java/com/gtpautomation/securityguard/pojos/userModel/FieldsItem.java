package com.gtpautomation.securityguard.pojos.userModel;

import com.google.gson.annotations.SerializedName;

public class FieldsItem{

	@SerializedName("userField")
	private UserField userField;

	@SerializedName("_id")
	private String id;

	@SerializedName("value")
	private String value;

	@SerializedName("order")
	private int order;

	public UserField getUserField(){
		return userField;
	}

	public String getId(){
		return id;
	}

	public String getValue(){
		return value;
	}

	public int getOrder(){
		return order;
	}
}