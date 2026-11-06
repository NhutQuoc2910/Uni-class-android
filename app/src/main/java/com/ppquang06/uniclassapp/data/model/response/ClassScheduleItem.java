package com.ppquang06.uniclassapp.data.model.response;

import com.google.gson.annotations.SerializedName;

public class ClassScheduleItem {

    @SerializedName("id")
    private String id;

    @SerializedName("classId")
    private String classId;

    @SerializedName("classCode")
    private String classCode;

    @SerializedName("className")
    private String className;

    @SerializedName("dayOfWeek")
    private Integer dayOfWeek;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("room")
    private String room;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    public ClassScheduleItem() {
    }

    public ClassScheduleItem(String id, String classId, String classCode, String className,
                             Integer dayOfWeek, String startTime, String endTime,
                             String room, String startDate, String endDate) {
        this.id = id;
        this.classId = classId;
        this.classCode = classCode;
        this.className = className;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDayOfWeekLabel() {
        if (dayOfWeek == null) {
            return "";
        }
        switch (dayOfWeek) {
            case 1:
                return "Thứ Hai";
            case 2:
                return "Thứ Ba";
            case 3:
                return "Thứ Tư";
            case 4:
                return "Thứ Năm";
            case 5:
                return "Thứ Sáu";
            case 6:
                return "Thứ Bảy";
            case 7:
                return "Chủ Nhật";
            default:
                return "Thứ " + dayOfWeek;
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
