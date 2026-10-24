package com.ppquang06.uniclassapp.data.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class HomeSummaryResponse {
    @SerializedName("activeCoursesCount")
    private int activeCoursesCount;

    @SerializedName("pendingAssignmentsCount")
    private int pendingAssignmentsCount;

    @SerializedName("courses")
    private List<CourseClassItem> courses = new ArrayList<>();

    @SerializedName("dueSoonAssignments")
    private List<AssignmentItem> dueSoonAssignments = new ArrayList<>();

    public HomeSummaryResponse() {}

    public int getActiveCoursesCount() {
        return activeCoursesCount;
    }

    public void setActiveCoursesCount(int activeCoursesCount) {
        this.activeCoursesCount = activeCoursesCount;
    }

    public int getPendingAssignmentsCount() {
        return pendingAssignmentsCount;
    }

    public void setPendingAssignmentsCount(int pendingAssignmentsCount) {
        this.pendingAssignmentsCount = pendingAssignmentsCount;
    }

    public List<CourseClassItem> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseClassItem> courses) {
        this.courses = courses;
    }

    public List<AssignmentItem> getDueSoonAssignments() {
        return dueSoonAssignments;
    }

    public void setDueSoonAssignments(List<AssignmentItem> dueSoonAssignments) {
        this.dueSoonAssignments = dueSoonAssignments;
    }
}
