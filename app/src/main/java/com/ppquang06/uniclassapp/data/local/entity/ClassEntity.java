package com.ppquang06.uniclassapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;

@Entity(tableName = "classes")
public class ClassEntity {

    @PrimaryKey
    @NonNull
    private String id = "";

    private String code;
    private String name;
    private String semester;
    private String room;
    private String lecturerName;
    private String inviteCode;
    private String status;
    private int progress;
    private int enrolledCount;
    private long updatedAt;

    public ClassEntity() {
    }

    @Ignore
    public ClassEntity(@NonNull String id, String code, String name, String semester, String room,
                       String lecturerName, String inviteCode, String status, int progress,
                       int enrolledCount, long updatedAt) {
        this.id = id != null ? id : "";
        this.code = code;
        this.name = name;
        this.semester = semester;
        this.room = room;
        this.lecturerName = lecturerName;
        this.inviteCode = inviteCode;
        this.status = status;
        this.progress = progress;
        this.enrolledCount = enrolledCount;
        this.updatedAt = updatedAt;
    }

    public static ClassEntity fromCourseClassItem(CourseClassItem item) {
        if (item == null) {
            return null;
        }
        ClassEntity entity = new ClassEntity();
        entity.setId(item.getId() != null ? item.getId() : "");
        entity.setCode(item.getCode());
        entity.setName(item.getName());
        entity.setSemester(item.getSemester());
        entity.setRoom(item.getRoom());
        entity.setLecturerName(item.getLecturerName());
        entity.setInviteCode(item.getInviteCode());
        entity.setStatus(item.getStatus() != null ? item.getStatus() : "ACTIVE");
        entity.setProgress(item.getProgress());
        entity.setEnrolledCount(item.getEnrolledCount());
        entity.setUpdatedAt(System.currentTimeMillis());
        return entity;
    }

    public CourseClassItem toCourseClassItem() {
        return new CourseClassItem(
                this.id,
                this.code,
                this.name,
                this.semester,
                this.room,
                this.lecturerName,
                this.inviteCode,
                this.status,
                this.progress,
                this.enrolledCount
        );
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id != null ? id : "";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(int enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
