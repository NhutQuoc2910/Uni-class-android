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

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.UpdateProfileRequest;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.databinding.DialogEditProfileBottomSheetBinding;
import com.ppquang06.uniclassapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnProfileUpdatedListener {
        void onProfileUpdated(UserResponse updatedUser);
    }

    private DialogEditProfileBottomSheetBinding binding;
    private OnProfileUpdatedListener listener;

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

        populateCurrentData();
        setupListeners();
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

    private void saveProfile() {
        if (getContext() == null || binding == null) return;

        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";

        if (TextUtils.isEmpty(fullName)) {
            binding.tilFullName.setError(getString(R.string.edit_profile_error_name_empty));
            return;
        }

        setLoading(true);

        UpdateProfileRequest request = new UpdateProfileRequest(fullName, phone);
        ApiClient.getUserClassApiService(requireContext())
                .updateCurrentUser(request)
                .enqueue(new Callback<ApiResponse<UserResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<UserResponse>> call,
                                           @NonNull Response<ApiResponse<UserResponse>> response) {
                        if (!isAdded() || binding == null) return;
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            UserResponse updatedUser = response.body().getData();
                            if (updatedUser == null) {
                                updatedUser = SessionManager.getInstance(requireContext()).getUser();
                                if (updatedUser == null) {
                                    updatedUser = new UserResponse();
                                }
                                updatedUser.setFullName(fullName);
                                updatedUser.setPhone(phone);
                            } else {
                                if (TextUtils.isEmpty(updatedUser.getFullName())) {
                                    updatedUser.setFullName(fullName);
                                }
                                if (TextUtils.isEmpty(updatedUser.getPhone())) {
                                    updatedUser.setPhone(phone);
                                }
                            }

                            SessionManager.getInstance(requireContext()).saveUser(updatedUser);

                            if (listener != null) {
                                listener.onProfileUpdated(updatedUser);
                            }

                            Toast.makeText(requireContext(), R.string.edit_profile_success, Toast.LENGTH_SHORT).show();
                            dismiss();
                        } else {
                            String errorMsg = resolveErrorMessage(response);
                            binding.tilFullName.setError(errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<UserResponse>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        setLoading(false);
                        binding.tilFullName.setError(getString(R.string.edit_profile_error_generic));
                    }
                });
    }

    private String resolveErrorMessage(Response<ApiResponse<UserResponse>> response) {
        String fallback = getString(R.string.edit_profile_error_generic);
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                ApiResponse<?> errRes = new Gson().fromJson(errorJson, ApiResponse.class);
                if (errRes != null && !TextUtils.isEmpty(errRes.getMessage())) {
                    return errRes.getMessage();
                }
            }
        } catch (Exception ignored) {
        }
        return fallback;
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
