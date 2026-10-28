package com.ppquang06.uniclassapp.data.model.request;

import com.google.gson.annotations.SerializedName;

public class GradeSubmissionRequest {
    @SerializedName("score")
    private Double score;

    @SerializedName("feedback")
    private String feedback;

    @SerializedName("publishAll")
    private Boolean publishAll;

    public GradeSubmissionRequest() {}

    public GradeSubmissionRequest(Double score, String feedback, Boolean publishAll) {
        this.score = score;
        this.feedback = feedback;
        this.publishAll = publishAll;
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

    public Boolean getPublishAll() {
        return publishAll;
    }

    public void setPublishAll(Boolean publishAll) {
        this.publishAll = publishAll;
    }
}
