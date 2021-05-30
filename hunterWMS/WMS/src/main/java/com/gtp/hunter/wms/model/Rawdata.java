package com.gtp.hunter.wms.model;

import androidx.annotation.NonNull;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Rawdata<T> {

    @Expose
    @SerializedName("tagId")
    private String tagId;

    @Expose
    @SerializedName("type")
    private String type;

    @Expose
    @SerializedName("payload")
    private T payload;

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    @NonNull
    @Override
    public String toString(){
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
    }
}
