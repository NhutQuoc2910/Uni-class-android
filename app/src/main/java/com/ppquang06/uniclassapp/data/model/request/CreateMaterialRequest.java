package com.ppquang06.uniclassapp.data.model.request;

import com.google.gson.annotations.SerializedName;

public class CreateMaterialRequest {
    @SerializedName("title")
    private String title;

    @SerializedName("fileUrl")
    private String fileUrl;

    @SerializedName("fileType")
    private String fileType;

    @SerializedName("urgentAnnouncement")
    private Boolean urgentAnnouncement;

    public CreateMaterialRequest() {}

    public CreateMaterialRequest(String title, String fileUrl, String fileType, Boolean urgentAnnouncement) {
        this.title = title;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.urgentAnnouncement = urgentAnnouncement;
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

    public Boolean getUrgentAnnouncement() {
        return urgentAnnouncement;
    }

    public void setUrgentAnnouncement(Boolean urgentAnnouncement) {
        this.urgentAnnouncement = urgentAnnouncement;
    }
}
