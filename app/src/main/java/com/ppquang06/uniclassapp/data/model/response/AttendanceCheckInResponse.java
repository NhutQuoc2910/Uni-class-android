package com.ppquang06.uniclassapp.data.model.response;

import com.google.gson.annotations.SerializedName;

public class AttendanceCheckInResponse {

    @SerializedName("recordId")
    private String recordId;

    @SerializedName("sessionId")
    private String sessionId;

    @SerializedName("classCode")
    private String classCode;

    @SerializedName("className")
    private String className;

    @SerializedName("room")
    private String room;

    @SerializedName("lecturerName")
    private String lecturerName;

    @SerializedName("scannedAt")
    private String scannedAt;

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("status")
    private String status;

    @SerializedName("qrVerified")
    private boolean qrVerified;

    @SerializedName("wifiVerified")
    private boolean wifiVerified;

    @SerializedName("gpsVerified")
    private boolean gpsVerified;

    @SerializedName("distanceMeters")
    private double distanceMeters;

    public AttendanceCheckInResponse() {}

    public AttendanceCheckInResponse(String recordId, String sessionId, String classCode,
                                   String className, String room, String lecturerName,
                                   String scannedAt, String deviceId, String status,
                                   boolean qrVerified, boolean wifiVerified, boolean gpsVerified,
                                   double distanceMeters) {
        this.recordId = recordId;
        this.sessionId = sessionId;
        this.classCode = classCode;
        this.className = className;
        this.room = room;
        this.lecturerName = lecturerName;
        this.scannedAt = scannedAt;
        this.deviceId = deviceId;
        this.status = status;
        this.qrVerified = qrVerified;
        this.wifiVerified = wifiVerified;
        this.gpsVerified = gpsVerified;
        this.distanceMeters = distanceMeters;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }

    public String getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(String scannedAt) {
        this.scannedAt = scannedAt;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isQrVerified() {
        return qrVerified;
    }

    public void setQrVerified(boolean qrVerified) {
        this.qrVerified = qrVerified;
    }

    public boolean isWifiVerified() {
        return wifiVerified;
    }

    public void setWifiVerified(boolean wifiVerified) {
        this.wifiVerified = wifiVerified;
    }

    public boolean isGpsVerified() {
        return gpsVerified;
    }

    public void setGpsVerified(boolean gpsVerified) {
        this.gpsVerified = gpsVerified;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }
}
