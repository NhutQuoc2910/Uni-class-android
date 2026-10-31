package com.ppquang06.uniclassapp.ui.common;

import android.app.Application;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ppquang06.uniclassapp.data.model.ApiResponse;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Base ViewModel that provides thread-safe Retrofit Call lifecycle management,
 * automatic UiState loading/success/error dispatching, and call cancellation on cleanup.
 */
public class BaseViewModel extends AndroidViewModel {

    private final Set<Call<?>> activeCalls = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Gson gson = new Gson();

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Executes a Retrofit Call with standard loading, success, and fallback error handling.
     *
     * @param call     Retrofit Call instance returning ApiResponse<T>.
     * @param liveData MutableLiveData target to receive UiState updates.
     * @param <T>      Type of data payload.
     */
    public <T> void executeCall(@NonNull Call<ApiResponse<T>> call,
                                @NonNull MutableLiveData<UiState<T>> liveData) {
        executeCall(call, liveData, null);
    }

    /**
     * Executes a Retrofit Call with standard loading, success, and customizable error fallback.
     *
     * @param call                Retrofit Call instance returning ApiResponse<T>.
     * @param liveData            MutableLiveData target to receive UiState updates.
     * @param defaultErrorMessage Fallback error message if the response error body cannot be parsed.
     * @param <T>                 Type of data payload.
     */
    public <T> void executeCall(@NonNull Call<ApiResponse<T>> call,
                                @NonNull MutableLiveData<UiState<T>> liveData,
                                @Nullable String defaultErrorMessage) {
        if (call == null || liveData == null) {
            return;
        }

        activeCalls.add(call);
        final T previousData = liveData.getValue() != null ? liveData.getValue().getData() : null;
        dispatchState(liveData, UiState.loading(previousData));

        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<T>> c,
                                   @NonNull Response<ApiResponse<T>> response) {
                activeCalls.remove(c);
                if (c.isCanceled()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    dispatchState(liveData, UiState.success(response.body().getData()));
                } else {
                    String errorMessage = null;

                    // 1. Try body message
                    if (response.body() != null && response.body().getMessage() != null
                            && !response.body().getMessage().trim().isEmpty()) {
                        errorMessage = response.body().getMessage();
                    }

                    // 2. Try errorBody JSON parsing if body message is not available
                    if (errorMessage == null) {
                        errorMessage = parseErrorBodyMessage(response.errorBody());
                    }

                    // 3. Fallback to defaultErrorMessage or generic message
                    if (errorMessage == null || errorMessage.trim().isEmpty()) {
                        errorMessage = (defaultErrorMessage != null && !defaultErrorMessage.trim().isEmpty())
                                ? defaultErrorMessage
                                : "Đã có lỗi xảy ra. Vui lòng thử lại.";
                    }

                    dispatchState(liveData, UiState.error(errorMessage, previousData));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<T>> c, @NonNull Throwable t) {
                activeCalls.remove(c);
                if (c.isCanceled()) {
                    return;
                }

                String errorMessage = (t.getMessage() != null && !t.getMessage().trim().isEmpty())
                        ? t.getMessage()
                        : ((defaultErrorMessage != null && !defaultErrorMessage.trim().isEmpty())
                                ? defaultErrorMessage
                                : "Lỗi kết nối máy chủ");

                dispatchState(liveData, UiState.error(errorMessage, t, previousData));
            }
        });
    }

    /**
     * Thread-safe LiveData state dispatcher supporting Main thread, worker threads, and test runners.
     */
    protected <V> void dispatchState(@NonNull MutableLiveData<V> liveData, @NonNull V value) {
        if (Looper.getMainLooper() != null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                liveData.setValue(value);
            } else {
                liveData.postValue(value);
            }
        } else {
            // Headless unit tests or test executors
            try {
                liveData.setValue(value);
            } catch (IllegalStateException e) {
                liveData.postValue(value);
            }
        }
    }

    @Nullable
    private String parseErrorBodyMessage(@Nullable ResponseBody errorBody) {
        if (errorBody == null) {
            return null;
        }
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

                // Fallback check for standard Spring Boot "error" or "detail" fields
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
            // Error body not JSON or unreadable
        }
        return null;
    }

    /**
     * Returns an unmodifiable view of currently active Retrofit calls.
     */
    public Set<Call<?>> getActiveCalls() {
        return Collections.unmodifiableSet(activeCalls);
    }

    /**
     * Returns the mutable set of active Retrofit calls for subclasses delegating to repository methods.
     */
    protected Set<Call<?>> getActiveCallsMutable() {
        return activeCalls;
    }

    /**
     * Cancels all tracked active calls and clears the active set safely.
     */
    public void cancelAllCalls() {
        for (Call<?> call : activeCalls) {
            if (call != null && !call.isCanceled()) {
                try {
                    call.cancel();
                } catch (Exception ignored) {
                    // Suppress exceptions from mock/underlying transport cancellation
                }
            }
        }
        activeCalls.clear();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelAllCalls();
    }
}
