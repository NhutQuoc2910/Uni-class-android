package com.ppquang06.uniclassapp.ui.lecturer.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ppquang06.uniclassapp.LoginActivity;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.request.UpdateProfileRequest;
import com.ppquang06.uniclassapp.data.model.response.FileUploadResponse;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerProfileBinding;
import com.ppquang06.uniclassapp.ui.common.profile.ChangePasswordBottomSheetDialogFragment;
import com.ppquang06.uniclassapp.ui.common.profile.EditProfileBottomSheetDialogFragment;
import com.ppquang06.uniclassapp.ui.common.profile.ProfileViewModel;
import com.ppquang06.uniclassapp.utils.SessionManager;
import com.ppquang06.uniclassapp.utils.UriFileMultipartHelper;

import okhttp3.MultipartBody;

public class LecturerProfileFragment extends Fragment {

    private FragmentLecturerProfileBinding binding;
    private ProfileViewModel profileViewModel;

    private final ActivityResultLauncher<String> pickAvatarLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && getContext() != null) {
                    uploadAvatarUri(uri);
                }
            });

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

        profileViewModel.getUploadAvatarState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || state.isIdle() || binding == null) {
                return;
            }
            if (state.isLoading()) {
                binding.pbAvatarLoading.setVisibility(View.VISIBLE);
            } else if (state.isSuccess()) {
                FileUploadResponse res = state.getData();
                profileViewModel.resetUploadAvatarState();
                if (res != null && !TextUtils.isEmpty(res.getFileUrl()) && getContext() != null) {
                    UserResponse current = SessionManager.getInstance(requireContext()).getUser();
                    String fullName = current != null ? current.getFullName() : "";
                    String phone = current != null ? current.getPhone() : "";
                    UpdateProfileRequest req = new UpdateProfileRequest(fullName, phone, res.getFileUrl(), null);
                    profileViewModel.updateProfile(req);
                } else {
                    binding.pbAvatarLoading.setVisibility(View.GONE);
                    bindUserData(null);
                }
            } else if (state.isError()) {
                binding.pbAvatarLoading.setVisibility(View.GONE);
                profileViewModel.resetUploadAvatarState();
                String err = state.getMessage() != null ? state.getMessage() : "Tải lên ảnh thất bại";
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
                bindUserData(null);
            }
        });

        profileViewModel.getUpdateProfileState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || state.isIdle() || binding == null) {
                return;
            }
            if (state.isSuccess()) {
                binding.pbAvatarLoading.setVisibility(View.GONE);
                UserResponse updated = state.getData();
                profileViewModel.resetUpdateProfileState();
                if (updated != null && getContext() != null) {
                    SessionManager.getInstance(requireContext()).saveUser(updated);
                    bindUserData(updated);
                    Toast.makeText(requireContext(), "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                }
            } else if (state.isError()) {
                binding.pbAvatarLoading.setVisibility(View.GONE);
                profileViewModel.resetUpdateProfileState();
                String err = state.getMessage() != null ? state.getMessage() : "Cập nhật ảnh thất bại";
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
                bindUserData(null);
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

            if (!TextUtils.isEmpty(user.getAvatarUrl())) {
                binding.ivAvatarProfile.setVisibility(View.VISIBLE);
                binding.tvAvatarInitial.setVisibility(View.GONE);
                Glide.with(this)
                        .load(user.getAvatarUrl())
                        .circleCrop()
                        .placeholder(R.drawable.bg_circle_aura)
                        .error(R.drawable.bg_circle_aura)
                        .into(binding.ivAvatarProfile);

                Glide.with(this)
                        .load(user.getAvatarUrl())
                        .circleCrop()
                        .into(binding.topAppBar.ivAvatar);
            } else {
                binding.ivAvatarProfile.setVisibility(View.GONE);
                binding.tvAvatarInitial.setVisibility(View.VISIBLE);
                binding.tvAvatarInitial.setText(initial);
            }

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
        binding.layoutAvatarContainer.setOnClickListener(v -> pickAvatarLauncher.launch("image/*"));
        binding.rowEditProfile.setOnClickListener(v -> openEditProfile());
        binding.rowChangePassword.setOnClickListener(v -> openChangePassword());
        binding.rowLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void uploadAvatarUri(Uri uri) {
        if (getContext() == null) return;
        try {
            MultipartBody.Part filePart = UriFileMultipartHelper.createMultipartPart(requireContext(), uri, "file");
            binding.pbAvatarLoading.setVisibility(View.VISIBLE);
            binding.tvAvatarInitial.setVisibility(View.GONE);
            profileViewModel.uploadAvatar(filePart);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không thể đọc file ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            binding.pbAvatarLoading.setVisibility(View.GONE);
            bindUserData(null);
        }
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
