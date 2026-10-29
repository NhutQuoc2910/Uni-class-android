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
import androidx.navigation.Navigation;

import com.google.android.material.tabs.TabLayoutMediator;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.databinding.FragmentStudentClassDetailBinding;

public class StudentClassDetailFragment extends Fragment {

    private FragmentStudentClassDetailBinding binding;
    private ClassDetailViewModel viewModel;

    private String classId = "";
    private String classCode = "";
    private String className = "";
    private String semester = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentClassDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readArguments();
        setupTopAppBar();
        setupHeaderInitial();
        setupViewPager();
        setupViewModel();
    }

    private void readArguments() {
        if (getArguments() != null) {
            classId = getArguments().getString("classId", "");
            classCode = getArguments().getString("classCode", "");
            className = getArguments().getString("className", "");
            semester = getArguments().getString("semester", "");
        }
    }

    private void setupTopAppBar() {
        binding.topAppBar.tvTitle.setText(R.string.class_detail_title);
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> {
            try {
                Navigation.findNavController(v).navigateUp();
            } catch (Exception ignored) {}
        });
    }

    private void setupHeaderInitial() {
        if (className != null && !className.isEmpty()) {
            binding.tvClassName.setText(className);
        } else {
            binding.tvClassName.setText(R.string.class_detail_title);
        }

        StringBuilder meta = new StringBuilder();
        if (classCode != null && !classCode.isEmpty()) {
            meta.append(classCode);
        }
        if (semester != null && !semester.isEmpty()) {
            if (meta.length() > 0) meta.append(" · ");
            meta.append(semester);
        }
        binding.tvClassMeta.setText(meta.toString());

        binding.btnViewGrades.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            String cid = classId;
            String ccode = classCode;
            String cname = className;
            if (viewModel != null && viewModel.getClassDetail().getValue() != null) {
                ClassDetailResponse d = viewModel.getClassDetail().getValue();
                if (d.getId() != null && !d.getId().isEmpty()) cid = d.getId();
                if (d.getCode() != null && !d.getCode().isEmpty()) ccode = d.getCode();
                if (d.getName() != null && !d.getName().isEmpty()) cname = d.getName();
            }
            bundle.putString("classId", cid);
            bundle.putString("classCode", ccode);
            bundle.putString("className", cname);
            try {
                Navigation.findNavController(v).navigate(R.id.nav_student_grades, bundle);
            } catch (Exception ignored) {}
        });
    }

    private void setupViewPager() {
        ClassDetailPagerAdapter pagerAdapter = new ClassDetailPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case ClassDetailPagerAdapter.TAB_ANNOUNCEMENTS:
                    tab.setText(R.string.tab_announcements);
                    break;
                case ClassDetailPagerAdapter.TAB_MATERIALS:
                    tab.setText(R.string.tab_materials);
                    break;
                case ClassDetailPagerAdapter.TAB_ASSIGNMENTS:
                    tab.setText(R.string.tab_assignments);
                    break;
                case ClassDetailPagerAdapter.TAB_ATTENDANCE:
                    tab.setText(R.string.tab_attendance);
                    break;
            }
        }).attach();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ClassDetailViewModel.class);

        viewModel.getClassDetail().observe(getViewLifecycleOwner(), this::bindHeader);

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (binding != null && loading != null) {
                binding.pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty() && getContext() != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        if (getContext() != null) {
            viewModel.loadClassDetail(requireContext(), classId, classCode, className, semester);
        }
    }

    private void bindHeader(@Nullable ClassDetailResponse detail) {
        if (detail == null || binding == null) return;

        if (detail.getName() != null && !detail.getName().isEmpty()) {
            binding.tvClassName.setText(detail.getName());
        }

        StringBuilder meta = new StringBuilder();
        if (detail.getCode() != null && !detail.getCode().isEmpty()) {
            meta.append(detail.getCode());
        }
        if (detail.getRoom() != null && !detail.getRoom().isEmpty()) {
            if (meta.length() > 0) meta.append(" · ");
            String room = detail.getRoom().trim();
            if (!room.startsWith("Phòng")) {
                room = "Phòng " + room;
            }
            meta.append(room);
        }
        if (detail.getLecturerName() != null && !detail.getLecturerName().isEmpty()) {
            if (meta.length() > 0) meta.append(" · ");
            meta.append(detail.getLecturerName());
        }
        if (meta.length() == 0 && detail.getSemester() != null && !detail.getSemester().isEmpty()) {
            meta.append(detail.getSemester());
        }

        binding.tvClassMeta.setText(meta.toString());
    }

    public void reload() {
        if (viewModel != null && getContext() != null) {
            viewModel.loadClassDetail(requireContext(), classId, classCode, className, semester);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
