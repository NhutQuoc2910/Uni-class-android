package com.ppquang06.uniclassapp.ui.student.home;

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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.databinding.FragmentStudentUpcomingAssignmentsBinding;
import com.ppquang06.uniclassapp.ui.common.HomeViewModel;
import com.ppquang06.uniclassapp.ui.common.adapter.AssignmentAdapter;

import java.util.Collections;
import java.util.List;

/**
 * Full-list screen for all assignments that are upcoming / due soon for a student.
 */
public class StudentUpcomingAssignmentsFragment extends Fragment {

    private FragmentStudentUpcomingAssignmentsBinding binding;
    private HomeViewModel homeViewModel;
    private AssignmentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentUpcomingAssignmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupTopBar();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
        loadUpcomingAssignments();
    }

    private void setupTopBar() {
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.tvTitle.setText(R.string.student_upcoming_assignments_title);
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
        binding.topAppBar.ivBack.setOnClickListener(v -> {
            try {
                if (!NavHostFragment.findNavController(this).popBackStack()) {
                    requireActivity().onBackPressed();
                }
            } catch (Exception e) {
                requireActivity().onBackPressed();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new AssignmentAdapter(item -> {
            if (item == null) return;
            Bundle bundle = new Bundle();
            bundle.putString("assignmentId", item.getId());
            bundle.putString("assignmentTitle", item.getTitle());
            bundle.putString("className", item.getClassName());
            try {
                NavHostFragment.findNavController(StudentUpcomingAssignmentsFragment.this)
                        .navigate(R.id.nav_student_assignment_detail, bundle);
            } catch (Exception e) {
                try {
                    Navigation.findNavController(requireActivity(), R.id.nav_host_student)
                            .navigate(R.id.nav_student_assignment_detail, bundle);
                } catch (Exception ex) {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        binding.rvAllUpcomingAssignments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAllUpcomingAssignments.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefreshUpcoming.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefreshUpcoming.setOnRefreshListener(this::loadUpcomingAssignments);
    }

    private void observeViewModel() {
        homeViewModel.getUpcomingAssignmentsState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || state.isIdle() || binding == null) {
                return;
            }
            if (state.isLoading()) {
                if (!binding.swipeRefreshUpcoming.isRefreshing()) {
                    binding.pbLoadingUpcoming.setVisibility(View.VISIBLE);
                }
            } else if (state.isSuccess()) {
                binding.pbLoadingUpcoming.setVisibility(View.GONE);
                binding.swipeRefreshUpcoming.setRefreshing(false);
                bindList(state.getData());
            } else if (state.isError()) {
                // Fallback to HomeSummary if the dedicated endpoint is unavailable on the server
                homeViewModel.loadHomeSummary();
            }
        });

        homeViewModel.getHomeSummaryState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || state.isIdle() || state.isLoading() || binding == null) {
                return;
            }
            binding.pbLoadingUpcoming.setVisibility(View.GONE);
            binding.swipeRefreshUpcoming.setRefreshing(false);
            if (state.getData() != null) {
                bindList(state.getData().getDueSoonAssignments());
            } else {
                bindList(null);
            }
        });
    }

    private void bindList(@Nullable List<AssignmentItem> items) {
        if (items != null && !items.isEmpty()) {
            adapter.submitList(items);
            binding.rvAllUpcomingAssignments.setVisibility(View.VISIBLE);
            binding.tvEmptyUpcomingAssignments.setVisibility(View.GONE);
        } else {
            adapter.submitList(Collections.emptyList());
            binding.rvAllUpcomingAssignments.setVisibility(View.GONE);
            binding.tvEmptyUpcomingAssignments.setVisibility(View.VISIBLE);
        }
    }

    private void loadUpcomingAssignments() {
        if (homeViewModel != null) {
            homeViewModel.loadUpcomingAssignments();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
