package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AGLDocumentProps extends AGLBaseDoc implements Serializable {

    @Expose
    @SerializedName("code")
    String code;

    @Expose
    @SerializedName("parent_id")
    String parent_id;

    @Expose
    @SerializedName("items")
    List<AGLDocItem> items = new ArrayList<>();

    @Expose
    @SerializedName("siblings")
    List<AGLDocumentProps> siblings = new ArrayList<>();

    @Expose
    @SerializedName("props")
    List<AGLDocModel> model = new ArrayList<>();

    @Expose
    @SerializedName("things")
    List<AGLThing> things = new ArrayList<>();

    public List<AGLThing> getThings() {
        return things;
    }

    public void setThings(List<AGLThing> things) {
        this.things = things;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public List<AGLDocItem> getItems() {
        return items;
    }

    public void setItems(List<AGLDocItem> items) {
        this.items = items;
    }

    public List<AGLDocumentProps> getSiblings() {
        return siblings;
    }

    public void setSiblings(List<AGLDocumentProps> siblings) {
        this.siblings = siblings;
    }

    public List<AGLDocModel> getModel() {
        return model;
    }

    public void setModel(List<AGLDocModel> model) {
        this.model = model;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
