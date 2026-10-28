package com.ppquang06.uniclassapp.ui.student.classes;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.databinding.FragmentStudentAttendanceHistoryBinding;
import com.ppquang06.uniclassapp.ui.common.adapter.AttendanceRecordAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentAttendanceHistoryFragment extends Fragment {

    public static final int ABSENCE_WARNING_THRESHOLD = 2;
    public static final int MAX_COURSE_SESSIONS = 15;
    public static final int ABSENCE_LIMIT_MAX = 3;

    private FragmentStudentAttendanceHistoryBinding binding;
    private AttendanceRecordAdapter adapter;

    private String classId = "";
    private String classCode = "";
    private String className = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentAttendanceHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readArguments();
        setupTopAppBar();
        setupRecyclerView();
        setupListeners();
        loadAttendanceData();
    }

    private void readArguments() {
        if (getArguments() != null) {
            classId = getArguments().getString("classId", "");
            classCode = getArguments().getString("classCode", "");
            className = getArguments().getString("className", "");
        }
    }

    private void setupTopAppBar() {
        binding.topAppBar.tvTitle.setText("Lịch sử điểm danh");
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> {
            try {
                Navigation.findNavController(v).navigateUp();
            } catch (Exception ignored) {}
        });
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        adapter = new AttendanceRecordAdapter();
        binding.rvAttendanceRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAttendanceRecords.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(this::loadAttendanceData);
    }

    private void loadAttendanceData() {
        if (getContext() == null) return;
        binding.swipeRefresh.setRefreshing(true);

        if (classId == null || classId.trim().isEmpty()) {
            bindData(buildFallbackDetail());
            binding.swipeRefresh.setRefreshing(false);
            return;
        }

        ApiClient.getUserClassApiService(requireContext())
                .getClassDetail(classId)
                .enqueue(new Callback<ApiResponse<ClassDetailResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<ClassDetailResponse>> call,
                                           @NonNull Response<ApiResponse<ClassDetailResponse>> response) {
                        if (binding == null) return;
                        binding.swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getData() != null) {
                            bindData(response.body().getData());
                        } else {
                            bindData(buildFallbackDetail());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ClassDetailResponse>> call, @NonNull Throwable t) {
                        if (binding == null) return;
                        binding.swipeRefresh.setRefreshing(false);
                        bindData(buildFallbackDetail());
                    }
                });
    }

    private void bindData(@NonNull ClassDetailResponse detail) {
        if (binding == null || getContext() == null) return;

        // Class Name
        String displayName = detail.getName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = (className != null && !className.trim().isEmpty()) ? className : "Lớp học";
        }
        binding.tvClassName.setText(displayName);

        // Attendance stats
        int presentCount = detail.getPresentCount();
        int absentCount = detail.getAbsentCount();
        int totalSessions = detail.getTotalSessions();

        List<ClassDetailResponse.AttendanceSessionRecordItem> records = detail.getAttendanceRecords();
        if (records != null && !records.isEmpty() && (presentCount == 0 && absentCount == 0)) {
            int p = 0;
            int a = 0;
            for (ClassDetailResponse.AttendanceSessionRecordItem r : records) {
                if ("PRESENT".equalsIgnoreCase(r.getStatus())) {
                    p++;
                } else if ("ABSENT".equalsIgnoreCase(r.getStatus())) {
                    a++;
                }
            }
            presentCount = p;
            absentCount = a;
            totalSessions = records.size();
        }

        int remainingCount = calculateRemainingSessions(totalSessions);

        // Course progress
        binding.tvCourseProgress.setText(String.format(Locale.getDefault(), "Tiến độ học: Tuần %d / %d tuần",
                Math.max(1, totalSessions), MAX_COURSE_SESSIONS));

        // 3 stats
        binding.tvPresentCount.setText(String.valueOf(presentCount));
        binding.tvAbsentCount.setText(String.valueOf(absentCount));
        binding.tvRemainingCount.setText(String.valueOf(remainingCount));

        // Absence progress track
        binding.tvAbsentRatio.setText(String.format(Locale.getDefault(), "%d/%d buổi", absentCount, ABSENCE_LIMIT_MAX));
        binding.pbAbsentProgress.setMax(ABSENCE_LIMIT_MAX);
        binding.pbAbsentProgress.setProgress(Math.min(ABSENCE_LIMIT_MAX, absentCount));

        // Warning banner & color logic
        boolean showWarning = shouldShowAbsenceWarning(absentCount);
        if (showWarning) {
            binding.bannerWarning.getRoot().setVisibility(View.VISIBLE);
            binding.bannerWarning.tvBannerTitle.setText("Cảnh báo chuyên cần");
            binding.bannerWarning.tvBannerTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_error));
            binding.bannerWarning.tvBannerSubtitle.setText(buildWarningSubtitle(absentCount));
            binding.bannerWarning.tvBannerSubtitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            binding.bannerWarning.ivBannerIcon.setImageResource(R.drawable.ic_error_circle);
            binding.bannerWarning.ivBannerIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.color_error));
            binding.bannerWarning.btnBannerAction.setVisibility(View.GONE);

            ViewCompat.setBackgroundTintList(
                    binding.bannerWarning.cardBanner,
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.color_error_container))
            );

            binding.tvAbsentCount.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_error));
            binding.tvAbsentRatio.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_error));
            binding.pbAbsentProgress.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.color_error)));
        } else {
            binding.bannerWarning.getRoot().setVisibility(View.GONE);
            binding.tvAbsentCount.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            binding.tvAbsentRatio.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            binding.pbAbsentProgress.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.color_absence)));
        }

        // List
        adapter.submitList(records);
        boolean isEmpty = (records == null || records.isEmpty());
        binding.rvAttendanceRecords.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyAttendance.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private ClassDetailResponse buildFallbackDetail() {
        ClassDetailResponse fallback = new ClassDetailResponse();
        fallback.setId(classId != null ? classId : "demo-class-id");
        fallback.setCode(classCode != null && !classCode.isEmpty() ? classCode : "NT118.Q11");
        fallback.setName(className != null && !className.isEmpty() ? className : "Phát triển ứng dụng di động");
        fallback.setPresentCount(4);
        fallback.setAbsentCount(2);
        fallback.setTotalSessions(6);

        List<ClassDetailResponse.AttendanceSessionRecordItem> list = new ArrayList<>();
        list.add(new ClassDetailResponse.AttendanceSessionRecordItem("s-6", 6, "2026-10-28T07:30:00Z", "PRESENT", "2026-10-28T07:35:12Z"));
        list.add(new ClassDetailResponse.AttendanceSessionRecordItem("s-5", 5, "2026-10-21T07:30:00Z", "ABSENT", null));
        list.add(new ClassDetailResponse.AttendanceSessionRecordItem("s-4", 4, "2026-10-14T07:30:00Z", "PRESENT", "2026-10-14T07:32:44Z"));
        list.add(new ClassDetailResponse.AttendanceSessionRecordItem("s-3", 3, "2026-10-07T07:30:00Z", "ABSENT", null));
        list.add(new ClassDetailResponse.AttendanceSessionRecordItem("s-2", 2, "2026-09-30T07:30:00Z", "PRESENT", "2026-09-30T07:31:05Z"));
        list.add(new ClassDetailResponse.AttendanceSessionRecordItem("s-1", 1, "2026-09-23T07:30:00Z", "PRESENT", "2026-09-23T07:34:50Z"));
        fallback.setAttendanceRecords(list);

        return fallback;
    }

    public static boolean shouldShowAbsenceWarning(int absentCount) {
        return absentCount >= ABSENCE_WARNING_THRESHOLD;
    }

    public static int calculateRemainingSessions(int totalSessions) {
        return Math.max(0, MAX_COURSE_SESSIONS - totalSessions);
    }

    public static String buildWarningSubtitle(int absentCount) {
        if (absentCount == 2) {
            return "Bạn đã vắng 2 buổi. Nếu vắng thêm 1 buổi nữa bạn sẽ bị cấm thi theo quy chế!";
        } else if (absentCount >= 3) {
            return String.format(Locale.getDefault(),
                    "Bạn đã vắng %d buổi. Bạn đã vượt quá ngưỡng cấm thi (20%%) theo quy chế!", absentCount);
        }
        return "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
