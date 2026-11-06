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

    public boolean isCancelled() {
        return scheduleType != null && "CANCELLED".equalsIgnoreCase(scheduleType.trim());
    }

    public String getScheduleTypeLabel() {
        if (scheduleType == null) {
            return "Lịch định kỳ";
        }
        switch (scheduleType.trim().toUpperCase()) {
            case "CANCELLED":
                return "Báo nghỉ";
            case "MAKEUP":
                return "Học bù";
            case "MODIFIED":
            case "RESCHEDULED":
            case "ROOM_CHANGE":
                return "Thay đổi lịch";
            case "REGULAR":
            default:
                return "Lịch định kỳ";
        }
    }

    public int getStartMinutes() {
        return parseTimeToMinutes(startTime);
    }

    public int getEndMinutes() {
        return parseTimeToMinutes(endTime);
    }

    public boolean isOngoing(int nowMinutes) {
        int start = getStartMinutes();
        int end = getEndMinutes();
        return !isCancelled() && start >= 0 && end >= 0 && nowMinutes >= start && nowMinutes <= end;
    }

    public boolean isUpcoming(int nowMinutes) {
        int start = getStartMinutes();
        return !isCancelled() && start >= 0 && nowMinutes < start;
    }

    public boolean canOpenOrScanQr(int nowMinutes) {
        int start = getStartMinutes();
        int end = getEndMinutes();
        return !isCancelled() && start >= 0 && end >= 0 && (nowMinutes >= (start - 15) && nowMinutes <= end);
    }

    public String getRealTimeStatusLabel(int nowMinutes) {
        if (isCancelled()) {
            return "Báo nghỉ";
        }
        int start = getStartMinutes();
        int end = getEndMinutes();
        if (start < 0 || end < 0) {
            return "";
        }
        if (nowMinutes >= start && nowMinutes <= end) {
            return "Đang diễn ra";
        }
        if (nowMinutes < start) {
            return "Sắp tới";
        }
        return "Đã kết thúc";
    }

    public String getFormattedTimeRange() {
        String start = formatShortTime(startTime);
        String end = formatShortTime(endTime);
        if (!start.isEmpty() && !end.isEmpty()) {
            return start + " - " + end;
        }
        return start + end;
    }

    private int parseTimeToMinutes(String rawTime) {
        if (rawTime == null || rawTime.trim().isEmpty()) {
            return -1;
        }
        try {
            String[] parts = rawTime.trim().split(":");
            if (parts.length < 2) {
                return -1;
            }
            int h = Integer.parseInt(parts[0].trim());
            int m = Integer.parseInt(parts[1].trim());
            if (h < 0 || h > 23 || m < 0 || m > 59) {
                return -1;
            }
            return h * 60 + m;
        } catch (Exception e) {
            return -1;
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
