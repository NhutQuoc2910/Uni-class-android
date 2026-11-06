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
import com.ppquang06.uniclassapp.databinding.FragmentLecturerHomeBinding;
import com.ppquang06.uniclassapp.ui.common.HomeViewModel;
import com.ppquang06.uniclassapp.ui.common.adapter.AssignmentAdapter;
import com.ppquang06.uniclassapp.ui.common.adapter.CourseClassAdapter;
import com.ppquang06.uniclassapp.ui.common.adapter.TodayScheduleAdapter;
import com.ppquang06.uniclassapp.ui.lecturer.LecturerMainActivity;
import com.ppquang06.uniclassapp.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class LecturerHomeFragment extends Fragment {

    private FragmentLecturerHomeBinding binding;
    private HomeViewModel homeViewModel;
    private CourseClassAdapter courseAdapter;
    private TodayScheduleAdapter todayScheduleAdapter;
    private AssignmentAdapter gradingAssignmentAdapter;
    private List<TodayScheduleItem> cachedSchedules = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerHomeBinding.inflate(inflater, container, false);
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
        binding.topAppBar.tvRoleBadge.setText(R.string.role_badge_lecturer);
    }

    private void updateTodayDate() {
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
        binding.metricPending.tvMetricLabel.setText(R.string.label_pending);
    }

    private void setupRecyclerViews() {
        courseAdapter = new CourseClassAdapter(this::openClassDetail);
        binding.rvLecturerCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLecturerCourses.setAdapter(courseAdapter);

        todayScheduleAdapter = new TodayScheduleAdapter(item -> {
            if (item == null || TextUtils.isEmpty(item.getClassId())) return;
            openClassDetailById(item.getClassId(), item.getClassCode(), item.getClassName());
        });
        binding.rvTodaySchedule.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTodaySchedule.setAdapter(todayScheduleAdapter);

        gradingAssignmentAdapter = new AssignmentAdapter(item -> {
            if (item == null) return;
            Bundle bundle = new Bundle();
            bundle.putString("assignmentId", item.getId() != null ? item.getId() : "");
            bundle.putString("assignmentTitle", item.getTitle() != null ? item.getTitle() : "");
            bundle.putString("dueDate", item.getDueDate() != null ? item.getDueDate() : "");
            bundle.putString("classCode", item.getClassName() != null ? item.getClassName() : "");
            try {
                NavHostFragment.findNavController(LecturerHomeFragment.this)
                        .navigate(R.id.nav_lecturer_assignment_submissions, bundle);
            } catch (Exception e) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.rvGradingAssignments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGradingAssignments.setAdapter(gradingAssignmentAdapter);
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

    private void openClassDetailById(String classId, String classCode, String className) {
        Bundle args = new Bundle();
        args.putString("classId", classId != null ? classId : "");
        args.putString("classCode", classCode != null ? classCode : "");
        args.putString("className", className != null ? className : "");
        args.putString("status", "ACTIVE");
        try {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.nav_lecturer_class_detail, args);
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(requireContext(), (classCode != null ? classCode : "") + " - " + (className != null ? className : ""), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(this::loadHomeSummary);

        binding.tvSeeAllCourses.setOnClickListener(v -> {
            if (getActivity() instanceof LecturerMainActivity) {
                ((LecturerMainActivity) getActivity()).switchToTab(R.id.nav_lecturer_classes);
            } else {
                NavHostFragment.findNavController(LecturerHomeFragment.this).navigate(R.id.nav_lecturer_classes);
            }
        });

        binding.tvSeeAllAssignments.setOnClickListener(v -> {
            try {
                NavHostFragment.findNavController(LecturerHomeFragment.this)
                        .navigate(R.id.nav_lecturer_grading_assignments);
            } catch (Exception e) {
                try {
                    androidx.navigation.Navigation.findNavController(requireActivity(), R.id.nav_host_lecturer)
                            .navigate(R.id.nav_lecturer_grading_assignments);
                } catch (Exception ex) {
                    if (getContext() != null) {
                        android.widget.Toast.makeText(requireContext(), "Bài tập cần chấm", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        binding.tvViewSemesterSchedule.setOnClickListener(v -> {
            try {
                Navigation.findNavController(requireActivity(), R.id.nav_host_lecturer)
                        .navigate(R.id.nav_lecturer_semester_schedule);
            } catch (Exception e) {
                try {
                    NavHostFragment.findNavController(LecturerHomeFragment.this)
                            .navigate(R.id.nav_lecturer_semester_schedule);
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

        if (user != null && !TextUtils.isEmpty(user.getFullName())) {
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
            binding.btnHeroOpenQr.setVisibility(heroItem.canOpenOrScanQr(nowMinutes) ? View.VISIBLE : View.GONE);
            binding.btnHeroEnterClass.setVisibility(View.VISIBLE);

            TodayScheduleItem finalHeroItem = heroItem;
            binding.btnHeroOpenQr.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("classId", finalHeroItem.getClassId() != null ? finalHeroItem.getClassId() : "");
                args.putString("classCode", finalHeroItem.getClassCode() != null ? finalHeroItem.getClassCode() : "");
                args.putString("className", finalHeroItem.getClassName() != null ? finalHeroItem.getClassName() : "");
                args.putString("room", finalHeroItem.getRoom() != null ? finalHeroItem.getRoom() : "");
                try {
                    NavHostFragment.findNavController(LecturerHomeFragment.this)
                            .navigate(R.id.nav_lecturer_qr_session, args);
                } catch (Exception e) {
                    openClassDetailById(finalHeroItem.getClassId(), finalHeroItem.getClassCode(), finalHeroItem.getClassName());
                }
            });

            View.OnClickListener enterClassListener = v -> {
                openClassDetailById(finalHeroItem.getClassId(), finalHeroItem.getClassCode(), finalHeroItem.getClassName());
            };
            binding.btnHeroEnterClass.setOnClickListener(enterClassListener);
            binding.heroCard.setOnClickListener(enterClassListener);
        } else if (!activeSchedules.isEmpty()) {
            binding.heroCard.setVisibility(View.VISIBLE);
            binding.tvHeroTag.setText(R.string.hero_lecturer_tag);
            binding.tvHeroCourseName.setText(R.string.hero_completed_today_title_lecturer);
            binding.tvHeroRoomSchedule.setText(R.string.hero_completed_today_sub_lecturer);
            binding.btnHeroOpenQr.setVisibility(View.GONE);
            binding.btnHeroEnterClass.setVisibility(View.GONE);
            binding.heroCard.setOnClickListener(null);
        } else {
            binding.heroCard.setVisibility(View.VISIBLE);
            binding.tvHeroTag.setText(R.string.hero_lecturer_tag);
            binding.tvHeroCourseName.setText(R.string.hero_empty_today_title_lecturer);
            binding.tvHeroRoomSchedule.setText(R.string.hero_empty_today_sub_lecturer);
            binding.btnHeroOpenQr.setVisibility(View.GONE);
            binding.btnHeroEnterClass.setVisibility(View.GONE);
            binding.heroCard.setOnClickListener(null);
        }
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
        } else {
            courseAdapter.submitList(Collections.emptyList());
            binding.rvLecturerCourses.setVisibility(View.GONE);
            binding.tvEmptyCourses.setVisibility(View.VISIBLE);
        }

        List<AssignmentItem> assignments = data.getDueSoonAssignments();
        if (assignments != null && !assignments.isEmpty()) {
            gradingAssignmentAdapter.submitList(assignments);
            binding.rvGradingAssignments.setVisibility(View.VISIBLE);
            binding.tvEmptyAssignments.setVisibility(View.GONE);
        } else {
            gradingAssignmentAdapter.submitList(Collections.emptyList());
            binding.rvGradingAssignments.setVisibility(View.GONE);
            binding.tvEmptyAssignments.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
