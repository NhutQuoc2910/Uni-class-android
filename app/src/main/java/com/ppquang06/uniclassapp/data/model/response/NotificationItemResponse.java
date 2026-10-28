package com.ppquang06.uniclassapp.data.model.response;

import com.google.gson.annotations.SerializedName;

public class NotificationItemResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    @SerializedName("courseCode")
    private String courseCode;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("category")
    private String category;

    @SerializedName("unread")
    private boolean unread;

    public NotificationItemResponse() {}

    public NotificationItemResponse(String id, String title, String body, String courseCode,
                                    String createdAt, String category, boolean unread) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.courseCode = courseCode;
        this.createdAt = createdAt;
        this.category = category;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }
}
