package com.gtp.hunter.wms.model;

import androidx.room.ColumnInfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BaseModelField extends UUIDBaseModel {

    @Expose
    @SerializedName("ordem")
    @ColumnInfo(name = "ordem")
    private Integer ordem;

    @Expose
    @SerializedName("visible")
    @ColumnInfo(name = "visible")
    private Boolean visible;

    @Expose
    @SerializedName("type")
    @ColumnInfo(name = "type")
    private String type;

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
