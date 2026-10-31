package com.ppquang06.uniclassapp.ui.student.classes;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ppquang06.uniclassapp.data.model.request.JoinClassRequest;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassGradesResponse;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.repository.ClassRepository;
import com.ppquang06.uniclassapp.ui.common.BaseViewModel;
import com.ppquang06.uniclassapp.ui.common.UiState;

import java.util.List;

/**
 * ViewModel for student class listing, joining a class, class details (with offline Room caching),
 * and class gradebook. Preserves legacy LiveData getters for child tab fragments.
 */
public class ClassDetailViewModel extends BaseViewModel {

    private final ClassRepository classRepository;

    private final MutableLiveData<ClassDetailResponse> classDetail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    private final MutableLiveData<UiState<ClassDetailResponse>> classDetailState =
            new MutableLiveData<UiState<ClassDetailResponse>>(UiState.idle()) {
                @Override
                public void setValue(UiState<ClassDetailResponse> value) {
                    super.setValue(value);
                    syncLegacyLiveData(value);
                }
            };

    private final MutableLiveData<UiState<List<CourseClassItem>>> classesState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<CourseClassItem>> joinClassState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<ClassGradesResponse>> classGradesState = new MutableLiveData<>(UiState.idle());

    private String lastClassId;
    private String lastClassCode;
    private String lastClassName;
    private String lastSemester;

    public ClassDetailViewModel(@NonNull Application application) {
        this(application, new ClassRepository(application));
    }

    public ClassDetailViewModel(@NonNull ClassRepository classRepository) {
        this(new Application(), classRepository);
    }

    public ClassDetailViewModel(@NonNull Application application, @NonNull ClassRepository classRepository) {
        super(application);
        this.classRepository = classRepository;
    }

    public LiveData<ClassDetailResponse> getClassDetail() {
        return classDetail;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<UiState<ClassDetailResponse>> getClassDetailState() {
        return classDetailState;
    }

    public LiveData<UiState<List<CourseClassItem>>> getClassesState() {
        return classesState;
    }

    public LiveData<UiState<CourseClassItem>> getJoinClassState() {
        return joinClassState;
    }

    public LiveData<UiState<ClassGradesResponse>> getClassGradesState() {
        return classGradesState;
    }

    public String getLastClassId() {
        return lastClassId != null ? lastClassId : "";
    }

    public String getLastClassCode() {
        return lastClassCode != null ? lastClassCode : "";
    }

    public String getLastClassName() {
        return lastClassName != null ? lastClassName : "";
    }

    public String getLastSemester() {
        return lastSemester != null ? lastSemester : "";
    }

    public void setClassDetail(@Nullable ClassDetailResponse detail) {
        dispatchState(classDetail, detail);
        if (detail != null) {
            superDispatchClassDetailState(UiState.success(detail));
        }
    }

    private void superDispatchClassDetailState(@NonNull UiState<ClassDetailResponse> state) {
        dispatchState(classDetailState, state);
    }

    public void loadClasses() {
        classRepository.fetchClassesOfflineFirst(classesState, getActiveCallsMutable());
    }

    public void joinClass(@NonNull String inviteCode) {
        joinClass(new JoinClassRequest(inviteCode));
    }

    public void joinClass(@NonNull JoinClassRequest request) {
        executeCall(
                classRepository.joinClass(request),
                joinClassState,
                "Không thể tham gia lớp học. Vui lòng kiểm tra mã lớp."
        );
    }

    public void resetJoinClassState() {
        dispatchState(joinClassState, UiState.idle());
    }

    public void loadClassGrades(@NonNull String classId) {
        executeCall(
                classRepository.getClassGrades(classId),
                classGradesState,
                "Không thể tải bảng điểm lớp học"
        );
    }

    public void loadClassDetail(@Nullable Context context,
                                @NonNull String classId,
                                @Nullable String classCode,
                                @Nullable String className,
                                @Nullable String semester) {
        loadClassDetail(classId, classCode, className, semester);
    }

    public void loadClassDetail(@NonNull String classId,
                                @Nullable String classCode,
                                @Nullable String className,
                                @Nullable String semester) {
        this.lastClassId = classId;
        this.lastClassCode = classCode;
        this.lastClassName = className;
        this.lastSemester = semester;

        classRepository.fetchClassDetailOfflineFirst(classId, classDetailState, getActiveCallsMutable());
    }

    public void loadClassDetail(@NonNull String classId) {
        loadClassDetail(classId, lastClassCode, lastClassName, lastSemester);
    }

    public void refresh(@Nullable Context context) {
        refresh();
    }

    public void refresh() {
        if (lastClassId == null) {
            return;
        }
        loadClassDetail(lastClassId, lastClassCode, lastClassName, lastSemester);
    }

    private void syncLegacyLiveData(@Nullable UiState<ClassDetailResponse> state) {
        if (state == null || state.isIdle()) {
            return;
        }
        if (state.isLoading()) {
            dispatchState(isLoading, true);
            dispatchState(errorMessage, null);
            ClassDetailResponse cached = state.getData();
            if (cached != null) {
                enrichFallbackFields(cached);
                dispatchState(classDetail, cached);
            }
        } else if (state.isSuccess()) {
            dispatchState(isLoading, false);
            dispatchState(errorMessage, null);
            ClassDetailResponse data = state.getData();
            if (data != null) {
                enrichFallbackFields(data);
            }
            dispatchState(classDetail, data);
        } else if (state.isError()) {
            dispatchState(isLoading, false);
            dispatchState(errorMessage, state.getMessage());
            ClassDetailResponse fallback = state.getData();
            if (fallback != null) {
                enrichFallbackFields(fallback);
            }
            dispatchState(classDetail, fallback);
        }
    }

    private void enrichFallbackFields(@NonNull ClassDetailResponse detail) {
        if ((detail.getCode() == null || detail.getCode().isEmpty()) && lastClassCode != null) {
            detail.setCode(lastClassCode);
        }
        if ((detail.getName() == null || detail.getName().isEmpty()) && lastClassName != null) {
            detail.setName(lastClassName);
        }
        if ((detail.getSemester() == null || detail.getSemester().isEmpty()) && lastSemester != null) {
            detail.setSemester(lastSemester);
        }
    }
}
