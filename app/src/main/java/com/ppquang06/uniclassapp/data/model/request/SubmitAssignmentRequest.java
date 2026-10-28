package com.ppquang06.uniclassapp.data.model.request;

import com.google.gson.annotations.SerializedName;

public class SubmitAssignmentRequest {

    @SerializedName("fileUrl")
    private String fileUrl;

    @SerializedName("fileName")
    private String fileName;

    public SubmitAssignmentRequest() {}

    public SubmitAssignmentRequest(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public SubmitAssignmentRequest(String fileUrl, String fileName) {
        this.fileUrl = fileUrl;
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
