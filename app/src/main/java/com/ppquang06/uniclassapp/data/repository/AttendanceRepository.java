package com.ppquang06.uniclassapp.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.api.UserClassApiService;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.AttendanceCheckInRequest;
import com.ppquang06.uniclassapp.data.model.request.OpenQrSessionRequest;
import com.ppquang06.uniclassapp.data.model.response.AttendanceCheckInResponse;
import com.ppquang06.uniclassapp.data.model.response.LiveAttendanceSessionResponse;

import retrofit2.Call;

/**
 * Repository encapsulating attendance check-in and live QR session operations.
 */
public class AttendanceRepository {

    private final UserClassApiService apiService;

    public AttendanceRepository(@NonNull Context context) {
        this(ApiClient.getUserClassApiService(context));
    }

    public AttendanceRepository(@NonNull UserClassApiService apiService) {
        this.apiService = apiService;
    }

    public Call<ApiResponse<AttendanceCheckInResponse>> checkInAttendance(AttendanceCheckInRequest request) {
        return apiService.checkInAttendance(request);
    }

    public Call<ApiResponse<LiveAttendanceSessionResponse>> openQrSession(String classId,
                                                                          OpenQrSessionRequest request) {
        return apiService.openQrSession(classId, request);
    }

    public Call<ApiResponse<LiveAttendanceSessionResponse>> getLiveAttendanceSession(String sessionId) {
        return apiService.getLiveAttendanceSession(sessionId);
    }

    public Call<ApiResponse<LiveAttendanceSessionResponse>> closeAttendanceSession(String sessionId) {
        return apiService.closeAttendanceSession(sessionId);
    }
}
