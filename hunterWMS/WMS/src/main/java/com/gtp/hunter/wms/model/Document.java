package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class Document extends UUIDBaseModel implements Serializable {

    @Expose
    @SerializedName("model")
    private DocumentModel model;

    @Expose
    @SerializedName("items")
    private Set<DocumentItem> items = new ConcurrentSkipListSet<>();

    @Expose
    @SerializedName("code")
    private String code;

    @Expose
    @SerializedName("parent_id")
    private String parent_id;

    @Expose
    @SerializedName("fields")
    private Set<DocumentField> fields = new HashSet<>();

    @Expose
    @SerializedName("things")
    private Set<DocumentThing> things = new HashSet<>();

    @Expose
    @SerializedName("transport")
    private List<DocumentTransport> transports = new ArrayList<>();

    @Expose
    @SerializedName("siblings")
    private List<Document> siblings = new ArrayList<>();

    @Expose
    @SerializedName("user")
    private User user;

    public Set<DocumentThing> getThings() {
        return things;
    }

    public void setThings(Set<DocumentThing> things) {
        this.things = things;
    }

    public List<DocumentTransport> getTransports() {
        return transports;
    }

    public void setTransports(List<DocumentTransport> transports) {
        this.transports = transports;
    }

    public Set<DocumentField> getFields() { return fields; }

    public void setFields(Set<DocumentField> fields) {
        this.fields = fields;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public DocumentModel getModel() {
        return model;
    }

    public void setModel(DocumentModel model) {
        this.model = model;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Set<DocumentItem> getItems() {
        return items;
    }

    public void setItems(Set<DocumentItem> items) {
        this.items = items;
    }

    public List<Document> getSiblings() {
        return siblings;
    }

    public void setSiblings(List<Document> siblings) {
        this.siblings = siblings;
    }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }
}
