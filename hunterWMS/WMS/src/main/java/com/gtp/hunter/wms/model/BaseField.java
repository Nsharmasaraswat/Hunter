package com.gtp.hunter.wms.model;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import androidx.room.TypeConverters;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.structure.converters.UUIDConverter;

import java.util.UUID;

public class BaseField extends UUIDBaseModel {

    @Expose
    @SerializedName("modelfield_id")
    @ColumnInfo(name = "modelfield_id")
    @TypeConverters(UUIDConverter.class)
    private UUID modelId;

    @Expose
    @SerializedName("value")
    @ColumnInfo(name = "value")
    private String value;

    @Expose
    @Ignore
    @SerializedName("field")
    private BaseModelField field;

    public UUID getModelId() {
        return modelId;
    }

    public void setModelId(UUID modelId) {
        this.modelId = modelId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public BaseModelField getField() {
        return field;
    }

    public void setField(BaseModelField field) {
        this.field = field;
        if(field != null)
            this.modelId = field.getId();
    }
}
