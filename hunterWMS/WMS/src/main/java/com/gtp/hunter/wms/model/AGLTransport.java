package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AGLTransport implements Comparable<AGLTransport> {

    @Expose
    @SerializedName("thing_id")
    private String thing_id;

    @Expose
    @SerializedName("address_id")
    private String address_id;

    @Expose
    @SerializedName("origin_id")
    private String origin_id;

    @Expose
    @SerializedName("parent_id")
    private String parent_id;

    @Expose
    @SerializedName("seq")
    private int seq;

    public String getThing_id() {
        return thing_id;
    }

    public void setThing_id(String thing_id) {
        this.thing_id = thing_id;
    }

    public String getAddress_id() {
        return address_id;
    }

    public void setAddress_id(String address_id) {
        this.address_id = address_id;
    }

    public String getOrigin_id() {
        return origin_id;
    }

    public void setOrigin_id(String origin_id) {
        this.origin_id = origin_id;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    @Override
    public int compareTo(AGLTransport dtr1) {
        return getSeq() - dtr1.getSeq();
    }
}
