package com.ppquang06.uniclassapp.data.api;

import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.ForgotPasswordRequest;
import com.ppquang06.uniclassapp.data.model.request.LoginRequest;
import com.ppquang06.uniclassapp.data.model.request.RegisterRequest;
import com.ppquang06.uniclassapp.data.model.request.ResetPasswordRequest;
import com.ppquang06.uniclassapp.data.model.request.VerifyOtpRequest;
import com.ppquang06.uniclassapp.data.model.response.AuthResponse;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<ApiResponse<UserResponse>> register(@Body RegisterRequest request);

    @POST("auth/forgot-password")
    Call<ApiResponse<String>> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("auth/verify-otp")
    Call<ApiResponse<Map<String, Object>>> verifyOtp(@Body VerifyOtpRequest request);

    @POST("auth/reset-password")
    Call<ApiResponse<String>> resetPassword(@Body ResetPasswordRequest request);
}
