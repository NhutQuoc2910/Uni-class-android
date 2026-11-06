package com.ppquang06.uniclassapp.data.model.request;

import com.google.gson.annotations.SerializedName;

public class CreateScheduleExceptionRequest {

    @SerializedName("classScheduleId")
    private String classScheduleId;

    @SerializedName("originalDate")
    private String originalDate;

    @SerializedName("occurrenceDate")
    private String occurrenceDate;

    @SerializedName("exceptionType")
    private String exceptionType;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("room")
    private String room;

    @SerializedName("reason")
    private String reason;

    @SerializedName("relatedExceptionId")
    private String relatedExceptionId;

    public CreateScheduleExceptionRequest() {
    }

    public CreateScheduleExceptionRequest(String classScheduleId, String originalDate,
                                          String occurrenceDate, String exceptionType,
                                          String startTime, String endTime, String room,
                                          String reason, String relatedExceptionId) {
        this.classScheduleId = classScheduleId;
        this.originalDate = originalDate;
        this.occurrenceDate = occurrenceDate;
        this.exceptionType = exceptionType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.reason = reason;
        this.relatedExceptionId = relatedExceptionId;
    }

    public String getClassScheduleId() {
        return classScheduleId;
    }

    public void setClassScheduleId(String classScheduleId) {
        this.classScheduleId = classScheduleId;
    }

    public String getOriginalDate() {
        return originalDate;
    }

    public void setOriginalDate(String originalDate) {
        this.originalDate = originalDate;
    }

    public String getOccurrenceDate() {
        return occurrenceDate;
    }

    public void setOccurrenceDate(String occurrenceDate) {
        this.occurrenceDate = occurrenceDate;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRelatedExceptionId() {
        return relatedExceptionId;
    }

    public void setRelatedExceptionId(String relatedExceptionId) {
        this.relatedExceptionId = relatedExceptionId;
    }
}
