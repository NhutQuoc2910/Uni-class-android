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
import com.ppquang06.uniclassapp.data.model.request.ChangePasswordRequest;
import com.ppquang06.uniclassapp.databinding.DialogChangePasswordBottomSheetBinding;

public class ChangePasswordBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private DialogChangePasswordBottomSheetBinding binding;
    private ProfileViewModel profileViewModel;

    public static ChangePasswordBottomSheetDialogFragment newInstance() {
        return new ChangePasswordBottomSheetDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogChangePasswordBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnSubmitChangePassword.setOnClickListener(v -> submitChangePassword());

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearErrors();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etCurrentPassword.addTextChangedListener(textWatcher);
        binding.etNewPassword.addTextChangedListener(textWatcher);
        binding.etConfirmPassword.addTextChangedListener(textWatcher);
    }

    private void observeViewModel() {
        profileViewModel.getChangePasswordState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || state.isIdle() || binding == null) {
                return;
            }
            if (state.isLoading()) {
                setLoading(true);
            } else if (state.isSuccess()) {
                setLoading(false);
                profileViewModel.resetChangePasswordState();
                if (getContext() != null) {
                    Toast.makeText(requireContext(), R.string.change_pass_success, Toast.LENGTH_SHORT).show();
                }
                dismiss();
            } else if (state.isError()) {
                setLoading(false);
                String errorMessage = state.getMessage() != null && !state.getMessage().trim().isEmpty()
                        ? state.getMessage()
                        : getString(R.string.change_pass_error_generic);
                profileViewModel.resetChangePasswordState();

                if (errorMessage.contains("hiện tại")
                        || errorMessage.toLowerCase().contains("current")
                        || errorMessage.contains("40006")) {
                    binding.tilCurrentPassword.setError(errorMessage);
                } else {
                    binding.tilNewPassword.setError(errorMessage);
                }
            }
        });
    }

    private void clearErrors() {
        if (binding == null) return;
        binding.tilCurrentPassword.setError(null);
        binding.tilNewPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
    }

    private void submitChangePassword() {
        if (getContext() == null || binding == null) return;

        clearErrors();

        String currentPass = binding.etCurrentPassword.getText() != null
                ? binding.etCurrentPassword.getText().toString() : "";
        String newPass = binding.etNewPassword.getText() != null
                ? binding.etNewPassword.getText().toString() : "";
        String confirmPass = binding.etConfirmPassword.getText() != null
                ? binding.etConfirmPassword.getText().toString() : "";

        if (TextUtils.isEmpty(currentPass)) {
            binding.tilCurrentPassword.setError(getString(R.string.change_pass_error_current_empty));
            return;
        }

        if (TextUtils.isEmpty(newPass) || newPass.length() < 6) {
            binding.tilNewPassword.setError(getString(R.string.change_pass_error_new_short));
            return;
        }

        if (newPass.equals(currentPass)) {
            binding.tilNewPassword.setError(getString(R.string.change_pass_error_same_as_old));
            return;
        }

        if (!newPass.equals(confirmPass)) {
            binding.tilConfirmPassword.setError(getString(R.string.change_pass_error_confirm_mismatch));
            return;
        }

        ChangePasswordRequest request = new ChangePasswordRequest(currentPass, newPass);
        profileViewModel.changePassword(request);
    }

    private void setLoading(boolean loading) {
        if (binding == null) return;
        binding.pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSubmitChangePassword.setEnabled(!loading);
        binding.btnCancel.setEnabled(!loading);
        binding.etCurrentPassword.setEnabled(!loading);
        binding.etNewPassword.setEnabled(!loading);
        binding.etConfirmPassword.setEnabled(!loading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
