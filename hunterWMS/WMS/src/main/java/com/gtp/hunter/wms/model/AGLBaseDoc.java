package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AGLBaseDoc extends BaseModel {

    @Expose
    @SerializedName("id")
    protected String id;

    @Expose
    @SerializedName("name")
    protected String name;

    @Expose
    @SerializedName("metaname")
    protected String metaname;

    @Expose
    @SerializedName("status")
    protected String status;

    @Expose
    @SerializedName("created_at")
    protected String createdAtSQL;

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

    public String getCreatedAtSQL() {
        return createdAtSQL;
    }

    public void setCreatedAtSQL(String createdAtSQL) {
        this.createdAtSQL = createdAtSQL;
    }

    public String getUpdatedAtSQL() {
        return updatedAtSQL;
    }

    public void setUpdatedAtSQL(String updatedAtSQL) {
        this.updatedAtSQL = updatedAtSQL;
    }

    @Expose
    @SerializedName("updated_at")
    protected String updatedAtSQL;
}
