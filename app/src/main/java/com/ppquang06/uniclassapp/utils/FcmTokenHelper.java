package com.ppquang06.uniclassapp.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessaging;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.UpdateProfileRequest;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Utility to fetch and sync the Firebase Cloud Messaging (FCM) registration token to the server
 * so that push notifications / pop-ups are reliably delivered to users.
 */
public final class FcmTokenHelper {

    private static final String TAG = "FcmTokenHelper";

    private FcmTokenHelper() {
    }

    public static void syncFcmToken(@NonNull Context context) {
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful() || task.getResult() == null) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                String token = task.getResult();
                Log.i(TAG, "Fetched FCM token: " + token);

                Context appContext = context.getApplicationContext();
                SessionManager sessionManager = SessionManager.getInstance(appContext);
                sessionManager.saveFcmToken(token);

                if (sessionManager.isLoggedIn()) {
                    UserResponse user = sessionManager.getUser();
                    String fullName = user != null ? user.getFullName() : "";
                    String phone = user != null ? user.getPhone() : "";
                    String avatarUrl = user != null ? user.getAvatarUrl() : null;

                    UpdateProfileRequest request = new UpdateProfileRequest(fullName, phone, avatarUrl, token);
                    ApiClient.getUserClassApiService(appContext).updateCurrentUser(request)
                            .enqueue(new Callback<ApiResponse<UserResponse>>() {
                                @Override
                                public void onResponse(@NonNull Call<ApiResponse<UserResponse>> call,
                                                       @NonNull Response<ApiResponse<UserResponse>> response) {
                                    if (response.isSuccessful()) {
                                        Log.i(TAG, "FCM token synced to server successfully");
                                    } else {
                                        Log.w(TAG, "Server responded with HTTP " + response.code() + " on FCM token sync");
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Throwable t) {
                                    Log.e(TAG, "Failed to sync FCM token to server", t);
                                }
                            });
                }
            });
        } catch (Throwable t) {
            Log.e(TAG, "Exception during FCM token retrieval", t);
        }
    }
}
