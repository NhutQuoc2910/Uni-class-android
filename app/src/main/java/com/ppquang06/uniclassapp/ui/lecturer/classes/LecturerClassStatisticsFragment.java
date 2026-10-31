package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.ClassMemberItemResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassStatisticsResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassStatisticsResponse.ScoreBucket;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerClassStatisticsBinding;
import com.ppquang06.uniclassapp.ui.common.UiState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LecturerClassStatisticsFragment extends Fragment {

    private FragmentLecturerClassStatisticsBinding binding;
    private LecturerManagementViewModel viewModel;

    private String classId = "";
    private String classCode = "";
    private String className = "";
    private String semester = "";
    private boolean isArchived = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerClassStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LecturerManagementViewModel.class);

        if (getArguments() != null) {
            classId = getArguments().getString("classId", "");
            classCode = getArguments().getString("classCode", "");
            className = getArguments().getString("className", "");
            semester = getArguments().getString("semester", "");
        }

        setupTopBar();
        setupHeader();
        setupSwipeRefresh();
        setupArchiveButton();
        observeViewModel();
        loadStatistics();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.class_statistics_title);
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    private void setupHeader() {
        if (!TextUtils.isEmpty(className)) {
            binding.tvStatClassName.setText(className);
        } else {
            binding.tvStatClassName.setText("Lớp học");
        }

        StringBuilder subtitle = new StringBuilder();
        if (!TextUtils.isEmpty(classCode)) {
            subtitle.append(classCode);
        }
        if (!TextUtils.isEmpty(semester)) {
            if (subtitle.length() > 0) subtitle.append(" · ");
            subtitle.append(semester);
        }
        if (subtitle.length() > 0) {
            binding.tvStatClassCodeSemester.setText(subtitle.toString());
        }
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshStats.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefreshStats.setOnRefreshListener(this::loadStatistics);
    }

    private void setupArchiveButton() {
        binding.btnArchiveClass.setOnClickListener(v -> showArchiveConfirmDialog());
    }

    private void showArchiveConfirmDialog() {
        if (getContext() == null) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_archive_class_title)
                .setMessage(R.string.dialog_archive_class_msg)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.btn_archive_confirm, (dialog, which) -> executeArchiveClass())
                .show();
    }

    private void observeViewModel() {
        viewModel.getStatisticsState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    if (!binding.swipeRefreshStats.isRefreshing()) {
                        binding.pbLoadingStats.setVisibility(View.VISIBLE);
                    }
                    break;
                case SUCCESS:
                    binding.pbLoadingStats.setVisibility(View.GONE);
                    binding.swipeRefreshStats.setRefreshing(false);
                    bindData(state.getData());
                    break;
                case ERROR:
                    binding.pbLoadingStats.setVisibility(View.GONE);
                    binding.swipeRefreshStats.setRefreshing(false);
                    String msg = (state.getMessage() != null && !state.getMessage().trim().isEmpty())
                            ? state.getMessage() : "Không thể tải thống kê lớp học";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    break;
                case IDLE:
                    break;
            }
        });

        viewModel.getArchiveClassState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.pbLoadingStats.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.pbLoadingStats.setVisibility(View.GONE);
                    markClassArchivedUi();
                    Toast.makeText(requireContext(), "Đã lưu trữ lớp học thành công", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR:
                    binding.pbLoadingStats.setVisibility(View.GONE);
                    String msg = (state.getMessage() != null && !state.getMessage().trim().isEmpty())
                            ? state.getMessage() : "Lưu trữ lớp học thất bại";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    break;
                case IDLE:
                    break;
            }
        });
    }

    private void loadStatistics() {
        if (getContext() == null || binding == null) return;

        if (TextUtils.isEmpty(classId)) {
            binding.pbLoadingStats.setVisibility(View.GONE);
            binding.swipeRefreshStats.setRefreshing(false);
            Toast.makeText(requireContext(), "Thiếu thông tin lớp học", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.loadClassStatistics(classId);
    }

    private void bindData(ClassStatisticsResponse data) {
        if (data == null || binding == null) return;

        if (!TextUtils.isEmpty(data.getClassName())) {
            className = data.getClassName();
            binding.tvStatClassName.setText(className);
        }
        if (!TextUtils.isEmpty(data.getClassCode())) {
            classCode = data.getClassCode();
            binding.tvStatClassCodeSemester.setText(classCode + (!TextUtils.isEmpty(data.getSemester()) ? " · " + data.getSemester() : ""));
        }

        // Status check
        if ("ARCHIVED".equalsIgnoreCase(data.getStatus()) || isArchived) {
            markClassArchivedUi();
        } else {
            binding.tvStatClassStatus.setText(R.string.status_active_pill);
            binding.tvStatClassStatus.setBackgroundResource(R.drawable.bg_pill_green);
            binding.tvStatClassStatus.setTextColor(requireContext().getColor(R.color.color_success));
        }

        // Metric Cards
        int attendanceRate = data.getAttendanceRate();
        binding.tvOverallAttendanceRate.setText(attendanceRate + "%");

        int warningCount = data.getWarningAbsentCount();
        binding.tvWarningAbsentCount.setText(warningCount + " SV");

        double avgScore = data.getAverageScore();
        binding.tvAverageScore.setText(String.format(Locale.US, "%.1f", avgScore));

        // Attendance Overview
        binding.progressAttendanceOverview.setProgress(attendanceRate);
        binding.tvAttendanceOverviewPercent.setText(attendanceRate + "%");
        binding.tvEnrolledSummary.setText("Sĩ số lớp: " + Math.max(0, data.getEnrolledCount()) + " sinh viên");

        // Warning Students List
        renderWarningStudents(data.getWarningStudents());

        // Score Distribution (6 buckets)
        renderScoreDistribution(data.getScoreDistribution());
    }

    private void renderWarningStudents(List<ClassMemberItemResponse> warningStudents) {
        if (binding == null) return;

        binding.layoutWarningStudentsList.removeAllViews();

        if (warningStudents == null || warningStudents.isEmpty()) {
            binding.tvNoWarningStudents.setVisibility(View.VISIBLE);
            binding.layoutWarningStudentsList.setVisibility(View.GONE);
            return;
        }

        binding.tvNoWarningStudents.setVisibility(View.GONE);
        binding.layoutWarningStudentsList.setVisibility(View.VISIBLE);

        for (ClassMemberItemResponse student : warningStudents) {
            View row = createWarningStudentRow(student);
            binding.layoutWarningStudentsList.addView(row);
        }
    }

    private View createWarningStudentRow(ClassMemberItemResponse student) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dpToPx(8);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(requireContext().getColor(R.color.color_surface));
        card.setRadius(dpToPx(10));
        card.setStrokeColor(requireContext().getColor(R.color.outline_border));
        card.setStrokeWidth(dpToPx(1));
        card.setCardElevation(dpToPx(0.5f));

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));

        // Initial Avatar
        FrameLayout avatarFrame = new FrameLayout(requireContext());
        avatarFrame.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(36), dpToPx(36)));
        avatarFrame.setBackgroundResource(R.drawable.bg_circle_red_light);

        TextView tvInitial = new TextView(requireContext());
        tvInitial.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        tvInitial.setTextColor(requireContext().getColor(R.color.color_absence));
        tvInitial.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvInitial.setTypeface(null, android.graphics.Typeface.BOLD);
        tvInitial.setText(!TextUtils.isEmpty(student.getAvatarInitial()) ? student.getAvatarInitial() : "SV");
        avatarFrame.addView(tvInitial);
        root.addView(avatarFrame);

        // Name & Info
        LinearLayout infoLayout = new LinearLayout(requireContext());
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        infoParams.leftMargin = dpToPx(10);
        infoLayout.setLayoutParams(infoParams);
        infoLayout.setOrientation(LinearLayout.VERTICAL);

        TextView tvName = new TextView(requireContext());
        tvName.setText(student.getFullName() != null ? student.getFullName() : "Sinh viên");
        tvName.setTextColor(requireContext().getColor(R.color.text_primary));
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        infoLayout.addView(tvName);

        TextView tvCode = new TextView(requireContext());
        tvCode.setText("MSSV: " + (student.getStudentCode() != null ? student.getStudentCode() : ""));
        tvCode.setTextColor(requireContext().getColor(R.color.text_secondary));
        tvCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        infoLayout.addView(tvCode);

        root.addView(infoLayout);

        // Absence count pill
        TextView tvAbsence = new TextView(requireContext());
        tvAbsence.setBackgroundResource(R.drawable.bg_pill_red);
        tvAbsence.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
        int absentSessions = student.getAbsentSessions();
        int totalSessions = student.getTotalSessions() > 0 ? student.getTotalSessions() : 15;
        int rate = student.getAttendanceRate();
        tvAbsence.setText("Vắng " + absentSessions + "/" + totalSessions + " buổi (" + (100 - rate) + "%)");
        tvAbsence.setTextColor(requireContext().getColor(R.color.color_error));
        tvAbsence.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tvAbsence.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(tvAbsence);

        card.addView(root);
        return card;
    }

    private void renderScoreDistribution(List<ScoreBucket> distribution) {
        if (binding == null) return;

        // Default empty buckets if null
        if (distribution == null || distribution.size() < 6) {
            distribution = buildEmptyScoreBuckets();
        }

        int maxCount = 1;
        for (ScoreBucket b : distribution) {
            if (b.getCount() > maxCount) maxCount = b.getCount();
        }

        // Max visual bar height in pixels (100dp)
        int maxBarPx = dpToPx(100);
        int minBarPx = dpToPx(10);

        // Bucket 0: 0-2
        ScoreBucket b0 = distribution.get(0);
        binding.tvScoreBar02Count.setText(String.valueOf(b0.getCount()));
        binding.tvScoreBar02Percent.setText(b0.getPercentage() + "%");
        setBarHeight(binding.viewScoreBar02Fill, minBarPx + (int) ((maxBarPx - minBarPx) * ((float) b0.getCount() / maxCount)));

        // Bucket 1: 2-4
        ScoreBucket b1 = distribution.get(1);
        binding.tvScoreBar24Count.setText(String.valueOf(b1.getCount()));
        binding.tvScoreBar24Percent.setText(b1.getPercentage() + "%");
        setBarHeight(binding.viewScoreBar24Fill, minBarPx + (int) ((maxBarPx - minBarPx) * ((float) b1.getCount() / maxCount)));

        // Bucket 2: 4-6
        ScoreBucket b2 = distribution.get(2);
        binding.tvScoreBar46Count.setText(String.valueOf(b2.getCount()));
        binding.tvScoreBar46Percent.setText(b2.getPercentage() + "%");
        setBarHeight(binding.viewScoreBar46Fill, minBarPx + (int) ((maxBarPx - minBarPx) * ((float) b2.getCount() / maxCount)));

        // Bucket 3: 6-8
        ScoreBucket b3 = distribution.get(3);
        binding.tvScoreBar68Count.setText(String.valueOf(b3.getCount()));
        binding.tvScoreBar68Percent.setText(b3.getPercentage() + "%");
        setBarHeight(binding.viewScoreBar68Fill, minBarPx + (int) ((maxBarPx - minBarPx) * ((float) b3.getCount() / maxCount)));

        // Bucket 4: 8-9
        ScoreBucket b4 = distribution.get(4);
        binding.tvScoreBar89Count.setText(String.valueOf(b4.getCount()));
        binding.tvScoreBar89Percent.setText(b4.getPercentage() + "%");
        setBarHeight(binding.viewScoreBar89Fill, minBarPx + (int) ((maxBarPx - minBarPx) * ((float) b4.getCount() / maxCount)));

        // Bucket 5: 9-10
        ScoreBucket b5 = distribution.get(5);
        binding.tvScoreBar910Count.setText(String.valueOf(b5.getCount()));
        binding.tvScoreBar910Percent.setText(b5.getPercentage() + "%");
        setBarHeight(binding.viewScoreBar910Fill, minBarPx + (int) ((maxBarPx - minBarPx) * ((float) b5.getCount() / maxCount)));
    }

    private void setBarHeight(View barView, int heightPx) {
        ViewGroup.LayoutParams lp = barView.getLayoutParams();
        lp.height = Math.max(dpToPx(8), heightPx);
        barView.setLayoutParams(lp);
    }

    private void executeArchiveClass() {
        if (getContext() == null || binding == null) return;
        viewModel.archiveClass(classId);
    }

    private void markClassArchivedUi() {
        isArchived = true;
        binding.tvStatClassStatus.setText(R.string.status_archived_pill);
        binding.tvStatClassStatus.setBackgroundResource(R.drawable.bg_pill_gray);
        binding.tvStatClassStatus.setTextColor(requireContext().getColor(R.color.text_secondary));

        binding.btnArchiveClass.setEnabled(false);
        binding.btnArchiveClass.setText(R.string.btn_class_archived);
        binding.btnArchiveClass.setStrokeColorResource(R.color.outline_border);
        binding.btnArchiveClass.setTextColor(requireContext().getColor(R.color.text_muted));
        binding.btnArchiveClass.setIconTintResource(R.color.text_muted);
    }

    private int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private List<ScoreBucket> buildEmptyScoreBuckets() {
        List<ScoreBucket> list = new ArrayList<>();
        list.add(new ScoreBucket("0–2", 0, 0));
        list.add(new ScoreBucket("2–4", 0, 0));
        list.add(new ScoreBucket("4–6", 0, 0));
        list.add(new ScoreBucket("6–8", 0, 0));
        list.add(new ScoreBucket("8–9", 0, 0));
        list.add(new ScoreBucket("9–10", 0, 0));
        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
