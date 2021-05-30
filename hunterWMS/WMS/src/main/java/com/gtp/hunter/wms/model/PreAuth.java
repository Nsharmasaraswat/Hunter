package com.gtp.hunter.wms.model;

import com.google.gson.annotations.SerializedName;

public class PreAuth {
    @SerializedName("id")
    private String userId;

    @SerializedName("type")
    private String type;

    @SerializedName("salt")
    private String salt;

    @SerializedName("session")
    private String session;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }
}
