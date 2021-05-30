package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ActionFormOption extends BaseModel implements Serializable {
    @Expose
    @SerializedName("label")
    String label;

    @Expose
    @SerializedName("value")
    String value;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
