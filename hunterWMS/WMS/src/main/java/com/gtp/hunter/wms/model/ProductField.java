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

@Entity(tableName = "productfield",
        foreignKeys = @ForeignKey(entity = Product.class,
        parentColumns = "id",
        childColumns = "product_id",
        onDelete = CASCADE),
        indices = {@Index(value = "product_id"), @Index(value = "modelfield_id")})
public class ProductField extends BaseField {

    @Expose
    @SerializedName("product_id")
    @ColumnInfo(name = "product_id")
    @TypeConverters(UUIDConverter.class)
    private UUID product_id;

    public UUID getProduct_id() {
        return product_id;
    }

    public void setProduct_id(UUID product_id) {
        this.product_id = product_id;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (!(other instanceof ProductField)) return false;
        if (other == this) return true;
        ProductField otherField = (ProductField) other;
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
