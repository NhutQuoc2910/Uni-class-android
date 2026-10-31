package com.ppquang06.uniclassapp.ui.student.classes;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.databinding.FragmentStudentAttendanceHistoryBinding;
import com.ppquang06.uniclassapp.ui.common.UiState;
import com.ppquang06.uniclassapp.ui.common.adapter.AttendanceRecordAdapter;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StudentAttendanceHistoryFragment extends Fragment {

    public static final int ABSENCE_WARNING_THRESHOLD = 2;
    public static final int MAX_COURSE_SESSIONS = 15;
    public static final int ABSENCE_LIMIT_MAX = 3;

    private FragmentStudentAttendanceHistoryBinding binding;
    private AttendanceRecordAdapter adapter;
    private ClassDetailViewModel viewModel;

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

        viewModel = new ViewModelProvider(this).get(ClassDetailViewModel.class);

        readArguments();
        setupTopAppBar();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
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

    private void observeViewModel() {
        viewModel.getClassDetailState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.swipeRefresh.setRefreshing(true);
                    if (state.getData() != null) {
                        bindData(state.getData());
                    }
                    break;
                case SUCCESS:
                    binding.swipeRefresh.setRefreshing(false);
                    if (state.getData() != null) {
                        bindData(state.getData());
                    }
                    break;
                case ERROR:
                    binding.swipeRefresh.setRefreshing(false);
                    if (state.getData() != null) {
                        bindData(state.getData());
                    } else {
                        String msg = state.getMessage() != null ? state.getMessage() : "Không thể tải lịch sử điểm danh";
                        if (getContext() != null) {
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case IDLE:
                    break;
            }
        });
    }

    private void loadAttendanceData() {
        if (getContext() == null) return;

        if (classId == null || classId.trim().isEmpty()) {
            binding.swipeRefresh.setRefreshing(false);
            Toast.makeText(requireContext(), "Mã lớp học không hợp lệ", Toast.LENGTH_SHORT).show();
            ClassDetailResponse empty = new ClassDetailResponse();
            empty.setId("");
            empty.setCode(classCode);
            empty.setName(className);
            empty.setAttendanceRecords(Collections.emptyList());
            bindData(empty);
            return;
        }

        viewModel.loadClassDetail(classId, classCode, className, null);
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
        adapter.submitList(records != null ? records : Collections.emptyList());
        boolean isEmpty = (records == null || records.isEmpty());
        binding.rvAttendanceRecords.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyAttendance.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
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
