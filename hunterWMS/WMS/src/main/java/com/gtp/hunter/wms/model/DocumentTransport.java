package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DocumentTransport extends UUIDBaseModel implements Comparable<DocumentTransport> {

    @Expose
    @SerializedName("thing")
    private Thing thing;

    @Expose
    @SerializedName("address")
    private Address address;

    @Expose
    @SerializedName("origin")
    private Address origin;

    @Expose
    @SerializedName("seq")
    private int seq;

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Thing getThing() {
        return thing;
    }

    public void setThing(Thing thing) {
        this.thing = thing;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Address getOrigin() { return origin; }

    public void setOrigin(Address origin) { this.origin = origin; }

    @Override
    public int compareTo(DocumentTransport dtr1) {
        return getSeq() - dtr1.getSeq();
    }
}
