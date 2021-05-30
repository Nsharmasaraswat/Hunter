package com.gtp.hunter.wms.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ViewThingStub extends BaseModel implements Comparable<ViewThingStub> {

    @Expose
    @SerializedName("thing")
    private Thing thing;

    @Expose
    @SerializedName("address")
    private Address destination;

    @Expose
    @SerializedName("manufacture")
    private Date manufacture;

    @Expose
    @SerializedName("expiry")
    private Date expiry;

    @Expose
    @SerializedName("quantity")
    private Double quantity;

    @Expose
    @SerializedName("tagid")
    private String tagId;

    private transient boolean sent;

    public ViewThingStub(Thing t) {
        this.thing = t;
    }

    @Override
    public int hashCode() {
        return thing.getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ViewThingStub) {
            ViewThingStub other = (ViewThingStub) obj;

            return this.thing.getId().equals(other.getThing().getId());
        }
        return false;
    }

    @Override
    public int compareTo(@NonNull ViewThingStub o) {
        if (this.equals(o))
            return 0;
        else
            return this.hashCode() > o.hashCode() ? -1 : 1;
    }

    public Date getManufacture() {
        return manufacture;
    }

    public void setManufacture(Date manufacture) {
        this.manufacture = manufacture;
    }

    public Date getExpiry() {
        return expiry;
    }

    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }

    public Thing getThing() {
        return thing;
    }

    public void setThing(Thing thing) {
        this.thing = thing;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}