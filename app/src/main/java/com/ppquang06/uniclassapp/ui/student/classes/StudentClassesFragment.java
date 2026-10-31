package com.ppquang06.uniclassapp.ui.student.classes;

import android.os.Bundle;
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
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.databinding.FragmentStudentClassesBinding;
import com.ppquang06.uniclassapp.ui.common.UiState;
import com.ppquang06.uniclassapp.ui.common.adapter.CourseClassAdapter;

import java.util.Collections;
import java.util.List;

public class StudentClassesFragment extends Fragment {

    private FragmentStudentClassesBinding binding;
    private CourseClassAdapter adapter;
    private ClassDetailViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentClassesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ClassDetailViewModel.class);

        setupTopBar();
        setupRecyclerView();
        setupEmptyAndErrorViews();
        setupListeners();
        observeViewModel();
        loadClasses();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.nav_classes);
        binding.topAppBar.tvRoleBadge.setText(R.string.role_badge_student);
    }

    private void setupRecyclerView() {
        adapter = new CourseClassAdapter(course -> {
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
                    NavHostFragment.findNavController(StudentClassesFragment.this)
                            .navigate(R.id.nav_student_class_detail, bundle);
                } catch (Exception ignored) {}
            }
        });
        binding.rvClasses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvClasses.setAdapter(adapter);
    }

    private void setupEmptyAndErrorViews() {
        binding.emptyState.tvEmptyTitle.setText(R.string.empty_courses_message);
        binding.emptyState.tvEmptyMessage.setText(R.string.join_class_desc);
        binding.emptyState.btnEmptyAction.setVisibility(View.VISIBLE);
        binding.emptyState.btnEmptyAction.setText(R.string.fab_join_class);
        binding.emptyState.btnEmptyAction.setOnClickListener(v -> openJoinClassSheet());

        binding.bannerError.tvBannerTitle.setText(R.string.classes_load_error);
        binding.bannerError.btnBannerAction.setVisibility(View.VISIBLE);
        binding.bannerError.btnBannerAction.setText(R.string.action_retry);
        binding.bannerError.btnBannerAction.setOnClickListener(v -> loadClasses());
    }

    private void setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(this::loadClasses);

        binding.fabJoinClass.setOnClickListener(v -> openJoinClassSheet());
    }

    private void observeViewModel() {
        viewModel.getClassesState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    if (state.getData() != null && !state.getData().isEmpty()) {
                        binding.swipeRefresh.setRefreshing(false);
                        handleClassesResult(state.getData());
                    } else if (!binding.swipeRefresh.isRefreshing()) {
                        showLoadingState();
                    }
                    break;
                case SUCCESS:
                    binding.swipeRefresh.setRefreshing(false);
                    handleClassesResult(state.getData());
                    break;
                case ERROR:
                    binding.swipeRefresh.setRefreshing(false);
                    if (state.getData() != null && !state.getData().isEmpty()) {
                        handleClassesResult(state.getData());
                    } else {
                        showErrorState(state.getMessage());
                    }
                    break;
                case IDLE:
                    break;
            }
        });
    }

    private void openJoinClassSheet() {
        JoinClassBottomSheetDialogFragment sheet = JoinClassBottomSheetDialogFragment.newInstance();
        sheet.setOnClassJoinedListener(newClass -> loadClasses());
        sheet.show(getChildFragmentManager(), "JoinClassBottomSheet");
    }

    private void loadClasses() {
        if (getContext() == null || binding == null) return;
        viewModel.loadClasses();
    }

    private void handleClassesResult(List<CourseClassItem> items) {
        if (items != null && !items.isEmpty()) {
            int count = items.size();
            binding.tvClassCount.setText(String.format(getString(R.string.classes_count_format), count));
            adapter.submitList(items);
            showContentState();
        } else {
            binding.tvClassCount.setText(String.format(getString(R.string.classes_count_format), 0));
            adapter.submitList(Collections.emptyList());
            showEmptyState();
        }
    }

    private void showLoadingState() {
        binding.progressLoading.setVisibility(View.VISIBLE);
        binding.swipeRefresh.setVisibility(View.GONE);
        binding.emptyState.getRoot().setVisibility(View.GONE);
        binding.bannerError.getRoot().setVisibility(View.GONE);
    }

    private void showContentState() {
        binding.progressLoading.setVisibility(View.GONE);
        binding.swipeRefresh.setVisibility(View.VISIBLE);
        binding.emptyState.getRoot().setVisibility(View.GONE);
        binding.bannerError.getRoot().setVisibility(View.GONE);
    }

    private void showEmptyState() {
        binding.progressLoading.setVisibility(View.GONE);
        binding.swipeRefresh.setVisibility(View.GONE);
        binding.emptyState.getRoot().setVisibility(View.VISIBLE);
        binding.bannerError.getRoot().setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        binding.progressLoading.setVisibility(View.GONE);
        binding.swipeRefresh.setVisibility(View.GONE);
        binding.emptyState.getRoot().setVisibility(View.GONE);
        binding.bannerError.getRoot().setVisibility(View.VISIBLE);
        if (message != null && !message.isEmpty()) {
            binding.bannerError.tvBannerSubtitle.setText(message);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
