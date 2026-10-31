package com.ppquang06.uniclassapp.ui.student.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.ClassGradesResponse;
import com.ppquang06.uniclassapp.databinding.FragmentStudentGradesBinding;
import com.ppquang06.uniclassapp.ui.common.UiState;
import com.ppquang06.uniclassapp.ui.common.adapter.GradeAdapter;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StudentGradesFragment extends Fragment {

    private FragmentStudentGradesBinding binding;
    private GradeAdapter adapter;
    private ClassDetailViewModel viewModel;

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

        viewModel = new ViewModelProvider(this).get(ClassDetailViewModel.class);

        readArguments();
        setupTopAppBar();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
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

    private void observeViewModel() {
        viewModel.getClassGradesState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.swipeRefresh.setRefreshing(true);
                    break;
                case SUCCESS:
                    binding.swipeRefresh.setRefreshing(false);
                    if (state.getData() != null) {
                        bindData(state.getData());
                    }
                    break;
                case ERROR:
                    binding.swipeRefresh.setRefreshing(false);
                    String msg = state.getMessage() != null ? state.getMessage() : "Không thể tải bảng điểm";
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case IDLE:
                    break;
            }
        });
    }

    private void loadGradesData() {
        if (getContext() == null) return;

        if (classId == null || classId.trim().isEmpty()) {
            binding.swipeRefresh.setRefreshing(false);
            Toast.makeText(requireContext(), "Mã lớp học không hợp lệ", Toast.LENGTH_SHORT).show();
            ClassGradesResponse empty = new ClassGradesResponse();
            empty.setClassId("");
            empty.setClassCode(classCode);
            empty.setClassName(className);
            empty.setGrades(Collections.emptyList());
            bindData(empty);
            return;
        }

        viewModel.loadClassGrades(classId);
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

        adapter.submitList(grades != null ? grades : Collections.emptyList());

        boolean isEmpty = (grades == null || grades.isEmpty());
        binding.rvGrades.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyGrades.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
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
