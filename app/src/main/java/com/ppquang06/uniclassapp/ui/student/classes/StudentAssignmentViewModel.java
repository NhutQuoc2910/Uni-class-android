package com.ppquang06.uniclassapp.ui.student.classes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ppquang06.uniclassapp.data.model.request.SubmitAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.response.AssignmentDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.FileUploadResponse;
import com.ppquang06.uniclassapp.data.repository.AssignmentRepository;
import com.ppquang06.uniclassapp.ui.common.BaseViewModel;
import com.ppquang06.uniclassapp.ui.common.UiState;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * ViewModel managing student assignment details, file upload, and assignment submission.
 */
public class StudentAssignmentViewModel extends BaseViewModel {

    private final AssignmentRepository assignmentRepository;

    private final MutableLiveData<UiState<AssignmentDetailResponse>> assignmentDetailState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<FileUploadResponse>> uploadFileState = new MutableLiveData<>(UiState.idle());
    private final MutableLiveData<UiState<AssignmentDetailResponse>> submitAssignmentState = new MutableLiveData<>(UiState.idle());

    public StudentAssignmentViewModel(@NonNull Application application) {
        this(application, new AssignmentRepository(application));
    }

    public StudentAssignmentViewModel(@NonNull AssignmentRepository assignmentRepository) {
        this(new Application(), assignmentRepository);
    }

    public StudentAssignmentViewModel(@NonNull Application application,
                                      @NonNull AssignmentRepository assignmentRepository) {
        super(application);
        this.assignmentRepository = assignmentRepository;
    }

    public LiveData<UiState<AssignmentDetailResponse>> getAssignmentDetailState() {
        return assignmentDetailState;
    }

    public LiveData<UiState<FileUploadResponse>> getUploadFileState() {
        return uploadFileState;
    }

    public LiveData<UiState<AssignmentDetailResponse>> getSubmitAssignmentState() {
        return submitAssignmentState;
    }

    public void loadAssignmentDetail(@NonNull String assignmentId) {
        executeCall(
                assignmentRepository.getAssignmentDetail(assignmentId),
                assignmentDetailState,
                "Không thể tải chi tiết bài tập"
        );
    }

    public void uploadFile(@NonNull MultipartBody.Part file, @NonNull RequestBody folder) {
        executeCall(
                assignmentRepository.uploadFile(file, folder),
                uploadFileState,
                "Tải tệp lên thất bại"
        );
    }

    public void submitAssignment(@NonNull String assignmentId, @NonNull SubmitAssignmentRequest request) {
        executeCall(
                assignmentRepository.submitAssignment(assignmentId, request),
                submitAssignmentState,
                "Nộp bài tập thất bại"
        );
    }

    public void resetUploadFileState() {
        dispatchState(uploadFileState, UiState.idle());
    }

    public void resetSubmitAssignmentState() {
        dispatchState(submitAssignmentState, UiState.idle());
    }
}
