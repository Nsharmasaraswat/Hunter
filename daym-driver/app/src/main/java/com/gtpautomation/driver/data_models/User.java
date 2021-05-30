package com.gtpautomation.driver.data_models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class User {

    @SerializedName("parent")
    private String parent;

    @SerializedName("sub")
    private String sub;

    @SerializedName("role")
    private int role;

    @SerializedName("name")
    private String name;

    @SerializedName("_id")
    private String id;

    @SerializedName("userType")
    private String userType;

    @SerializedName("userName")
    private String userName;

    @SerializedName("fields")
    private List<FieldsItem> fields;

    @SerializedName("status")
    private int status;

    public String getParent() {
        return parent;
    }

    public String getSub() {
        return sub;
    }

    public int getRole() {
        return role;
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

    public String getUserName() {
        return userName;
    }

    public List<FieldsItem> getFields() {
        return fields;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return
                "User{" +
                        "parent = '" + parent + '\'' +
                        ",sub = '" + sub + '\'' +
                        ",role = '" + role + '\'' +
                        ",name = '" + name + '\'' +
                        ",_id = '" + id + '\'' +
                        ",userType = '" + userType + '\'' +
                        ",userName = '" + userName + '\'' +
                        ",fields = '" + fields + '\'' +
                        ",status = '" + status + '\'' +
                        "}";
    }
}