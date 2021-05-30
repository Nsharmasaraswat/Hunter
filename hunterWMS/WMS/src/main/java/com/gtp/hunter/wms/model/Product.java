package com.gtp.hunter.wms.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.TypeConverters;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.structure.converters.ProductModelConverter;
import com.gtp.hunter.structure.converters.UUIDConverter;
import com.gtp.hunter.util.ProductUtil;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Entity(tableName = "product")
@ForeignKey(entity = ProductModel.class, parentColumns = "id", childColumns = "productmodel_id")
public class Product extends UUIDBaseModel implements Serializable {

    @Expose
    @SerializedName("sku")
    @ColumnInfo(name = "sku")
    private String sku;

    @Expose
    @Ignore
    @SerializedName("fields")
    private Set<ProductField> fields = new HashSet<>();

    @Expose
    @SerializedName("parent_id")
    @ColumnInfo(name = "parent_id")
    @TypeConverters(value = UUIDConverter.class)
    private UUID parent_id;

    @Expose
    @SerializedName("model")
    @ColumnInfo(name = "productmodel_id")
    @TypeConverters(value = ProductModelConverter.class)
    private ProductModel model;

    public ProductModel getModel() {
        return model;
    }

    public void setModel(ProductModel model) {
        this.model = model;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Set<ProductField> getFields() {
        return fields;
    }

    public void setFields(Set<ProductField> fields) {
        this.fields = fields;
    }

    public UUID getParent_id() {
        return parent_id;
    }

    public void setParent_id(UUID parent_id) {
        this.parent_id = parent_id;
    }

    @NonNull
    @Override
    public String toString() {
        ProductField pf = ProductUtil.getBoxUnit(this);

        return getSku() == null && getName() == null ? super.toString() : (getSku().isEmpty() ? getName() : getSku() + " - " + getName() + (pf == null || pf.getValue().isEmpty() ? "" : String.format(Locale.US, " - C%02d", Integer.parseInt(pf.getValue()))));
    }
}
