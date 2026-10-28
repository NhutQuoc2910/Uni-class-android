package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.ClassMemberItemResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerMemberProfileBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LecturerMemberProfileFragment extends Fragment {

    private FragmentLecturerMemberProfileBinding binding;
    private MemberAssignmentAdapter assignmentAdapter;
    private ClassMemberItemResponse member;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerMemberProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readArguments();
        setupTopAppBar();
        setupRecyclerView();
        bindMemberData();
    }

    private void readArguments() {
        if (getArguments() != null) {
            if (getArguments().containsKey("member")) {
                try {
                    member = (ClassMemberItemResponse) getArguments().getSerializable("member");
                } catch (Exception ignored) {}
            }
            if (member == null && getArguments().containsKey("memberJson")) {
                String json = getArguments().getString("memberJson", "");
                if (!json.isEmpty()) {
                    try {
                        member = new Gson().fromJson(json, ClassMemberItemResponse.class);
                    } catch (Exception ignored) {}
                }
            }
        }

        if (member == null) {
            // Fallback synthesis
            member = new ClassMemberItemResponse(
                    "stu-001", "Nguyễn Văn An", "an.nv@uit.edu.vn", "21520001",
                    "NA", 93, 1, 15, 4, 4, 8.8,
                    Arrays.asList(
                            new ClassMemberItemResponse.MemberAssignmentSummary("a1", "Bài tập 1: Layouts & ViewBinding", "2026-10-15", true, "2026-10-14 20:30", 9.0),
                            new ClassMemberItemResponse.MemberAssignmentSummary("a2", "Bài tập 2: Retrofit & MVVM", "2026-10-22", true, "2026-10-21 18:15", 8.5)
                    )
            );
        }
    }

    private void setupTopAppBar() {
        binding.topAppBar.tvTitle.setText(R.string.lecturer_profile_title);
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> {
            try {
                Navigation.findNavController(v).navigateUp();
            } catch (Exception ignored) {}
        });
    }

    private void setupRecyclerView() {
        assignmentAdapter = new MemberAssignmentAdapter();
        binding.rvMemberAssignments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMemberAssignments.setAdapter(assignmentAdapter);
    }

    private void bindMemberData() {
        if (member == null || binding == null || getContext() == null) return;

        // Avatar Initial
        String initial = member.getAvatarInitial();
        if (initial == null || initial.isEmpty()) {
            String name = member.getFullName();
            if (name != null && !name.trim().isEmpty()) {
                String[] parts = name.trim().split("\\s+");
                if (parts.length == 1) {
                    initial = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
                } else {
                    initial = ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
                }
            } else {
                initial = "SV";
            }
        }
        binding.tvProfileAvatarInitial.setText(initial);

        // Header Info
        binding.tvProfileStudentName.setText(member.getFullName() != null ? member.getFullName() : "Sinh viên");
        binding.tvProfileStudentCode.setText("MSSV: " + (member.getStudentCode() != null ? member.getStudentCode() : "---"));
        binding.tvProfileStudentEmail.setText(member.getEmail() != null ? member.getEmail() : "---");

        // 3 Metric Cards
        binding.tvProfileAttendanceRate.setText(member.getAttendanceRate() + "%");
        int sub = member.getSubmittedAssignments();
        int totalAssign = member.getTotalAssignments() > 0 ? member.getTotalAssignments() : 4;
        binding.tvProfileSubmittedAssignments.setText(sub + "/" + totalAssign);

        if (member.getAverageScore() != null) {
            binding.tvProfileAverageScore.setText(String.format(Locale.US, "%.1f", member.getAverageScore()));
        } else {
            binding.tvProfileAverageScore.setText("--");
        }

        // Absence Progress Bar Card
        int absent = member.getAbsentSessions();
        int totalSessions = member.getTotalSessions() > 0 ? member.getTotalSessions() : 15;
        binding.tvAbsentSessionsText.setText(String.format(getString(R.string.format_absence_sessions), absent, totalSessions));

        binding.pbAbsenceProgress.setMax(totalSessions);
        binding.pbAbsenceProgress.setProgress(absent);

        double absentRate = (absent * 100.0) / totalSessions;
        if (absent <= 3 && absentRate <= 20.0) {
            binding.pbAbsenceProgress.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.color_success));
            binding.tvAbsenceWarning.setVisibility(View.GONE);
        } else if (absent <= 5) {
            binding.pbAbsenceProgress.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.color_warning));
            binding.tvAbsenceWarning.setVisibility(View.VISIBLE);
        } else {
            binding.pbAbsenceProgress.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.color_error));
            binding.tvAbsenceWarning.setVisibility(View.VISIBLE);
        }

        // Recent Assignments
        List<ClassMemberItemResponse.MemberAssignmentSummary> assignments = member.getRecentAssignments();
        if (assignments != null && !assignments.isEmpty()) {
            assignmentAdapter.submitList(assignments);
            binding.rvMemberAssignments.setVisibility(View.VISIBLE);
            binding.tvNoAssignments.setVisibility(View.GONE);
        } else {
            binding.rvMemberAssignments.setVisibility(View.GONE);
            binding.tvNoAssignments.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
