package com.ppquang06.uniclassapp.ui.student.home;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.HomeSummaryResponse;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.databinding.FragmentStudentHomeBinding;
import com.ppquang06.uniclassapp.ui.common.HomeViewModel;
import com.ppquang06.uniclassapp.ui.common.adapter.AssignmentAdapter;
import com.ppquang06.uniclassapp.ui.common.adapter.CourseClassAdapter;
import com.ppquang06.uniclassapp.ui.student.StudentMainActivity;
import com.ppquang06.uniclassapp.utils.SessionManager;

import java.util.Collections;
import java.util.List;

public class StudentHomeFragment extends Fragment {

    private FragmentStudentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private CourseClassAdapter courseAdapter;
    private AssignmentAdapter assignmentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupTopBar();
        setupMetricLabels();
        setupRecyclerViews();
        setupListeners();
        bindUserData();
        observeViewModel();
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

    private void setupRecyclerViews() {
        courseAdapter = new CourseClassAdapter(course -> {
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
                    NavHostFragment.findNavController(StudentHomeFragment.this)
                            .navigate(R.id.nav_student_class_detail, bundle);
                } catch (Exception ignored) {}
            }
        });
        binding.rvMyCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMyCourses.setAdapter(courseAdapter);

        assignmentAdapter = new AssignmentAdapter(item -> {
            if (item == null) return;
            Bundle bundle = new Bundle();
            bundle.putString("assignmentId", item.getId());
            bundle.putString("assignmentTitle", item.getTitle());
            bundle.putString("className", item.getClassName());
            try {
                Navigation.findNavController(requireActivity(), R.id.nav_host_student)
                        .navigate(R.id.nav_student_assignment_detail, bundle);
            } catch (Exception e) {
                try {
                    NavHostFragment.findNavController(StudentHomeFragment.this)
                            .navigate(R.id.nav_student_assignment_detail, bundle);
                } catch (Exception ignored) {}
            }
        });
        binding.rvDueSoonAssignments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDueSoonAssignments.setAdapter(assignmentAdapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(this::loadHomeSummary);

        binding.tvSeeAllCourses.setOnClickListener(v -> {
            if (getActivity() instanceof StudentMainActivity) {
                ((StudentMainActivity) getActivity()).switchToTab(R.id.nav_student_classes);
            } else {
                NavHostFragment.findNavController(StudentHomeFragment.this)
                        .navigate(R.id.nav_student_classes);
            }
        });

        binding.tvSeeAllAssignments.setOnClickListener(v -> {
            if (getActivity() instanceof StudentMainActivity) {
                ((StudentMainActivity) getActivity()).switchToTab(R.id.nav_student_notifications);
            } else {
                NavHostFragment.findNavController(StudentHomeFragment.this)
                        .navigate(R.id.nav_student_notifications);
            }
        });

        binding.btnScanHero.setOnClickListener(v -> {
            if (getActivity() instanceof StudentMainActivity) {
                ((StudentMainActivity) getActivity()).switchToTab(R.id.nav_student_scan_qr);
            } else {
                NavHostFragment.findNavController(StudentHomeFragment.this)
                        .navigate(R.id.nav_student_scan_qr);
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

    private void observeViewModel() {
        homeViewModel.getHomeSummaryState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || state.isIdle() || binding == null) {
                return;
            }
            if (state.isLoading()) {
                if (state.getData() != null) {
                    bindHomeData(state.getData());
                }
            } else if (state.isSuccess()) {
                binding.swipeRefresh.setRefreshing(false);
                if (state.getData() != null) {
                    bindHomeData(state.getData());
                }
            } else if (state.isError()) {
                binding.swipeRefresh.setRefreshing(false);
                if (state.getData() != null) {
                    bindHomeData(state.getData());
                }
            }
        });
    }

    private void loadHomeSummary() {
        if (homeViewModel != null) {
            homeViewModel.loadHomeSummary();
        }
    }

    private void bindHomeData(@NonNull HomeSummaryResponse data) {
        binding.metricCourses.tvMetricValue.setText(String.valueOf(data.getActiveCoursesCount()));
        binding.metricAttendance.tvMetricValue.setText(data.getAttendanceRate() + "%");
        binding.metricAssignments.tvMetricValue.setText(String.valueOf(data.getPendingAssignmentsCount()));

        List<CourseClassItem> courses = data.getCourses();
        if (courses != null && !courses.isEmpty()) {
            CourseClassItem first = courses.get(0);
            binding.heroCard.setVisibility(View.VISIBLE);
            binding.tvHeroCourseName.setText(first.getName());
            String roomText = !TextUtils.isEmpty(first.getRoom())
                    ? ("Phòng " + first.getRoom() + " · " + (first.getSemester() != null ? first.getSemester() : ""))
                    : (first.getSemester() != null ? first.getSemester() : "");
            binding.tvHeroRoomSchedule.setText(roomText);

            courseAdapter.submitList(courses);
            binding.rvMyCourses.setVisibility(View.VISIBLE);
            binding.tvEmptyCourses.setVisibility(View.GONE);
        } else {
            binding.heroCard.setVisibility(View.GONE);
            courseAdapter.submitList(Collections.emptyList());
            binding.rvMyCourses.setVisibility(View.GONE);
            binding.tvEmptyCourses.setVisibility(View.VISIBLE);
        }

        List<AssignmentItem> assignments = data.getDueSoonAssignments();
        if (assignments != null && !assignments.isEmpty()) {
            assignmentAdapter.submitList(assignments);
            binding.rvDueSoonAssignments.setVisibility(View.VISIBLE);
            binding.tvEmptyAssignments.setVisibility(View.GONE);
        } else {
            assignmentAdapter.submitList(Collections.emptyList());
            binding.rvDueSoonAssignments.setVisibility(View.GONE);
            binding.tvEmptyAssignments.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
