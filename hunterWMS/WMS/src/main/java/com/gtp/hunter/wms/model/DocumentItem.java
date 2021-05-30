package com.gtp.hunter.wms.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.HunterMobileWMS;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DocumentItem extends UUIDBaseModel implements Comparable {
    @Expose
    @SerializedName("product")
    private Product product;

    @Expose
    @SerializedName("qty")
    private double qty;

    @Expose
    @SerializedName("layer")
    private int layer;

    @Expose
    @SerializedName("measureUnit")
    private String measureUnit;

    @Expose
    @SerializedName("properties")
    private Map<String, String> props = new HashMap<>();

    @Expose
    @SerializedName("product_id")
    private String product_id;

    public Map<String, String> getProps() {
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
        if (product_id != null)
            this.product = HunterMobileWMS.findProduct(UUID.fromString(product_id));
    }

    @Override
    public int compareTo(@NonNull Object o) {
        if (o instanceof DocumentItem) {
            DocumentItem di = (DocumentItem) o;

            if (di.getProduct() == null)
                return -1;
            return di.getProduct().getName().compareTo(this.getProduct().getName());
        }
        return -1;
    }
}
