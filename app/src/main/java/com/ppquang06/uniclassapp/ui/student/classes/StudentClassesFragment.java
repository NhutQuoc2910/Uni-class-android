package com.ppquang06.uniclassapp.ui.student.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.HomeSummaryResponse;
import com.ppquang06.uniclassapp.databinding.FragmentStudentClassesBinding;
import com.ppquang06.uniclassapp.ui.common.adapter.CourseClassAdapter;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentClassesFragment extends Fragment {

    private FragmentStudentClassesBinding binding;
    private CourseClassAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentClassesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTopBar();
        setupRecyclerView();
        setupEmptyAndErrorViews();
        setupListeners();
        loadClasses();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.nav_classes);
        binding.topAppBar.tvRoleBadge.setText(R.string.role_badge_student);
    }

    private void setupRecyclerView() {
        adapter = new CourseClassAdapter(course -> {
            if (course == null) return;
            Bundle bundle = new Bundle();
            bundle.putString("classId", course.getId());
            bundle.putString("classCode", course.getCode());
            bundle.putString("className", course.getName());
            bundle.putString("semester", course.getSemester());
            try {
                Navigation.findNavController(requireActivity(), R.id.nav_host_student)
                        .navigate(R.id.nav_student_class_detail, bundle);
            } catch (Exception e) {
                try {
                    NavHostFragment.findNavController(StudentClassesFragment.this)
                            .navigate(R.id.nav_student_class_detail, bundle);
                } catch (Exception ignored) {}
            }
        });
        binding.rvClasses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvClasses.setAdapter(adapter);
    }

    private void setupEmptyAndErrorViews() {
        binding.emptyState.tvEmptyTitle.setText(R.string.empty_courses_message);
        binding.emptyState.tvEmptyMessage.setText(R.string.join_class_desc);
        binding.emptyState.btnEmptyAction.setVisibility(View.VISIBLE);
        binding.emptyState.btnEmptyAction.setText(R.string.fab_join_class);
        binding.emptyState.btnEmptyAction.setOnClickListener(v -> openJoinClassSheet());

        binding.bannerError.tvBannerTitle.setText(R.string.classes_load_error);
        binding.bannerError.btnBannerAction.setVisibility(View.VISIBLE);
        binding.bannerError.btnBannerAction.setText(R.string.action_retry);
        binding.bannerError.btnBannerAction.setOnClickListener(v -> loadClasses());
    }

    private void setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(this::loadClasses);

        binding.fabJoinClass.setOnClickListener(v -> openJoinClassSheet());
    }

    private void openJoinClassSheet() {
        JoinClassBottomSheetDialogFragment sheet = JoinClassBottomSheetDialogFragment.newInstance();
        sheet.setOnClassJoinedListener(newClass -> loadClasses());
        sheet.show(getChildFragmentManager(), "JoinClassBottomSheet");
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
                            // Fallback to home summary if classes endpoint is not available or errored
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
        if (items != null && !items.isEmpty()) {
            int count = items.size();
            binding.tvClassCount.setText(String.format(getString(R.string.classes_count_format), count));
            adapter.submitList(items);
            showContentState();
        } else {
            binding.tvClassCount.setText(String.format(getString(R.string.classes_count_format), 0));
            adapter.submitList(Collections.emptyList());
            showEmptyState();
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
