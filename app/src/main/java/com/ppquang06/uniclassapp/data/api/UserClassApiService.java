package com.ppquang06.uniclassapp.data.api;

import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.AttendanceCheckInRequest;
import com.ppquang06.uniclassapp.data.model.request.ChangePasswordRequest;
import com.ppquang06.uniclassapp.data.model.request.JoinClassRequest;
import com.ppquang06.uniclassapp.data.model.request.SubmitAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.request.UpdateProfileRequest;
import com.ppquang06.uniclassapp.data.model.response.AssignmentDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.AttendanceCheckInResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassGradesResponse;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.HomeSummaryResponse;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
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
}
