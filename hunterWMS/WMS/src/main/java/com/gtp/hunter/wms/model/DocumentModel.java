package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.Set;

public class DocumentModel extends UUIDBaseModel {
    @Expose
    @SerializedName("fields")
    private Set<DocumentModelField> fields = new HashSet<>();

    public Set<DocumentModelField> getFields() {
        return fields;
    }

    public void setFields(Set<DocumentModelField> fields) {
        this.fields = fields;
    }
}
