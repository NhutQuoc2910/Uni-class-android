package com.ppquang06.uniclassapp.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.api.AuthApiService;
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

/**
 * Repository encapsulating all authentication network operations via {@link AuthApiService}.
 */
public class AuthRepository {

    private final AuthApiService authApiService;

    public AuthRepository(@NonNull Context context) {
        this(ApiClient.getAuthApiService(context));
    }

    public AuthRepository(@NonNull AuthApiService authApiService) {
        this.authApiService = authApiService;
    }

    public Call<ApiResponse<AuthResponse>> login(LoginRequest request) {
        return authApiService.login(request);
    }

    public Call<ApiResponse<UserResponse>> register(RegisterRequest request) {
        return authApiService.register(request);
    }

    public Call<ApiResponse<String>> forgotPassword(ForgotPasswordRequest request) {
        return authApiService.forgotPassword(request);
    }

    public Call<ApiResponse<Map<String, Object>>> verifyOtp(VerifyOtpRequest request) {
        return authApiService.verifyOtp(request);
    }

    public Call<ApiResponse<String>> resetPassword(ResetPasswordRequest request) {
        return authApiService.resetPassword(request);
    }
}
