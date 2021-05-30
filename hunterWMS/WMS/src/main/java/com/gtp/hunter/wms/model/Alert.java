package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.util.AlertSeverity;
import com.gtp.hunter.util.AlertType;

public class Alert extends BaseModel {

    @Expose
    @SerializedName("description")
    private String description;

    @Expose
    @SerializedName("item")
    private String item;

    @Expose
    @SerializedName("msg")
    private String msg;

    @Expose
    @SerializedName("severity")
    private AlertSeverity severity;

    @Expose
    @SerializedName("type")
    private AlertType type;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }
}
