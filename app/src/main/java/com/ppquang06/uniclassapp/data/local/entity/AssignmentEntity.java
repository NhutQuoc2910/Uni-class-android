package com.ppquang06.uniclassapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;

@Entity(tableName = "assignments", indices = {@Index(value = "classId")})
public class AssignmentEntity {

    @PrimaryKey
    @NonNull
    private String id = "";

    private String classId;
    private String className;
    private String title;
    private String description;
    private String dueDate;
    private String attachmentUrl;
    private boolean submitted;
    private Double score;
    private String feedback;
    private long updatedAt;

    public AssignmentEntity() {
    }

    @Ignore
    public AssignmentEntity(@NonNull String id, String classId, String className, String title,
                            String description, String dueDate, String attachmentUrl,
                            boolean submitted, Double score, String feedback, long updatedAt) {
        this.id = id != null ? id : "";
        this.classId = classId;
        this.className = className;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.attachmentUrl = attachmentUrl;
        this.submitted = submitted;
        this.score = score;
        this.feedback = feedback;
        this.updatedAt = updatedAt;
    }

    public static AssignmentEntity fromAssignmentItem(AssignmentItem item) {
        if (item == null) {
            return null;
        }
        AssignmentEntity entity = new AssignmentEntity();
        entity.setId(item.getId() != null ? item.getId() : "");
        entity.setTitle(item.getTitle());
        entity.setClassName(item.getClassName());
        entity.setDueDate(item.getDueDate());
        entity.setSubmitted(item.isSubmitted());
        entity.setUpdatedAt(System.currentTimeMillis());
        return entity;
    }

    public AssignmentItem toAssignmentItem() {
        AssignmentItem item = new AssignmentItem();
        item.setId(this.id);
        item.setTitle(this.title);
        item.setClassName(this.className);
        item.setDueDate(this.dueDate);
        item.setSubmitted(this.submitted);
        return item;
    }

    public static AssignmentEntity fromClassAssignmentItem(
            String classId,
            String className,
            ClassDetailResponse.ClassAssignmentItem item) {
        if (item == null) {
            return null;
        }
        AssignmentEntity entity = new AssignmentEntity();
        entity.setId(item.getId() != null ? item.getId() : "");
        entity.setClassId(classId);
        entity.setClassName(className);
        entity.setTitle(item.getTitle());
        entity.setDescription(item.getDescription());
        entity.setDueDate(item.getDueDate());
        entity.setAttachmentUrl(item.getAttachmentUrl());
        entity.setSubmitted(item.isSubmitted());
        entity.setScore(item.getScore());
        entity.setFeedback(item.getFeedback());
        entity.setUpdatedAt(System.currentTimeMillis());
        return entity;
    }

    public ClassDetailResponse.ClassAssignmentItem toClassAssignmentItem() {
        return new ClassDetailResponse.ClassAssignmentItem(
                this.id,
                this.title,
                this.description,
                this.dueDate,
                this.attachmentUrl,
                this.submitted,
                this.score,
                this.feedback
        );
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id != null ? id : "";
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
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

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
