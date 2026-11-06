package com.ppquang06.uniclassapp.ui.common;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.SemesterScheduleResponse;
import com.ppquang06.uniclassapp.databinding.FragmentSemesterScheduleBinding;
import com.ppquang06.uniclassapp.ui.common.adapter.ScheduleExceptionAdapter;
import com.ppquang06.uniclassapp.ui.common.adapter.SemesterScheduleAdapter;

public class SemesterScheduleFragment extends Fragment {

    private FragmentSemesterScheduleBinding binding;
    private HomeViewModel homeViewModel;
    private SemesterScheduleAdapter semesterScheduleAdapter;
    private ScheduleExceptionAdapter scheduleExceptionAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSemesterScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        setupTopAppBar();
        setupRecyclerViews();
        setupFilterChips();
        setupSwipeRefresh();
        observeViewModel();

        homeViewModel.loadSemesterSchedule();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupTopAppBar() {
        if (binding == null) return;
        binding.topAppBar.tvTitle.setText(R.string.semester_schedule_title);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> {
            try {
                Navigation.findNavController(v).navigateUp();
            } catch (Exception ignored) {
            }
        });
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
    }

    private void setupRecyclerViews() {
        if (binding == null) return;

        semesterScheduleAdapter = new SemesterScheduleAdapter(item -> {
            if (item != null && !TextUtils.isEmpty(item.getClassId())) {
                navigateToClassDetail(item.getClassId(), item.getClassCode(), item.getClassName());
            }
        });
        binding.rvRegularSchedules.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRegularSchedules.setAdapter(semesterScheduleAdapter);

        scheduleExceptionAdapter = new ScheduleExceptionAdapter(item -> {
            if (item != null && !TextUtils.isEmpty(item.getClassId())) {
                navigateToClassDetail(item.getClassId(), item.getClassCode(), item.getClassName());
            }
        });
        binding.rvScheduleExceptions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvScheduleExceptions.setAdapter(scheduleExceptionAdapter);
    }

    private void setupFilterChips() {
        if (binding == null) return;

        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (binding == null) return;
            int checkedId = binding.chipGroupFilter.getCheckedChipId();
            int dayOfWeek = 0;
            if (checkedId == R.id.chipMon) {
                dayOfWeek = 1;
            } else if (checkedId == R.id.chipTue) {
                dayOfWeek = 2;
            } else if (checkedId == R.id.chipWed) {
                dayOfWeek = 3;
            } else if (checkedId == R.id.chipThu) {
                dayOfWeek = 4;
            } else if (checkedId == R.id.chipFri) {
                dayOfWeek = 5;
            } else if (checkedId == R.id.chipSat) {
                dayOfWeek = 6;
            } else if (checkedId == R.id.chipSun) {
                dayOfWeek = 7;
            }
            semesterScheduleAdapter.filterByDayOfWeek(dayOfWeek);
            updateRegularEmptyState();
        });
    }

    private void setupSwipeRefresh() {
        if (binding == null) return;
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(() -> homeViewModel.loadSemesterSchedule());
    }

    private void observeViewModel() {
        homeViewModel.getSemesterScheduleState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    if (!binding.swipeRefresh.isRefreshing()) {
                        binding.pbLoading.setVisibility(View.VISIBLE);
                    }
                    break;
                case SUCCESS:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    SemesterScheduleResponse data = state.getData();
                    if (data != null) {
                        semesterScheduleAdapter.submitList(data.getRegularSchedules());
                        scheduleExceptionAdapter.submitList(data.getScheduleExceptions());
                    } else {
                        semesterScheduleAdapter.submitList(null);
                        scheduleExceptionAdapter.submitList(null);
                    }
                    updateRegularEmptyState();
                    updateExceptionsEmptyState();
                    break;
                case ERROR:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    if (getContext() != null) {
                        String msg = state.getMessage();
                        Toast.makeText(requireContext(), !TextUtils.isEmpty(msg) ? msg : "Không thể tải thời khóa biểu học kỳ", Toast.LENGTH_SHORT).show();
                    }
                    SemesterScheduleResponse fallback = state.getData();
                    if (fallback != null) {
                        semesterScheduleAdapter.submitList(fallback.getRegularSchedules());
                        scheduleExceptionAdapter.submitList(fallback.getScheduleExceptions());
                    }
                    updateRegularEmptyState();
                    updateExceptionsEmptyState();
                    break;
                case IDLE:
                default:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    break;
            }
        });
    }

    private void updateRegularEmptyState() {
        if (binding == null) return;
        boolean isEmpty = semesterScheduleAdapter == null || semesterScheduleAdapter.getItemCount() == 0;
        binding.rvRegularSchedules.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyRegularSchedules.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void updateExceptionsEmptyState() {
        if (binding == null) return;
        boolean isEmpty = scheduleExceptionAdapter == null || scheduleExceptionAdapter.getItemCount() == 0;
        binding.rvScheduleExceptions.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyExceptions.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void navigateToClassDetail(String classId, String classCode, String className) {
        if (classId == null || getView() == null) return;
        Bundle args = new Bundle();
        args.putString("classId", classId);
        args.putString("classCode", classCode != null ? classCode : "");
        args.putString("className", className != null ? className : "");
        args.putString("semester", "");
        args.putString("status", "ACTIVE");

        try {
            NavController navController = Navigation.findNavController(requireView());
            try {
                navController.navigate(R.id.action_semester_schedule_to_class_detail, args);
            } catch (Exception eAction) {
                try {
                    navController.navigate(R.id.nav_student_class_detail, args);
                } catch (Exception eStudent) {
                    try {
                        navController.navigate(R.id.nav_lecturer_class_detail, args);
                    } catch (Exception eLecturer) {
                        if (getContext() != null) {
                            Toast.makeText(requireContext(), (classCode != null ? classCode : "") + " - " + (className != null ? className : ""), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
