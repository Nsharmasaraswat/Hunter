package com.gtpautomation.securityguard.pojos.order_items;

import android.os.Parcel;
import android.os.Parcelable;

public class Products implements Parcelable {
    int status;
    String productName;
    String productCode;
    String productPrice;
    String warehouse;
    String dock;

    protected Products(Parcel in) {
        status = in.readInt();
        productName = in.readString();
        productCode = in.readString();
        productPrice = in.readString();
        warehouse = in.readString();
        dock = in.readString();
    }

    public static final Creator<Products> CREATOR = new Creator<Products>() {
        @Override
        public Products createFromParcel(Parcel in) {
            return new Products(in);
        }

        @Override
        public Products[] newArray(int size) {
            return new Products[size];
        }
    };

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public String getDock() {
        return dock;
    }

    public void setDock(String dock) {
        this.dock = dock;
    }

    public Products() {
    }

    public Products(int status, String productName, String productCode, String productPrice, String warehouse, String dock) {
        this.status = status;
        this.productName = productName;
        this.productCode = productCode;
        this.productPrice = productPrice;
        this.warehouse = warehouse;
        this.dock = dock;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(status);
        parcel.writeString(productName);
        parcel.writeString(productCode);
        parcel.writeString(productPrice);
        parcel.writeString(warehouse);
        parcel.writeString(dock);
    }
}
