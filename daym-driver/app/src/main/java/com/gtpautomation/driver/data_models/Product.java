package com.gtpautomation.driver.data_models;

import com.google.gson.annotations.SerializedName;

public class Product {

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("productCode")
    private String productCode;

    @SerializedName("__v")
    private int V;

    @SerializedName("name")
    private String name;

    @SerializedName("_id")
    private String id;

    @SerializedName("productType")
    private String productType;

    @SerializedName("barCode")
    private String barCode;

    @SerializedName("status")
    private int status;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getCreatedAt() {
        return createdAt;
    }

    public String getProductCode() {
        return productCode;
    }

    public int getV() {
        return V;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getProductType() {
        return productType;
    }

    public String getBarCode() {
        return barCode;
    }

    public int getStatus() {
        return status;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return
                "Product{" +
                        "createdAt = '" + createdAt + '\'' +
                        ",productCode = '" + productCode + '\'' +
                        ",__v = '" + V + '\'' +
                        ",name = '" + name + '\'' +
                        ",_id = '" + id + '\'' +
                        ",productType = '" + productType + '\'' +
                        ",barCode = '" + barCode + '\'' +
                        ",status = '" + status + '\'' +
                        ",updatedAt = '" + updatedAt + '\'' +
                        "}";
    }
}