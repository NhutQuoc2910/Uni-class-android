package com.ppquang06.uniclassapp.data.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ClassMemberItemResponse {
    @SerializedName("studentId")
    private String studentId;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("studentCode")
    private String studentCode;

    @SerializedName("avatarInitial")
    private String avatarInitial;

    @SerializedName("attendanceRate")
    private int attendanceRate;

    @SerializedName("absentSessions")
    private int absentSessions;

    @SerializedName("totalSessions")
    private int totalSessions;

    @SerializedName("submittedAssignments")
    private int submittedAssignments;

    @SerializedName("totalAssignments")
    private int totalAssignments;

    @SerializedName("averageScore")
    private Double averageScore;

    @SerializedName("recentAssignments")
    private List<MemberAssignmentSummary> recentAssignments;

    public ClassMemberItemResponse() {}

    public ClassMemberItemResponse(String studentId, String fullName, String email, String studentCode,
                                   String avatarInitial, int attendanceRate, int absentSessions,
                                   int totalSessions, int submittedAssignments, int totalAssignments,
                                   Double averageScore, List<MemberAssignmentSummary> recentAssignments) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.email = email;
        this.studentCode = studentCode;
        this.avatarInitial = avatarInitial;
        this.attendanceRate = attendanceRate;
        this.absentSessions = absentSessions;
        this.totalSessions = totalSessions;
        this.submittedAssignments = submittedAssignments;
        this.totalAssignments = totalAssignments;
        this.averageScore = averageScore;
        this.recentAssignments = recentAssignments;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public int getAttendanceRate() {
        return attendanceRate;
    }

    public void setAttendanceRate(int attendanceRate) {
        this.attendanceRate = attendanceRate;
    }

    public int getAbsentSessions() {
        return absentSessions;
    }

    public void setAbsentSessions(int absentSessions) {
        this.absentSessions = absentSessions;
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }

    public int getSubmittedAssignments() {
        return submittedAssignments;
    }

    public void setSubmittedAssignments(int submittedAssignments) {
        this.submittedAssignments = submittedAssignments;
    }

    public int getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(int totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public List<MemberAssignmentSummary> getRecentAssignments() {
        return recentAssignments;
    }

    public void setRecentAssignments(List<MemberAssignmentSummary> recentAssignments) {
        this.recentAssignments = recentAssignments;
    }

    public static class MemberAssignmentSummary {
        @SerializedName("assignmentId")
        private String assignmentId;

        @SerializedName("title")
        private String title;

        @SerializedName("deadline")
        private String deadline;

        @SerializedName("submitted")
        private boolean submitted;

        @SerializedName("submittedAt")
        private String submittedAt;

        @SerializedName("score")
        private Double score;

        public MemberAssignmentSummary() {}

        public MemberAssignmentSummary(String assignmentId, String title, String deadline,
                                      boolean submitted, String submittedAt, Double score) {
            this.assignmentId = assignmentId;
            this.title = title;
            this.deadline = deadline;
            this.submitted = submitted;
            this.submittedAt = submittedAt;
            this.score = score;
        }

        public String getAssignmentId() {
            return assignmentId;
        }

        public void setAssignmentId(String assignmentId) {
            this.assignmentId = assignmentId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDeadline() {
            return deadline;
        }

        public void setDeadline(String deadline) {
            this.deadline = deadline;
        }

        public boolean isSubmitted() {
            return submitted;
        }

        public void setSubmitted(boolean submitted) {
            this.submitted = submitted;
        }

        public String getSubmittedAt() {
            return submittedAt;
        }

        public void setSubmittedAt(String submittedAt) {
            this.submittedAt = submittedAt;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }
    }
}
