package com.gtp.hunter.wms.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.structure.converters.DateConverter;
import com.gtp.hunter.structure.converters.UUIDConverter;

import java.util.Date;
import java.util.UUID;

abstract class UUIDBaseModel extends BaseModel {

    @Expose
    @NonNull
    @PrimaryKey
    @SerializedName("id")
    @ColumnInfo(name = "id")
    @TypeConverters(UUIDConverter.class)
    private UUID id;

    @Expose
    @SerializedName("name")
    @ColumnInfo(name = "name")
    private String name;

    @Expose
    @SerializedName("metaname")
    @ColumnInfo(name = "metaname")
    private String metaname;

    @Expose
    @SerializedName("status")
    @ColumnInfo(name = "status")
    private String status;

    @Expose
    @SerializedName("createdAt")
    @ColumnInfo(name = "createdAt")
    @TypeConverters(DateConverter.class)
    private Date createdAt;

    @Expose
    @SerializedName("updatedAt")
    @ColumnInfo(name = "updatedAt")
    @TypeConverters(DateConverter.class)
    private Date updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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
}
