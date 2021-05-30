package com.gtp.hunter.wms.model;

import androidx.room.Ignore;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Set;

public class ModelBase extends UUIDBaseModel {

    @Expose
    @SerializedName("fields")
    @Ignore
    private Set<BaseModelField> fields;

    public Set<BaseModelField> getFields() {
        return fields;
    }

    public void setFields(Set<BaseModelField> fields) {
        this.fields = fields;
    }
}
