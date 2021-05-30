package com.gtp.hunter.wms.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.util.ThingUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import timber.log.Timber;

public class AGLThing {

    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("createdAt")
    private Date createdAt;

    @Expose
    @SerializedName("updatedAt")
    private Date updatedAt;

    @Expose
    @SerializedName("status")
    private String status;

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("metaname")
    private String metaname;

    @Expose
    @SerializedName("created_at")
    private String created_at;

    @Expose
    @SerializedName("updated_at")
    private String updated_at;

    @Expose
    @SerializedName("user_id")
    private String user_id;

    @Expose
    @SerializedName("parent_id")
    private String parent_id;

    @Expose
    @SerializedName("product_id")
    private String product_id;

    @Expose
    @SerializedName("address_id")
    private String address_id;

    @Expose
    @SerializedName("siblings")
    private Set<AGLThing> siblings = new HashSet<>();

    @Expose
    @SerializedName("props")
    private Map<String, String> props = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetaname() {
        return metaname;
    }

    public void setMetaname(String metaname) {
        this.metaname = metaname;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public Set<AGLThing> getSiblings() {
        return siblings;
    }

    public void setSiblings(Set<AGLThing> siblings) {
        this.siblings = siblings;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public Map<String, String> getProps() {
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    public String getAddress_id() {
        return address_id;
    }

    public void setAddress_id(String address_id) {
        this.address_id = address_id;
    }

    public Thing getThing() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Thing t = new Thing();
        Product p = HunterMobileWMS.findProduct(UUID.fromString(product_id));
        Address a = address_id == null ? null : HunterMobileWMS.findAddress(UUID.fromString(address_id));

        t.setId(UUID.fromString(id));
        t.setName(name);
        t.setStatus(status);
        t.setProduct(p);
        t.setAddress(a);
        try {
            t.setCreatedAt(sdf.parse(created_at));
            t.setUpdatedAt(sdf.parse(updated_at));
        } catch (Exception e) {
            Timber.e("Thing Time Parse Error %s", e.getLocalizedMessage());
        }
        for (String key : props.keySet()) {
            if (!key.equals("payload")) {
                BaseModelField bmf = new BaseModelField();
                BaseField tField = new BaseField();

                bmf.setId(ThingUtil.getFieldId(key.toUpperCase()));
                bmf.setMetaname(key.toUpperCase());
                tField.setModelId(ThingUtil.getFieldId(key.toUpperCase()));
                tField.setField(bmf);
                tField.setValue(props.get(key));
                t.getProperties().add(tField);
            } else
                t.setPayload(props.get(key));
        }

        return t;
    }
}
