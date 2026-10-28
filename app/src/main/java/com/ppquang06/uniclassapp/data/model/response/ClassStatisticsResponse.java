package com.ppquang06.uniclassapp.data.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ClassStatisticsResponse {
    @SerializedName("classId")
    private String classId;

    @SerializedName("classCode")
    private String classCode;

    @SerializedName("className")
    private String className;

    @SerializedName("semester")
    private String semester;

    @SerializedName("status")
    private String status;

    @SerializedName("enrolledCount")
    private int enrolledCount;

    @SerializedName("attendanceRate")
    private int attendanceRate;

    @SerializedName("warningAbsentCount")
    private int warningAbsentCount;

    @SerializedName("averageScore")
    private double averageScore;

    @SerializedName("warningStudents")
    private List<ClassMemberItemResponse> warningStudents;

    @SerializedName("scoreDistribution")
    private List<ScoreBucket> scoreDistribution;

    public ClassStatisticsResponse() {}

    public ClassStatisticsResponse(String classId, String classCode, String className, String semester,
                                   String status, int enrolledCount, int attendanceRate,
                                   int warningAbsentCount, double averageScore,
                                   List<ClassMemberItemResponse> warningStudents,
                                   List<ScoreBucket> scoreDistribution) {
        this.classId = classId;
        this.classCode = classCode;
        this.className = className;
        this.semester = semester;
        this.status = status;
        this.enrolledCount = enrolledCount;
        this.attendanceRate = attendanceRate;
        this.warningAbsentCount = warningAbsentCount;
        this.averageScore = averageScore;
        this.warningStudents = warningStudents;
        this.scoreDistribution = scoreDistribution;
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

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public int getWarningAbsentCount() {
        return warningAbsentCount;
    }

    public void setWarningAbsentCount(int warningAbsentCount) {
        this.warningAbsentCount = warningAbsentCount;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public List<ClassMemberItemResponse> getWarningStudents() {
        return warningStudents;
    }

    public void setWarningStudents(List<ClassMemberItemResponse> warningStudents) {
        this.warningStudents = warningStudents;
    }

    public List<ScoreBucket> getScoreDistribution() {
        return scoreDistribution;
    }

    public void setScoreDistribution(List<ScoreBucket> scoreDistribution) {
        this.scoreDistribution = scoreDistribution;
    }

    public static class ScoreBucket {
        @SerializedName("label")
        private String label;

        @SerializedName("count")
        private int count;

        @SerializedName("percentage")
        private int percentage;

        public ScoreBucket() {}

        public ScoreBucket(String label, int count, int percentage) {
            this.label = label;
            this.count = count;
            this.percentage = percentage;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getPercentage() {
            return percentage;
        }

        public void setPercentage(int percentage) {
            this.percentage = percentage;
        }
    }
}
