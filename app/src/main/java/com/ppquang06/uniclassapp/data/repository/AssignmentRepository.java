package com.ppquang06.uniclassapp.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.api.UserClassApiService;
import com.ppquang06.uniclassapp.data.local.UniClassDatabase;
import com.ppquang06.uniclassapp.data.local.dao.AssignmentDao;
import com.ppquang06.uniclassapp.data.local.entity.AssignmentEntity;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.CreateAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.request.GradeSubmissionRequest;
import com.ppquang06.uniclassapp.data.model.request.SubmitAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.response.AssignmentDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.AssignmentSubmissionListResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.FileUploadResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * Repository encapsulating assignment, submission, and grading operations along with
 * offline Room caching via {@link AssignmentDao}.
 */
public class AssignmentRepository {

    private final UserClassApiService apiService;
    @Nullable
    private final AssignmentDao assignmentDao;
    private final Executor executor;

    public AssignmentRepository(@NonNull Context context) {
        UniClassDatabase db = UniClassDatabase.getInstance(context);
        this.apiService = ApiClient.getUserClassApiService(context);
        this.assignmentDao = db.assignmentDao();
        this.executor = UniClassDatabase.databaseWriteExecutor;
    }

    public AssignmentRepository(@NonNull UserClassApiService apiService) {
        this(apiService, null, Runnable::run);
    }

    public AssignmentRepository(@NonNull UserClassApiService apiService,
                                @Nullable AssignmentDao assignmentDao,
                                @NonNull Executor executor) {
        this.apiService = apiService;
        this.assignmentDao = assignmentDao;
        this.executor = executor != null ? executor : Runnable::run;
    }

    public Call<ApiResponse<AssignmentDetailResponse>> getAssignmentDetail(String assignmentId) {
        return apiService.getAssignmentDetail(assignmentId);
    }

    public Call<ApiResponse<AssignmentDetailResponse>> submitAssignment(String assignmentId,
                                                                        SubmitAssignmentRequest request) {
        return apiService.submitAssignment(assignmentId, request);
    }

    public Call<ApiResponse<ClassDetailResponse.ClassAssignmentItem>> createAssignment(String classId,
                                                                                       CreateAssignmentRequest request) {
        return apiService.createAssignment(classId, request);
    }

    public Call<ApiResponse<AssignmentSubmissionListResponse>> getAssignmentSubmissions(String assignmentId) {
        return apiService.getAssignmentSubmissions(assignmentId);
    }

    public Call<ApiResponse<AssignmentSubmissionListResponse>> gradeSubmission(String assignmentId,
                                                                               String studentId,
                                                                               GradeSubmissionRequest request) {
        return apiService.gradeSubmission(assignmentId, studentId, request);
    }

    public Call<ApiResponse<FileUploadResponse>> uploadFile(MultipartBody.Part file, RequestBody folder) {
        return apiService.uploadFile(file, folder);
    }

    /**
     * Caches the assignments belonging to a class in {@link AssignmentDao} on the background executor.
     */
    public void cacheClassAssignments(@NonNull String classId,
                                      @Nullable String className,
                                      @Nullable List<ClassDetailResponse.ClassAssignmentItem> items) {
        if (assignmentDao == null || classId == null) {
            return;
        }
        executor.execute(() -> {
            try {
                assignmentDao.clearByClass(classId);
                if (items != null && !items.isEmpty()) {
                    List<AssignmentEntity> entities = new ArrayList<>();
                    for (ClassDetailResponse.ClassAssignmentItem item : items) {
                        AssignmentEntity entity = AssignmentEntity.fromClassAssignmentItem(classId, className, item);
                        if (entity != null) {
                            entities.add(entity);
                        }
                    }
                    if (!entities.isEmpty()) {
                        assignmentDao.insertAssignments(entities);
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * Reads cached assignments for the specified class from {@link AssignmentDao}.
     */
    @NonNull
    public List<ClassDetailResponse.ClassAssignmentItem> getCachedClassAssignments(@NonNull String classId) {
        if (assignmentDao == null || classId == null) {
            return Collections.emptyList();
        }
        try {
            List<AssignmentEntity> entities = assignmentDao.getAssignmentsByClass(classId);
            if (entities == null || entities.isEmpty()) {
                return Collections.emptyList();
            }
            List<ClassDetailResponse.ClassAssignmentItem> result = new ArrayList<>();
            for (AssignmentEntity entity : entities) {
                if (entity != null) {
                    result.add(entity.toClassAssignmentItem());
                }
            }
            return result;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}
