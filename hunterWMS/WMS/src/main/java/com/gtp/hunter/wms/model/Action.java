package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Action extends BaseModel {

    @Expose
    @SerializedName("icon")
    private String icon;

    @Expose
    @SerializedName("taskstatus")
    private String taskstatus;

    @Expose
    @SerializedName("actionDef")
    private String actionDef;

    @Expose
    @SerializedName("classe")
    private String classe;

    @Expose
    @SerializedName("route")
    private String route;

    @Expose
    @SerializedName("params")
    private String params;

    @Expose
    @SerializedName("createdAt")
    private Date createdAt;

    @Expose
    @SerializedName("updatedAt")
    private Date updatedAt;

    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("metaname")
    private String metaname;

    @Expose
    @SerializedName("status")
    private String status;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTaskstatus() {
        return taskstatus;
    }

    public void setTaskstatus(String taskstatus) {
        this.taskstatus = taskstatus;
    }

    public String getActionDef() {
        return actionDef;
    }

    public void setActionDef(String actionDef) {
        this.actionDef = actionDef;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetaname() {
        return metaname;
    }

    public void setMetaname(String metaname) {
        this.metaname = metaname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
