package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AGLDocItem extends AGLBaseDoc {
    @Expose
    @SerializedName("product_id")
    private String product_id;

    @Expose
    @SerializedName("qty")
    private double qty;

    @Expose
    @SerializedName("layer")
    private int layer;

    @Expose
    @SerializedName("unit_measure")
    private String measureUnit;

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public String getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(String measureUnit) {
        this.measureUnit = measureUnit;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }
}
