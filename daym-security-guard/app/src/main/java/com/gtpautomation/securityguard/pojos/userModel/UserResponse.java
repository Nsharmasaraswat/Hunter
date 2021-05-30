package com.gtpautomation.securityguard.pojos.userModel;

import com.google.gson.annotations.SerializedName;

public class UserResponse{

	@SerializedName("accessToken")
	private String accessToken;

	@SerializedName("user")
	private User user;

	@SerializedName("authentication")
	private Authentication authentication;

	public String getAccessToken(){
		return accessToken;
	}

	public User getUser(){
		return user;
	}

	public Authentication getAuthentication(){
		return authentication;
	}
}