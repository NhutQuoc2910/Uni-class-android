package com.ppquang06.uniclassapp.data.api;

import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.AttendanceCheckInRequest;
import com.ppquang06.uniclassapp.data.model.request.ChangePasswordRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateClassRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateMaterialRequest;
import com.ppquang06.uniclassapp.data.model.request.GradeSubmissionRequest;
import com.ppquang06.uniclassapp.data.model.request.JoinClassRequest;
import com.ppquang06.uniclassapp.data.model.request.OpenQrSessionRequest;
import com.ppquang06.uniclassapp.data.model.request.SubmitAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.request.UpdateProfileRequest;
import com.ppquang06.uniclassapp.data.model.response.AssignmentDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.AssignmentSubmissionListResponse;
import com.ppquang06.uniclassapp.data.model.response.AttendanceCheckInResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassGradesResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassMemberItemResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassStatisticsResponse;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.HomeSummaryResponse;
import com.ppquang06.uniclassapp.data.model.response.LiveAttendanceSessionResponse;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserClassApiService {
    @GET("users/me")
    Call<ApiResponse<UserResponse>> getCurrentUser();

    @PUT("users/me")
    Call<ApiResponse<UserResponse>> updateCurrentUser(@Body UpdateProfileRequest request);

    @PUT("users/me")
    Call<ApiResponse<UserResponse>> updateCurrentUser(@Body UserResponse request);

    @PUT("users/me/password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest request);

    @GET("home/summary")
    Call<ApiResponse<HomeSummaryResponse>> getHomeSummary();

    @GET("classes")
    Call<ApiResponse<List<CourseClassItem>>> getClasses();

    @POST("classes/join")
    Call<ApiResponse<CourseClassItem>> joinClass(@Body JoinClassRequest request);

    @GET("classes/{id}")
    Call<ApiResponse<ClassDetailResponse>> getClassDetail(@Path("id") String classId);

    @GET("notifications")
    Call<ApiResponse<List<NotificationItemResponse>>> getNotifications();

    @GET("assignments/{id}")
    Call<ApiResponse<AssignmentDetailResponse>> getAssignmentDetail(@Path("id") String id);

    @POST("assignments/{id}/submit")
    Call<ApiResponse<AssignmentDetailResponse>> submitAssignment(@Path("id") String id, @Body SubmitAssignmentRequest request);

    @POST("attendance/check-in")
    Call<ApiResponse<AttendanceCheckInResponse>> checkInAttendance(@Body AttendanceCheckInRequest request);

    @GET("classes/{id}/grades")
    Call<ApiResponse<ClassGradesResponse>> getClassGrades(@Path("id") String classId);

    // ==========================================
    // Lecturer Management Endpoints (Phase 4)
    // ==========================================

    @POST("classes")
    Call<ApiResponse<CourseClassItem>> createClass(@Body CreateClassRequest request);

    @PUT("classes/{classId}/archive")
    Call<ApiResponse<CourseClassItem>> archiveClass(@Path("classId") String classId);

    @GET("classes/{classId}/members")
    Call<ApiResponse<List<ClassMemberItemResponse>>> getClassMembers(@Path("classId") String classId);

    @DELETE("classes/{classId}/members/{studentId}")
    Call<ApiResponse<Void>> removeClassMember(@Path("classId") String classId, @Path("studentId") String studentId);

    @POST("classes/{classId}/materials")
    Call<ApiResponse<ClassDetailResponse.MaterialItem>> createMaterial(@Path("classId") String classId, @Body CreateMaterialRequest request);

    @GET("classes/{classId}/statistics")
    Call<ApiResponse<ClassStatisticsResponse>> getClassStatistics(@Path("classId") String classId);

    @POST("classes/{classId}/assignments")
    Call<ApiResponse<ClassDetailResponse.ClassAssignmentItem>> createAssignment(@Path("classId") String classId, @Body CreateAssignmentRequest request);

    @GET("assignments/{assignmentId}/submissions")
    Call<ApiResponse<AssignmentSubmissionListResponse>> getAssignmentSubmissions(@Path("assignmentId") String assignmentId);

    @PUT("assignments/{assignmentId}/submissions/{studentId}/grade")
    Call<ApiResponse<AssignmentSubmissionListResponse>> gradeSubmission(@Path("assignmentId") String assignmentId, @Path("studentId") String studentId, @Body GradeSubmissionRequest request);

    @POST("classes/{classId}/attendance-sessions")
    Call<ApiResponse<LiveAttendanceSessionResponse>> openQrSession(@Path("classId") String classId, @Body OpenQrSessionRequest request);

    @GET("attendance-sessions/{sessionId}/live")
    Call<ApiResponse<LiveAttendanceSessionResponse>> getLiveAttendanceSession(@Path("sessionId") String sessionId);

    @PUT("attendance-sessions/{sessionId}/close")
    Call<ApiResponse<LiveAttendanceSessionResponse>> closeAttendanceSession(@Path("sessionId") String sessionId);
}
