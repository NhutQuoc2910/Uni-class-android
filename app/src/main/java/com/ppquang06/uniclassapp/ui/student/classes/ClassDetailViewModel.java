package com.ppquang06.uniclassapp.ui.student.classes;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassDetailViewModel extends ViewModel {

    private final MutableLiveData<ClassDetailResponse> classDetail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    private String lastClassId;
    private String lastClassCode;
    private String lastClassName;
    private String lastSemester;

    public LiveData<ClassDetailResponse> getClassDetail() {
        return classDetail;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
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

    public void setClassDetail(ClassDetailResponse detail) {
        this.classDetail.setValue(detail);
    }

    public void loadClassDetail(Context context, String classId, String classCode, String className, String semester) {
        if (context == null) return;
        this.lastClassId = classId;
        this.lastClassCode = classCode;
        this.lastClassName = className;
        this.lastSemester = semester;

        isLoading.setValue(true);
        errorMessage.setValue(null);

        ApiClient.getUserClassApiService(context)
                .getClassDetail(classId)
                .enqueue(new Callback<ApiResponse<ClassDetailResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<ClassDetailResponse>> call,
                                           @NonNull Response<ApiResponse<ClassDetailResponse>> response) {
                        isLoading.setValue(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                            ClassDetailResponse data = response.body().getData();
                            if (data.getCode() == null || data.getCode().isEmpty()) data.setCode(classCode);
                            if (data.getName() == null || data.getName().isEmpty()) data.setName(className);
                            if (data.getSemester() == null || data.getSemester().isEmpty()) data.setSemester(semester);
                            classDetail.setValue(data);
                        } else {
                            String msg = "Không thể tải thông tin lớp học";
                            if (response.body() != null && response.body().getMessage() != null) {
                                msg = response.body().getMessage();
                            }
                            errorMessage.setValue(msg);
                            classDetail.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ClassDetailResponse>> call, @NonNull Throwable t) {
                        isLoading.setValue(false);
                        errorMessage.setValue(t.getMessage() != null ? t.getMessage() : "Lỗi kết nối máy chủ");
                        classDetail.setValue(null);
                    }
                });
    }

    public void refresh(Context context) {
        if (context == null || lastClassId == null) return;
        loadClassDetail(context, lastClassId, lastClassCode, lastClassName, lastSemester);
    }
}
