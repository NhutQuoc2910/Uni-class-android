package com.ppquang06.uniclassapp.data.repository;

import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ppquang06.uniclassapp.data.model.ApiResponse;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Base abstract repository providing standardized error message parsing from Retrofit responses,
 * thread-safe LiveData state dispatching, and common JSON utilities.
 */
public abstract class BaseRepository {

    protected final Gson gson = new Gson();

    public BaseRepository() {
    }

    /**
     * Extracts user-friendly error message from a Retrofit Response, checking response body,
     * errorBody JSON, known error codes, and fallback message.
     */
    @NonNull
    protected String extractErrorMessage(@NonNull Response<?> response, @NonNull String fallbackMessage) {
        if (response.body() instanceof ApiResponse) {
            ApiResponse<?> apiResponse = (ApiResponse<?>) response.body();
            if (apiResponse.getMessage() != null && !apiResponse.getMessage().trim().isEmpty()) {
                return apiResponse.getMessage();
            }
        }
        ResponseBody errorBody = response.errorBody();
        if (errorBody != null) {
            try {
                String errorJson = errorBody.string();
                if (errorJson != null && !errorJson.trim().isEmpty()) {
                    ApiResponse<?> errorResponse = gson.fromJson(errorJson, ApiResponse.class);
                    if (errorResponse != null) {
                        if (errorResponse.getCode() == 40006) {
                            return "Mật khẩu hiện tại không chính xác";
                        } else if (errorResponse.getCode() == 40007) {
                            return "Mật khẩu mới không được trùng mật khẩu cũ";
                        } else if (errorResponse.getCode() == 40008) {
                            return "ALREADY_JOINED";
                        } else if (errorResponse.getCode() == 40402) {
                            return "CLASS_NOT_FOUND";
                        } else if (errorResponse.getCode() == 40010 || "QR_EXPIRED".equalsIgnoreCase(errorResponse.getErrorCode())) {
                            return "QR_EXPIRED";
                        } else if (errorResponse.getCode() == 40011 || "WIFI_MISMATCH".equalsIgnoreCase(errorResponse.getErrorCode())) {
                            return "WIFI_MISMATCH";
                        } else if (errorResponse.getCode() == 40012 || "GPS_OUT_OF_RANGE".equalsIgnoreCase(errorResponse.getErrorCode())) {
                            return "GPS_OUT_OF_RANGE";
                        } else if (errorResponse.getCode() == 40013 || "DEVICE_ALREADY_USED".equalsIgnoreCase(errorResponse.getErrorCode())) {
                            return "DEVICE_ALREADY_USED";
                        }
                        if (errorResponse.getMessage() != null && !errorResponse.getMessage().trim().isEmpty()) {
                            return errorResponse.getMessage();
                        }
                    }

                    JsonObject jsonObject = gson.fromJson(errorJson, JsonObject.class);
                    if (jsonObject != null) {
                        if (jsonObject.has("error") && !jsonObject.get("error").isJsonNull()) {
                            String err = jsonObject.get("error").getAsString();
                            if (err != null && !err.trim().isEmpty()) {
                                return err;
                            }
                        }
                        if (jsonObject.has("detail") && !jsonObject.get("detail").isJsonNull()) {
                            String detail = jsonObject.get("detail").getAsString();
                            if (detail != null && !detail.trim().isEmpty()) {
                                return detail;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return fallbackMessage;
    }

    /**
     * Extracts user-friendly error message using a standard fallback message.
     */
    @NonNull
    protected String extractErrorMessage(@NonNull Response<?> response) {
        return extractErrorMessage(response, "Đã có lỗi xảy ra. Vui lòng thử lại.");
    }

    /**
     * Thread-safe LiveData state dispatcher supporting Main thread, worker threads, and test runners.
     */
    protected <V> void dispatchState(@NonNull MutableLiveData<V> liveData, @NonNull V value) {
        try {
            if (Looper.getMainLooper() != null) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    liveData.setValue(value);
                } else {
                    liveData.postValue(value);
                }
            } else {
                liveData.setValue(value);
            }
        } catch (RuntimeException e) {
            try {
                liveData.postValue(value);
            } catch (RuntimeException ignored) {
            }
        }
    }
}

