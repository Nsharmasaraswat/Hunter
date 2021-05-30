package com.gtp.hunter.wms.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class ViewFinishedProductStub extends BaseModel implements Comparable<ViewFinishedProductStub> {

    @Expose
    @SerializedName("address")
    private String addressName;

    @Expose
    @SerializedName("product")
    private String productName;

    @Expose
    @SerializedName("pallets")
    private int pallets;

    @Expose
    @SerializedName("boxes")
    private double boxes;

    private transient boolean error;

    private Address address;
    private Product product;

    public ViewFinishedProductStub(Address a, @NonNull Product p, int palCount, double boxCount) {
        this.address = a;
        this.addressName = a == null ? "" : a.getName();
        this.product = p;
        this.productName = p.getSku() + " - " + p.getName();
        this.pallets = palCount;
        this.boxes = boxCount;
        this.error = false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, product, pallets, boxes);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ViewFinishedProductStub)) {
            return false;
        }
        ViewFinishedProductStub stub = (ViewFinishedProductStub) o;
        return pallets == stub.pallets &&
                boxes == stub.boxes &&
                Objects.equals(address, stub.address) &&
                Objects.equals(product, stub.product);
    }

    @Override
    public int compareTo(@NonNull ViewFinishedProductStub o) {
        if (this.equals(o)) {
            return 0;
        } else {
            if (this.address == null && o.address == null) return 0;
            if (this.address == null || this.address.getMetaname() == null) return 1;
            if (o.address == null || o.address.getMetaname() == null) return -1;
            if (this.address != null && o.address != null && this.address.getMetaname().equals(o.address.getMetaname()))
                return this.productName.compareTo(o.productName);
            assert this.address != null;//burro, nao pode ser nulo
            return this.address.getMetaname().compareTo(o.address.getMetaname());
        }
    }

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getPallets() {
        return pallets;
    }

    public void setPallets(int pallets) {
        this.pallets = pallets;
    }

    public double getBoxes() {
        return boxes;
    }

    public void setBoxes(double boxes) {
        this.boxes = boxes;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isError() {
        return this.error;
    }
}