package com.gtpautomation.securityguard.pojos.appointment_details;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Product implements Parcelable {

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("productCode")
	private String productCode;

	@SerializedName("__v")
	private int V;

	@SerializedName("name")
	private String name;

	@SerializedName("_id")
	private String id;

	@SerializedName("productType")
	private String productType;

	@SerializedName("barCode")
	private String barCode;

	@SerializedName("status")
	private int status;

	@SerializedName("updatedAt")
	private String updatedAt;

	protected Product(Parcel in) {
		createdAt = in.readString();
		productCode = in.readString();
		V = in.readInt();
		name = in.readString();
		id = in.readString();
		productType = in.readString();
		barCode = in.readString();
		status = in.readInt();
		updatedAt = in.readString();
	}

	public static final Creator<Product> CREATOR = new Creator<Product>() {
		@Override
		public Product createFromParcel(Parcel in) {
			return new Product(in);
		}

		@Override
		public Product[] newArray(int size) {
			return new Product[size];
		}
	};

	public void setCreatedAt(String createdAt){
		this.createdAt = createdAt;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public void setProductCode(String productCode){
		this.productCode = productCode;
	}

	public String getProductCode(){
		return productCode;
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

	public void setProductType(String productType){
		this.productType = productType;
	}

	public String getProductType(){
		return productType;
	}

	public void setBarCode(String barCode){
		this.barCode = barCode;
	}

	public String getBarCode(){
		return barCode;
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
		parcel.writeString(productCode);
		parcel.writeInt(V);
		parcel.writeString(name);
		parcel.writeString(id);
		parcel.writeString(productType);
		parcel.writeString(barCode);
		parcel.writeInt(status);
		parcel.writeString(updatedAt);
	}
}