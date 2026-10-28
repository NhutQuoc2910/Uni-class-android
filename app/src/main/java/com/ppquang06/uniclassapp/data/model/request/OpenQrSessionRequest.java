package com.ppquang06.uniclassapp.data.model.request;

import com.google.gson.annotations.SerializedName;

public class OpenQrSessionRequest {
    @SerializedName("wifiBssid")
    private String wifiBssid;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("radiusMeters")
    private Double radiusMeters;

    public OpenQrSessionRequest() {}

    public OpenQrSessionRequest(String wifiBssid, Double latitude, Double longitude, Double radiusMeters) {
        this.wifiBssid = wifiBssid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;
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

    public Double getRadiusMeters() {
        return radiusMeters;
    }

    public void setRadiusMeters(Double radiusMeters) {
        this.radiusMeters = radiusMeters;
    }
}
