package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ppquang06.uniclassapp.data.model.request.CreateClassRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateMaterialRequest;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassMemberItemResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassStatisticsResponse;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.FileUploadResponse;
import com.ppquang06.uniclassapp.data.repository.ClassRepository;
import com.ppquang06.uniclassapp.ui.common.BaseViewModel;
import com.ppquang06.uniclassapp.ui.common.UiState;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * ViewModel managing lecturer class operations: listing classes (offline-first), creating/archiving classes,
 * class detail (offline-first), member roster & removal, course materials, file uploads, and class statistics.
 */
public class LecturerManagementViewModel extends BaseViewModel {

    private final ClassRepository classRepository;

    private final MutableLiveData<UiState<List<CourseClassItem>>> classesState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<CourseClassItem>> createClassState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<ClassDetailResponse>> classDetailState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<CourseClassItem>> archiveClassState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<List<ClassMemberItemResponse>>> membersState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<Void>> removeMemberState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<ClassDetailResponse.MaterialItem>> createMaterialState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<FileUploadResponse>> uploadFileState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<ClassStatisticsResponse>> statisticsState = new MutableLiveData<>(UiState.idle());

    public LecturerManagementViewModel(@NonNull Application application) {
        this(application, new ClassRepository(application));
    }

    public LecturerManagementViewModel(@NonNull ClassRepository classRepository) {
        this(new Application(), classRepository);
    }

    public LecturerManagementViewModel(@NonNull Application application,
                                       @NonNull ClassRepository classRepository) {
        super(application);
        this.classRepository = classRepository;
    }

    public LiveData<UiState<List<CourseClassItem>>> getClassesState() {
        return classesState;
    }

    public LiveData<UiState<CourseClassItem>> getCreateClassState() {
        return createClassState;
    }

    public LiveData<UiState<ClassDetailResponse>> getClassDetailState() {
        return classDetailState;
    }

    public LiveData<UiState<CourseClassItem>> getArchiveClassState() {
        return archiveClassState;
    }

    public LiveData<UiState<List<ClassMemberItemResponse>>> getMembersState() {
        return membersState;
    }

    public LiveData<UiState<Void>> getRemoveMemberState() {
        return removeMemberState;
    }

    public LiveData<UiState<ClassDetailResponse.MaterialItem>> getCreateMaterialState() {
        return createMaterialState;
    }

    public LiveData<UiState<FileUploadResponse>> getUploadFileState() {
        return uploadFileState;
    }

    public LiveData<UiState<ClassStatisticsResponse>> getStatisticsState() {
        return statisticsState;
    }

    public void loadClasses() {
        classRepository.fetchClassesOfflineFirst(classesState, getActiveCallsMutable());
    }

    public void createClass(@NonNull CreateClassRequest request) {
        executeCall(
                classRepository.createClass(request),
                createClassState,
                "Không thể tạo lớp học"
        );
    }

    public void loadClassDetail(@NonNull String classId) {
        classRepository.fetchClassDetailOfflineFirst(classId, classDetailState, getActiveCallsMutable());
    }

    public void loadClassDetail(@NonNull String classId,
                                @Nullable String classCode,
                                @Nullable String className,
                                @Nullable String semester) {
        loadClassDetail(classId);
    }

    public void archiveClass(@NonNull String classId) {
        executeCall(
                classRepository.archiveClass(classId),
                archiveClassState,
                "Không thể lưu trữ lớp học"
        );
    }

    public void loadClassMembers(@NonNull String classId) {
        executeCall(
                classRepository.getClassMembers(classId),
                membersState,
                "Không thể tải danh sách sinh viên"
        );
    }

    public void removeClassMember(@NonNull String classId, @NonNull String studentId) {
        executeCall(
                classRepository.removeClassMember(classId, studentId),
                removeMemberState,
                "Không thể xóa sinh viên khỏi lớp học"
        );
    }

    public void createMaterial(@NonNull String classId, @NonNull CreateMaterialRequest request) {
        executeCall(
                classRepository.createMaterial(classId, request),
                createMaterialState,
                "Không thể tải lên tài liệu"
        );
    }

    public void uploadFile(@NonNull MultipartBody.Part file, @NonNull RequestBody folder) {
        executeCall(
                classRepository.uploadFile(file, folder),
                uploadFileState,
                "Tải tệp lên thất bại"
        );
    }

    public void loadClassStatistics(@NonNull String classId) {
        executeCall(
                classRepository.getClassStatistics(classId),
                statisticsState,
                "Không thể tải thống kê lớp học"
        );
    }

    public void resetCreateClassState() {
        dispatchState(createClassState, UiState.idle());
    }

    public void resetArchiveClassState() {
        dispatchState(archiveClassState, UiState.idle());
    }

    public void resetRemoveMemberState() {
        dispatchState(removeMemberState, UiState.idle());
    }

    public void resetCreateMaterialState() {
        dispatchState(createMaterialState, UiState.idle());
    }

    public void resetUploadFileState() {
        dispatchState(uploadFileState, UiState.idle());
    }
}
