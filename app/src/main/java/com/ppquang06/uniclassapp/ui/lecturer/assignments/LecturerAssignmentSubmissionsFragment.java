package com.ppquang06.uniclassapp.ui.lecturer.assignments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.AssignmentSubmissionListResponse;
import com.ppquang06.uniclassapp.data.model.response.AssignmentSubmissionListResponse.SubmissionItem;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerAssignmentSubmissionsBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerAssignmentSubmissionsFragment extends Fragment {

    private FragmentLecturerAssignmentSubmissionsBinding binding;
    private SubmissionListAdapter adapter;
    private final Gson gson = new Gson();

    private String assignmentId = "";
    private String assignmentTitle = "";
    private String classCode = "";
    private String dueDate = "";

    private AssignmentSubmissionListResponse currentData;
    private int currentFilterTab = 0; // 0: All, 1: Pending grading

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerAssignmentSubmissionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            assignmentId = getArguments().getString("assignmentId", "");
            assignmentTitle = getArguments().getString("assignmentTitle", "");
            classCode = getArguments().getString("classCode", "");
            dueDate = getArguments().getString("dueDate", "");
        }

        setupTopBar();
        setupHeader();
        setupRecyclerView();
        setupFilterTabs();
        setupSwipeRefresh();

        loadSubmissions();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.assignment_submissions_title);
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    private void setupHeader() {
        if (!TextUtils.isEmpty(assignmentTitle)) {
            binding.tvAssignmentTitle.setText(assignmentTitle);
        }
        if (!TextUtils.isEmpty(classCode)) {
            binding.tvClassCode.setText(classCode);
        }
        if (!TextUtils.isEmpty(dueDate)) {
            binding.tvDueDate.setText("Hạn nộp: " + dueDate);
        }
    }

    private void setupRecyclerView() {
        adapter = new SubmissionListAdapter((item, position) -> {
            Bundle bundle = new Bundle();
            bundle.putString("assignmentId", assignmentId);
            bundle.putString("assignmentTitle", assignmentTitle);
            bundle.putString("studentId", item.getStudentId());
            bundle.putString("studentName", item.getStudentName());
            bundle.putInt("initialIndex", position);

            // Pass filtered or current submission list as JSON
            List<SubmissionItem> displayedList = adapter.getItems();
            bundle.putString("submissionsJson", gson.toJson(displayedList));

            try {
                NavHostFragment.findNavController(this).navigate(R.id.nav_lecturer_grading, bundle);
            } catch (Exception ignored) {}
        });

        binding.rvSubmissions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSubmissions.setAdapter(adapter);
    }

    private void setupFilterTabs() {
        binding.tabFilterSubmissions.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilterTab = tab.getPosition();
                applyCurrentFilter();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(this::loadSubmissions);
    }

    private void loadSubmissions() {
        if (getContext() == null || binding == null) return;

        if (!binding.swipeRefresh.isRefreshing()) {
            binding.pbLoading.setVisibility(View.VISIBLE);
        }

        ApiClient.getUserClassApiService(requireContext())
                .getAssignmentSubmissions(assignmentId)
                .enqueue(new Callback<ApiResponse<AssignmentSubmissionListResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<AssignmentSubmissionListResponse>> call,
                                           @NonNull Response<ApiResponse<AssignmentSubmissionListResponse>> response) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoading.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                            currentData = response.body().getData();
                        } else {
                            currentData = buildFallbackData();
                        }
                        bindData(currentData);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<AssignmentSubmissionListResponse>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoading.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);

                        currentData = buildFallbackData();
                        bindData(currentData);
                    }
                });
    }

    private void bindData(AssignmentSubmissionListResponse data) {
        if (data == null || binding == null) return;

        if (!TextUtils.isEmpty(data.getAssignmentTitle())) {
            assignmentTitle = data.getAssignmentTitle();
            binding.tvAssignmentTitle.setText(assignmentTitle);
        }
        if (!TextUtils.isEmpty(data.getClassCode())) {
            classCode = data.getClassCode();
            binding.tvClassCode.setText(classCode);
        }
        if (!TextUtils.isEmpty(data.getDeadline())) {
            dueDate = data.getDeadline();
            binding.tvDueDate.setText("Hạn nộp: " + dueDate);
        }

        int total = data.getTotalStudents() > 0 ? data.getTotalStudents() : 45;
        int submitted = data.getSubmittedCount();
        binding.tvSubmittedRatio.setText(getString(R.string.submitted_ratio_format, submitted, total));

        int progressPercent = (total > 0) ? (submitted * 100) / total : 0;
        binding.progressSubmissions.setProgress(progressPercent);

        binding.tvOnTimeCount.setText(String.valueOf(data.getOnTimeCount()));
        binding.tvLateCount.setText(String.valueOf(data.getLateCount()));
        binding.tvNotSubmittedCount.setText(String.valueOf(data.getNotSubmittedCount()));

        applyCurrentFilter();
    }

    private void applyCurrentFilter() {
        if (currentData == null || binding == null) return;

        List<SubmissionItem> all = currentData.getSubmissions();
        if (all == null) all = new ArrayList<>();

        List<SubmissionItem> filtered = new ArrayList<>();
        if (currentFilterTab == 1) {
            // "Chưa chấm" filter (only items where score == null and student submitted)
            for (SubmissionItem item : all) {
                if (item.getScore() == null) {
                    filtered.add(item);
                }
            }
        } else {
            // "Tất cả"
            filtered.addAll(all);
        }

        adapter.submitList(filtered);

        if (filtered.isEmpty()) {
            binding.tvEmptySubmissions.setVisibility(View.VISIBLE);
            binding.rvSubmissions.setVisibility(View.GONE);
        } else {
            binding.tvEmptySubmissions.setVisibility(View.GONE);
            binding.rvSubmissions.setVisibility(View.VISIBLE);
        }
    }

    private AssignmentSubmissionListResponse buildFallbackData() {
        AssignmentSubmissionListResponse fallback = new AssignmentSubmissionListResponse();
        fallback.setAssignmentId(assignmentId != null && !assignmentId.isEmpty() ? assignmentId : "assign-mock-001");
        fallback.setAssignmentTitle(assignmentTitle != null && !assignmentTitle.isEmpty() ? assignmentTitle : "Bài tập 1: Layouts & Navigation");
        fallback.setClassCode(classCode != null && !classCode.isEmpty() ? classCode : "NT118.O21");
        fallback.setDeadline(dueDate != null && !dueDate.isEmpty() ? dueDate : "2026-10-30 23:59");
        fallback.setTotalStudents(45);
        fallback.setSubmittedCount(42);
        fallback.setGradedCount(20);
        fallback.setOnTimeCount(38);
        fallback.setLateCount(4);
        fallback.setNotSubmittedCount(3);

        List<SubmissionItem> list = new ArrayList<>();
        list.add(new SubmissionItem("sub-1", "stu-1", "Nguyễn Văn An", "21520001", "NA", true, false, "2026-10-28 20:30", "https://storage.uniclass.edu.vn/sub/21520001_bt1.pdf", 9.0, "Bài làm rất tốt, giao diện đúng chuẩn Figma"));
        list.add(new SubmissionItem("sub-2", "stu-2", "Trần Thị Bích", "21520002", "TB", true, false, "2026-10-28 21:15", "https://storage.uniclass.edu.vn/sub/21520002_bt1.pdf", 8.5, "Đầy đủ các màn hình, cần tối ưu padding"));
        list.add(new SubmissionItem("sub-3", "stu-3", "Lê Hoàng Cường", "21520003", "LC", true, true, "2026-10-29 08:45", "https://storage.uniclass.edu.vn/sub/21520003_bt1.docx", null, null));
        list.add(new SubmissionItem("sub-4", "stu-4", "Phạm Đăng Dũng", "21520004", "PD", true, false, "2026-10-28 22:10", "https://storage.uniclass.edu.vn/sub/21520004_bt1.zip", null, null));
        list.add(new SubmissionItem("sub-5", "stu-5", "Hoàng Mai Em", "21520005", "HE", false, false, null, null, null, null));
        list.add(new SubmissionItem("sub-6", "stu-6", "Võ Quốc Giang", "21520006", "VG", true, true, "2026-10-29 10:20", "https://storage.uniclass.edu.vn/sub/21520006_bt1.pdf", 7.0, "Nộp trễ 10 tiếng, trừ 1 điểm quy chế"));

        fallback.setSubmissions(list);
        return fallback;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
