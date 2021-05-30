package com.gtpautomation.driver.data_models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserField {

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("values")
    private List<Object> values;

    @SerializedName("__v")
    private int V;

    @SerializedName("name")
    private String name;

    @SerializedName("_id")
    private String id;

    @SerializedName("userType")
    private String userType;

    @SerializedName("type")
    private String type;

    @SerializedName("status")
    private int status;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getCreatedAt() {
        return createdAt;
    }

    public List<Object> getValues() {
        return values;
    }

    public int getV() {
        return V;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getUserType() {
        return userType;
    }

    public String getType() {
        return type;
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
                "UserField{" +
                        "createdAt = '" + createdAt + '\'' +
                        ",values = '" + values + '\'' +
                        ",__v = '" + V + '\'' +
                        ",name = '" + name + '\'' +
                        ",_id = '" + id + '\'' +
                        ",userType = '" + userType + '\'' +
                        ",type = '" + type + '\'' +
                        ",status = '" + status + '\'' +
                        ",updatedAt = '" + updatedAt + '\'' +
                        "}";
    }
}