package com.gtp.hunter.wms.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ViewPalletStub extends BaseModel implements Comparable<ViewPalletStub> {

    @Expose
    @SerializedName("thing")
    private Thing thing;

    @Expose
    @SerializedName("quantity")
    private Double quantity;

    @Expose
    @SerializedName("manufacture")
    private String manfuacture;

    @Expose
    @SerializedName("expiry")
    private String expiry;

    public ViewPalletStub(Thing t) {
        this.thing = t;
    }

    @Override
    public int hashCode() {
        return thing.getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ViewPalletStub) {
            ViewPalletStub other = (ViewPalletStub) obj;

            return this.thing.getId().equals(other.getThing().getId());
        }
        return false;
    }

    @Override
    public int compareTo(@NonNull ViewPalletStub o) {
        if (this.equals(o)) {
            return 0;
        } else if (this.thing != null && o.thing != null) {
            Address a1 = this.thing.getAddress();
            Address a2 = o.thing.getAddress();

            if (a1 != null && a2 != null) {
                if (a1.getMetaname() == null) {
                    return 1;
                }
                if (a2.getMetaname() == null) {
                    return -1;
                }
                return a1.getMetaname().compareTo(a2.getMetaname());
            }
        }
        return this.hashCode() > o.hashCode() ? -1 : 1;
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

    public String getManfuacture() {
        return manfuacture;
    }

    public void setManfuacture(String manfuacture) {
        this.manfuacture = manfuacture;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }
}