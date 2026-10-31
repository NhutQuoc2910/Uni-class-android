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
import com.ppquang06.uniclassapp.data.model.request.CreateClassRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateMaterialRequest;
import com.ppquang06.uniclassapp.data.model.request.JoinClassRequest;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassGradesResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassMemberItemResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassStatisticsResponse;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.FileUploadResponse;
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
 * Repository encapsulating class management endpoints and offline Room caching
 * via {@link ClassDao} and {@link AssignmentDao}.
 */
public class ClassRepository {

    private final UserClassApiService apiService;
    @Nullable
    private final ClassDao classDao;
    @Nullable
    private final AssignmentDao assignmentDao;
    private final Executor executor;
    private final Gson gson = new Gson();

    public ClassRepository(@NonNull Context context) {
        UniClassDatabase db = UniClassDatabase.getInstance(context);
        this.apiService = ApiClient.getUserClassApiService(context);
        this.classDao = db.classDao();
        this.assignmentDao = db.assignmentDao();
        this.executor = UniClassDatabase.databaseWriteExecutor;
    }

    public ClassRepository(@NonNull UserClassApiService apiService) {
        this(apiService, null, null, Runnable::run);
    }

    public ClassRepository(@NonNull UserClassApiService apiService,
                           @Nullable ClassDao classDao,
                           @NonNull Executor executor) {
        this(apiService, classDao, null, executor);
    }

    public ClassRepository(@NonNull UserClassApiService apiService,
                           @Nullable ClassDao classDao,
                           @Nullable AssignmentDao assignmentDao,
                           @NonNull Executor executor) {
        this.apiService = apiService;
        this.classDao = classDao;
        this.assignmentDao = assignmentDao;
        this.executor = executor != null ? executor : Runnable::run;
    }

    public Call<ApiResponse<List<CourseClassItem>>> getClasses() {
        return apiService.getClasses();
    }

    public Call<ApiResponse<ClassDetailResponse>> getClassDetail(String classId) {
        return apiService.getClassDetail(classId);
    }

    public Call<ApiResponse<CourseClassItem>> joinClass(JoinClassRequest request) {
        return apiService.joinClass(request);
    }

    public Call<ApiResponse<CourseClassItem>> createClass(CreateClassRequest request) {
        return apiService.createClass(request);
    }

    public Call<ApiResponse<CourseClassItem>> archiveClass(String classId) {
        return apiService.archiveClass(classId);
    }

    public Call<ApiResponse<List<ClassMemberItemResponse>>> getClassMembers(String classId) {
        return apiService.getClassMembers(classId);
    }

    public Call<ApiResponse<Void>> removeClassMember(String classId, String studentId) {
        return apiService.removeClassMember(classId, studentId);
    }

    public Call<ApiResponse<ClassDetailResponse.MaterialItem>> createMaterial(String classId,
                                                                              CreateMaterialRequest request) {
        return apiService.createMaterial(classId, request);
    }

    public Call<ApiResponse<ClassStatisticsResponse>> getClassStatistics(String classId) {
        return apiService.getClassStatistics(classId);
    }

    public Call<ApiResponse<ClassGradesResponse>> getClassGrades(String classId) {
        return apiService.getClassGrades(classId);
    }

    public Call<ApiResponse<FileUploadResponse>> uploadFile(MultipartBody.Part file, RequestBody folder) {
        return apiService.uploadFile(file, folder);
    }

