package com.ppquang06.uniclassapp.data.repository;

import android.content.Context;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.api.UserClassApiService;
import com.ppquang06.uniclassapp.data.local.UniClassDatabase;
import com.ppquang06.uniclassapp.data.local.dao.AssignmentDao;
import com.ppquang06.uniclassapp.data.local.dao.ClassDao;
import com.ppquang06.uniclassapp.data.local.entity.AssignmentEntity;
import com.ppquang06.uniclassapp.data.local.entity.ClassEntity;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.ChangePasswordRequest;
import com.ppquang06.uniclassapp.data.model.request.UpdateProfileRequest;
import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.FileUploadResponse;
import com.ppquang06.uniclassapp.data.model.response.HomeSummaryResponse;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.ui.common.UiState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository encapsulating user profile, home summary (with offline Room caching),
 * notifications, and file uploads.
 */
public class UserRepository {

    private final UserClassApiService apiService;
    @Nullable
    private final ClassDao classDao;
    @Nullable
    private final AssignmentDao assignmentDao;
    private final Executor executor;
    private final Gson gson = new Gson();

    public UserRepository(@NonNull Context context) {
        UniClassDatabase db = UniClassDatabase.getInstance(context);
        this.apiService = ApiClient.getUserClassApiService(context);
        this.classDao = db.classDao();
        this.assignmentDao = db.assignmentDao();
        this.executor = UniClassDatabase.databaseWriteExecutor;
    }

    public UserRepository(@NonNull UserClassApiService apiService) {
        this(apiService, null, null, Runnable::run);
    }

    public UserRepository(@NonNull UserClassApiService apiService,
                          @Nullable ClassDao classDao,
                          @Nullable AssignmentDao assignmentDao,
                          @NonNull Executor executor) {
        this.apiService = apiService;
        this.classDao = classDao;
        this.assignmentDao = assignmentDao;
        this.executor = executor != null ? executor : Runnable::run;
    }

    public Call<ApiResponse<UserResponse>> getCurrentUser() {
        return apiService.getCurrentUser();
    }

    public Call<ApiResponse<UserResponse>> updateCurrentUser(UpdateProfileRequest request) {
        return apiService.updateCurrentUser(request);
    }

    public Call<ApiResponse<UserResponse>> updateCurrentUser(UserResponse request) {
        return apiService.updateCurrentUser(request);
    }

    public Call<ApiResponse<Void>> changePassword(ChangePasswordRequest request) {
        return apiService.changePassword(request);
    }

    public Call<ApiResponse<HomeSummaryResponse>> getHomeSummary() {
        return apiService.getHomeSummary();
    }

    public Call<ApiResponse<List<NotificationItemResponse>>> getNotifications() {
        return apiService.getNotifications();
    }

    public Call<ApiResponse<FileUploadResponse>> uploadFile(MultipartBody.Part file, RequestBody folder) {
        return apiService.uploadFile(file, folder);
    }

