package com.gtpautomation.securityguard.pojos.userModel;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtpautomation.securityguard.pojos.truck.TruckField;

public class UserField{

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("values")
	private List<TruckField.SelectValue> values;

	@SerializedName("__v")
	private int V;

	@SerializedName("name")
	private String name;

	@SerializedName("_id")
	private String id;

	@SerializedName("userType")
	private String userType;

	@SerializedName("type")
	private String type;

	@SerializedName("required")
	private boolean required;

	@SerializedName("status")
	private int status;

	@SerializedName("updatedAt")
	private String updatedAt;

	public String getCreatedAt(){
		return createdAt;
	}

	public List<TruckField.SelectValue> getValues(){
		return values;
	}

	public int getV(){
		return V;
	}

	public String getName(){
		return name;
	}

	public String getId(){
		return id;
	}

	public String getUserType(){
		return userType;
	}

	public String getType(){
		return type;
	}

	public boolean isRequired(){
		return required;
	}

	public int getStatus(){
		return status;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}
}