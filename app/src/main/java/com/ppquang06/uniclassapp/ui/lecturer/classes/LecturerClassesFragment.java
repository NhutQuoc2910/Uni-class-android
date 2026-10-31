package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerClassesBinding;
import com.ppquang06.uniclassapp.ui.common.UiState;
import com.ppquang06.uniclassapp.ui.common.adapter.CourseClassAdapter;

import java.util.ArrayList;
import java.util.List;

public class LecturerClassesFragment extends Fragment {

    private FragmentLecturerClassesBinding binding;
    private CourseClassAdapter adapter;
    private LecturerManagementViewModel viewModel;
    private final List<CourseClassItem> allLoadedClasses = new ArrayList<>();
    private boolean showArchived = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerClassesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LecturerManagementViewModel.class);

        setupTopBar();
        setupTabs();
        setupRecyclerView();
        setupEmptyAndErrorViews();
        setupListeners();
        observeViewModel();
        loadClasses();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.nav_classes);
        binding.topAppBar.tvRoleBadge.setText(R.string.role_badge_lecturer);
    }

    private void setupTabs() {
        binding.tabLayoutClassStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showArchived = (tab.getPosition() == 1);
                applyFilter();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new CourseClassAdapter(this::onClassItemClicked);
        binding.rvClasses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvClasses.setAdapter(adapter);
    }

    private void onClassItemClicked(CourseClassItem item) {
        Bundle args = new Bundle();
        args.putString("classId", item.getId() != null ? item.getId() : "");
        args.putString("classCode", item.getCode() != null ? item.getCode() : "");
        args.putString("className", item.getName() != null ? item.getName() : "");
        args.putString("semester", item.getSemester() != null ? item.getSemester() : "");
        args.putString("inviteCode", item.getInviteCode() != null ? item.getInviteCode() : "");
        args.putString("status", item.getStatus() != null ? item.getStatus() : "ACTIVE");

        try {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.nav_lecturer_class_detail, args);
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(requireContext(), item.getCode() + " - " + item.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupEmptyAndErrorViews() {
        binding.emptyState.tvEmptyTitle.setText(R.string.empty_courses_message);
        binding.emptyState.tvEmptyMessage.setText(R.string.overview_semester);

        binding.bannerError.tvBannerTitle.setText(R.string.classes_load_error);
        binding.bannerError.btnBannerAction.setVisibility(View.VISIBLE);
        binding.bannerError.btnBannerAction.setText(R.string.action_retry);
        binding.bannerError.btnBannerAction.setOnClickListener(v -> loadClasses());
    }

    private void setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(this::loadClasses);

        binding.fabCreateClass.setOnClickListener(v -> {
            try {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.nav_lecturer_create_class);
            } catch (Exception e) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Mở tạo lớp", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.getClassesState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    if (state.getData() != null && !state.getData().isEmpty()) {
                        binding.swipeRefresh.setRefreshing(false);
                        handleClassesResult(state.getData());
                    } else if (!binding.swipeRefresh.isRefreshing()) {
                        showLoadingState();
                    }
                    break;
                case SUCCESS:
                    binding.swipeRefresh.setRefreshing(false);
                    handleClassesResult(state.getData());
                    break;
                case ERROR:
                    binding.swipeRefresh.setRefreshing(false);
                    if (state.getData() != null && !state.getData().isEmpty()) {
                        handleClassesResult(state.getData());
                    } else {
                        showErrorState(state.getMessage());
                    }
                    break;
                case IDLE:
                    break;
            }
        });
    }

    private void loadClasses() {
        if (getContext() == null || binding == null) return;
        viewModel.loadClasses();
    }

    private void handleClassesResult(List<CourseClassItem> items) {
        allLoadedClasses.clear();
        if (items != null) {
            allLoadedClasses.addAll(items);
        }
        applyFilter();
    }

    public void applyFilter() {
        List<CourseClassItem> filtered = new ArrayList<>();
        for (CourseClassItem item : allLoadedClasses) {
            boolean isArchived = "ARCHIVED".equalsIgnoreCase(item.getStatus());
            if (showArchived && isArchived) {
                filtered.add(item);
            } else if (!showArchived && !isArchived) {
                filtered.add(item);
            }
        }

        int count = filtered.size();
        if (binding != null) {
            binding.tvClassCount.setText(String.format(getString(R.string.classes_count_format), count));
            adapter.submitList(filtered);

            if (count > 0) {
                showContentState();
            } else {
                if (showArchived) {
                    binding.emptyState.tvEmptyTitle.setText("Chưa có lớp đã lưu trữ");
                    binding.emptyState.tvEmptyMessage.setText("Các lớp học sau khi kết thúc kỳ và lưu trữ sẽ xuất hiện tại đây.");
                } else {
                    binding.emptyState.tvEmptyTitle.setText(R.string.empty_courses_message);
                    binding.emptyState.tvEmptyMessage.setText(R.string.overview_semester);
                }
                showEmptyState();
            }
        }
    }

    private void showLoadingState() {
        binding.progressLoading.setVisibility(View.VISIBLE);
        binding.swipeRefresh.setVisibility(View.GONE);
        binding.emptyState.getRoot().setVisibility(View.GONE);
        binding.bannerError.getRoot().setVisibility(View.GONE);
    }

    private void showContentState() {
        binding.progressLoading.setVisibility(View.GONE);
        binding.swipeRefresh.setVisibility(View.VISIBLE);
        binding.emptyState.getRoot().setVisibility(View.GONE);
        binding.bannerError.getRoot().setVisibility(View.GONE);
    }

    private void showEmptyState() {
        binding.progressLoading.setVisibility(View.GONE);
        binding.swipeRefresh.setVisibility(View.GONE);
        binding.emptyState.getRoot().setVisibility(View.VISIBLE);
        binding.bannerError.getRoot().setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        binding.progressLoading.setVisibility(View.GONE);
        binding.swipeRefresh.setVisibility(View.GONE);
        binding.emptyState.getRoot().setVisibility(View.GONE);
        binding.bannerError.getRoot().setVisibility(View.VISIBLE);
        if (message != null && !message.isEmpty()) {
            binding.bannerError.tvBannerSubtitle.setText(message);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
