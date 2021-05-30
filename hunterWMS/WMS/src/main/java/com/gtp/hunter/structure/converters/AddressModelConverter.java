package com.gtp.hunter.structure.converters;

import androidx.room.TypeConverter;

import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.wms.model.AddressModel;

import java.util.UUID;

public class AddressModelConverter {
    @TypeConverter
    public static AddressModel fromString(String value) {
        return value == null ? null : HunterMobileWMS.getDB().amDao().findById(UUID.fromString(value));
    }

    @TypeConverter
    public static String amToString(AddressModel model) {
        return model == null ? null : model.getId().toString();
    }
}
