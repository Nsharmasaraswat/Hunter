package com.gtpautomation.driver.data_models;

import com.google.gson.annotations.SerializedName;

public class DriverSession {

    @SerializedName("appointment")
    private Appointment appointment;

    @SerializedName("sessionId")
    private String sessionId;

    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("user")
    private User user;

    public Appointment getAppointment() {
        return appointment;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return
                "DriverSession{" +
                        "appointment = '" + appointment + '\'' +
                        ",sessionId = '" + sessionId + '\'' +
                        ",accessToken = '" + accessToken + '\'' +
                        ",user = '" + user + '\'' +
                        "}";
    }
}