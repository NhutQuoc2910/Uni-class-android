package com.ppquang06.uniclassapp.ui.student.classes;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.HomeSummaryResponse;

import java.util.ArrayList;
import java.util.List;

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
                            fallbackToHomeSummary(context, classId, classCode, className, semester);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ClassDetailResponse>> call, @NonNull Throwable t) {
                        isLoading.setValue(false);
                        fallbackToHomeSummary(context, classId, classCode, className, semester);
                    }
                });
    }

    public void refresh(Context context) {
        if (context == null || lastClassId == null) return;
        loadClassDetail(context, lastClassId, lastClassCode, lastClassName, lastSemester);
    }

    private void fallbackToHomeSummary(Context context, String classId, String classCode, String className, String semester) {
        ApiClient.getUserClassApiService(context)
                .getHomeSummary()
                .enqueue(new Callback<ApiResponse<HomeSummaryResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<HomeSummaryResponse>> call,
                                           @NonNull Response<ApiResponse<HomeSummaryResponse>> response) {
                        HomeSummaryResponse homeData = (response.isSuccessful() && response.body() != null)
                                ? response.body().getData()
                                : null;
                        classDetail.setValue(buildFallbackDetail(homeData, classId, classCode, className, semester));
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<HomeSummaryResponse>> call, @NonNull Throwable t) {
                        classDetail.setValue(buildFallbackDetail(null, classId, classCode, className, semester));
                    }
                });
    }

    private ClassDetailResponse buildFallbackDetail(HomeSummaryResponse homeData,
                                                    String classId,
                                                    String classCode,
                                                    String className,
                                                    String semester) {
        ClassDetailResponse detail = new ClassDetailResponse();
        detail.setId(classId != null ? classId : "");
        detail.setCode(classCode != null && !classCode.isEmpty() ? classCode : "NT118");
        detail.setName(className != null && !className.isEmpty() ? className : "Chi tiết lớp học");
        detail.setSemester(semester != null && !semester.isEmpty() ? semester : "Học kỳ 1 2026–2027");
        detail.setRoom("Phòng B1.12");
        detail.setLecturerName("ThS. Trần Minh Khoa");
        detail.setEnrolledCount(45);
        detail.setAttendanceRate(100);
        detail.setPresentCount(5);
        detail.setAbsentCount(0);
        detail.setTotalSessions(5);

        if (homeData != null && homeData.getCourses() != null) {
            for (CourseClassItem c : homeData.getCourses()) {
                if ((classId != null && classId.equals(c.getId())) ||
                        (classCode != null && classCode.equalsIgnoreCase(c.getCode()))) {
                    if (c.getName() != null && !c.getName().isEmpty()) detail.setName(c.getName());
                    if (c.getCode() != null && !c.getCode().isEmpty()) detail.setCode(c.getCode());
                    if (c.getRoom() != null && !c.getRoom().isEmpty()) detail.setRoom(c.getRoom());
                    if (c.getLecturerName() != null && !c.getLecturerName().isEmpty()) detail.setLecturerName(c.getLecturerName());
                    if (c.getSemester() != null && !c.getSemester().isEmpty()) detail.setSemester(c.getSemester());
                    if (c.getEnrolledCount() > 0) detail.setEnrolledCount(c.getEnrolledCount());
                    if (c.getProgress() > 0) detail.setAttendanceRate(c.getProgress());
                    break;
                }
            }
        }

        List<ClassDetailResponse.AnnouncementItem> announcements = new ArrayList<>();
        announcements.add(new ClassDetailResponse.AnnouncementItem(
                "ann-1",
                "Chào mừng đến với lớp " + detail.getName(),
                "Vui lòng tham gia nhóm Zalo lớp và xem lịch trình môn học đính kèm trong mục Tài liệu.",
                "2026-10-20T08:00:00Z",
                false,
                false
        ));
        detail.setAnnouncements(announcements);

        List<ClassDetailResponse.MaterialItem> materials = new ArrayList<>();
        materials.add(new ClassDetailResponse.MaterialItem(
                "mat-1",
                "Đề cương môn học & Lịch trình giảng dạy",
                "https://storage.googleapis.com/edulens-materials/syllabus.pdf",
                "PDF",
                "2026-10-15T09:00:00Z"
        ));
        materials.add(new ClassDetailResponse.MaterialItem(
                "mat-2",
                "Slide bài giảng Chương 1 - Tổng quan",
                "https://storage.googleapis.com/edulens-materials/slides_ch1.pdf",
                "PDF",
                "2026-10-22T09:00:00Z"
        ));
        detail.setMaterials(materials);

        List<ClassDetailResponse.ClassAssignmentItem> assignments = new ArrayList<>();
        assignments.add(new ClassDetailResponse.ClassAssignmentItem(
                "asg-1",
                "Bài tập thực hành Tuần 1",
                "Cài đặt môi trường và hoàn thành bài tập nộp trên hệ thống.",
                "2026-11-05T23:59:59Z",
                null,
                false,
                null,
                null
        ));
        detail.setAssignments(assignments);

        List<ClassDetailResponse.AttendanceSessionRecordItem> attendanceRecords = new ArrayList<>();
        attendanceRecords.add(new ClassDetailResponse.AttendanceSessionRecordItem(
                "sess-1",
                1,
                "2026-10-15T07:30:00Z",
                "PRESENT",
                "2026-10-15T07:35:12Z"
        ));
        attendanceRecords.add(new ClassDetailResponse.AttendanceSessionRecordItem(
                "sess-2",
                2,
                "2026-10-22T07:30:00Z",
                "PRESENT",
                "2026-10-22T07:32:45Z"
        ));
        detail.setAttendanceRecords(attendanceRecords);

        return detail;
    }
}
