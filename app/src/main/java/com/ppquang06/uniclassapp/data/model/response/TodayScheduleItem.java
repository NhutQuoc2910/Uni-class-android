package com.ppquang06.uniclassapp.data.model.response;

import com.google.gson.annotations.SerializedName;

public class TodayScheduleItem {

    @SerializedName("classId")
    private String classId;

    @SerializedName("scheduleId")
    private String scheduleId;

    @SerializedName("classCode")
    private String classCode;

    @SerializedName("className")
    private String className;

    @SerializedName("date")
    private String date;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("room")
    private String room;

    @SerializedName("scheduleType")
    private String scheduleType;

    public TodayScheduleItem() {
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getFormattedTimeRange() {
        String start = formatShortTime(startTime);
        String end = formatShortTime(endTime);
        if (!start.isEmpty() && !end.isEmpty()) {
            return start + " - " + end;
        }
        return start + end;
    }

    public String getScheduleTypeLabel() {
        if (scheduleType == null) {
            return "Lịch định kỳ";
        }
        switch (scheduleType.toUpperCase()) {
            case "MAKEUP":
                return "Học bù";
            case "RESCHEDULED":
                return "Đổi lịch";
            case "ROOM_CHANGE":
                return "Đổi phòng";
            case "REGULAR":
            default:
                return "Lịch định kỳ";
        }
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
