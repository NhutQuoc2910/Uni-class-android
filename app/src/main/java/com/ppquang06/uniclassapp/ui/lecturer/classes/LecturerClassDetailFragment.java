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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerClassDetailBinding;
import com.ppquang06.uniclassapp.ui.common.adapter.AssignmentAdapter;
import com.ppquang06.uniclassapp.ui.common.adapter.MaterialAdapter;
import com.ppquang06.uniclassapp.ui.common.adapter.NotificationAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerClassDetailFragment extends Fragment {

    private FragmentLecturerClassDetailBinding binding;

    private NotificationAdapter announcementAdapter;
    private MaterialAdapter materialAdapter;
    private AssignmentAdapter assignmentAdapter;

    private String classId = "";
    private String classCode = "";
    private String className = "";
    private String semester = "";
    private String inviteCode = "";
    private String status = "ACTIVE";
    private String room = "Phòng B5.12";
    private int enrolledCount = 45;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerClassDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readArguments();
        setupTopAppBar();
        setupInitialHeaders();
        setupRecyclerViews();
        setupActionButtons();
        setupSwipeRefresh();
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

        String displayInvite = (inviteCode != null && !inviteCode.isEmpty()) ? inviteCode : "K7M-2QX";
        binding.tvInviteCode.setText("Mã: " + displayInvite);

        binding.btnCopyInviteCode.setOnClickListener(v -> copyInviteCodeToClipboard());
    }

    private void copyInviteCodeToClipboard() {
        if (getContext() == null) return;
        String code = (inviteCode != null && !inviteCode.isEmpty()) ? inviteCode : "K7M-2QX";
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
        assignmentAdapter = new AssignmentAdapter();
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

    private void loadClassDetail() {
        if (getContext() == null || binding == null) return;

        if (!binding.swipeRefresh.isRefreshing()) {
            binding.pbLoading.setVisibility(View.VISIBLE);
        }

        ApiClient.getUserClassApiService(requireContext())
                .getClassDetail(classId)
                .enqueue(new Callback<ApiResponse<ClassDetailResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<ClassDetailResponse>> call,
                                           @NonNull Response<ApiResponse<ClassDetailResponse>> response) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoading.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                            bindDetailData(response.body().getData());
                        } else {
                            bindDetailData(buildFallbackDetail());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ClassDetailResponse>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoading.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        bindDetailData(buildFallbackDetail());
                    }
                });
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

        binding.tvClassCodeSemester.setText(classCode + " · " + semester);
        binding.tvClassRoom.setText(room);
        binding.tvInviteCode.setText("Mã: " + ((inviteCode != null && !inviteCode.isEmpty()) ? inviteCode : "K7M-2QX"));

        // 3 Metric Cards
        enrolledCount = detail.getEnrolledCount() > 0 ? detail.getEnrolledCount() : 45;
        binding.tvEnrolledCount.setText(String.valueOf(enrolledCount));

        int assignCount = (detail.getAssignments() != null) ? detail.getAssignments().size() : 4;
        binding.tvAssignmentCount.setText(String.valueOf(assignCount));

        int attRate = detail.getAttendanceRate() > 0 ? detail.getAttendanceRate() : 92;
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
            binding.rvAssignments.setVisibility(View.GONE);
            binding.tvEmptyAssignments.setVisibility(View.VISIBLE);
        }
    }

    private ClassDetailResponse buildFallbackDetail() {
        ClassDetailResponse detail = new ClassDetailResponse();
        detail.setId(classId != null && !classId.isEmpty() ? classId : "class-uuid-001");
        detail.setCode(classCode != null && !classCode.isEmpty() ? classCode : "NT118.O21");
        detail.setName(className != null && !className.isEmpty() ? className : "Thiết kế giao diện người dùng");
        detail.setSemester(semester != null && !semester.isEmpty() ? semester : "HK1 (2026-2027)");
        detail.setRoom(room != null && !room.isEmpty() ? room : "Phòng B5.12");
        detail.setInviteCode(inviteCode != null && !inviteCode.isEmpty() ? inviteCode : "K7M-2QX");
        detail.setEnrolledCount(45);
        detail.setAttendanceRate(92);
        detail.setPresentCount(41);
        detail.setAbsentCount(4);
        detail.setTotalSessions(15);

        List<ClassDetailResponse.AnnouncementItem> announcements = new ArrayList<>();
        announcements.add(new ClassDetailResponse.AnnouncementItem("ann-1", "Thông báo kiểm tra giữa kỳ", "Lớp sẽ có bài kiểm tra thực hành vào tuần thứ 8 tại phòng máy B5.12.", "2026-10-25", true, false));
        announcements.add(new ClassDetailResponse.AnnouncementItem("ann-2", "Chào mừng các bạn đến với học phần", "Vui lòng xem kỹ đề cương môn học và cài đặt Android Studio trước buổi học tiếp theo.", "2026-10-18", false, false));
        detail.setAnnouncements(announcements);

        List<ClassDetailResponse.MaterialItem> materials = new ArrayList<>();
        materials.add(new ClassDetailResponse.MaterialItem("mat-1", "Slide Bài 1 - Giới thiệu Android & Jetpack", "https://cdn.example.com/slide1.pdf", "PDF", "2026-10-18"));
        materials.add(new ClassDetailResponse.MaterialItem("mat-2", "Tài liệu thực hành Lab 01 - ViewBinding", "https://cdn.example.com/lab1.docx", "DOCX", "2026-10-20"));
        materials.add(new ClassDetailResponse.MaterialItem("mat-3", "Mã nguồn tham khảo Clean Architecture", "https://github.com/example/clean-android", "LINK", "2026-10-22"));
        detail.setMaterials(materials);

        List<ClassDetailResponse.ClassAssignmentItem> assignments = new ArrayList<>();
        assignments.add(new ClassDetailResponse.ClassAssignmentItem("assign-1", "Bài tập 1: Layouts & Navigation", "Thực hành thiết kế UI", "2026-10-30 23:59", null, false, null, "Chưa nộp"));
        assignments.add(new ClassDetailResponse.ClassAssignmentItem("assign-2", "Bài tập 2: Retrofit & REST APIs", "Kết nối Spring Boot backend", "2026-11-06 23:59", null, false, null, "Chưa nộp"));
        detail.setAssignments(assignments);

        return detail;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
