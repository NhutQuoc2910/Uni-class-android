package com.ppquang06.uniclassapp.ui.student.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.databinding.FragmentClassAttendanceTabBinding;
import com.ppquang06.uniclassapp.ui.common.UiBadgeHelper;
import com.ppquang06.uniclassapp.ui.common.adapter.AttendanceRecordAdapter;

import java.util.Collections;
import java.util.List;

public class ClassAttendanceTabFragment extends Fragment {

    private FragmentClassAttendanceTabBinding binding;
    private AttendanceRecordAdapter adapter;
    private ClassDetailViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentClassAttendanceTabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupListeners();
        setupViewModel();
    }

    private void setupRecyclerView() {
        adapter = new AttendanceRecordAdapter();
        binding.rvAttendanceRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAttendanceRecords.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (viewModel != null && getContext() != null) {
                viewModel.refresh(requireContext());
            } else {
                binding.swipeRefresh.setRefreshing(false);
            }
        });

        binding.btnScanAttendance.setOnClickListener(v -> {
            try {
                Navigation.findNavController(requireActivity(), R.id.nav_host_student)
                        .navigate(R.id.nav_student_scan_qr);
            } catch (Exception e) {
                try {
                    NavHostFragment.findNavController(ClassAttendanceTabFragment.this)
                            .navigate(R.id.nav_student_scan_qr);
                } catch (Exception ignored) {}
            }
        });

        View.OnClickListener historyClickListener = v -> {
            Bundle bundle = new Bundle();
            String classId = viewModel != null ? viewModel.getLastClassId() : "";
            String classCode = viewModel != null ? viewModel.getLastClassCode() : "";
            String className = viewModel != null ? viewModel.getLastClassName() : "";
            if (viewModel != null && viewModel.getClassDetail().getValue() != null) {
                ClassDetailResponse d = viewModel.getClassDetail().getValue();
                if (d.getId() != null && !d.getId().isEmpty()) classId = d.getId();
                if (d.getCode() != null && !d.getCode().isEmpty()) classCode = d.getCode();
                if (d.getName() != null && !d.getName().isEmpty()) className = d.getName();
            }
            bundle.putString("classId", classId);
            bundle.putString("classCode", classCode);
            bundle.putString("className", className);

            try {
                Navigation.findNavController(requireActivity(), R.id.nav_host_student)
                        .navigate(R.id.nav_student_attendance_history, bundle);
            } catch (Exception e) {
                try {
                    NavHostFragment.findNavController(ClassAttendanceTabFragment.this)
                            .navigate(R.id.nav_student_attendance_history, bundle);
                } catch (Exception ignored) {}
            }
        };

        binding.cardAttendanceSummary.setOnClickListener(historyClickListener);
        binding.btnViewFullHistory.setOnClickListener(historyClickListener);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireParentFragment()).get(ClassDetailViewModel.class);
        viewModel.getClassDetail().observe(getViewLifecycleOwner(), detail -> {
            if (detail != null) {
                bindAttendanceData(detail);
            } else {
                adapter.submitList(Collections.emptyList());
                binding.tvEmptyAttendance.setVisibility(View.VISIBLE);
                binding.rvAttendanceRecords.setVisibility(View.GONE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading != null) {
                binding.swipeRefresh.setRefreshing(loading);
            }
        });
    }

    private void bindAttendanceData(@NonNull ClassDetailResponse detail) {
        int present = detail.getPresentCount();
        int total = detail.getTotalSessions();
        int rate = detail.getAttendanceRate();

        binding.tvPresentFraction.setText(String.format(getString(R.string.attendance_fraction_format), present, total));

        UiBadgeHelper.Status badgeStatus = (rate >= 80) ? UiBadgeHelper.Status.PRESENT : UiBadgeHelper.Status.ABSENT;
        UiBadgeHelper.applyBadge(binding.tvAttendanceRateBadge, badgeStatus, rate + "%");

        binding.pbAttendanceProgress.setProgress(rate);

        List<ClassDetailResponse.AttendanceSessionRecordItem> records = detail.getAttendanceRecords();
        adapter.submitList(records);

        boolean isEmpty = (records == null || records.isEmpty());
        binding.tvEmptyAttendance.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvAttendanceRecords.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
