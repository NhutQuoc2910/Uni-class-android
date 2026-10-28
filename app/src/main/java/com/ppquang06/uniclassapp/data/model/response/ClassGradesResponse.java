package com.ppquang06.uniclassapp.data.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ClassGradesResponse {

    @SerializedName("classId")
    private String classId;

    @SerializedName("classCode")
    private String classCode;

    @SerializedName("className")
    private String className;

    @SerializedName("lecturerName")
    private String lecturerName;

    @SerializedName("averageScore")
    private double averageScore;

    @SerializedName("gradedCount")
    private int gradedCount;

    @SerializedName("totalAssignments")
    private int totalAssignments;

    @SerializedName("grades")
    private List<GradeItem> grades = new ArrayList<>();

    public ClassGradesResponse() {}

    public ClassGradesResponse(String classId, String classCode, String className,
                               String lecturerName, double averageScore, int gradedCount,
                               int totalAssignments, List<GradeItem> grades) {
        this.classId = classId;
        this.classCode = classCode;
        this.className = className;
        this.lecturerName = lecturerName;
        this.averageScore = averageScore;
        this.gradedCount = gradedCount;
        this.totalAssignments = totalAssignments;
        this.grades = grades != null ? grades : new ArrayList<>();
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

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public int getGradedCount() {
        return gradedCount;
    }

    public void setGradedCount(int gradedCount) {
        this.gradedCount = gradedCount;
    }

    public int getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(int totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public List<GradeItem> getGrades() {
        return grades != null ? grades : new ArrayList<>();
    }

    public void setGrades(List<GradeItem> grades) {
        this.grades = grades != null ? grades : new ArrayList<>();
    }

    public static class GradeItem {
        @SerializedName("assignmentId")
        private String assignmentId;

        @SerializedName("assignmentTitle")
        private String assignmentTitle;

        @SerializedName("submittedAt")
        private String submittedAt;

        @SerializedName("submitted")
        private boolean submitted;

        @SerializedName("score")
        private Double score;

        @SerializedName("feedback")
        private String feedback;

        @SerializedName("lecturerName")
        private String lecturerName;

        @SerializedName("lecturerInitial")
        private String lecturerInitial;

        public GradeItem() {}

        public GradeItem(String assignmentId, String assignmentTitle, String submittedAt,
                         boolean submitted, Double score, String feedback,
                         String lecturerName, String lecturerInitial) {
            this.assignmentId = assignmentId;
            this.assignmentTitle = assignmentTitle;
            this.submittedAt = submittedAt;
            this.submitted = submitted;
            this.score = score;
            this.feedback = feedback;
            this.lecturerName = lecturerName;
            this.lecturerInitial = lecturerInitial;
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

        public String getSubmittedAt() {
            return submittedAt;
        }

        public void setSubmittedAt(String submittedAt) {
            this.submittedAt = submittedAt;
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

        public String getLecturerName() {
            return lecturerName;
        }

        public void setLecturerName(String lecturerName) {
            this.lecturerName = lecturerName;
        }

        public String getLecturerInitial() {
            return lecturerInitial;
        }

        public void setLecturerInitial(String lecturerInitial) {
            this.lecturerInitial = lecturerInitial;
        }
    }
}
