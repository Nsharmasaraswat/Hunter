package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationPayload extends RawdataPayload {

    @Expose
    @SerializedName("x")
    private Double x;

    @Expose
    @SerializedName("y")
    private Double y;

    @Expose
    @SerializedName("z")
    private Double z;

    @Expose
    @SerializedName("nearby")
    private String nearby;

    @Expose
    @SerializedName("nearby-name")
    private String nearbyName;

    @Expose
    @SerializedName("value")
    private Double value;

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getZ() {
        return z;
    }

    public void setZ(Double z) {
        this.z = z;
    }

    public String getNearby() {
        return nearby;
    }

    public void setNearby(String nearby) {
        this.nearby = nearby;
    }

    public String getNearbyName() {
        return nearbyName;
    }

    public void setNearbyName(String nearbyName) {
        this.nearbyName = nearbyName;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
