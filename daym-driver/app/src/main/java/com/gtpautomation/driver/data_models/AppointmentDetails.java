package com.gtpautomation.driver.data_models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AppointmentDetails {

	@SerializedName("appointmentType")
	private int appointmentType;

	@SerializedName("ticket")
	private String ticket;

	@SerializedName("truck")
	private String truck;

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
	private String truckExitedAt;

	@SerializedName("driver")
	private String driver;

	@SerializedName("completedOn")
	private String completedOn;

	@SerializedName("supplier")
	private String supplier;

	@SerializedName("__v")
	private int V;

	@SerializedName("receipt")
	private List<String> receipt;

	@SerializedName("exitGate")
	private ExitGate exitGate;

	@SerializedName("orders")
	private List<OrdersItem> orders;

	@SerializedName("_id")
	private String id;

	@SerializedName("user")
	private String user;

	@SerializedName("entranceGate")
	private String entranceGate;

	@SerializedName("status")
	private int status;

	@SerializedName("updatedAt")
	private String updatedAt;

	public int getAppointmentType() {
		return appointmentType;
	}

	public String getTicket() {
		return ticket;
	}

	public String getTruck() {
		return truck;
	}

	public int getOrderCount() {
		return orderCount;
	}

	public String getAcceptedOrRejectedBy() {
		return acceptedOrRejectedBy;
	}

	public int getCompletedOrderCount() {
		return completedOrderCount;
	}

	public String getDeliveryOn() {
		return deliveryOn;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getTruckArrivedAt() {
		return truckArrivedAt;
	}

	public Object getTruckExitedAt() {
		return truckExitedAt;
	}

	public String getDriver() {
		return driver;
	}

	public Object getCompletedOn() {
		return completedOn;
	}

	public String getSupplier() {
		return supplier;
	}

	public int getV() {
		return V;
	}

	public List<String> getReceipt() {
		return receipt;
	}

	public ExitGate getExitGate() {
		return exitGate;
	}

	public List<OrdersItem> getOrders() {
		return orders;
	}

	public String getId() {
		return id;
	}

	public String getUser() {
		return user;
	}

	public String getEntranceGate() {
		return entranceGate;
	}

	public int getStatus() {
		return status;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	@Override
	public String toString() {
		return
				"AppointmentDetails{" +
						"appointmentType = '" + appointmentType + '\'' +
						",ticket = '" + ticket + '\'' +
						",truck = '" + truck + '\'' +
						",orderCount = '" + orderCount + '\'' +
						",acceptedOrRejectedBy = '" + acceptedOrRejectedBy + '\'' +
						",completedOrderCount = '" + completedOrderCount + '\'' +
						",deliveryOn = '" + deliveryOn + '\'' +
						",createdAt = '" + createdAt + '\'' +
						",truckArrivedAt = '" + truckArrivedAt + '\'' +
						",truckExitedAt = '" + truckExitedAt + '\'' +
						",driver = '" + driver + '\'' +
						",completedOn = '" + completedOn + '\'' +
						",supplier = '" + supplier + '\'' +
						",__v = '" + V + '\'' +
						",receipt = '" + receipt + '\'' +
						",exitGate = '" + exitGate + '\'' +
						",orders = '" + orders + '\'' +
						",_id = '" + id + '\'' +
						",user = '" + user + '\'' +
						",entranceGate = '" + entranceGate + '\'' +
						",status = '" + status + '\'' +
						",updatedAt = '" + updatedAt + '\'' +
						"}";
	}
}