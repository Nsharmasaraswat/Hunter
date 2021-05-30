package com.gtpautomation.driver.data_models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Dock {

    @SerializedName("wkt")
    private String wkt;

    @SerializedName("addressType")
    private int addressType;

    @SerializedName("productTypes")
    private List<String> productTypes;

    @SerializedName("occupiedStatus")
    private int occupiedStatus;

    @SerializedName("parentType")
    private String parentType;

    @SerializedName("parentId")
    private String parentId;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("totalSlots")
    private int totalSlots;

    @SerializedName("startingTime")
    private String startingTime;

    @SerializedName("__v")
    private int V;

    @SerializedName("name")
    private String name;

    @SerializedName("endingTime")
    private String endingTime;

    @SerializedName("location")
    private String location;

    @SerializedName("_id")
    private String id;

    @SerializedName("status")
    private int status;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getWkt() {
        return wkt;
    }

    public int getAddressType() {
        return addressType;
    }

    public List<String> getProductTypes() {
        return productTypes;
    }

    public int getOccupiedStatus() {
        return occupiedStatus;
    }

    public String getParentType() {
        return parentType;
    }

    public String getParentId() {
        return parentId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public String getStartingTime() {
        return startingTime;
    }

    public int getV() {
        return V;
    }

    public String getName() {
        return name;
    }

    public String getEndingTime() {
        return endingTime;
    }

    public String getLocation() {
        return location;
    }

    public String getId() {
        return id;
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
                "Dock{" +
                        "wkt = '" + wkt + '\'' +
                        ",addressType = '" + addressType + '\'' +
                        ",productTypes = '" + productTypes + '\'' +
                        ",occupiedStatus = '" + occupiedStatus + '\'' +
                        ",parentType = '" + parentType + '\'' +
                        ",parentId = '" + parentId + '\'' +
                        ",createdAt = '" + createdAt + '\'' +
                        ",totalSlots = '" + totalSlots + '\'' +
                        ",startingTime = '" + startingTime + '\'' +
                        ",__v = '" + V + '\'' +
                        ",name = '" + name + '\'' +
                        ",endingTime = '" + endingTime + '\'' +
                        ",location = '" + location + '\'' +
                        ",_id = '" + id + '\'' +
                        ",status = '" + status + '\'' +
                        ",updatedAt = '" + updatedAt + '\'' +
                        "}";
    }
}