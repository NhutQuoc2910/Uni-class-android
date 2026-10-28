package com.ppquang06.uniclassapp.ui.lecturer.home;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerHomeBinding;
import com.ppquang06.uniclassapp.ui.common.adapter.CourseClassAdapter;
import com.ppquang06.uniclassapp.utils.SessionManager;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerHomeFragment extends Fragment {

    private FragmentLecturerHomeBinding binding;
    private CourseClassAdapter courseAdapter;
    private CourseClassItem activeHeroClass;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTopBar();
        setupMetricLabels();
        setupRecyclerView();
        setupListeners();
        bindUserData();
        loadHomeSummary();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.edu_lens_title);
        binding.topAppBar.tvRoleBadge.setText(R.string.role_badge_lecturer);
    }

    private void setupMetricLabels() {
        binding.metricCourses.tvMetricLabel.setText(R.string.label_courses);
        binding.metricAttendance.tvMetricLabel.setText(R.string.label_attendance);
        binding.metricPending.tvMetricLabel.setText(R.string.label_pending);
    }

    private void setupRecyclerView() {
        courseAdapter = new CourseClassAdapter(this::openClassDetail);
        binding.rvLecturerCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLecturerCourses.setAdapter(courseAdapter);
    }

    private void openClassDetail(CourseClassItem item) {
        if (item == null) return;
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

    private void setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(this::loadHomeSummary);

        binding.tvSeeAllCourses.setOnClickListener(v -> {
            try {
                Navigation.findNavController(requireActivity(), R.id.nav_host_lecturer)
                        .navigate(R.id.nav_lecturer_classes);
            } catch (Exception e) {
                NavHostFragment.findNavController(LecturerHomeFragment.this)
                        .navigate(R.id.nav_lecturer_classes);
            }
        });

        binding.btnHeroOpenQr.setOnClickListener(v -> {
            if (activeHeroClass != null) {
                Bundle args = new Bundle();
                args.putString("classId", activeHeroClass.getId() != null ? activeHeroClass.getId() : "");
                args.putString("classCode", activeHeroClass.getCode() != null ? activeHeroClass.getCode() : "");
                args.putString("className", activeHeroClass.getName() != null ? activeHeroClass.getName() : "");
                args.putString("room", activeHeroClass.getRoom() != null ? activeHeroClass.getRoom() : "");
                try {
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.nav_lecturer_qr_session, args);
                } catch (Exception e) {
                    openClassDetail(activeHeroClass);
                }
            }
        });

        binding.btnHeroEnterClass.setOnClickListener(v -> {
            if (activeHeroClass != null) {
                openClassDetail(activeHeroClass);
            }
        });

        binding.heroCard.setOnClickListener(v -> {
            if (activeHeroClass != null) {
                openClassDetail(activeHeroClass);
            }
        });
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
                binding.swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    HomeSummaryResponse data = response.body().getData();
                    if (data != null) {
                        bindHomeData(data);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<HomeSummaryResponse>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void bindHomeData(@NonNull HomeSummaryResponse data) {
        binding.metricCourses.tvMetricValue.setText(String.valueOf(data.getActiveCoursesCount()));
        binding.metricAttendance.tvMetricValue.setText(data.getAttendanceRate() + "%");
        binding.metricPending.tvMetricValue.setText(String.valueOf(data.getPendingAssignmentsCount()));

        List<CourseClassItem> courses = data.getCourses();
        if (courses != null && !courses.isEmpty()) {
            courseAdapter.submitList(courses);
            binding.rvLecturerCourses.setVisibility(View.VISIBLE);
            binding.tvEmptyCourses.setVisibility(View.GONE);

            // Bind first active course to Hero Card
            CourseClassItem firstClass = courses.get(0);
            activeHeroClass = firstClass;
            binding.tvHeroCourseName.setText(firstClass.getName());
            String room = firstClass.getRoom() != null ? firstClass.getRoom().trim() : "";
            String code = firstClass.getCode() != null ? firstClass.getCode() : "";
            if (!room.isEmpty()) {
                binding.tvHeroRoomSchedule.setText(code + " · Phòng " + room);
            } else {
                binding.tvHeroRoomSchedule.setText(code);
            }
            binding.heroCard.setVisibility(View.VISIBLE);
        } else {
            activeHeroClass = null;
            courseAdapter.submitList(Collections.emptyList());
            binding.rvLecturerCourses.setVisibility(View.GONE);
            binding.tvEmptyCourses.setVisibility(View.VISIBLE);
            binding.heroCard.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
