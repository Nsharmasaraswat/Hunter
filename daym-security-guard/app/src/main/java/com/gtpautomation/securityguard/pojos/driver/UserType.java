package com.gtpautomation.securityguard.pojos.driver;

import com.google.gson.annotations.SerializedName;

public class UserType{

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("role")
	private int role;

	@SerializedName("__v")
	private int V;

	@SerializedName("name")
	private String name;

	@SerializedName("description")
	private String description;

	@SerializedName("_id")
	private String id;

	@SerializedName("status")
	private int status;

	@SerializedName("updatedAt")
	private String updatedAt;

	public String getCreatedAt(){
		return createdAt;
	}

	public int getRole(){
		return role;
	}

	public int getV(){
		return V;
	}

	public String getName(){
		return name;
	}

	public String getDescription(){
		return description;
	}

	public String getId(){
		return id;
	}

	public int getStatus(){
		return status;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}
}