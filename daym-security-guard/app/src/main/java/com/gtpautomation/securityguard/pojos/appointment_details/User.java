package com.gtpautomation.securityguard.pojos.appointment_details;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class User implements Parcelable {

	@SerializedName("parent")
	private Object parent;

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

	protected User(Parcel in) {
		createdAt = in.readString();
		password = in.readString();
		role = in.readInt();
		V = in.readInt();
		name = in.readString();
		id = in.readString();
		userType = in.readString();
		userName = in.readString();
		status = in.readInt();
		updatedAt = in.readString();
	}

	public static final Creator<User> CREATOR = new Creator<User>() {
		@Override
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		@Override
		public User[] newArray(int size) {
			return new User[size];
		}
	};

	public void setParent(Object parent){
		this.parent = parent;
	}

	public Object getParent(){
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(createdAt);
		parcel.writeString(password);
		parcel.writeInt(role);
		parcel.writeInt(V);
		parcel.writeString(name);
		parcel.writeString(id);
		parcel.writeString(userType);
		parcel.writeString(userName);
		parcel.writeInt(status);
		parcel.writeString(updatedAt);
	}
}