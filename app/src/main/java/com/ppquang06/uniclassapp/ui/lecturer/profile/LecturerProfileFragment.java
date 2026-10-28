package com.ppquang06.uniclassapp.ui.lecturer.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ppquang06.uniclassapp.LoginActivity;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerProfileBinding;
import com.ppquang06.uniclassapp.utils.SessionManager;

public class LecturerProfileFragment extends Fragment {

    private FragmentLecturerProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTopBar();
        bindUserData();
        setupListeners();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.nav_profile);
        binding.topAppBar.tvRoleBadge.setText(R.string.role_badge_lecturer);
    }

    private void bindUserData() {
        if (getContext() == null) return;
        UserResponse user = SessionManager.getInstance(requireContext()).getUser();
        if (user != null) {
            binding.tvFullName.setText(!TextUtils.isEmpty(user.getFullName()) ? user.getFullName() : "Giảng viên");
            binding.tvEmail.setText(!TextUtils.isEmpty(user.getEmail()) ? user.getEmail() : "N/A");
            binding.tvRoleBadge.setText(R.string.role_badge_lecturer);
        }
    }

    private void setupListeners() {
        binding.btnLogout.setOnClickListener(v -> performLogout());
    }

    private void performLogout() {
        if (getContext() == null) return;
        SessionManager.getInstance(requireContext()).clearSession();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
