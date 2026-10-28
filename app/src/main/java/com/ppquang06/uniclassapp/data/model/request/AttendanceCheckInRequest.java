package com.ppquang06.uniclassapp.data.model.request;

import com.google.gson.annotations.SerializedName;

public class AttendanceCheckInRequest {

    @SerializedName("sessionId")
    private String sessionId;

    @SerializedName("wifiBssid")
    private String wifiBssid;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("deviceId")
    private String deviceId;

    public AttendanceCheckInRequest() {}

    public AttendanceCheckInRequest(String sessionId, String wifiBssid, Double latitude, Double longitude, String deviceId) {
        this.sessionId = sessionId;
        this.wifiBssid = wifiBssid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.deviceId = deviceId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getWifiBssid() {
        return wifiBssid;
    }

    public void setWifiBssid(String wifiBssid) {
        this.wifiBssid = wifiBssid;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
