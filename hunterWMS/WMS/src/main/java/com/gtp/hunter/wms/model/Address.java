package com.gtp.hunter.wms.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.TypeConverters;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.structure.converters.AddressModelConverter;
import com.gtp.hunter.structure.converters.UUIDConverter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity(tableName = "address")
@ForeignKey(entity = AddressModel.class, parentColumns = "id", childColumns = "addressmodel_id")
public class Address extends UUIDBaseModel {

    @Expose
    @Ignore
    @SerializedName("fields")
    private Set<AddressField> fields = new HashSet<>();

    @Expose
    @SerializedName("parent_id")
    @ColumnInfo(name = "parent_id")
    @TypeConverters(value = UUIDConverter.class)
    private UUID parent_id;

    @Expose
    @SerializedName("model")
    @ColumnInfo(name = "addressmodel_id")
    @TypeConverters(value = AddressModelConverter.class)
    private AddressModel model;

    @Expose
    @SerializedName("wkt")
    @ColumnInfo(name = "wkt")
    private String wkt;

    public Set<AddressField> getFields() {
        return fields;
    }

    public void setFields(Set<AddressField> fields) {
        this.fields = fields;
    }

    public UUID getParent_id() {
        return parent_id;
    }

    public void setParent_id(UUID parent_id) {
        this.parent_id = parent_id;
    }

    public AddressModel getModel() {
        return model;
    }

    public void setModel(AddressModel model) {
        this.model = model;
    }

    public String getWkt() {
        return wkt;
    }

    public void setWkt(String wkt) {
        this.wkt = wkt;
    }

    @NonNull
    @Override
    public String toString() {
        return getName() == null ? super.toString() : getName();
    }
}