package com.ppquang06.uniclassapp.data.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LiveAttendanceSessionResponse {
    @SerializedName("sessionId")
    private String sessionId;

    @SerializedName("classId")
    private String classId;

    @SerializedName("classCode")
    private String classCode;

    @SerializedName("className")
    private String className;

    @SerializedName("room")
    private String room;

    @SerializedName("inviteCode")
    private String inviteCode;

    @SerializedName("status")
    private String status;

    @SerializedName("wifiBssid")
    private String wifiBssid;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("radiusMeters")
    private Double radiusMeters;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("totalStudents")
    private int totalStudents;

    @SerializedName("presentCount")
    private int presentCount;

    @SerializedName("absentCount")
    private int absentCount;

    @SerializedName("attendees")
    private List<AttendeeItem> attendees;

    public LiveAttendanceSessionResponse() {}

    public LiveAttendanceSessionResponse(String sessionId, String classId, String classCode, String className,
                                        String room, String inviteCode, String status, String wifiBssid,
                                        Double latitude, Double longitude, Double radiusMeters,
                                        String createdAt, int totalStudents, int presentCount,
                                        int absentCount, List<AttendeeItem> attendees) {
        this.sessionId = sessionId;
        this.classId = classId;
        this.classCode = classCode;
        this.className = className;
        this.room = room;
        this.inviteCode = inviteCode;
        this.status = status;
        this.wifiBssid = wifiBssid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;
        this.createdAt = createdAt;
        this.totalStudents = totalStudents;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.attendees = attendees;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(int presentCount) {
        this.presentCount = presentCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(int absentCount) {
        this.absentCount = absentCount;
    }

    public List<AttendeeItem> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<AttendeeItem> attendees) {
        this.attendees = attendees;
    }

    public static class AttendeeItem {
        @SerializedName("studentId")
        private String studentId;

        @SerializedName("studentName")
        private String studentName;

        @SerializedName("studentCode")
        private String studentCode;

        @SerializedName("avatarInitial")
        private String avatarInitial;

        @SerializedName("status")
        private String status;

        @SerializedName("scannedAt")
        private String scannedAt;

        @SerializedName("deviceId")
        private String deviceId;

        public AttendeeItem() {}

        public AttendeeItem(String studentId, String studentName, String studentCode,
                            String avatarInitial, String status, String scannedAt, String deviceId) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.studentCode = studentCode;
            this.avatarInitial = avatarInitial;
            this.status = status;
            this.scannedAt = scannedAt;
            this.deviceId = deviceId;
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }

        public String getStudentCode() {
            return studentCode;
        }

        public void setStudentCode(String studentCode) {
            this.studentCode = studentCode;
        }

        public String getAvatarInitial() {
            return avatarInitial;
        }

        public void setAvatarInitial(String avatarInitial) {
            this.avatarInitial = avatarInitial;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
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
    }
}
