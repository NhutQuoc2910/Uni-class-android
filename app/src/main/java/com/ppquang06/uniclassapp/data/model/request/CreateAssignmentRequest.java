package com.ppquang06.uniclassapp.data.model.request;

import com.google.gson.annotations.SerializedName;

public class CreateAssignmentRequest {
    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("deadline")
    private String deadline;

    @SerializedName("attachmentUrl")
    private String attachmentUrl;

    @SerializedName("notifyStudents")
    private Boolean notifyStudents;

    @SerializedName("allowLate")
    private Boolean allowLate;

    public CreateAssignmentRequest() {}

    public CreateAssignmentRequest(String title, String description, String deadline, String attachmentUrl,
                                  Boolean notifyStudents, Boolean allowLate) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.attachmentUrl = attachmentUrl;
        this.notifyStudents = notifyStudents;
        this.allowLate = allowLate;
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

    public Boolean getNotifyStudents() {
        return notifyStudents;
    }

    public void setNotifyStudents(Boolean notifyStudents) {
        this.notifyStudents = notifyStudents;
    }

    public Boolean getAllowLate() {
        return allowLate;
    }

    public void setAllowLate(Boolean allowLate) {
        this.allowLate = allowLate;
    }
}
