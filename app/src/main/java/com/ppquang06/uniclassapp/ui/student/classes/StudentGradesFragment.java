package com.ppquang06.uniclassapp.ui.student.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassGradesResponse;
import com.ppquang06.uniclassapp.databinding.FragmentStudentGradesBinding;
import com.ppquang06.uniclassapp.ui.common.adapter.GradeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentGradesFragment extends Fragment {

    private FragmentStudentGradesBinding binding;
    private GradeAdapter adapter;

    private String classId = "";
    private String classCode = "";
    private String className = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentGradesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readArguments();
        setupTopAppBar();
        setupRecyclerView();
        setupListeners();
        loadGradesData();
    }

    private void readArguments() {
        if (getArguments() != null) {
            classId = getArguments().getString("classId", "");
            classCode = getArguments().getString("classCode", "");
            className = getArguments().getString("className", "");
        }
    }

    private void setupTopAppBar() {
        binding.topAppBar.tvTitle.setText("Bảng điểm & Nhận xét");
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
        adapter = new GradeAdapter();
        binding.rvGrades.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGrades.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(this::loadGradesData);
    }

    private void loadGradesData() {
        if (getContext() == null) return;
        binding.swipeRefresh.setRefreshing(true);

        if (classId == null || classId.trim().isEmpty()) {
            bindData(buildFallbackGrades());
            binding.swipeRefresh.setRefreshing(false);
            return;
        }

        ApiClient.getUserClassApiService(requireContext())
                .getClassGrades(classId)
                .enqueue(new Callback<ApiResponse<ClassGradesResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<ClassGradesResponse>> call,
                                           @NonNull Response<ApiResponse<ClassGradesResponse>> response) {
                        if (binding == null) return;
                        binding.swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getData() != null) {
                            bindData(response.body().getData());
                        } else {
                            bindData(buildFallbackGrades());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ClassGradesResponse>> call, @NonNull Throwable t) {
                        if (binding == null) return;
                        binding.swipeRefresh.setRefreshing(false);
                        bindData(buildFallbackGrades());
                    }
                });
    }

    private void bindData(@NonNull ClassGradesResponse data) {
        if (binding == null || getContext() == null) return;

        double avg = data.getAverageScore();
        int gradedCount = data.getGradedCount();
        int total = data.getTotalAssignments();

        List<ClassGradesResponse.GradeItem> grades = data.getGrades();
        if (grades != null && !grades.isEmpty()) {
            if (total == 0) total = grades.size();
            if (gradedCount == 0) {
                int g = 0;
                double sum = 0;
                for (ClassGradesResponse.GradeItem item : grades) {
                    if (item.getScore() != null) {
                        g++;
                        sum += item.getScore();
                    }
                }
                gradedCount = g;
                if (g > 0 && avg == 0.0) {
                    avg = sum / g;
                }
            }
        }

        if (gradedCount > 0) {
            binding.tvAverageScore.setText(formatAverageScore(avg));
        } else {
            binding.tvAverageScore.setText("-");
        }

        binding.tvGradingScale.setText("Thang 10");
        binding.tvGradesOverviewTitle.setText("Điểm trung bình tích lũy");
        binding.tvGradesOverviewDesc.setText(buildGradesOverviewDesc(total, gradedCount));

        adapter.submitList(grades);

        boolean isEmpty = (grades == null || grades.isEmpty());
        binding.rvGrades.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyGrades.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private ClassGradesResponse buildFallbackGrades() {
        ClassGradesResponse fallback = new ClassGradesResponse();
        fallback.setClassId(classId != null ? classId : "demo-class-id");
        fallback.setClassCode(classCode != null && !classCode.isEmpty() ? classCode : "NT118.Q11");
        fallback.setClassName(className != null && !className.isEmpty() ? className : "Phát triển ứng dụng di động");
        fallback.setLecturerName("ThS. Trần Minh Khoa");
        fallback.setAverageScore(8.5);
        fallback.setGradedCount(2);
        fallback.setTotalAssignments(3);

        List<ClassGradesResponse.GradeItem> list = new ArrayList<>();
        list.add(new ClassGradesResponse.GradeItem(
                "assign-1",
                "Bài tập 1: Xây dựng Layout XML & Navigation Component",
                "2026-10-24T14:30:00Z",
                true,
                8.5,
                "Bài làm tốt, cấu trúc rõ ràng và tuân thủ đúng Material Design 3.",
                "ThS. Trần Minh Khoa",
                "K"
        ));
        list.add(new ClassGradesResponse.GradeItem(
                "assign-2",
                "Bài tập 2: Tích hợp Retrofit REST API & Authentication",
                "2026-10-26T10:15:00Z",
                true,
                9.0,
                "Xử lý lỗi mạng và token interceptor rất chuẩn xác, code sạch.",
                "ThS. Trần Minh Khoa",
                "K"
        ));
        list.add(new ClassGradesResponse.GradeItem(
                "assign-3",
                "Bài tập lớn: Đồ án cuối kỳ - Ứng dụng EduLens",
                null,
                false,
                null,
                null,
                "ThS. Trần Minh Khoa",
                "K"
        ));
        fallback.setGrades(list);

        return fallback;
    }

    public static String formatAverageScore(double averageScore) {
        return String.format(Locale.getDefault(), "%.1f", averageScore);
    }

    public static String buildGradesOverviewDesc(int totalAssignments, int gradedCount) {
        return String.format(Locale.getDefault(),
                "Đã hoàn thành %d bài tập · %d bài đã có điểm",
                totalAssignments, gradedCount);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
