package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AGLDocument extends AGLBaseDoc implements Serializable {

    @Expose
    @SerializedName("code")
    private String code;

    @Expose
    @SerializedName("parent_id")
    private String parent_id;

    @Expose
    @SerializedName("user_id")
    private String user_id;

    @Expose
    @SerializedName("items")
    private List<AGLDocItem> items = new ArrayList<>();

    @Expose
    @SerializedName("siblings")
    private List<AGLDocument> siblings = new ArrayList<>();

    @Expose
    @SerializedName("model")
    private List<AGLDocModel> model = new ArrayList<>();

    @Expose
    @SerializedName("props")
    private Map<String, String> props = new HashMap<>();

    @Expose
    @SerializedName("things")
    private List<AGLThing> things = new ArrayList<>();

    @Expose
    @SerializedName("addresses")
    private Set<AGLAddress> addresses = new HashSet<>();

    @Expose
    @SerializedName("transport")
    private List<AGLTransport> transports = new ArrayList<>();

    public List<AGLThing> getThings() {
        return things;
    }

    public void setThings(List<AGLThing> things) {
        this.things = things;
    }

    public Map<String, String> getProps() {
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public List<AGLDocItem> getItems() {
        return items;
    }

    public void setItems(List<AGLDocItem> items) {
        this.items = items;
    }

    public List<AGLDocument> getSiblings() {
        return siblings;
    }

    public void setSiblings(List<AGLDocument> siblings) {
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

    public Set<AGLAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<AGLAddress> addresses) {
        this.addresses = addresses;
    }

    public List<AGLTransport> getTransports() {
        return transports;
    }

    public void setTransports(List<AGLTransport> transports) {
        this.transports = transports;
    }
}
