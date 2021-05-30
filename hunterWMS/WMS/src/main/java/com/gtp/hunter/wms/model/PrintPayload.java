package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrintPayload {
    @Expose
    private UUID thing;
    @Expose
    private UUID product;
    @Expose
    private UUID document;
    @Expose
    private String sku;
    @Expose
    private Map<String, String> properties = new HashMap<>();

    public UUID getThing() {
        return thing;
    }

    public void setThing(UUID thing) {
        this.thing = thing;
    }

    public UUID getProduct() {
        return product;
    }

    public void setProduct(UUID product) {
        this.product = product;
    }

    public UUID getDocument() {
        return document;
    }

    public void setDocument(UUID document) {
        this.document = document;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
