package com.ppquang06.uniclassapp.ui.lecturer.assignments;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ppquang06.uniclassapp.data.model.request.CreateAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.request.GradeSubmissionRequest;
import com.ppquang06.uniclassapp.data.model.response.AssignmentSubmissionListResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.FileUploadResponse;
import com.ppquang06.uniclassapp.data.repository.AssignmentRepository;
import com.ppquang06.uniclassapp.ui.common.BaseViewModel;
import com.ppquang06.uniclassapp.ui.common.UiState;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * ViewModel managing lecturer assignment creation, attachment upload, submission list retrieval,
 * and student submission grading.
 */
public class GradingViewModel extends BaseViewModel {

    private final AssignmentRepository assignmentRepository;

    private final MutableLiveData<UiState<ClassDetailResponse.ClassAssignmentItem>> createAssignmentState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<FileUploadResponse>> uploadFileState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<AssignmentSubmissionListResponse>> submissionsState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<AssignmentSubmissionListResponse>> gradeState = new MutableLiveData<>(UiState.idle());

    public GradingViewModel(@NonNull Application application) {
        this(application, new AssignmentRepository(application));
    }

    public GradingViewModel(@NonNull AssignmentRepository assignmentRepository) {
        this(new Application(), assignmentRepository);
    }

    public GradingViewModel(@NonNull Application application,
                            @NonNull AssignmentRepository assignmentRepository) {
        super(application);
        this.assignmentRepository = assignmentRepository;
    }

    public LiveData<UiState<ClassDetailResponse.ClassAssignmentItem>> getCreateAssignmentState() {
        return createAssignmentState;
    }

    public LiveData<UiState<FileUploadResponse>> getUploadFileState() {
        return uploadFileState;
    }

    public LiveData<UiState<AssignmentSubmissionListResponse>> getSubmissionsState() {
        return submissionsState;
    }

    public LiveData<UiState<AssignmentSubmissionListResponse>> getGradeState() {
        return gradeState;
    }

    public void createAssignment(@NonNull String classId, @NonNull CreateAssignmentRequest request) {
        executeCall(
                assignmentRepository.createAssignment(classId, request),
                createAssignmentState,
                "Không thể tạo bài tập"
        );
    }

    public void uploadFile(@NonNull MultipartBody.Part file, @NonNull RequestBody folder) {
        executeCall(
                assignmentRepository.uploadFile(file, folder),
                uploadFileState,
                "Tải tệp lên thất bại"
        );
    }

    public void loadSubmissions(@NonNull String assignmentId) {
        executeCall(
                assignmentRepository.getAssignmentSubmissions(assignmentId),
                submissionsState,
                "Không thể tải danh sách bài nộp"
        );
    }

    public void gradeSubmission(@NonNull String assignmentId,
                                @NonNull String studentId,
                                @NonNull GradeSubmissionRequest request) {
        executeCall(
                assignmentRepository.gradeSubmission(assignmentId, studentId, request),
                gradeState,
                "Chấm điểm thất bại"
        );
    }

    public void resetCreateAssignmentState() {
        dispatchState(createAssignmentState, UiState.idle());
    }

    public void resetUploadFileState() {
        dispatchState(uploadFileState, UiState.idle());
    }

    public void resetGradeState() {
        dispatchState(gradeState, UiState.idle());
    }
}
