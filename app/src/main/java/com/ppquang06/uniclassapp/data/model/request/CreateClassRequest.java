package com.ppquang06.uniclassapp.data.model.request;

import com.google.gson.annotations.SerializedName;

public class CreateClassRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("code")
    private String code;

    @SerializedName("semester")
    private String semester;

    @SerializedName("room")
    private String room;

    public CreateClassRequest() {}

    public CreateClassRequest(String name, String code, String semester, String room) {
        this.name = name;
        this.code = code;
        this.semester = semester;
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
}
