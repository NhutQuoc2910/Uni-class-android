package com.ppquang06.uniclassapp.ui.student.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.HomeSummaryResponse;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.databinding.FragmentStudentHomeBinding;
import com.ppquang06.uniclassapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentHomeFragment extends Fragment {

    private FragmentStudentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTopBar();
        setupMetricLabels();
        bindUserData();
        loadHomeSummary();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.edu_lens_title);
        binding.topAppBar.tvRoleBadge.setText(R.string.role_badge_student);
    }

    private void setupMetricLabels() {
        binding.metricCourses.tvMetricLabel.setText(R.string.label_courses);
        binding.metricAttendance.tvMetricLabel.setText(R.string.label_attendance);
        binding.metricAssignments.tvMetricLabel.setText(R.string.label_assignments);
    }

    private void bindUserData() {
        if (getContext() == null) return;
        UserResponse user = SessionManager.getInstance(requireContext()).getUser();
        if (user != null && !TextUtils.isEmpty(user.getFullName())) {
            binding.tvGreetingUser.setText("Chào buổi sáng, " + user.getFullName() + "!");
        }
    }

    private void loadHomeSummary() {
        if (getContext() == null) return;
        ApiClient.getUserClassApiService(requireContext()).getHomeSummary().enqueue(new Callback<ApiResponse<HomeSummaryResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<HomeSummaryResponse>> call, @NonNull Response<ApiResponse<HomeSummaryResponse>> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    HomeSummaryResponse data = response.body().getData();
                    if (data != null) {
                        binding.metricCourses.tvMetricValue.setText(String.valueOf(data.getActiveCoursesCount()));
                        binding.metricAttendance.tvMetricValue.setText(data.getAttendanceRate() + "%");
                        binding.metricAssignments.tvMetricValue.setText(String.valueOf(data.getPendingAssignmentsCount()));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<HomeSummaryResponse>> call, @NonNull Throwable t) {
                // Keep default layout values on failure
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
