package com.ppquang06.uniclassapp.ui.student.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.databinding.FragmentClassTabListBinding;
import com.ppquang06.uniclassapp.ui.common.adapter.AssignmentAdapter;

import java.util.Collections;
import java.util.List;

public class ClassAssignmentsTabFragment extends Fragment {

    private FragmentClassTabListBinding binding;
    private AssignmentAdapter adapter;
    private ClassDetailViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentClassTabListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupListeners();
        setupViewModel();
    }

    private void setupRecyclerView() {
        adapter = new AssignmentAdapter(item -> {
            if (getContext() != null && item != null) {
                Toast.makeText(requireContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        binding.rvTabItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTabItems.setAdapter(adapter);
        binding.tvEmptyMessage.setText(R.string.empty_assignments);
    }

    private void setupListeners() {
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (viewModel != null && getContext() != null) {
                viewModel.refresh(requireContext());
            } else {
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireParentFragment()).get(ClassDetailViewModel.class);
        viewModel.getClassDetail().observe(getViewLifecycleOwner(), detail -> {
            if (detail != null) {
                List<ClassDetailResponse.ClassAssignmentItem> list = detail.getAssignments();
                String className = detail.getName() != null ? detail.getName() : "";
                List<AssignmentItem> items = AssignmentAdapter.fromClassAssignments(list, className);
                adapter.submitList(items);
                binding.tvEmptyMessage.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                adapter.submitList(Collections.emptyList());
                binding.tvEmptyMessage.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading != null) {
                binding.swipeRefresh.setRefreshing(loading);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
