package com.gtpautomation.driver.data_models;

import com.google.gson.annotations.SerializedName;

public class FieldsItem {

    @SerializedName("userField")
    private UserField userField;

    @SerializedName("_id")
    private String id;

    @SerializedName("value")
    private String value;

    public UserField getUserField() {
        return userField;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return
                "FieldsItem{" +
                        "userField = '" + userField + '\'' +
                        ",_id = '" + id + '\'' +
                        ",value = '" + value + '\'' +
                        "}";
    }
}