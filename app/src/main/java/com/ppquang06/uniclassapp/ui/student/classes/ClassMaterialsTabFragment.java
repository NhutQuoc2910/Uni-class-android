package com.ppquang06.uniclassapp.ui.student.classes;

import android.content.Intent;
import android.net.Uri;
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
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.databinding.FragmentClassMaterialsTabBinding;
import com.ppquang06.uniclassapp.ui.common.adapter.MaterialAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ClassMaterialsTabFragment extends Fragment {

    public enum MaterialFilter {
        ALL,
        PDF,
        WORD,
        POWERPOINT
    }

    private FragmentClassMaterialsTabBinding binding;
    private MaterialAdapter adapter;
    private ClassDetailViewModel viewModel;
    private final List<ClassDetailResponse.MaterialItem> masterMaterials = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentClassMaterialsTabBinding.inflate(inflater, container, false);
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
        adapter = new MaterialAdapter(item -> {
            if (item == null || item.getFileUrl() == null || item.getFileUrl().trim().isEmpty()) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Tệp không có đường dẫn hợp lệ", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getFileUrl().trim()));
                startActivity(intent);
            } catch (Exception e) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Không thể mở tệp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.rvTabItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTabItems.setAdapter(adapter);
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

        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> applyCurrentFilter());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireParentFragment()).get(ClassDetailViewModel.class);
        viewModel.getClassDetail().observe(getViewLifecycleOwner(), detail -> {
            masterMaterials.clear();
            if (detail != null && detail.getMaterials() != null) {
                masterMaterials.addAll(detail.getMaterials());
            }
            applyCurrentFilter();
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading != null) {
                binding.swipeRefresh.setRefreshing(loading);
            }
        });
    }

    private void applyCurrentFilter() {
        if (binding == null) return;

        int checkedId = binding.chipGroupFilter.getCheckedChipId();
        MaterialFilter filter = MaterialFilter.ALL;
        if (checkedId == R.id.chipPdf) {
            filter = MaterialFilter.PDF;
        } else if (checkedId == R.id.chipWord) {
            filter = MaterialFilter.WORD;
        } else if (checkedId == R.id.chipPowerPoint) {
            filter = MaterialFilter.POWERPOINT;
        }

        List<ClassDetailResponse.MaterialItem> filtered = filterMaterials(masterMaterials, filter);
        adapter.submitList(filtered);

        if (masterMaterials.isEmpty()) {
            binding.tvEmptyMessage.setText(R.string.empty_materials);
            binding.tvEmptyMessage.setVisibility(View.VISIBLE);
        } else if (filtered.isEmpty()) {
            binding.tvEmptyMessage.setText(R.string.empty_materials_filtered);
            binding.tvEmptyMessage.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptyMessage.setVisibility(View.GONE);
        }
    }

    public static List<ClassDetailResponse.MaterialItem> filterMaterials(List<ClassDetailResponse.MaterialItem> items, MaterialFilter filter) {
        if (items == null) {
            return Collections.emptyList();
        }
        if (filter == null || filter == MaterialFilter.ALL) {
            return new ArrayList<>(items);
        }
        List<ClassDetailResponse.MaterialItem> result = new ArrayList<>();
        for (ClassDetailResponse.MaterialItem item : items) {
            if (item == null || item.getFileType() == null) {
                continue;
            }
            String lowerType = item.getFileType().trim().toLowerCase(Locale.ROOT);
            switch (filter) {
                case PDF:
                    if (lowerType.contains("pdf")) {
                        result.add(item);
                    }
                    break;
                case WORD:
                    if (lowerType.contains("word") || lowerType.contains("doc")) {
                        result.add(item);
                    }
                    break;
                case POWERPOINT:
                    if (lowerType.contains("ppt") || lowerType.contains("powerpoint") || lowerType.contains("slide")) {
                        result.add(item);
                    }
                    break;
            }
        }
        return result;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
