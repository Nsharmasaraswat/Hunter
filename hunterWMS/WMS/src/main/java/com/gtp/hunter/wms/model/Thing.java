package com.gtp.hunter.wms.model;

import androidx.room.Ignore;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class Thing extends UUIDBaseModel {
    @Expose
    @SerializedName("model")
    private ThingModel model;

    @Expose
    @SerializedName("product")
    private Product product;

    @Expose
    @SerializedName("address")
    private Address address;

    @Expose
    @SerializedName("properties")
    private Set<BaseField> properties = new HashSet<>();

    @Expose
    @SerializedName("unitModel")
    private Set<Unit> units = new HashSet<>();

    @Expose
    @SerializedName("siblings")
    private Set<Thing> siblings = new HashSet<>();

    @Expose
    @Ignore
    @SerializedName("payload")
    private String payload;

    @Expose
    @Ignore
    @SerializedName("product_id")
    private UUID product_id;

    private boolean error;

    public ThingModel getModel() {
        return model;
    }

    public void setModel(ThingModel model) {
        this.model = model;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Set<BaseField> getProperties() {
        return properties;
    }

    public void setProperties(Set<BaseField> properties) {
        this.properties = properties;
    }

    public Set<Unit> getUnits() {
        return units;
    }

    public void setUnits(Set<Unit> units) {
        this.units = units;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Set<Thing> getSiblings() {
        return siblings;
    }

    public void setSiblings(Set<Thing> siblings) {
        this.siblings = siblings;
    }

    public String getPayload() {
        return this.payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public UUID getProduct_id() {
        return product_id;
    }

    public void setProduct_id(UUID product_id) {
        this.product_id = product_id;
    }

    public AGLThing getAGLThing() {
        final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        AGLThing at = new AGLThing();

        if (getId() != null)
            at.setId(getId().toString());
        at.setName(getName());
        at.setMetaname(getMetaname());
        at.setStatus(getStatus());
        at.setCreated_at(SDF.format(getCreatedAt()));
        at.setUpdated_at(SDF.format(getUpdatedAt()));
        if (getProduct() != null && getProduct().getId() != null) {
            at.setProduct_id(getProduct().getId().toString());
        }
        if (getAddress() != null && getAddress().getId() != null) {
            at.setAddress_id(getAddress().getId().toString());
        }
        for (BaseField bf : getProperties()) {
            BaseModelField bmf = bf.getField();

            at.getProps().put(bmf.getMetaname().toLowerCase(), bf.getValue());
        }
        for (Thing t : getSiblings()) {
            AGLThing ats = t.getAGLThing();

            ats.setParent_id(at.getId());
            at.getSiblings().add(ats);
        }
        return at;
    }
}
