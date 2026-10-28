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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ppquang06.uniclassapp.LoginActivity;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerProfileBinding;
import com.ppquang06.uniclassapp.ui.common.profile.ChangePasswordBottomSheetDialogFragment;
import com.ppquang06.uniclassapp.ui.common.profile.EditProfileBottomSheetDialogFragment;
import com.ppquang06.uniclassapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        bindUserData(null);
        setupActions();
        loadLiveUserData();
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.nav_profile);
        binding.topAppBar.tvRoleBadge.setText(R.string.role_badge_lecturer);
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
        if (getContext() == null) return;

        ApiClient.getUserClassApiService(requireContext())
                .getCurrentUser()
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<UserResponse>> call,
                                           @NonNull Response<ApiResponse<UserResponse>> response) {
                        if (!isAdded() || binding == null) return;

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            UserResponse liveUser = response.body().getData();
                            SessionManager.getInstance(requireContext()).saveUser(liveUser);
                            bindUserData(liveUser);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Throwable t) {
                        // Silent fallback, cached session user is already shown
                    }
                });
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
