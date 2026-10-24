package com.ppquang06.uniclassapp.data.api;

import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.HomeSummaryResponse;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface UserClassApiService {
    @GET("users/me")
    Call<ApiResponse<UserResponse>> getCurrentUser();

    @PUT("users/me")
    Call<ApiResponse<UserResponse>> updateCurrentUser(@Body UserResponse request);

    @GET("home/summary")
    Call<ApiResponse<HomeSummaryResponse>> getHomeSummary();
}
