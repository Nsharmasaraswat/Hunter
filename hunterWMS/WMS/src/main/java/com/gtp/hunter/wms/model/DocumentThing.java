package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DocumentThing extends UUIDBaseModel {
    @Expose
    @SerializedName("thing")
    private Thing thing;

    private Document document;

    public Thing getThing() {
        return thing;
    }

    public void setThing(Thing thing) {
        this.thing = thing;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
