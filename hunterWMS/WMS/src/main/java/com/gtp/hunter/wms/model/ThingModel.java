package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

import kotlin.jvm.Transient;

public class ThingModel extends UUIDBaseModel {

    @Expose
    @SerializedName("fields")
    private Set<BaseModelField> fields = new HashSet<>();

    @Expose
    @Transient
    @SerializedName("payload")
    private String payload;

    public Set<BaseModelField> getFields() {
        return fields;
    }

    public void setFields(Set<BaseModelField> fields) {
        this.fields = fields;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
