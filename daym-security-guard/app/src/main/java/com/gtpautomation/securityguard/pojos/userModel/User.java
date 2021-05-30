package com.gtpautomation.securityguard.pojos.userModel;

import java.util.List;
import com.google.gson.annotations.SerializedName;
import com.gtpautomation.securityguard.pojos.gate.Gate;

public class User{

	@SerializedName("parent")
	private Object parent;

	@SerializedName("role")
	private int role;

	@SerializedName("userName")
	private String userName;

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("permissions")
	private List<Object> permissions;

	@SerializedName("__v")
	private int V;

	@SerializedName("name")
	private String name;

	@SerializedName("_id")
	private String id;

	@SerializedName("userType")
	private String userType;

	@SerializedName("gate")
	private Gate gate;

	@SerializedName("fields")
	private List<FieldsItem> fields;

	@SerializedName("status")
	private int status;

	@SerializedName("updatedAt")
	private String updatedAt;

	public Object getParent(){
		return parent;
	}

	public int getRole(){
		return role;
	}

	public String getUserName(){
		return userName;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public List<Object> getPermissions(){
		return permissions;
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

	public Gate getGate(){
		return gate;
	}

	public List<FieldsItem> getFields(){
		return fields;
	}

	public int getStatus(){
		return status;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}
}