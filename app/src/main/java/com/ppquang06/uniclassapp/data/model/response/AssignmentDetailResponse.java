package com.ppquang06.uniclassapp.data.model.response;

import com.google.gson.annotations.SerializedName;

public class AssignmentDetailResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("classId")
    private String classId;

    @SerializedName("classCode")
    private String classCode;

    @SerializedName("className")
    private String className;

    @SerializedName("lecturerName")
    private String lecturerName;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("deadline")
    private String deadline;

    @SerializedName("attachmentUrl")
    private String attachmentUrl;

    @SerializedName("submitted")
    private boolean submitted;

    @SerializedName("submittedFileUrl")
    private String submittedFileUrl;

    @SerializedName("submittedAt")
    private String submittedAt;

    @SerializedName("score")
    private Double score;

    @SerializedName("feedback")
    private String feedback;

    public AssignmentDetailResponse() {}

    public AssignmentDetailResponse(String id, String classId, String classCode, String className,
                                  String lecturerName, String title, String description,
                                  String deadline, String attachmentUrl, boolean submitted,
                                  String submittedFileUrl, String submittedAt, Double score,
                                  String feedback) {
        this.id = id;
        this.classId = classId;
        this.classCode = classCode;
        this.className = className;
        this.lecturerName = lecturerName;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.attachmentUrl = attachmentUrl;
        this.submitted = submitted;
        this.submittedFileUrl = submittedFileUrl;
        this.submittedAt = submittedAt;
        this.score = score;
        this.feedback = feedback;
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

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
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

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
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

    public String getSubmittedFileUrl() {
        return submittedFileUrl;
    }

    public void setSubmittedFileUrl(String submittedFileUrl) {
        this.submittedFileUrl = submittedFileUrl;
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

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
