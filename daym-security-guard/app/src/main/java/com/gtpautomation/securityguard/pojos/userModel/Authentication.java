package com.gtpautomation.securityguard.pojos.userModel;

import com.google.gson.annotations.SerializedName;

public class Authentication{

	@SerializedName("payload")
	private Payload payload;

	@SerializedName("strategy")
	private String strategy;

	@SerializedName("accessToken")
	private String accessToken;

	public Payload getPayload(){
		return payload;
	}

	public String getStrategy(){
		return strategy;
	}

	public String getAccessToken(){
		return accessToken;
	}
}