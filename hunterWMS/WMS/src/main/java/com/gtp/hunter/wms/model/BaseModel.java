package com.gtp.hunter.wms.model;

import com.google.gson.GsonBuilder;

public abstract class BaseModel {

    @Override
    public String toString() {
        return new GsonBuilder().serializeNulls().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
    }
}
