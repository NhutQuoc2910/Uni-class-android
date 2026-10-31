package com.ppquang06.uniclassapp.ui.lecturer.assignments;

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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.AssignmentSubmissionListResponse;
import com.ppquang06.uniclassapp.data.model.response.AssignmentSubmissionListResponse.SubmissionItem;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerAssignmentSubmissionsBinding;
import com.ppquang06.uniclassapp.ui.common.UiState;

import java.util.ArrayList;
import java.util.List;

public class LecturerAssignmentSubmissionsFragment extends Fragment {

    private FragmentLecturerAssignmentSubmissionsBinding binding;
    private GradingViewModel viewModel;
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

        viewModel = new ViewModelProvider(this).get(GradingViewModel.class);

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
        observeViewModel();
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

    private void observeViewModel() {
        viewModel.getSubmissionsState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    if (!binding.swipeRefresh.isRefreshing()) {
                        binding.pbLoading.setVisibility(View.VISIBLE);
                    }
                    break;
                case SUCCESS:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    currentData = state.getData();
                    bindData(currentData);
                    break;
                case ERROR:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    String msg = (state.getMessage() != null && !state.getMessage().trim().isEmpty())
                            ? state.getMessage() : "Không thể tải danh sách bài nộp";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    break;
                case IDLE:
                    break;
            }
        });
    }

    private void loadSubmissions() {
        if (getContext() == null || binding == null) return;
        viewModel.loadSubmissions(assignmentId);
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

        int total = Math.max(0, data.getTotalStudents());
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
