package com.gtp.hunter.wms.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.TypeConverters;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.structure.converters.UUIDConverter;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity(tableName = "productmodel")
public class ProductModel extends UUIDBaseModel {

    @Expose
    @SerializedName("parent_id")
    @ColumnInfo(name = "parent_id")
    @TypeConverters(value = UUIDConverter.class)
    private UUID parent_id;

    @Expose
    @SerializedName("fields")
    @Ignore
    private Set<BaseModelField> fields;
    //TODO: TypeConverterModafocka

    @Expose(serialize = false)
    @SerializedName("propertymodel")
    @Ignore
    private ThingModel propertymodel;

    @Expose(serialize = false)
    @SerializedName("properties")
    @Ignore
    private Map<String, String> properties;

    public UUID getParent_id() {
        return parent_id;
    }

    public void setParent_id(UUID parent_id) {
        this.parent_id = parent_id;
    }

    public Set<BaseModelField> getFields() {
        return fields;
    }

    public void setFields(Set<BaseModelField> fields) {
        this.fields = fields;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (!(other instanceof AddressModel)) return false;
        if (other == this) return true;
        return getId().equals(((AddressModel) other).getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}