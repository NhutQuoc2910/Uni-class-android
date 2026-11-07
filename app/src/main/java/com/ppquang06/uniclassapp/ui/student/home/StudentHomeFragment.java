package com.ppquang06.uniclassapp.ui.student.home;

import android.graphics.Color;
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
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.HomeSummaryResponse;
import com.ppquang06.uniclassapp.data.model.response.TodayScheduleItem;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.databinding.FragmentStudentHomeBinding;
import com.ppquang06.uniclassapp.ui.common.HomeViewModel;
import com.ppquang06.uniclassapp.ui.common.adapter.AssignmentAdapter;
import com.ppquang06.uniclassapp.ui.common.adapter.CourseClassAdapter;
import com.ppquang06.uniclassapp.ui.common.adapter.TodayScheduleAdapter;
import com.ppquang06.uniclassapp.ui.student.StudentMainActivity;
import com.ppquang06.uniclassapp.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StudentHomeFragment extends Fragment {

    private FragmentStudentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private CourseClassAdapter courseAdapter;
    private AssignmentAdapter assignmentAdapter;
    private TodayScheduleAdapter todayScheduleAdapter;
    private List<TodayScheduleItem> cachedSchedules = null;

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
        updateTodayDate();
        setupListeners();
        bindUserData();
        observeViewModel();
        loadHomeSummary();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.edu_lens_title);
        binding.topAppBar.tvRoleBadge.setText(R.string.role_badge_student);
        binding.tvHeroTag.setText(R.string.hero_today_class_tag);
        binding.tvHeroCourseName.setText(R.string.hero_empty_today_title);
        binding.tvHeroRoomSchedule.setText(R.string.hero_empty_today_sub);
        binding.btnScanHero.setVisibility(View.GONE);
    }

    private void updateTodayDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(
                "EEEE, dd/MM/yyyy",
                new Locale("vi", "VN")
        );
        String formattedDate = formatter.format(calendar.getTime());
        formattedDate = formattedDate.substring(0, 1).toUpperCase(new Locale("vi", "VN"))
                + formattedDate.substring(1);
        binding.tvTodayDate.setText(formattedDate);
    }
    @Override
    public void onResume() {
        super.onResume();

        if (binding != null) {
            updateTodayDate();
            if (cachedSchedules != null) {
                bindTodaySchedule(cachedSchedules);
            }
            loadHomeSummary();
        }
    }
    private void setupMetricLabels() {
        binding.metricCourses.tvMetricLabel.setText(R.string.label_courses);
        binding.metricAttendance.tvMetricLabel.setText(R.string.label_attendance);
        binding.metricAssignments.tvMetricLabel.setText(R.string.label_assignments);

        binding.metricAttendance.cardMetric.setBackgroundResource(R.drawable.bg_unified_bento_stat_highlight);
        binding.metricAttendance.tvMetricValue.setTextColor(Color.parseColor("#C5BAC4"));
        binding.metricAttendance.tvMetricLabel.setTextColor(Color.parseColor("#C5BAC4"));
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

        todayScheduleAdapter = new TodayScheduleAdapter(item -> {
            if (item == null || TextUtils.isEmpty(item.getClassId())) return;
            Bundle bundle = new Bundle();
            bundle.putString("classId", item.getClassId());
            bundle.putString("classCode", item.getClassCode());
            bundle.putString("className", item.getClassName());
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
        binding.rvTodaySchedule.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTodaySchedule.setAdapter(todayScheduleAdapter);
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
            try {
                NavHostFragment.findNavController(StudentHomeFragment.this)
                        .navigate(R.id.nav_student_upcoming_assignments);
            } catch (Exception e) {
                try {
                    Navigation.findNavController(requireActivity(), R.id.nav_host_student)
                            .navigate(R.id.nav_student_upcoming_assignments);
                } catch (Exception ex) {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), R.string.student_upcoming_assignments_title, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        binding.topAppBar.ivAvatar.setOnClickListener(v -> {
            if (getActivity() instanceof StudentMainActivity) {
                ((StudentMainActivity) getActivity()).switchToTab(R.id.nav_student_profile);
            }
        });

        binding.metricCourses.cardMetric.setOnClickListener(v -> {
            if (getActivity() instanceof StudentMainActivity) {
                ((StudentMainActivity) getActivity()).switchToTab(R.id.nav_student_classes);
            }
        });

        binding.metricAttendance.cardMetric.setOnClickListener(v -> {
            if (getActivity() instanceof StudentMainActivity) {
                ((StudentMainActivity) getActivity()).switchToTab(R.id.nav_student_scan_qr);
            }
        });

        binding.metricAssignments.cardMetric.setOnClickListener(v -> {
            if (binding.tvSeeAllAssignments != null) {
                binding.tvSeeAllAssignments.performClick();
            }
        });

        binding.btnEmptyJoinClass.setOnClickListener(v -> {
            com.ppquang06.uniclassapp.ui.student.classes.JoinClassBottomSheetDialogFragment sheet =
                    com.ppquang06.uniclassapp.ui.student.classes.JoinClassBottomSheetDialogFragment.newInstance();
            sheet.setOnClassJoinedListener(course -> loadHomeSummary());
            sheet.show(getChildFragmentManager(), "JoinClassBottomSheet");
        });

        binding.btnScanHero.setOnClickListener(v -> {
            if (getActivity() instanceof StudentMainActivity) {
                ((StudentMainActivity) getActivity()).switchToTab(R.id.nav_student_scan_qr);
            } else {
                NavHostFragment.findNavController(StudentHomeFragment.this)
                        .navigate(R.id.nav_student_scan_qr);
            }
        });

        binding.tvViewSemesterSchedule.setOnClickListener(v -> {
            try {
                Navigation.findNavController(requireActivity(), R.id.nav_host_student)
                        .navigate(R.id.nav_student_semester_schedule);
            } catch (Exception e) {
                try {
                    NavHostFragment.findNavController(StudentHomeFragment.this)
                            .navigate(R.id.nav_student_semester_schedule);
                } catch (Exception ex) {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), "Mở Thời khóa biểu học kỳ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void bindUserData() {
        if (getContext() == null || binding == null) return;

        UserResponse user = SessionManager.getInstance(requireContext()).getUser();

        if (user != null) {
            if (!TextUtils.isEmpty(user.getFullName())) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);

                String greeting;

                if (hour >= 5 && hour < 12) {
                    greeting = "Chào buổi sáng";
                } else if (hour >= 12 && hour < 18) {
                    greeting = "Chào buổi chiều";
                } else {
                    greeting = "Chào buổi tối";
                }

                binding.tvGreetingUser.setText(
                        greeting + ", " + user.getFullName() + "!"
                );
            }

            if (!TextUtils.isEmpty(user.getAvatarUrl())) {
                com.bumptech.glide.Glide.with(this)
                        .load(user.getAvatarUrl())
                        .circleCrop()
                        .into(binding.topAppBar.ivAvatar);
            }
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

        homeViewModel.getTodayScheduleState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || state.isIdle() || binding == null) {
                return;
            }
            if (state.isSuccess()) {
                bindTodaySchedule(state.getData());
            } else if (state.isError()) {
                bindTodaySchedule(state.getData());
            }
        });
    }

    private void loadHomeSummary() {
        if (homeViewModel != null) {
            homeViewModel.loadHomeSummary();
            homeViewModel.loadTodaySchedule();
        }
    }

    private void bindTodaySchedule(@Nullable List<TodayScheduleItem> schedules) {
        cachedSchedules = schedules;
        if (schedules != null && !schedules.isEmpty()) {
            todayScheduleAdapter.submitList(schedules);
            binding.rvTodaySchedule.setVisibility(View.VISIBLE);
            binding.tvEmptyTodaySchedule.setVisibility(View.GONE);
        } else {
            todayScheduleAdapter.submitList(Collections.emptyList());
            binding.rvTodaySchedule.setVisibility(View.GONE);
            binding.tvEmptyTodaySchedule.setVisibility(View.VISIBLE);
        }

        Calendar cal = Calendar.getInstance();
        int nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);

        List<TodayScheduleItem> activeSchedules = new ArrayList<>();
        if (schedules != null) {
            for (TodayScheduleItem item : schedules) {
                if (item != null && !item.isCancelled()) {
                    activeSchedules.add(item);
                }
            }
        }

        TodayScheduleItem heroItem = null;
        if (!activeSchedules.isEmpty()) {
            for (TodayScheduleItem item : activeSchedules) {
                if (item.isOngoing(nowMinutes)) {
                    heroItem = item;
                    break;
                }
            }
            if (heroItem == null) {
                for (TodayScheduleItem item : activeSchedules) {
                    if (item.isUpcoming(nowMinutes)) {
                        heroItem = item;
                        break;
                    }
                }
            }
        }

        if (heroItem != null) {
            binding.heroCard.setVisibility(View.VISIBLE);
            String tagPrefix = heroItem.isOngoing(nowMinutes) ? "ĐANG DIỄN RA · " : "SẮP DIỄN RA · ";
            binding.tvHeroTag.setText((tagPrefix + heroItem.getScheduleTypeLabel()).toUpperCase(new Locale("vi", "VN")));
            String courseName = !TextUtils.isEmpty(heroItem.getClassCode())
                    ? (heroItem.getClassCode() + " - " + (heroItem.getClassName() != null ? heroItem.getClassName() : ""))
                    : (heroItem.getClassName() != null ? heroItem.getClassName() : "");
            binding.tvHeroCourseName.setText(courseName);
            String roomPart = !TextUtils.isEmpty(heroItem.getRoom()) ? ("Phòng " + heroItem.getRoom()) : "Chưa xếp phòng";
            binding.tvHeroRoomSchedule.setText(roomPart + " · " + heroItem.getFormattedTimeRange());
            binding.btnScanHero.setVisibility(heroItem.canOpenOrScanQr(nowMinutes) ? View.VISIBLE : View.GONE);

            TodayScheduleItem finalHeroItem = heroItem;
            binding.heroCard.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("classId", finalHeroItem.getClassId());
                bundle.putString("classCode", finalHeroItem.getClassCode());
                bundle.putString("className", finalHeroItem.getClassName());
                try {
                    Navigation.findNavController(requireActivity(), R.id.nav_host_student).navigate(R.id.nav_student_class_detail, bundle);
                } catch (Exception e) {
                    try {
                        NavHostFragment.findNavController(StudentHomeFragment.this).navigate(R.id.nav_student_class_detail, bundle);
                    } catch (Exception ignored) {}
                }
            });
        } else if (!activeSchedules.isEmpty()) {
            binding.heroCard.setVisibility(View.VISIBLE);
            binding.tvHeroTag.setText(R.string.hero_today_class_tag);
            binding.tvHeroCourseName.setText(R.string.hero_completed_today_title);
            binding.tvHeroRoomSchedule.setText(R.string.hero_completed_today_sub);
            binding.btnScanHero.setVisibility(View.GONE);
            binding.heroCard.setOnClickListener(null);
        } else {
            binding.heroCard.setVisibility(View.VISIBLE);
            binding.tvHeroTag.setText(R.string.hero_today_class_tag);
            binding.tvHeroCourseName.setText(R.string.hero_empty_today_title);
            binding.tvHeroRoomSchedule.setText(R.string.hero_empty_today_sub);
            binding.btnScanHero.setVisibility(View.GONE);
            binding.heroCard.setOnClickListener(null);
        }
    }

    private void bindHomeData(@NonNull HomeSummaryResponse data) {
        binding.metricCourses.tvMetricValue.setText(String.valueOf(data.getActiveCoursesCount()));
        binding.metricAttendance.tvMetricValue.setText(data.getAttendanceRate() + "%");
        binding.metricAssignments.tvMetricValue.setText(String.valueOf(data.getPendingAssignmentsCount()));

        List<CourseClassItem> courses = data.getCourses();
        if (courses != null && !courses.isEmpty()) {
            courseAdapter.submitList(courses);
            binding.rvMyCourses.setVisibility(View.VISIBLE);
            binding.tvEmptyCourses.setVisibility(View.GONE);
            binding.cardEmptyOnboarding.setVisibility(View.GONE);
        } else {
            courseAdapter.submitList(Collections.emptyList());
            binding.rvMyCourses.setVisibility(View.GONE);
            binding.tvEmptyCourses.setVisibility(View.VISIBLE);
            binding.cardEmptyOnboarding.setVisibility(View.VISIBLE);
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
