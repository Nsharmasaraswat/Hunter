package com.gtp.hunter.wms.model;

import com.google.gson.annotations.SerializedName;

public class Auth {
    @SerializedName("id")
    private String id;

    @SerializedName("credential")
    private String credential;

    public Auth(String id, String credential) {
        this.id = id;
        this.credential = credential;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }
}
