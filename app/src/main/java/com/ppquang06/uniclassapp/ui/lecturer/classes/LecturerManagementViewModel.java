package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ppquang06.uniclassapp.data.model.request.CreateClassRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateClassScheduleRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateMaterialRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateScheduleExceptionRequest;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassMemberItemResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassScheduleItem;
import com.ppquang06.uniclassapp.data.model.response.ClassStatisticsResponse;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.FileUploadResponse;
import com.ppquang06.uniclassapp.data.model.response.ScheduleExceptionItem;
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
    private final MutableLiveData<UiState<Void>> deleteClassState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<List<ClassMemberItemResponse>>> membersState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<Void>> removeMemberState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<ClassDetailResponse.MaterialItem>> createMaterialState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<FileUploadResponse>> uploadFileState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<ClassStatisticsResponse>> statisticsState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<List<ClassScheduleItem>>> classSchedulesState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<ClassScheduleItem>> createScheduleState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<List<ScheduleExceptionItem>>> classExceptionsState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<ScheduleExceptionItem>> createExceptionState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<ScheduleExceptionItem>> exceptionActionState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<Void>> deleteItemState = new MutableLiveData<>(UiState.idle());

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

    public LiveData<UiState<Void>> getDeleteClassState() {
        return deleteClassState;
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

    public LiveData<UiState<List<ClassScheduleItem>>> getClassSchedulesState() {
        return classSchedulesState;
    }

    public LiveData<UiState<ClassScheduleItem>> getCreateScheduleState() {
        return createScheduleState;
    }

    public LiveData<UiState<List<ScheduleExceptionItem>>> getClassExceptionsState() {
        return classExceptionsState;
    }

    public LiveData<UiState<ScheduleExceptionItem>> getCreateExceptionState() {
        return createExceptionState;
    }

    public LiveData<UiState<ScheduleExceptionItem>> getExceptionActionState() {
        return exceptionActionState;
    }

    public LiveData<UiState<Void>> getDeleteItemState() {
        return deleteItemState;
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

    public void deleteClass(@NonNull String classId) {
        executeCall(
                classRepository.deleteClass(classId),
                deleteClassState,
                "Xóa lớp học thất bại"
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

    public void loadClassSchedules(@NonNull String classId) {
        executeCall(
                classRepository.getClassSchedules(classId),
                classSchedulesState,
                "Không thể tải lịch dạy của lớp"
        );
    }

    public void createClassSchedule(@NonNull String classId, @NonNull CreateClassScheduleRequest request) {
        executeCall(
                classRepository.createClassSchedule(classId, request),
                createScheduleState,
                "Không thể tạo lịch dạy"
        );
    }

    public void updateClassSchedule(@NonNull String classId, @NonNull String scheduleId, @NonNull CreateClassScheduleRequest request) {
        executeCall(
                classRepository.updateClassSchedule(classId, scheduleId, request),
                createScheduleState,
                "Không thể cập nhật lịch dạy"
        );
    }

    public void loadClassExceptions(@NonNull String classId) {
        executeCall(
                classRepository.getClassScheduleExceptions(classId),
                classExceptionsState,
                "Không thể tải danh sách thay đổi lịch dạy"
        );
    }

    public void createScheduleException(@NonNull String classId, @NonNull CreateScheduleExceptionRequest request) {
        executeCall(
                classRepository.createScheduleException(classId, request),
                createExceptionState,
                "Không thể tạo thay đổi lịch dạy"
        );
    }

    public void publishScheduleException(@NonNull String classId, @NonNull String exceptionId) {
        executeCall(
                classRepository.publishScheduleException(classId, exceptionId),
                exceptionActionState,
                "Không thể công bố thay đổi lịch dạy"
        );
    }

    public void revokeScheduleException(@NonNull String classId, @NonNull String exceptionId) {
        executeCall(
                classRepository.revokeScheduleException(classId, exceptionId),
                exceptionActionState,
                "Không thể thu hồi thay đổi lịch dạy"
        );
    }

    public void deleteMaterial(@NonNull String classId, @NonNull String materialId) {
        executeCall(
                classRepository.deleteMaterial(classId, materialId),
                deleteItemState,
                "Không thể xóa tài liệu"
        );
    }

    public void deleteAssignment(@NonNull String classId, @NonNull String assignmentId) {
        executeCall(
                classRepository.deleteAssignment(classId, assignmentId),
                deleteItemState,
                "Không thể xóa bài tập"
        );
    }

    public void deleteClassSchedule(@NonNull String classId, @NonNull String scheduleId) {
        executeCall(
                classRepository.deleteClassSchedule(classId, scheduleId),
                deleteItemState,
                "Không thể xóa lịch dạy"
        );
    }

    public void resetCreateClassState() {
        dispatchState(createClassState, UiState.idle());
    }

    public void resetArchiveClassState() {
        dispatchState(archiveClassState, UiState.idle());
    }

    public void resetDeleteClassState() {
        dispatchState(deleteClassState, UiState.idle());
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

    public void resetCreateScheduleState() {
        dispatchState(createScheduleState, UiState.idle());
    }

    public void resetCreateExceptionState() {
        dispatchState(createExceptionState, UiState.idle());
    }

    public void resetExceptionActionState() {
        dispatchState(exceptionActionState, UiState.idle());
    }

    public void resetDeleteItemState() {
        dispatchState(deleteItemState, UiState.idle());
    }
}
