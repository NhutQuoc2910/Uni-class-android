package com.ppquang06.uniclassapp.data.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ClassDetailResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("code")
    private String code;

    @SerializedName("name")
    private String name;

    @SerializedName("semester")
    private String semester;

    @SerializedName("room")
    private String room;

    @SerializedName("lecturerName")
    private String lecturerName;

    @SerializedName("inviteCode")
    private String inviteCode;

    @SerializedName("enrolledCount")
    private int enrolledCount;

    @SerializedName("attendanceRate")
    private int attendanceRate;

    @SerializedName("presentCount")
    private int presentCount;

    @SerializedName("absentCount")
    private int absentCount;

    @SerializedName("totalSessions")
    private int totalSessions;

    @SerializedName("announcements")
    private List<AnnouncementItem> announcements = new ArrayList<>();

    @SerializedName("materials")
    private List<MaterialItem> materials = new ArrayList<>();

    @SerializedName("assignments")
    private List<ClassAssignmentItem> assignments = new ArrayList<>();

    @SerializedName("attendanceRecords")
    private List<AttendanceSessionRecordItem> attendanceRecords = new ArrayList<>();

    public ClassDetailResponse() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
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

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(int enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public int getAttendanceRate() {
        return attendanceRate;
    }

    public void setAttendanceRate(int attendanceRate) {
        this.attendanceRate = attendanceRate;
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

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }

    public List<AnnouncementItem> getAnnouncements() {
        return announcements != null ? announcements : new ArrayList<>();
    }

    public void setAnnouncements(List<AnnouncementItem> announcements) {
        this.announcements = announcements != null ? announcements : new ArrayList<>();
    }

    public List<MaterialItem> getMaterials() {
        return materials != null ? materials : new ArrayList<>();
    }

    public void setMaterials(List<MaterialItem> materials) {
        this.materials = materials != null ? materials : new ArrayList<>();
    }

    public List<ClassAssignmentItem> getAssignments() {
        return assignments != null ? assignments : new ArrayList<>();
    }

    public void setAssignments(List<ClassAssignmentItem> assignments) {
        this.assignments = assignments != null ? assignments : new ArrayList<>();
    }

    public List<AttendanceSessionRecordItem> getAttendanceRecords() {
        return attendanceRecords != null ? attendanceRecords : new ArrayList<>();
    }

    public void setAttendanceRecords(List<AttendanceSessionRecordItem> attendanceRecords) {
        this.attendanceRecords = attendanceRecords != null ? attendanceRecords : new ArrayList<>();
    }

    public static class AnnouncementItem {
        @SerializedName("id")
        private String id;

        @SerializedName("title")
        private String title;

        @SerializedName("content")
        private String content;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("urgent")
        private boolean urgent;

        @SerializedName("unread")
        private boolean unread;

        public AnnouncementItem() {}

        public AnnouncementItem(String id, String title, String content, String createdAt, boolean urgent, boolean unread) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.createdAt = createdAt;
            this.urgent = urgent;
            this.unread = unread;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public boolean isUrgent() {
            return urgent;
        }

        public void setUrgent(boolean urgent) {
            this.urgent = urgent;
        }

        public boolean isUnread() {
            return unread;
        }

        public void setUnread(boolean unread) {
            this.unread = unread;
        }
    }

    public static class MaterialItem {
        @SerializedName("id")
        private String id;

        @SerializedName("title")
        private String title;

        @SerializedName("fileUrl")
        private String fileUrl;

        @SerializedName("fileType")
        private String fileType;

        @SerializedName("createdAt")
        private String createdAt;

        public MaterialItem() {}

        public MaterialItem(String id, String title, String fileUrl, String fileType, String createdAt) {
            this.id = id;
            this.title = title;
            this.fileUrl = fileUrl;
            this.fileType = fileType;
            this.createdAt = createdAt;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class ClassAssignmentItem {
        @SerializedName("id")
        private String id;

        @SerializedName("title")
        private String title;

        @SerializedName("description")
        private String description;

        @SerializedName("dueDate")
        private String dueDate;

        @SerializedName("attachmentUrl")
        private String attachmentUrl;

        @SerializedName("submitted")
        private boolean submitted;

        @SerializedName("score")
        private Double score;

        @SerializedName("feedback")
        private String feedback;

        public ClassAssignmentItem() {}

        public ClassAssignmentItem(String id, String title, String description, String dueDate,
                                   String attachmentUrl, boolean submitted, Double score, String feedback) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.dueDate = dueDate;
            this.attachmentUrl = attachmentUrl;
            this.submitted = submitted;
            this.score = score;
            this.feedback = feedback;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDueDate() {
            return dueDate;
        }

        public void setDueDate(String dueDate) {
            this.dueDate = dueDate;
        }

        public String getAttachmentUrl() {
            return attachmentUrl;
        }

        public void setAttachmentUrl(String attachmentUrl) {
            this.attachmentUrl = attachmentUrl;
        }

        public boolean isSubmitted() {
            return submitted;
        }

        public void setSubmitted(boolean submitted) {
            this.submitted = submitted;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }
    }

    public static class AttendanceSessionRecordItem {
        @SerializedName("sessionId")
        private String sessionId;

        @SerializedName("sessionNumber")
        private int sessionNumber;

        @SerializedName("date")
        private String date;

        @SerializedName("status")
        private String status;

        @SerializedName("scannedAt")
        private String scannedAt;

        public AttendanceSessionRecordItem() {}

        public AttendanceSessionRecordItem(String sessionId, int sessionNumber, String date, String status, String scannedAt) {
            this.sessionId = sessionId;
            this.sessionNumber = sessionNumber;
            this.date = date;
            this.status = status;
            this.scannedAt = scannedAt;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public int getSessionNumber() {
            return sessionNumber;
        }

        public void setSessionNumber(int sessionNumber) {
            this.sessionNumber = sessionNumber;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
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
    }
}
