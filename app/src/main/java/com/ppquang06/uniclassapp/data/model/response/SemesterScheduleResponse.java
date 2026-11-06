package com.ppquang06.uniclassapp.data.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SemesterScheduleResponse {

    @SerializedName("regularSchedules")
    private List<ClassScheduleItem> regularSchedules = new ArrayList<>();

    @SerializedName("scheduleExceptions")
    private List<ScheduleExceptionItem> scheduleExceptions = new ArrayList<>();

    public SemesterScheduleResponse() {
    }

    public SemesterScheduleResponse(List<ClassScheduleItem> regularSchedules,
                                    List<ScheduleExceptionItem> scheduleExceptions) {
        this.regularSchedules = regularSchedules != null ? regularSchedules : new ArrayList<>();
        this.scheduleExceptions = scheduleExceptions != null ? scheduleExceptions : new ArrayList<>();
    }

    public List<ClassScheduleItem> getRegularSchedules() {
        return regularSchedules != null ? regularSchedules : new ArrayList<>();
    }

    public void setRegularSchedules(List<ClassScheduleItem> regularSchedules) {
        this.regularSchedules = regularSchedules != null ? regularSchedules : new ArrayList<>();
    }

    public List<ScheduleExceptionItem> getScheduleExceptions() {
        return scheduleExceptions != null ? scheduleExceptions : new ArrayList<>();
    }

    public void setScheduleExceptions(List<ScheduleExceptionItem> scheduleExceptions) {
        this.scheduleExceptions = scheduleExceptions != null ? scheduleExceptions : new ArrayList<>();
    }
}
