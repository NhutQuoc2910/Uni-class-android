package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerClassDetailBinding;
import com.ppquang06.uniclassapp.ui.common.UiState;
import com.ppquang06.uniclassapp.ui.common.adapter.AssignmentAdapter;
import com.ppquang06.uniclassapp.ui.common.adapter.MaterialAdapter;
import com.ppquang06.uniclassapp.ui.common.adapter.NotificationAdapter;

import java.util.ArrayList;
import java.util.List;

public class LecturerClassDetailFragment extends Fragment {

    private FragmentLecturerClassDetailBinding binding;
    private LecturerManagementViewModel viewModel;

    private NotificationAdapter announcementAdapter;
    private MaterialAdapter materialAdapter;
    private AssignmentAdapter assignmentAdapter;

    private String classId = "";
    private String classCode = "";
    private String className = "";
    private String semester = "";
    private String inviteCode = "";
    private String status = "ACTIVE";
    private String room = "";
    private int enrolledCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerClassDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LecturerManagementViewModel.class);

        readArguments();
        setupTopAppBar();
        setupInitialHeaders();
        setupRecyclerViews();
        setupActionButtons();
        setupSwipeRefresh();
        observeViewModel();
        loadClassDetail();
    }

    private void readArguments() {
        if (getArguments() != null) {
            classId = getArguments().getString("classId", "");
            classCode = getArguments().getString("classCode", "");
            className = getArguments().getString("className", "");
            semester = getArguments().getString("semester", "");
            inviteCode = getArguments().getString("inviteCode", "");
            status = getArguments().getString("status", "ACTIVE");
        }
    }

    private void setupTopAppBar() {
        binding.topAppBar.tvTitle.setText(R.string.lecturer_class_detail_title);
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> {
            try {
                Navigation.findNavController(v).navigateUp();
            } catch (Exception ignored) {}
        });
    }

    private void setupInitialHeaders() {
        if (className != null && !className.isEmpty()) {
            binding.tvClassName.setText(className);
        } else {
            binding.tvClassName.setText(R.string.lecturer_class_detail_title);
        }

        StringBuilder meta = new StringBuilder();
        if (classCode != null && !classCode.isEmpty()) meta.append(classCode);
        if (semester != null && !semester.isEmpty()) {
            if (meta.length() > 0) meta.append(" · ");
            meta.append(semester);
        }
        binding.tvClassCodeSemester.setText(meta.toString());
        binding.tvClassRoom.setText(room);

        String displayInvite = (inviteCode != null && !inviteCode.isEmpty()) ? inviteCode : "";
        binding.tvInviteCode.setText(displayInvite.isEmpty() ? "" : ("Mã: " + displayInvite));

        binding.btnCopyInviteCode.setOnClickListener(v -> copyInviteCodeToClipboard());
    }

    private void copyInviteCodeToClipboard() {
        if (getContext() == null) return;
        String code = (inviteCode != null && !inviteCode.isEmpty()) ? inviteCode : "";
        if (code.isEmpty()) {
            Toast.makeText(requireContext(), "Chưa có mã mời", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("Class Invite Code", code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), getString(R.string.copied_invite_code) + ": " + code, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerViews() {
        // Announcements
        announcementAdapter = new NotificationAdapter();
        binding.rvAnnouncements.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAnnouncements.setAdapter(announcementAdapter);

        // Materials
        materialAdapter = new MaterialAdapter();
        binding.rvMaterials.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMaterials.setAdapter(materialAdapter);

        // Assignments
        assignmentAdapter = new AssignmentAdapter(item -> {
            Bundle bundle = new Bundle();
            bundle.putString("assignmentId", item.getId());
            bundle.putString("assignmentTitle", item.getTitle());
            bundle.putString("classCode", classCode);
            bundle.putString("dueDate", item.getDueDate());
            try {
                Navigation.findNavController(requireView()).navigate(R.id.nav_lecturer_assignment_submissions, bundle);
            } catch (Exception e) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Mở danh sách bài nộp: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.rvAssignments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAssignments.setAdapter(assignmentAdapter);
    }

    private void setupActionButtons() {
        // Quick Actions
        binding.btnManageMembers.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("classId", classId);
            bundle.putString("classCode", classCode);
            bundle.putString("className", className);
            bundle.putInt("enrolledCount", enrolledCount);
            try {
                Navigation.findNavController(v).navigate(R.id.nav_lecturer_members, bundle);
            } catch (Exception e) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Mở danh sách sinh viên", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnOpenQrSession.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("classId", classId);
            bundle.putString("classCode", classCode);
            bundle.putString("className", className);
            bundle.putString("room", room);
            try {
                Navigation.findNavController(v).navigate(R.id.nav_lecturer_qr_session, bundle);
            } catch (Exception e) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Mở phiên QR", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnClassStatistics.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("classId", classId);
            bundle.putString("classCode", classCode);
            bundle.putString("className", className);
            bundle.putString("semester", semester);
            try {
                Navigation.findNavController(v).navigate(R.id.nav_lecturer_class_statistics, bundle);
            } catch (Exception e) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Mở thống kê lớp học", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Section Actions
        binding.btnCreateAnnouncement.setOnClickListener(v -> {
            CreateAnnouncementBottomSheetDialogFragment dialog = CreateAnnouncementBottomSheetDialogFragment.newInstance(classId);
            dialog.setOnAnnouncementCreatedListener(this::loadClassDetail);
            dialog.show(getParentFragmentManager(), "CreateAnnouncementBottomSheet");
        });

        binding.btnUploadMaterial.setOnClickListener(v -> {
            UploadMaterialBottomSheetDialogFragment dialog = UploadMaterialBottomSheetDialogFragment.newInstance(classId);
            dialog.setOnMaterialUploadedListener(this::loadClassDetail);
            dialog.show(getParentFragmentManager(), "UploadMaterialBottomSheet");
        });

        binding.btnCreateAssignment.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("classId", classId);
            bundle.putString("classCode", classCode);
            bundle.putString("className", className);
            try {
                Navigation.findNavController(v).navigate(R.id.nav_lecturer_create_assignment, bundle);
            } catch (Exception e) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Mở tạo bài tập mới", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(this::loadClassDetail);
    }

    private void observeViewModel() {
        viewModel.getClassDetailState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    if (state.getData() != null) {
                        bindDetailData(state.getData());
                    } else if (!binding.swipeRefresh.isRefreshing()) {
                        binding.pbLoading.setVisibility(View.VISIBLE);
                    }
                    break;
                case SUCCESS:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    if (state.getData() != null) {
                        bindDetailData(state.getData());
                    }
                    break;
                case ERROR:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    if (state.getData() != null) {
                        bindDetailData(state.getData());
                    } else {
                        String msg = (state.getMessage() != null && !state.getMessage().trim().isEmpty())
                                ? state.getMessage() : getString(R.string.cannot_load_classes);
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case IDLE:
                    break;
            }
        });
    }

    private void loadClassDetail() {
        if (getContext() == null || binding == null) return;
        viewModel.loadClassDetail(classId, classCode, className, semester);
    }

    private void bindDetailData(ClassDetailResponse detail) {
        if (detail == null || binding == null) return;

        if (detail.getName() != null && !detail.getName().isEmpty()) {
            className = detail.getName();
            binding.tvClassName.setText(className);
        }
        if (detail.getCode() != null && !detail.getCode().isEmpty()) {
            classCode = detail.getCode();
        }
        if (detail.getSemester() != null && !detail.getSemester().isEmpty()) {
            semester = detail.getSemester();
        }
        if (detail.getRoom() != null && !detail.getRoom().isEmpty()) {
            room = detail.getRoom();
        }
        if (detail.getInviteCode() != null && !detail.getInviteCode().isEmpty()) {
            inviteCode = detail.getInviteCode();
        }

        binding.tvClassCodeSemester.setText(classCode + (semester.isEmpty() ? "" : (" · " + semester)));
        binding.tvClassRoom.setText(room);
        binding.tvInviteCode.setText(inviteCode.isEmpty() ? "" : ("Mã: " + inviteCode));

        // 3 Metric Cards
        enrolledCount = Math.max(0, detail.getEnrolledCount());
        binding.tvEnrolledCount.setText(String.valueOf(enrolledCount));

        int assignCount = (detail.getAssignments() != null) ? detail.getAssignments().size() : 0;
        binding.tvAssignmentCount.setText(String.valueOf(assignCount));

        int attRate = Math.max(0, detail.getAttendanceRate());
        binding.tvAttendanceRate.setText(attRate + "%");

        // Announcements
        List<ClassDetailResponse.AnnouncementItem> announcements = detail.getAnnouncements();
        if (announcements != null && !announcements.isEmpty()) {
            List<NotificationItemResponse> notifList = new ArrayList<>();
            for (ClassDetailResponse.AnnouncementItem a : announcements) {
                notifList.add(NotificationAdapter.fromAnnouncement(a, classCode));
            }
            announcementAdapter.submitList(notifList);
            binding.rvAnnouncements.setVisibility(View.VISIBLE);
            binding.tvEmptyAnnouncements.setVisibility(View.GONE);
        } else {
            announcementAdapter.submitList(new ArrayList<>());
            binding.rvAnnouncements.setVisibility(View.GONE);
            binding.tvEmptyAnnouncements.setVisibility(View.VISIBLE);
        }

        // Materials
        List<ClassDetailResponse.MaterialItem> materials = detail.getMaterials();
        if (materials != null && !materials.isEmpty()) {
            materialAdapter.submitList(materials);
            binding.rvMaterials.setVisibility(View.VISIBLE);
            binding.tvEmptyMaterials.setVisibility(View.GONE);
        } else {
            materialAdapter.submitList(new ArrayList<>());
            binding.rvMaterials.setVisibility(View.GONE);
            binding.tvEmptyMaterials.setVisibility(View.VISIBLE);
        }

        // Assignments
        List<ClassDetailResponse.ClassAssignmentItem> assignments = detail.getAssignments();
        if (assignments != null && !assignments.isEmpty()) {
            List<AssignmentItem> assignItems = new ArrayList<>();
            for (ClassDetailResponse.ClassAssignmentItem a : assignments) {
                assignItems.add(AssignmentAdapter.fromClassAssignment(a, className));
            }
            assignmentAdapter.submitList(assignItems);
            binding.rvAssignments.setVisibility(View.VISIBLE);
            binding.tvEmptyAssignments.setVisibility(View.GONE);
        } else {
            assignmentAdapter.submitList(new ArrayList<>());
            binding.rvAssignments.setVisibility(View.GONE);
            binding.tvEmptyAssignments.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
