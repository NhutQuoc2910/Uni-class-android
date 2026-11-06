package com.ppquang06.uniclassapp.data.model.response;

import com.google.gson.annotations.SerializedName;

public class ScheduleExceptionItem {

    @SerializedName("id")
    private String id;

    @SerializedName("classId")
    private String classId;

    @SerializedName("classCode")
    private String classCode;

    @SerializedName("className")
    private String className;

    @SerializedName("classScheduleId")
    private String classScheduleId;

    @SerializedName("originalDate")
    private String originalDate;

    @SerializedName("occurrenceDate")
    private String occurrenceDate;

    @SerializedName("exceptionType")
    private String exceptionType;

    @SerializedName("status")
    private String status;

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

    public ScheduleExceptionItem() {
    }

    public ScheduleExceptionItem(String id, String classId, String classCode, String className,
                                 String classScheduleId, String originalDate, String occurrenceDate,
                                 String exceptionType, String status, String startTime,
                                 String endTime, String room, String reason, String relatedExceptionId) {
        this.id = id;
        this.classId = classId;
        this.classCode = classCode;
        this.className = className;
        this.classScheduleId = classScheduleId;
        this.originalDate = originalDate;
        this.occurrenceDate = occurrenceDate;
        this.exceptionType = exceptionType;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.reason = reason;
        this.relatedExceptionId = relatedExceptionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getExceptionTypeLabel() {
        if (exceptionType == null) {
            return "";
        }
        switch (exceptionType.trim().toUpperCase()) {
            case "CANCELLED":
                return "Báo nghỉ";
            case "MAKEUP":
                return "Học bù";
            case "MODIFIED":
            case "RESCHEDULED":
            case "ROOM_CHANGE":
                return "Thay đổi lịch";
            default:
                return exceptionType;
        }
    }

    public String getStatusLabel() {
        if (status == null) {
            return "";
        }
        switch (status.trim().toUpperCase()) {
            case "DRAFT":
                return "Bản nháp";
            case "PUBLISHED":
                return "Đã công bố";
            case "REVOKED":
                return "Đã thu hồi";
            default:
                return status;
        }
    }

    public String getFormattedTimeRange() {
        String start = formatShortTime(startTime);
        String end = formatShortTime(endTime);
        if (!start.isEmpty() && !end.isEmpty()) {
            return start + " - " + end;
        }
        return start + end;
    }

    private String formatShortTime(String rawTime) {
        if (rawTime == null || rawTime.trim().isEmpty()) {
            return "";
        }
        String trimmed = rawTime.trim();
        if (trimmed.length() >= 5) {
            return trimmed.substring(0, 5);
        }
        return trimmed;
    }
}