    /**
     * Fetches home summary with offline-first caching into {@link ClassDao} and {@link AssignmentDao}.
     * On network success: persists courses and dueSoonAssignments, then emits {@link UiState#success(Object)}.
     * On network failure: builds a fallback {@link HomeSummaryResponse} from Room cache if available
     * and emits {@link UiState#error(String, Throwable, Object)}.
     */
    public void fetchHomeSummaryOfflineFirst(@NonNull MutableLiveData<UiState<HomeSummaryResponse>> liveData,
                                             @Nullable Set<Call<?>> activeCalls) {
        final HomeSummaryResponse previousData = liveData.getValue() != null ? liveData.getValue().getData() : null;
        dispatchState(liveData, UiState.loading(previousData));

        final AtomicBoolean networkCompleted = new AtomicBoolean(false);
        if (previousData == null && (classDao != null || assignmentDao != null)) {
            executor.execute(() -> {
                if (networkCompleted.get()) {
                    return;
                }
                HomeSummaryResponse cached = buildCachedHomeSummary();
                if (cached != null && !networkCompleted.get()) {
                    UiState<HomeSummaryResponse> current = liveData.getValue();
                    if (current == null || current.isLoading()) {
                        dispatchState(liveData, UiState.loading(cached));
                    }
                }
            });
        }

        Call<ApiResponse<HomeSummaryResponse>> call = apiService.getHomeSummary();
        if (call == null) {
            networkCompleted.set(true);
            emitFallbackHomeSummaryOnError(liveData, "Không thể tải dữ liệu trang chủ", null, previousData);
            return;
        }
        if (activeCalls != null) {
            activeCalls.add(call);
        }

        call.enqueue(new Callback<ApiResponse<HomeSummaryResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<HomeSummaryResponse>> c,
                                   @NonNull Response<ApiResponse<HomeSummaryResponse>> response) {
                if (activeCalls != null) {
                    activeCalls.remove(c);
                }
                if (c.isCanceled()) {
                    return;
                }
                networkCompleted.set(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null) {
                    final HomeSummaryResponse data = response.body().getData();
                    executor.execute(() -> {
                        if (c.isCanceled()) {
                            return;
                        }
                        try {
                            if (classDao != null && data.getCourses() != null && !data.getCourses().isEmpty()) {
                                List<ClassEntity> classEntities = new ArrayList<>();
                                for (CourseClassItem item : data.getCourses()) {
                                    ClassEntity entity = ClassEntity.fromCourseClassItem(item);
                                    if (entity != null) {
                                        classEntities.add(entity);
                                    }
                                }
                                if (!classEntities.isEmpty()) {
                                    classDao.insertClasses(classEntities);
                                }
                            }
                        } catch (Exception ignored) {
                        }

                        try {
                            if (assignmentDao != null && data.getDueSoonAssignments() != null
                                    && !data.getDueSoonAssignments().isEmpty()) {
                                List<AssignmentEntity> assignmentEntities = new ArrayList<>();
                                for (AssignmentItem item : data.getDueSoonAssignments()) {
                                    AssignmentEntity entity = AssignmentEntity.fromAssignmentItem(item);
                                    if (entity != null) {
                                        assignmentEntities.add(entity);
                                    }
                                }
                                if (!assignmentEntities.isEmpty()) {
                                    assignmentDao.insertAssignments(assignmentEntities);
                                }
                            }
                        } catch (Exception ignored) {
                        }

                        if (!c.isCanceled()) {
                            dispatchState(liveData, UiState.success(data));
                        }
                    });
                } else {
                    String errorMessage = extractErrorMessage(response, "Không thể tải dữ liệu trang chủ");
                    emitFallbackHomeSummaryOnError(liveData, errorMessage, null, previousData);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<HomeSummaryResponse>> c, @NonNull Throwable t) {
                if (activeCalls != null) {
                    activeCalls.remove(c);
                }
                if (c.isCanceled()) {
                    return;
                }
                networkCompleted.set(true);

                String errorMessage = (t.getMessage() != null && !t.getMessage().trim().isEmpty())
                        ? t.getMessage()
                        : "Lỗi kết nối máy chủ";
                emitFallbackHomeSummaryOnError(liveData, errorMessage, t, previousData);
            }
        });
    }

    private void emitFallbackHomeSummaryOnError(@NonNull MutableLiveData<UiState<HomeSummaryResponse>> liveData,
                                                @NonNull String errorMessage,
                                                @Nullable Throwable t,
                                                @Nullable HomeSummaryResponse previousData) {
        executor.execute(() -> {
            HomeSummaryResponse fallback = buildCachedHomeSummary();
            if (fallback == null) {
                UiState<HomeSummaryResponse> current = liveData.getValue();
                fallback = (current != null && current.getData() != null) ? current.getData() : previousData;
            }
            dispatchState(liveData, UiState.error(errorMessage, t, fallback));
        });
    }

    @Nullable
    private HomeSummaryResponse buildCachedHomeSummary() {
        List<CourseClassItem> cachedCourses = new ArrayList<>();
        if (classDao != null) {
            try {
                List<ClassEntity> entities = classDao.getAllClasses();
                if (entities != null) {
                    for (ClassEntity entity : entities) {
                        if (entity != null) {
                            cachedCourses.add(entity.toCourseClassItem());
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        List<AssignmentItem> cachedAssignments = new ArrayList<>();
        if (assignmentDao != null) {
            try {
                List<AssignmentEntity> entities = assignmentDao.getAllAssignments();
                if (entities != null) {
                    for (AssignmentEntity entity : entities) {
                        if (entity != null) {
                            cachedAssignments.add(entity.toAssignmentItem());
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (!cachedCourses.isEmpty() || !cachedAssignments.isEmpty()) {
            HomeSummaryResponse fallback = new HomeSummaryResponse();
            fallback.setCourses(cachedCourses);
            fallback.setDueSoonAssignments(cachedAssignments);
            fallback.setActiveCoursesCount(cachedCourses.size());
            fallback.setPendingAssignmentsCount(cachedAssignments.size());
            return fallback;
        }
        return null;
    }

    @NonNull
    private String extractErrorMessage(@NonNull Response<?> response, @NonNull String fallbackMessage) {
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
                    if (errorResponse != null && errorResponse.getMessage() != null
                            && !errorResponse.getMessage().trim().isEmpty()) {
                        return errorResponse.getMessage();
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

    private static <V> void dispatchState(@NonNull MutableLiveData<V> liveData, @NonNull V value) {
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
