package com.ppquang06.uniclassapp.ui.common.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.request.UpdateProfileRequest;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.databinding.DialogEditProfileBottomSheetBinding;
import com.ppquang06.uniclassapp.utils.SessionManager;

public class EditProfileBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnProfileUpdatedListener {
        void onProfileUpdated(UserResponse updatedUser);
    }

    private DialogEditProfileBottomSheetBinding binding;
    private ProfileViewModel profileViewModel;
    private OnProfileUpdatedListener listener;
    private String pendingFullName = "";
    private String pendingPhone = "";

    public static EditProfileBottomSheetDialogFragment newInstance() {
        return new EditProfileBottomSheetDialogFragment();
    }

    public void setOnProfileUpdatedListener(OnProfileUpdatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogEditProfileBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        populateCurrentData();
        setupListeners();
        observeViewModel();
    }

    private void populateCurrentData() {
        if (getContext() == null) return;
        UserResponse user = SessionManager.getInstance(requireContext()).getUser();
        if (user != null) {
            if (!TextUtils.isEmpty(user.getFullName())) {
                binding.etFullName.setText(user.getFullName());
                binding.etFullName.setSelection(user.getFullName().length());
            }
            if (!TextUtils.isEmpty(user.getPhone())) {
                binding.etPhone.setText(user.getPhone());
            }
        }
    }

    private void setupListeners() {
        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());

        binding.etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilFullName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeViewModel() {
        profileViewModel.getUpdateProfileState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || state.isIdle() || binding == null) {
                return;
            }
            if (state.isLoading()) {
                setLoading(true);
            } else if (state.isSuccess()) {
                setLoading(false);
                UserResponse updatedUser = state.getData();
                profileViewModel.resetUpdateProfileState();

                if (getContext() != null) {
                    if (updatedUser == null) {
                        updatedUser = SessionManager.getInstance(requireContext()).getUser();
                        if (updatedUser == null) {
                            updatedUser = new UserResponse();
                        }
                        updatedUser.setFullName(pendingFullName);
                        updatedUser.setPhone(pendingPhone);
                    } else {
                        if (TextUtils.isEmpty(updatedUser.getFullName())) {
                            updatedUser.setFullName(pendingFullName);
                        }
                        if (TextUtils.isEmpty(updatedUser.getPhone())) {
                            updatedUser.setPhone(pendingPhone);
                        }
                    }

                    SessionManager.getInstance(requireContext()).saveUser(updatedUser);

                    if (listener != null) {
                        listener.onProfileUpdated(updatedUser);
                    }

                    Toast.makeText(requireContext(), R.string.edit_profile_success, Toast.LENGTH_SHORT).show();
                }
                dismiss();
            } else if (state.isError()) {
                setLoading(false);
                String errorMsg = state.getMessage() != null && !state.getMessage().trim().isEmpty()
                        ? state.getMessage()
                        : getString(R.string.edit_profile_error_generic);
                profileViewModel.resetUpdateProfileState();
                binding.tilFullName.setError(errorMsg);
            }
        });
    }

    private void saveProfile() {
        if (getContext() == null || binding == null) return;

        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";

        if (TextUtils.isEmpty(fullName)) {
            binding.tilFullName.setError(getString(R.string.edit_profile_error_name_empty));
            return;
        }

        pendingFullName = fullName;
        pendingPhone = phone;

        UpdateProfileRequest request = new UpdateProfileRequest(fullName, phone);
        profileViewModel.updateProfile(request);
    }

    private void setLoading(boolean loading) {
        if (binding == null) return;
        binding.pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSaveProfile.setEnabled(!loading);
        binding.btnCancel.setEnabled(!loading);
        binding.etFullName.setEnabled(!loading);
        binding.etPhone.setEnabled(!loading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
