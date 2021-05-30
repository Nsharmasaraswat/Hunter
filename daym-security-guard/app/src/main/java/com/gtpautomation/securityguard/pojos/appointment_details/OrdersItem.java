package com.gtpautomation.securityguard.pojos.appointment_details;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.gtpautomation.securityguard.pojos.dock.Dock;
import com.gtpautomation.securityguard.pojos.warehouse.Warehouse;

public class OrdersItem implements Parcelable {

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("deliveryTime")
	private String deliveryTime;

	@SerializedName("product")
	private Product product;

	@SerializedName("quantity")
	private int quantity;

	@SerializedName("__v")
	private int V;

	@SerializedName("appointment")
	private String appointment;

	@SerializedName("_id")
	private String id;

	@SerializedName("dock")
	private Dock dock;

	@SerializedName("warehouse")
	private Warehouse warehouse;

	@SerializedName("user")
	private String user;

	@SerializedName("status")
	private int status;

	@SerializedName("updatedAt")
	private String updatedAt;

	protected OrdersItem(Parcel in) {
		createdAt = in.readString();
		quantity = in.readInt();
		V = in.readInt();
		appointment = in.readString();
		id = in.readString();
		user = in.readString();
		status = in.readInt();
		updatedAt = in.readString();
		deliveryTime = in.readString();
	}

	public static final Creator<OrdersItem> CREATOR = new Creator<OrdersItem>() {
		@Override
		public OrdersItem createFromParcel(Parcel in) {
			return new OrdersItem(in);
		}

		@Override
		public OrdersItem[] newArray(int size) {
			return new OrdersItem[size];
		}
	};

	public void setCreatedAt(String createdAt){
		this.createdAt = createdAt;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public void setProduct(Product product){
		this.product = product;
	}

	public Product getProduct(){
		return product;
	}

	public void setQuantity(int quantity){
		this.quantity = quantity;
	}

	public int getQuantity(){
		return quantity;
	}

	public void setV(int V){
		this.V = V;
	}

	public int getV(){
		return V;
	}

	public void setAppointment(String appointment){
		this.appointment = appointment;
	}

	public String getAppointment(){
		return appointment;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setDock(Dock dock){
		this.dock = dock;
	}

	public Dock getDock(){
		return dock;
	}

	public void setWarehouse(Warehouse warehouse){
		this.warehouse = warehouse;
	}

	public Warehouse getWarehouse(){
		return warehouse;
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

	public String getDeliveryTime() {
		return deliveryTime;
	}

	public void setDeliveryTime(String deliveryTime) {
		this.deliveryTime = deliveryTime;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(createdAt);
		parcel.writeInt(quantity);
		parcel.writeInt(V);
		parcel.writeString(appointment);
		parcel.writeString(id);
		parcel.writeString(user);
		parcel.writeInt(status);
		parcel.writeString(updatedAt);
		parcel.writeString(deliveryTime);
	}
}