    /**
     * Fetches the list of classes with offline-first Room caching via {@link ClassDao}.
     * <ul>
     *   <li>Reads cached classes on the background executor to emit {@link UiState#loading(Object)} if non-empty.</li>
     *   <li>On network success: clears and saves fresh classes into {@link ClassDao} and emits {@link UiState#success(Object)}.</li>
     *   <li>On network failure: reads cached classes from {@link ClassDao} and emits {@link UiState#error(String, Throwable, Object)}.</li>
     * </ul>
     */
    public void fetchClassesOfflineFirst(@NonNull MutableLiveData<UiState<List<CourseClassItem>>> liveData,
                                         @Nullable Set<Call<?>> activeCalls) {
        final List<CourseClassItem> previousData = liveData.getValue() != null ? liveData.getValue().getData() : null;
        dispatchState(liveData, UiState.loading(previousData));

        final AtomicBoolean networkCompleted = new AtomicBoolean(false);
        if (classDao != null) {
            executor.execute(() -> {
                if (networkCompleted.get()) {
                    return;
                }
                List<CourseClassItem> cached = getCachedCourseClasses();
                if (!cached.isEmpty() && !networkCompleted.get()) {
                    UiState<List<CourseClassItem>> current = liveData.getValue();
                    if (current == null || current.isLoading()) {
                        dispatchState(liveData, UiState.loading(cached));
                    }
                }
            });
        }

        Call<ApiResponse<List<CourseClassItem>>> call = apiService.getClasses();
        if (call == null) {
            networkCompleted.set(true);
            emitCachedClassesOnError(liveData, "Không thể tải danh sách lớp học", null, previousData);
            return;
        }
        if (activeCalls != null) {
            activeCalls.add(call);
        }

        call.enqueue(new Callback<ApiResponse<List<CourseClassItem>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CourseClassItem>>> c,
                                   @NonNull Response<ApiResponse<List<CourseClassItem>>> response) {
                if (activeCalls != null) {
                    activeCalls.remove(c);
                }
                if (c.isCanceled()) {
                    return;
                }
                networkCompleted.set(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null) {
                    final List<CourseClassItem> freshClasses = response.body().getData();
                    executor.execute(() -> {
                        if (c.isCanceled()) {
                            return;
                        }
                        if (classDao != null) {
                            try {
                                List<ClassEntity> entities = new ArrayList<>();
                                for (CourseClassItem item : freshClasses) {
                                    ClassEntity entity = ClassEntity.fromCourseClassItem(item);
                                    if (entity != null) {
                                        entities.add(entity);
                                    }
                                }
                                classDao.clearClasses();
                                if (!entities.isEmpty()) {
                                    classDao.insertClasses(entities);
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        if (!c.isCanceled()) {
                            dispatchState(liveData, UiState.success(freshClasses));
                        }
                    });
                } else {
                    String errorMessage = extractErrorMessage(response, "Không thể tải danh sách lớp học");
                    emitCachedClassesOnError(liveData, errorMessage, null, previousData);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<CourseClassItem>>> c, @NonNull Throwable t) {
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
                emitCachedClassesOnError(liveData, errorMessage, t, previousData);
            }
        });
    }

    /**
     * Fetches class detail with offline-first Room caching via {@link ClassDao} and {@link AssignmentDao}.
     * <ul>
     *   <li>On network success: caches assignments into {@link AssignmentDao} and updates the class record in {@link ClassDao}, then emits {@link UiState#success(Object)}.</li>
     *   <li>On network failure: reconstructs a fallback {@link ClassDetailResponse} from {@link ClassDao} and {@link AssignmentDao} if cached data is found, and emits {@link UiState#error(String, Throwable, Object)}.</li>
     * </ul>
     */
    public void fetchClassDetailOfflineFirst(@NonNull String classId,
                                             @NonNull MutableLiveData<UiState<ClassDetailResponse>> liveData,
                                             @Nullable Set<Call<?>> activeCalls) {
        final ClassDetailResponse previousData = liveData.getValue() != null ? liveData.getValue().getData() : null;
        dispatchState(liveData, UiState.loading(previousData));

        final AtomicBoolean networkCompleted = new AtomicBoolean(false);
        if (previousData == null && (classDao != null || assignmentDao != null)) {
            executor.execute(() -> {
                if (networkCompleted.get()) {
                    return;
                }
                ClassDetailResponse cachedDetail = buildCachedClassDetail(classId);
                if (cachedDetail != null && !networkCompleted.get()) {
                    UiState<ClassDetailResponse> current = liveData.getValue();
                    if (current == null || current.isLoading()) {
                        dispatchState(liveData, UiState.loading(cachedDetail));
                    }
                }
            });
        }

        Call<ApiResponse<ClassDetailResponse>> call = apiService.getClassDetail(classId);
        if (call == null) {
            networkCompleted.set(true);
            emitCachedClassDetailOnError(classId, liveData, "Không thể tải thông tin lớp học", null, previousData);
            return;
        }
        if (activeCalls != null) {
            activeCalls.add(call);
        }

        call.enqueue(new Callback<ApiResponse<ClassDetailResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ClassDetailResponse>> c,
                                   @NonNull Response<ApiResponse<ClassDetailResponse>> response) {
                if (activeCalls != null) {
                    activeCalls.remove(c);
                }
                if (c.isCanceled()) {
                    return;
                }
                networkCompleted.set(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null) {
                    final ClassDetailResponse detail = response.body().getData();
                    executor.execute(() -> {
                        if (c.isCanceled()) {
                            return;
                        }
                        String effectiveClassId = (detail.getId() != null && !detail.getId().isEmpty())
                                ? detail.getId()
                                : classId;

                        if (classDao != null && effectiveClassId != null) {
                            try {
                                ClassEntity existing = classDao.getClassById(effectiveClassId);
                                ClassEntity classEntity = new ClassEntity();
                                classEntity.setId(effectiveClassId);
                                classEntity.setCode(detail.getCode() != null ? detail.getCode()
                                        : (existing != null ? existing.getCode() : null));
                                classEntity.setName(detail.getName() != null ? detail.getName()
                                        : (existing != null ? existing.getName() : null));
                                classEntity.setSemester(detail.getSemester() != null ? detail.getSemester()
                                        : (existing != null ? existing.getSemester() : null));
                                classEntity.setRoom(detail.getRoom() != null ? detail.getRoom()
                                        : (existing != null ? existing.getRoom() : null));
                                classEntity.setLecturerName(detail.getLecturerName() != null ? detail.getLecturerName()
                                        : (existing != null ? existing.getLecturerName() : null));
                                classEntity.setInviteCode(detail.getInviteCode() != null ? detail.getInviteCode()
                                        : (existing != null ? existing.getInviteCode() : null));
                                classEntity.setStatus(existing != null && existing.getStatus() != null
                                        ? existing.getStatus() : "ACTIVE");
                                classEntity.setProgress(existing != null ? existing.getProgress()
                                        : detail.getAttendanceRate());
                                classEntity.setEnrolledCount(detail.getEnrolledCount());
                                classEntity.setUpdatedAt(System.currentTimeMillis());
                                classDao.insertClass(classEntity);
                            } catch (Exception ignored) {
                            }
                        }

                        if (assignmentDao != null && effectiveClassId != null) {
                            try {
                                assignmentDao.clearByClass(effectiveClassId);
                                List<ClassDetailResponse.ClassAssignmentItem> items = detail.getAssignments();
                                if (items != null && !items.isEmpty()) {
                                    List<AssignmentEntity> assignmentEntities = new ArrayList<>();
                                    for (ClassDetailResponse.ClassAssignmentItem item : items) {
                                        AssignmentEntity entity = AssignmentEntity.fromClassAssignmentItem(
                                                effectiveClassId,
                                                detail.getName(),
                                                item
                                        );
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
                        }

                        if (!c.isCanceled()) {
                            dispatchState(liveData, UiState.success(detail));
                        }
                    });
                } else {
                    String errorMessage = extractErrorMessage(response, "Không thể tải thông tin lớp học");
                    emitCachedClassDetailOnError(classId, liveData, errorMessage, null, previousData);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<ClassDetailResponse>> c, @NonNull Throwable t) {
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
                emitCachedClassDetailOnError(classId, liveData, errorMessage, t, previousData);
            }
        });
    }

    private void emitCachedClassesOnError(@NonNull MutableLiveData<UiState<List<CourseClassItem>>> liveData,
                                          @NonNull String errorMessage,
                                          @Nullable Throwable t,
                                          @Nullable List<CourseClassItem> previousData) {
        executor.execute(() -> {
            List<CourseClassItem> cached = getCachedCourseClasses();
            List<CourseClassItem> fallback = !cached.isEmpty() ? cached : previousData;
            if (fallback == null) {
                UiState<List<CourseClassItem>> current = liveData.getValue();
                fallback = current != null ? current.getData() : null;
            }
            dispatchState(liveData, UiState.error(errorMessage, t, fallback));
        });
    }

    private void emitCachedClassDetailOnError(@NonNull String classId,
                                              @NonNull MutableLiveData<UiState<ClassDetailResponse>> liveData,
                                              @NonNull String errorMessage,
                                              @Nullable Throwable t,
                                              @Nullable ClassDetailResponse previousData) {
        executor.execute(() -> {
            ClassDetailResponse fallbackDetail = buildCachedClassDetail(classId);
            if (fallbackDetail == null) {
                UiState<ClassDetailResponse> current = liveData.getValue();
                fallbackDetail = (current != null && current.getData() != null) ? current.getData() : previousData;
            }
            dispatchState(liveData, UiState.error(errorMessage, t, fallbackDetail));
        });
    }

    @Nullable
    private ClassDetailResponse buildCachedClassDetail(@NonNull String classId) {
        ClassEntity cachedClass = null;
        if (classDao != null) {
            try {
                cachedClass = classDao.getClassById(classId);
            } catch (Exception ignored) {
            }
        }

        List<AssignmentEntity> cachedAssignments = null;
        if (assignmentDao != null) {
            try {
                cachedAssignments = assignmentDao.getAssignmentsByClass(classId);
            } catch (Exception ignored) {
            }
        }

        boolean hasAssignments = cachedAssignments != null && !cachedAssignments.isEmpty();
        if (cachedClass != null || hasAssignments) {
            ClassDetailResponse fallbackDetail = new ClassDetailResponse();
            fallbackDetail.setId(classId);
            if (cachedClass != null) {
                fallbackDetail.setId(cachedClass.getId());
                fallbackDetail.setCode(cachedClass.getCode());
                fallbackDetail.setName(cachedClass.getName());
                fallbackDetail.setSemester(cachedClass.getSemester());
                fallbackDetail.setRoom(cachedClass.getRoom());
                fallbackDetail.setLecturerName(cachedClass.getLecturerName());
                fallbackDetail.setInviteCode(cachedClass.getInviteCode());
                fallbackDetail.setEnrolledCount(cachedClass.getEnrolledCount());
                fallbackDetail.setAttendanceRate(cachedClass.getProgress());
            }
            if (hasAssignments) {
                List<ClassDetailResponse.ClassAssignmentItem> items = new ArrayList<>();
                for (AssignmentEntity entity : cachedAssignments) {
                    if (entity != null) {
                        items.add(entity.toClassAssignmentItem());
                        if (fallbackDetail.getName() == null && entity.getClassName() != null) {
                            fallbackDetail.setName(entity.getClassName());
                        }
                    }
                }
                fallbackDetail.setAssignments(items);
            }
            return fallbackDetail;
        }
        return null;
    }

    @NonNull
    private List<CourseClassItem> getCachedCourseClasses() {
        List<CourseClassItem> result = new ArrayList<>();
        if (classDao != null) {
            try {
                List<ClassEntity> entities = classDao.getAllClasses();
                if (entities != null) {
                    for (ClassEntity entity : entities) {
                        if (entity != null) {
                            result.add(entity.toCourseClassItem());
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return result;
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
