package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class Permission extends BaseModel implements Serializable {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("metaname")
    @Expose
    private String metaname;

    @SerializedName("createdAt")
    @Expose
    private Date createdAt;

    @SerializedName("updatedAt")
    @Expose
    private Date updatedAt;

    @SerializedName("route")
    @Expose
    private String route;

    @SerializedName("params")
    @Expose
    private String params;

    @SerializedName("icon")
    @Expose
    private String icon;

    @SerializedName("app")
    @Expose
    private String app;

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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

}
