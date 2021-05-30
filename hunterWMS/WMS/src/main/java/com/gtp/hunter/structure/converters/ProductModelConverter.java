package com.gtp.hunter.structure.converters;

import androidx.room.TypeConverter;

import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.wms.model.ProductModel;

import java.util.UUID;

public class ProductModelConverter {
    @TypeConverter
    public static ProductModel fromString(String value) {
        return value == null ? null : HunterMobileWMS.getDB().pmDao().findById(UUID.fromString(value));
    }

    @TypeConverter
    public static String amToString(ProductModel model) {
        return model == null ? null : model.getId().toString();
    }
}
