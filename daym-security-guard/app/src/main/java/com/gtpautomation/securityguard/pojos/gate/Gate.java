package com.gtpautomation.securityguard.pojos.gate;

import com.google.gson.annotations.SerializedName;

public class Gate{

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

	public String getWkt(){
		return wkt;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public int getAddressType(){
		return addressType;
	}

	public int getV(){
		return V;
	}

	public String getName(){
		return name;
	}

	public int getOccupiedStatus(){
		return occupiedStatus;
	}

	public String getLocation(){
		return location;
	}

	public String getId(){
		return id;
	}

	public String getParentType(){
		return parentType;
	}

	public Object getParentId(){
		return parentId;
	}

	public int getStatus(){
		return status;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}
}