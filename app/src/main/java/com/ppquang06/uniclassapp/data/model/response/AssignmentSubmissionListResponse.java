package com.ppquang06.uniclassapp.data.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AssignmentSubmissionListResponse {
    @SerializedName("assignmentId")
    private String assignmentId;

    @SerializedName("assignmentTitle")
    private String assignmentTitle;

    @SerializedName("classCode")
    private String classCode;

    @SerializedName("deadline")
    private String deadline;

    @SerializedName("totalStudents")
    private int totalStudents;

    @SerializedName("submittedCount")
    private int submittedCount;

    @SerializedName("gradedCount")
    private int gradedCount;

    @SerializedName("onTimeCount")
    private int onTimeCount;

    @SerializedName("lateCount")
    private int lateCount;

    @SerializedName("notSubmittedCount")
    private int notSubmittedCount;

    @SerializedName("submissions")
    private List<SubmissionItem> submissions;

    public AssignmentSubmissionListResponse() {}

    public AssignmentSubmissionListResponse(String assignmentId, String assignmentTitle, String classCode,
                                            String deadline, int totalStudents, int submittedCount,
                                            int gradedCount, int onTimeCount, int lateCount,
                                            int notSubmittedCount, List<SubmissionItem> submissions) {
        this.assignmentId = assignmentId;
        this.assignmentTitle = assignmentTitle;
        this.classCode = classCode;
        this.deadline = deadline;
        this.totalStudents = totalStudents;
        this.submittedCount = submittedCount;
        this.gradedCount = gradedCount;
        this.onTimeCount = onTimeCount;
        this.lateCount = lateCount;
        this.notSubmittedCount = notSubmittedCount;
        this.submissions = submissions;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getSubmittedCount() {
        return submittedCount;
    }

    public void setSubmittedCount(int submittedCount) {
        this.submittedCount = submittedCount;
    }

    public int getGradedCount() {
        return gradedCount;
    }

    public void setGradedCount(int gradedCount) {
        this.gradedCount = gradedCount;
    }

    public int getOnTimeCount() {
        return onTimeCount;
    }

    public void setOnTimeCount(int onTimeCount) {
        this.onTimeCount = onTimeCount;
    }

    public int getLateCount() {
        return lateCount;
    }

    public void setLateCount(int lateCount) {
        this.lateCount = lateCount;
    }

    public int getNotSubmittedCount() {
        return notSubmittedCount;
    }

    public void setNotSubmittedCount(int notSubmittedCount) {
        this.notSubmittedCount = notSubmittedCount;
    }

    public List<SubmissionItem> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<SubmissionItem> submissions) {
        this.submissions = submissions;
    }

    public static class SubmissionItem {
        @SerializedName("submissionId")
        private String submissionId;

        @SerializedName("studentId")
        private String studentId;

        @SerializedName("studentName")
        private String studentName;

        @SerializedName("studentCode")
        private String studentCode;

        @SerializedName("avatarInitial")
        private String avatarInitial;

        @SerializedName("submitted")
        private boolean submitted;

        @SerializedName("late")
        private boolean late;

        @SerializedName("submittedAt")
        private String submittedAt;

        @SerializedName("fileUrl")
        private String fileUrl;

        @SerializedName("score")
        private Double score;

        @SerializedName("feedback")
        private String feedback;

        public SubmissionItem() {}

        public SubmissionItem(String submissionId, String studentId, String studentName, String studentCode,
                              String avatarInitial, boolean submitted, boolean late, String submittedAt,
                              String fileUrl, Double score, String feedback) {
            this.submissionId = submissionId;
            this.studentId = studentId;
            this.studentName = studentName;
            this.studentCode = studentCode;
            this.avatarInitial = avatarInitial;
            this.submitted = submitted;
            this.late = late;
            this.submittedAt = submittedAt;
            this.fileUrl = fileUrl;
            this.score = score;
            this.feedback = feedback;
        }

        public String getSubmissionId() {
            return submissionId;
        }

        public void setSubmissionId(String submissionId) {
            this.submissionId = submissionId;
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

        public boolean isSubmitted() {
            return submitted;
        }

        public void setSubmitted(boolean submitted) {
            this.submitted = submitted;
        }

        public boolean isLate() {
            return late;
        }

        public void setLate(boolean late) {
            this.late = late;
        }

        public String getSubmittedAt() {
            return submittedAt;
        }

        public void setSubmittedAt(String submittedAt) {
            this.submittedAt = submittedAt;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
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
}
