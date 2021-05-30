package com.gtp.hunter.wms.model;

import androidx.room.Entity;
import androidx.room.Ignore;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Set;

@Entity(tableName = "addressmodel")
public class AddressModel extends UUIDBaseModel {

    @Expose
    @SerializedName("fields")
    @Ignore
    private Set<BaseModelField> fields;
    //TODO: TypeConverterModafocka

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