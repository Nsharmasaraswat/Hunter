package com.gtpautomation.driver.data_models;

import com.google.gson.annotations.SerializedName;

public class ExitGate {

    @SerializedName("wkt")
    private String wkt;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("addressType")
    private int addressType;

    @SerializedName("__v")
    private int V;

    @SerializedName("name")
    private String name;

    @SerializedName("occupiedStatus")
    private int occupiedStatus;

    @SerializedName("location")
    private String location;

    @SerializedName("_id")
    private String id;

    @SerializedName("parentType")
    private String parentType;

    @SerializedName("parentId")
    private Object parentId;

    @SerializedName("status")
    private int status;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getWkt() {
        return wkt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getAddressType() {
        return addressType;
    }

    public int getV() {
        return V;
    }

    public String getName() {
        return name;
    }

    public int getOccupiedStatus() {
        return occupiedStatus;
    }

    public String getLocation() {
        return location;
    }

    public String getId() {
        return id;
    }

    public String getParentType() {
        return parentType;
    }

    public Object getParentId() {
        return parentId;
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
                "ExitGate{" +
                        "wkt = '" + wkt + '\'' +
                        ",createdAt = '" + createdAt + '\'' +
                        ",addressType = '" + addressType + '\'' +
                        ",__v = '" + V + '\'' +
                        ",name = '" + name + '\'' +
                        ",occupiedStatus = '" + occupiedStatus + '\'' +
                        ",location = '" + location + '\'' +
                        ",_id = '" + id + '\'' +
                        ",parentType = '" + parentType + '\'' +
                        ",parentId = '" + parentId + '\'' +
                        ",status = '" + status + '\'' +
                        ",updatedAt = '" + updatedAt + '\'' +
                        "}";
    }
}