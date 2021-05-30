package com.gtpautomation.securityguard.pojos.appointment;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import com.google.gson.annotations.SerializedName;
import com.gtpautomation.securityguard.pojos.driver.Driver;
import com.gtpautomation.securityguard.pojos.gate.Gate;
import com.gtpautomation.securityguard.pojos.supplier.Supplier;
import com.gtpautomation.securityguard.pojos.truck.Truck;
import com.gtpautomation.securityguard.pojos.userModel.User;

public class AppointmentResponse implements Parcelable {

	@SerializedName("appointmentType")
	private int appointmentType;

	@SerializedName("ticket")
	private String ticket;

	@SerializedName("truck")
	private Truck truck;

	@SerializedName("orderCount")
	private int orderCount;

	@SerializedName("acceptedOrRejectedBy")
	private String acceptedOrRejectedBy;

	@SerializedName("completedOrderCount")
	private int completedOrderCount;

	@SerializedName("deliveryOn")
	private String deliveryOn;

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("truckArrivedAt")
	private String truckArrivedAt;

	@SerializedName("truckExitedAt")
	private Object truckExitedAt;

	@SerializedName("driver")
	private Driver driver;

	@SerializedName("completedOn")
	private Object completedOn;

	@SerializedName("supplier")
	private Supplier supplier;

	@SerializedName("__v")
	private int V;

	@SerializedName("receipt")
	private List<String> receipt;

	@SerializedName("exitGate")
	private Gate exitGate;

	@SerializedName("entranceGate")
	private Gate entranceGate;

	@SerializedName("_id")
	private String id;

	@SerializedName("user")
	private User user;

	@SerializedName("status")
	private int status;

	@SerializedName("updatedAt")
	private String updatedAt;

	protected AppointmentResponse(Parcel in) {
		appointmentType = in.readInt();
		ticket = in.readString();
		orderCount = in.readInt();
		acceptedOrRejectedBy = in.readString();
		completedOrderCount = in.readInt();
		deliveryOn = in.readString();
		createdAt = in.readString();
		truckArrivedAt = in.readString();
		V = in.readInt();
		receipt = in.createStringArrayList();
		id = in.readString();
		status = in.readInt();
		updatedAt = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(appointmentType);
		dest.writeString(ticket);
		dest.writeInt(orderCount);
		dest.writeString(acceptedOrRejectedBy);
		dest.writeInt(completedOrderCount);
		dest.writeString(deliveryOn);
		dest.writeString(createdAt);
		dest.writeString(truckArrivedAt);
		dest.writeInt(V);
		dest.writeStringList(receipt);
		dest.writeString(id);
		dest.writeInt(status);
		dest.writeString(updatedAt);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<AppointmentResponse> CREATOR = new Creator<AppointmentResponse>() {
		@Override
		public AppointmentResponse createFromParcel(Parcel in) {
			return new AppointmentResponse(in);
		}

		@Override
		public AppointmentResponse[] newArray(int size) {
			return new AppointmentResponse[size];
		}
	};

	public void setAppointmentType(int appointmentType){
		this.appointmentType = appointmentType;
	}

	public int getAppointmentType(){
		return appointmentType;
	}

	public void setTicket(String ticket){
		this.ticket = ticket;
	}

	public String getTicket(){
		return ticket;
	}

	public void setTruck(Truck truck){
		this.truck = truck;
	}

	public Truck getTruck(){
		return truck;
	}

	public void setOrderCount(int orderCount){
		this.orderCount = orderCount;
	}

	public int getOrderCount(){
		return orderCount;
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

	public void setTruckArrivedAt(String truckArrivedAt){
		this.truckArrivedAt = truckArrivedAt;
	}

	public String getTruckArrivedAt(){
		return truckArrivedAt;
	}

	public void setTruckExitedAt(Object truckExitedAt){
		this.truckExitedAt = truckExitedAt;
	}

	public Object getTruckExitedAt(){
		return truckExitedAt;
	}

	public void setDriver(Driver driver){
		this.driver = driver;
	}

	public Driver getDriver(){
		return driver;
	}

	public void setCompletedOn(Object completedOn){
		this.completedOn = completedOn;
	}

	public Object getCompletedOn(){
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
}