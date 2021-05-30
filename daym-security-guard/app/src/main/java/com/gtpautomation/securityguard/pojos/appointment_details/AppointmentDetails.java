package com.gtpautomation.securityguard.pojos.appointment_details;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import com.google.gson.annotations.SerializedName;
import com.gtpautomation.securityguard.pojos.driver.Driver;
import com.gtpautomation.securityguard.pojos.gate.Gate;
import com.gtpautomation.securityguard.pojos.supplier.Supplier;
import com.gtpautomation.securityguard.pojos.truck.Truck;

public class AppointmentDetails implements Parcelable {

	@SerializedName("ticket")
	private String ticket;

	@SerializedName("orderCount")
	private int orderCount;

	@SerializedName("truck")
	private Truck truck;

	@SerializedName("acceptedOrRejectedBy")
	private String acceptedOrRejectedBy;

	@SerializedName("completedOrderCount")
	private int completedOrderCount;

	@SerializedName("deliveryOn")
	private String deliveryOn;

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("driver")
	private Driver driver;

	@SerializedName("completedOn")
	private String completedOn;

	@SerializedName("supplier")
	private Supplier supplier;

	@SerializedName("__v")
	private int V;

	@SerializedName("receipt")
	private List<String> receipt;

	@SerializedName("orders")
	private List<OrdersItem> orders;

	@SerializedName("_id")
	private String id;

	@SerializedName("exitGate")
	private Gate exitGate;

	@SerializedName("entranceGate")
	private Gate entranceGate;

	@SerializedName("user")
	private User user;

	@SerializedName("status")
	private int status;

	@SerializedName("updatedAt")
	private String updatedAt;

	protected AppointmentDetails(Parcel in) {
		ticket = in.readString();
		orderCount = in.readInt();
		acceptedOrRejectedBy = in.readString();
		completedOrderCount = in.readInt();
		deliveryOn = in.readString();
		createdAt = in.readString();
		completedOn = in.readString();
		V = in.readInt();
		receipt = in.createStringArrayList();
		orders = in.createTypedArrayList(OrdersItem.CREATOR);
		id = in.readString();
		user = in.readParcelable(User.class.getClassLoader());
		status = in.readInt();
		updatedAt = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(ticket);
		dest.writeInt(orderCount);
		dest.writeString(acceptedOrRejectedBy);
		dest.writeInt(completedOrderCount);
		dest.writeString(deliveryOn);
		dest.writeString(createdAt);
		dest.writeString(completedOn);
		dest.writeInt(V);
		dest.writeStringList(receipt);
		dest.writeTypedList(orders);
		dest.writeString(id);
		dest.writeParcelable(user, flags);
		dest.writeInt(status);
		dest.writeString(updatedAt);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<AppointmentDetails> CREATOR = new Creator<AppointmentDetails>() {
		@Override
		public AppointmentDetails createFromParcel(Parcel in) {
			return new AppointmentDetails(in);
		}

		@Override
		public AppointmentDetails[] newArray(int size) {
			return new AppointmentDetails[size];
		}
	};

	public void setTicket(String ticket){
		this.ticket = ticket;
	}

	public String getTicket(){
		return ticket;
	}

	public void setOrderCount(int orderCount){
		this.orderCount = orderCount;
	}

	public int getOrderCount(){
		return orderCount;
	}

	public void setExitGate(Gate exitGate){
		this.exitGate = exitGate;
	}

	public Gate getExitGate(){
		return exitGate;
	}

	public void setEntranceGate(Gate entranceGate){
		this.entranceGate = entranceGate;
	}

	public Gate getEntranceGate(){
		return entranceGate;
	}

	public void setAcceptedOrRejectedBy(String acceptedOrRejectedBy){
		this.acceptedOrRejectedBy = acceptedOrRejectedBy;
	}

	public String getAcceptedOrRejectedBy(){
		return acceptedOrRejectedBy;
	}

	public void setCompletedOrderCount(int completedOrderCount){
		this.completedOrderCount = completedOrderCount;
	}

	public int getCompletedOrderCount(){
		return completedOrderCount;
	}

	public void setDeliveryOn(String deliveryOn){
		this.deliveryOn = deliveryOn;
	}

	public String getDeliveryOn(){
		return deliveryOn;
	}

	public void setCreatedAt(String createdAt){
		this.createdAt = createdAt;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public void setDriver(Driver driver){
		this.driver = driver;
	}

	public Driver getDriver(){
		return driver;
	}

	public void setCompletedOn(String completedOn){
		this.completedOn = completedOn;
	}

	public String getCompletedOn(){
		return completedOn;
	}

	public void setSupplier(Supplier supplier){
		this.supplier = supplier;
	}

	public Supplier getSupplier(){
		return supplier;
	}

	public void setV(int V){
		this.V = V;
	}

	public int getV(){
		return V;
	}

	public void setReceipt(List<String> receipt){
		this.receipt = receipt;
	}

	public List<String> getReceipt(){
		return receipt;
	}

	public void setOrders(List<OrdersItem> orders){
		this.orders = orders;
	}

	public List<OrdersItem> getOrders(){
		return orders;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setUser(User user){
		this.user = user;
	}

	public User getUser(){
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

	public Truck getTruck() {
		return truck;
	}

	public void setTruck(Truck truck) {
		this.truck = truck;
	}
}