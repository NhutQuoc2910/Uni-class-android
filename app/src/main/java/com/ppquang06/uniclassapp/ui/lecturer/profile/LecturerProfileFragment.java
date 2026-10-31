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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ppquang06.uniclassapp.LoginActivity;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerProfileBinding;
import com.ppquang06.uniclassapp.ui.common.profile.ChangePasswordBottomSheetDialogFragment;
import com.ppquang06.uniclassapp.ui.common.profile.EditProfileBottomSheetDialogFragment;
import com.ppquang06.uniclassapp.ui.common.profile.ProfileViewModel;
import com.ppquang06.uniclassapp.utils.SessionManager;

public class LecturerProfileFragment extends Fragment {

    private FragmentLecturerProfileBinding binding;
    private ProfileViewModel profileViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupTopBar();
        bindUserData(null);
        setupActions();
        observeViewModel();
        loadLiveUserData();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.nav_profile);
        binding.topAppBar.tvRoleBadge.setText(R.string.role_badge_lecturer);
    }

    private void observeViewModel() {
        profileViewModel.getProfileState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) {
                return;
            }
            if (state.isSuccess() && state.getData() != null) {
                UserResponse liveUser = state.getData();
                if (getContext() != null) {
                    SessionManager.getInstance(requireContext()).saveUser(liveUser);
                }
                bindUserData(liveUser);
            }
        });
    }

    private void bindUserData(@Nullable UserResponse freshUser) {
        if (getContext() == null || binding == null) return;

        UserResponse user = freshUser != null ? freshUser : SessionManager.getInstance(requireContext()).getUser();
        if (user != null) {
            String fullName = !TextUtils.isEmpty(user.getFullName()) ? user.getFullName() : getString(R.string.role_badge_lecturer);
            binding.tvFullName.setText(fullName);

            String initial = "G";
            if (!TextUtils.isEmpty(fullName)) {
                initial = fullName.trim().substring(0, 1).toUpperCase();
            }
            binding.tvAvatarInitial.setText(initial);

            String roleName = getString(R.string.role_badge_lecturer);
            String emailOrId = !TextUtils.isEmpty(user.getEmail()) ? user.getEmail()
                    : (!TextUtils.isEmpty(user.getLecturerId()) ? user.getLecturerId() : "");
            String subtitle = emailOrId.isEmpty() ? roleName : roleName + " · " + emailOrId;
            binding.tvUserSubtitle.setText(subtitle);

            binding.tvRoleBadge.setText(R.string.role_badge_lecturer);

            binding.tvContactFullName.setText(!TextUtils.isEmpty(user.getFullName())
                    ? user.getFullName() : getString(R.string.profile_not_set));
            binding.tvContactEmail.setText(!TextUtils.isEmpty(user.getEmail())
                    ? user.getEmail() : getString(R.string.profile_not_set));
            binding.tvContactPhone.setText(!TextUtils.isEmpty(user.getPhone())
                    ? user.getPhone() : getString(R.string.profile_not_set));
        }
    }

    private void loadLiveUserData() {
        if (profileViewModel != null) {
            profileViewModel.loadProfile();
        }
    }

    private void setupActions() {
        binding.rowEditProfile.setOnClickListener(v -> openEditProfile());
        binding.rowChangePassword.setOnClickListener(v -> openChangePassword());
        binding.rowLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void openEditProfile() {
        EditProfileBottomSheetDialogFragment sheet = EditProfileBottomSheetDialogFragment.newInstance();
        sheet.setOnProfileUpdatedListener(this::bindUserData);
        sheet.show(getChildFragmentManager(), "edit_profile");
    }

    private void openChangePassword() {
        ChangePasswordBottomSheetDialogFragment sheet = ChangePasswordBottomSheetDialogFragment.newInstance();
        sheet.show(getChildFragmentManager(), "change_password");
    }

    private void showLogoutDialog() {
        if (getContext() == null) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.logout_confirm_btn, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
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
