package com.gtpautomation.securityguard.pojos.warehouse;

import com.google.gson.annotations.SerializedName;

public class Warehouse{

	@SerializedName("wkt")
	private String wkt;

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("addressType")
	private int addressType;

	@SerializedName("__v")
	private int V;

	@SerializedName("name")
	private String name;

	@SerializedName("occupiedStatus")
	private int occupiedStatus;

	@SerializedName("location")
	private String location;

	@SerializedName("_id")
	private String id;

	@SerializedName("parentType")
	private String parentType;

	@SerializedName("parentId")
	private Object parentId;

	@SerializedName("status")
	private int status;

	@SerializedName("updatedAt")
	private String updatedAt;

	public void setWkt(String wkt){
		this.wkt = wkt;
	}

	public String getWkt(){
		return wkt;
	}

	public void setCreatedAt(String createdAt){
		this.createdAt = createdAt;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public void setAddressType(int addressType){
		this.addressType = addressType;
	}

	public int getAddressType(){
		return addressType;
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

	public void setOccupiedStatus(int occupiedStatus){
		this.occupiedStatus = occupiedStatus;
	}

	public int getOccupiedStatus(){
		return occupiedStatus;
	}

	public void setLocation(String location){
		this.location = location;
	}

	public String getLocation(){
		return location;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setParentType(String parentType){
		this.parentType = parentType;
	}

	public String getParentType(){
		return parentType;
	}

	public void setParentId(Object parentId){
		this.parentId = parentId;
	}

	public Object getParentId(){
		return parentId;
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