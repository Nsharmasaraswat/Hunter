package com.gtp.hunter.wms.model;

import com.google.gson.annotations.SerializedName;

public class Validate {

    @SerializedName("token")
    private String token;

    @SerializedName("userid")
    private String userId;

    @SerializedName("name")
    private String userName;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
