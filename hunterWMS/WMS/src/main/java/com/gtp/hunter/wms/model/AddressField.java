package com.gtp.hunter.wms.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.TypeConverters;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.structure.converters.UUIDConverter;

import java.util.UUID;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "addressfield",
        foreignKeys = @ForeignKey(entity = Address.class,
                parentColumns = "id",
                childColumns = "address_id",
                onDelete = CASCADE),
        indices = {@Index(value = "address_id"), @Index(value = "modelfield_id")})
public class AddressField extends BaseField {

    @Expose
    @SerializedName("address_id")
    @ColumnInfo(name = "address_id")
    @TypeConverters(UUIDConverter.class)
    private UUID address_id;

    public UUID getAddress_id() {
        return address_id;
    }

    public void setAddress_id(UUID address_id) {
        this.address_id = address_id;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (!(other instanceof AddressField)) return false;
        if (other == this) return true;
        AddressField otherField = (AddressField) other;
        boolean sameId = getId().equals(otherField.getId());
        boolean sameModel = getModelId().equals(otherField.getModelId());
        boolean sameValue = getValue().equals(otherField.getValue());

        return sameId && sameModel && sameValue;
    }

    @Override
    public int hashCode() {
        int code = 31;

        code = 17 * code + (getId() == null ? 0 : getId().hashCode());
        code = 17 * code + (getModelId() == null ? 0 : getModelId().hashCode());
        code = 17 * code + (getValue() == null ? 0 : getValue().hashCode());
        return code;
    }
}
