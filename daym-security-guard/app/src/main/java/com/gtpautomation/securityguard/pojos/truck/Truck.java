package com.gtpautomation.securityguard.pojos.truck;

import com.google.gson.annotations.SerializedName;

public class Truck{

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("licensePlate")
	private String licensePlate;

	@SerializedName("supplier")
	private String supplier;

	@SerializedName("__v")
	private int V;

	@SerializedName("name")
	private String name;

	@SerializedName("_id")
	private String id;

	@SerializedName("user")
	private String user;

	@SerializedName("status")
	private int status;

	@SerializedName("updatedAt")
	private String updatedAt;

	public void setCreatedAt(String createdAt){
		this.createdAt = createdAt;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public void setLicensePlate(String licensePlate){
		this.licensePlate = licensePlate;
	}

	public String getLicensePlate(){
		return licensePlate;
	}

	public void setSupplier(String supplier){
		this.supplier = supplier;
	}

	public String getSupplier(){
		return supplier;
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

	public void setUser(String user){
		this.user = user;
	}

	public String getUser(){
		return user;
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