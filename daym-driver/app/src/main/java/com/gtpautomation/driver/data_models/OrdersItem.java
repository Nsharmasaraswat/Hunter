package com.gtpautomation.driver.data_models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrdersItem {

    @SerializedName("product")
    private Product product;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("deliveryTime")
    private String deliveryTime;

    @SerializedName("truckEnteredAt")
    private Object truckEnteredAt;

    @SerializedName("appointment")
    private String appointment;

    @SerializedName("warehouse")
    private Warehouse warehouse;

    @SerializedName("deliveryOn")
    private String deliveryOn;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("slots")
    private List<Integer> slots;

    @SerializedName("truckExitedAt")
    private Object truckExitedAt;

    @SerializedName("totalElapsedTime")
    private String totalElapsedTime;

    @SerializedName("__v")
    private int V;

    @SerializedName("_id")
    private String id;

    @SerializedName("dock")
    private Dock dock;

    @SerializedName("user")
    private String user;

    @SerializedName("status")
    private int status;

    @SerializedName("updatedAt")
    private String updatedAt;

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public Object getTruckEnteredAt() {
        return truckEnteredAt;
    }

    public String getAppointment() {
        return appointment;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public String getDeliveryOn() {
        return deliveryOn;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public Object getTruckExitedAt() {
        return truckExitedAt;
    }

    public String getTotalElapsedTime() {
        return totalElapsedTime;
    }

    public int getV() {
        return V;
    }

    public String getId() {
        return id;
    }

    public Dock getDock() {
        return dock;
    }

    public String getUser() {
        return user;
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
                "OrdersItem{" +
                        "product = '" + product + '\'' +
                        ",quantity = '" + quantity + '\'' +
                        ",deliveryTime = '" + deliveryTime + '\'' +
                        ",truckEnteredAt = '" + truckEnteredAt + '\'' +
                        ",appointment = '" + appointment + '\'' +
                        ",warehouse = '" + warehouse + '\'' +
                        ",deliveryOn = '" + deliveryOn + '\'' +
                        ",createdAt = '" + createdAt + '\'' +
                        ",slots = '" + slots + '\'' +
                        ",truckExitedAt = '" + truckExitedAt + '\'' +
                        ",totalElapsedTime = '" + totalElapsedTime + '\'' +
                        ",__v = '" + V + '\'' +
                        ",_id = '" + id + '\'' +
                        ",dock = '" + dock + '\'' +
                        ",user = '" + user + '\'' +
                        ",status = '" + status + '\'' +
                        ",updatedAt = '" + updatedAt + '\'' +
                        "}";
    }
}