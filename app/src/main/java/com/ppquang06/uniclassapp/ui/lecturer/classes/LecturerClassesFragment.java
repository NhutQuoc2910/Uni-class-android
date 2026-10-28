package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.HomeSummaryResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerClassesBinding;
import com.ppquang06.uniclassapp.ui.common.adapter.CourseClassAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerClassesFragment extends Fragment {

    private FragmentLecturerClassesBinding binding;
    private CourseClassAdapter adapter;
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

        setupTopBar();
        setupTabs();
        setupRecyclerView();
        setupEmptyAndErrorViews();
        setupListeners();
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

    private void loadClasses() {
        if (getContext() == null || binding == null) return;

        if (!binding.swipeRefresh.isRefreshing()) {
            showLoadingState();
        }

        ApiClient.getUserClassApiService(requireContext())
                .getClasses()
                .enqueue(new Callback<ApiResponse<List<CourseClassItem>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<CourseClassItem>>> call,
                                           @NonNull Response<ApiResponse<List<CourseClassItem>>> response) {
                        if (!isAdded() || binding == null) return;
                        binding.swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<CourseClassItem> items = response.body().getData();
                            handleClassesResult(items);
                        } else {
                            fallbackToHomeSummary();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<CourseClassItem>>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        fallbackToHomeSummary();
                    }
                });
    }

    private void fallbackToHomeSummary() {
        if (getContext() == null || binding == null) return;

        ApiClient.getUserClassApiService(requireContext())
                .getHomeSummary()
                .enqueue(new Callback<ApiResponse<HomeSummaryResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<HomeSummaryResponse>> call,
                                           @NonNull Response<ApiResponse<HomeSummaryResponse>> response) {
                        if (!isAdded() || binding == null) return;
                        binding.swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            HomeSummaryResponse homeData = response.body().getData();
                            List<CourseClassItem> courses = homeData != null ? homeData.getCourses() : null;
                            handleClassesResult(courses);
                        } else {
                            showErrorState(getString(R.string.classes_load_error));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<HomeSummaryResponse>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        binding.swipeRefresh.setRefreshing(false);
                        showErrorState(t.getMessage());
                    }
                });
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
