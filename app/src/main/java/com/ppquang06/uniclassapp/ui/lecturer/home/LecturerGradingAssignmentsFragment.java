package com.ppquang06.uniclassapp.ui.lecturer.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerGradingAssignmentsBinding;
import com.ppquang06.uniclassapp.ui.common.HomeViewModel;
import com.ppquang06.uniclassapp.ui.common.adapter.AssignmentAdapter;

import java.util.ArrayList;
import java.util.List;

public class LecturerGradingAssignmentsFragment extends Fragment {

    private FragmentLecturerGradingAssignmentsBinding binding;
    private HomeViewModel homeViewModel;
    private AssignmentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerGradingAssignmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupTopBar();
        setupRecyclerView();
        setupSwipeRefresh();
        observeViewModel();

        homeViewModel.loadGradingAssignments();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.lecturer_grading_assignments_title);
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(LecturerGradingAssignmentsFragment.this).popBackStack();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new AssignmentAdapter(item -> {
            if (item == null) return;
            Bundle bundle = new Bundle();
            bundle.putString("assignmentId", item.getId() != null ? item.getId() : "");
            bundle.putString("assignmentTitle", item.getTitle() != null ? item.getTitle() : "");
            bundle.putString("dueDate", item.getDueDate() != null ? item.getDueDate() : "");
            bundle.putString("classCode", item.getClassName() != null ? item.getClassName() : "");
            try {
                NavHostFragment.findNavController(LecturerGradingAssignmentsFragment.this)
                        .navigate(R.id.nav_lecturer_assignment_submissions, bundle);
            } catch (Exception e) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.rvAllGradingAssignments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAllGradingAssignments.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(() -> homeViewModel.loadGradingAssignments());
    }

    private void observeViewModel() {
        homeViewModel.getGradingAssignmentsState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;

            switch (state.getStatus()) {
                case LOADING:
                    if (!binding.swipeRefresh.isRefreshing()) {
                        binding.pbLoadingGrading.setVisibility(View.VISIBLE);
                    }
                    binding.tvEmptyGradingAssignments.setVisibility(View.GONE);
                    break;

                case SUCCESS:
                    binding.pbLoadingGrading.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    List<AssignmentItem> items = state.getData();
                    if (items == null || items.isEmpty()) {
                        adapter.submitList(new ArrayList<>());
                        binding.tvEmptyGradingAssignments.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmptyGradingAssignments.setVisibility(View.GONE);
                        adapter.submitList(items);
                    }
                    break;

                case ERROR:
                    binding.pbLoadingGrading.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    String msg = state.getMessage();
                    if (msg != null && !msg.trim().isEmpty() && getContext() != null) {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                    if (adapter.getItemCount() == 0) {
                        binding.tvEmptyGradingAssignments.setVisibility(View.VISIBLE);
                    }
                    break;

                case IDLE:
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
