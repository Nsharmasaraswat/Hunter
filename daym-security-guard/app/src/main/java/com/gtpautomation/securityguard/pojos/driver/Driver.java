package com.gtpautomation.securityguard.pojos.driver;

import com.google.gson.annotations.SerializedName;

public class Driver{

	@SerializedName("parent")
	private String parent;

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("password")
	private String password;

	@SerializedName("role")
	private int role;

	@SerializedName("__v")
	private int V;

	@SerializedName("name")
	private String name;

	@SerializedName("_id")
	private String id;

	@SerializedName("userType")
	private String userType;

	@SerializedName("userName")
	private String userName;

	@SerializedName("status")
	private int status;

	@SerializedName("updatedAt")
	private String updatedAt;

	public void setParent(String parent){
		this.parent = parent;
	}

	public String getParent(){
		return parent;
	}

	public void setCreatedAt(String createdAt){
		this.createdAt = createdAt;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public String getPassword(){
		return password;
	}

	public void setRole(int role){
		this.role = role;
	}

	public int getRole(){
		return role;
	}

	public void setV(int V){
		this.V = V;
	}

	public int getV(){
		return V;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setUserType(String userType){
		this.userType = userType;
	}

	public String getUserType(){
		return userType;
	}

	public void setUserName(String userName){
		this.userName = userName;
	}

	public String getUserName(){
		return userName;
	}

	public void setStatus(int status){
		this.status = status;
	}

	public int getStatus(){
		return status;
	}

	public void setUpdatedAt(String updatedAt){
		this.updatedAt = updatedAt;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}
}