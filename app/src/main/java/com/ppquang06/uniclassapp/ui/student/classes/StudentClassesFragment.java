package com.ppquang06.uniclassapp.ui.student.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.databinding.FragmentStudentClassesBinding;

public class StudentClassesFragment extends Fragment {

    private FragmentStudentClassesBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentClassesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.topAppBar.tvTitle.setText(R.string.nav_classes);
        binding.topAppBar.tvRoleBadge.setText(R.string.role_badge_student);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
