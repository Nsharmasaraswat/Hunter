package com.gtp.hunter.wms.model;

import android.view.View;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.gtp.hunter.structure.converters.UUIDConverter;

import java.util.UUID;

@Entity(tableName = "tmpchecking")
public class CheckingItem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "document_id")
    @TypeConverters(UUIDConverter.class)
    private UUID document_id;

    @ColumnInfo(name = "thing_id")
    @TypeConverters(UUIDConverter.class)
    private UUID thing_id;

    @ColumnInfo(name = "product_id")
    @TypeConverters(UUIDConverter.class)
    private UUID product_id;

    @ColumnInfo(name = "address_id")
    @TypeConverters(UUIDConverter.class)
    private UUID address_id;

    @ColumnInfo(name = "quantity")
    private Double quantity;

    @ColumnInfo(name = "lot_id")
    private String lot_id;

    @ColumnInfo(name = "lot_exp")
    private String lot_exp;

    @ColumnInfo(name = "lot_mfg")
    private String lot_mfg;

    @Ignore
    private View view;

    @Ignore
    private Thing t;

    @Ignore
    private boolean wrong;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getDocument_id() {
        return document_id;
    }

    public void setDocument_id(UUID document_id) {
        this.document_id = document_id;
    }

    public UUID getThing_id() {
        return thing_id;
    }

    public void setThing_id(UUID thing_id) {
        this.thing_id = thing_id;
    }

    public UUID getProduct_id() {
        return product_id;
    }

    public void setProduct_id(UUID product_id) {
        this.product_id = product_id;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getLot_id() {
        return lot_id;
    }

    public void setLot_id(String lot_id) {
        this.lot_id = lot_id;
    }

    public String getLot_exp() {
        return lot_exp;
    }

    public void setLot_exp(String lot_exp) {
        this.lot_exp = lot_exp;
    }

    public String getLot_mfg() {
        return lot_mfg;
    }

    public void setLot_mfg(String lot_mfg) {
        this.lot_mfg = lot_mfg;
    }

    public UUID getAddress_id() {
        return address_id;
    }

    public void setAddress_id(UUID address_id) {
        this.address_id = address_id;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public Thing getThing() {
        return t;
    }

    public void setThing(Thing t) {
        this.t = t;
        this.thing_id = t.getId();
    }

    public boolean isWrong() {
        return wrong;
    }

    public void setWrong(boolean wrong) {
        this.wrong = wrong;
    }
}
