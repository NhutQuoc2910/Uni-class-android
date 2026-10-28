package com.ppquang06.uniclassapp.ui.student.classes;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.JoinClassRequest;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.databinding.DialogJoinClassBottomSheetBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinClassBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnClassJoinedListener {
        void onClassJoined(CourseClassItem newClass);
    }

    private DialogJoinClassBottomSheetBinding binding;
    private OnClassJoinedListener listener;

    public static JoinClassBottomSheetDialogFragment newInstance() {
        return new JoinClassBottomSheetDialogFragment();
    }

    public void setOnClassJoinedListener(OnClassJoinedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogJoinClassBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnCancelJoin.setOnClickListener(v -> dismiss());
        binding.btnSubmitJoin.setOnClickListener(v -> submitJoin());

        binding.etInviteCode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitJoin();
                return true;
            }
            return false;
        });
    }

    private void submitJoin() {
        if (getContext() == null || binding == null) return;

        String code = binding.etInviteCode.getText() != null
                ? binding.etInviteCode.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(code)) {
            binding.tilInviteCode.setError(getString(R.string.join_class_code_empty));
            return;
        }

        binding.tilInviteCode.setError(null);
        setLoading(true);

        String normalizedCode = code.toUpperCase();
        ApiClient.getUserClassApiService(requireContext())
                .joinClass(new JoinClassRequest(normalizedCode))
                .enqueue(new Callback<ApiResponse<CourseClassItem>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<CourseClassItem>> call,
                                           @NonNull Response<ApiResponse<CourseClassItem>> response) {
                        if (!isAdded() || binding == null) return;
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(requireContext(), R.string.join_class_success, Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onClassJoined(response.body().getData());
                            }
                            dismiss();
                        } else {
                            String errorMsg = resolveErrorMessage(response);
                            binding.tilInviteCode.setError(errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<CourseClassItem>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        setLoading(false);
                        binding.tilInviteCode.setError(getString(R.string.join_class_error_generic));
                    }
                });
    }

    private String resolveErrorMessage(Response<ApiResponse<CourseClassItem>> response) {
        String fallback = getString(R.string.join_class_error_generic);
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                ApiResponse<?> errRes = new Gson().fromJson(errorJson, ApiResponse.class);
                if (errRes != null) {
                    if (errRes.getCode() == 40008) {
                        return getString(R.string.join_class_error_already_joined);
                    } else if (errRes.getCode() == 40402) {
                        return getString(R.string.join_class_error_not_found);
                    } else if (!TextUtils.isEmpty(errRes.getMessage())) {
                        return errRes.getMessage();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private void setLoading(boolean loading) {
        if (binding == null) return;
        binding.pbJoinLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSubmitJoin.setEnabled(!loading);
        binding.btnCancelJoin.setEnabled(!loading);
        binding.etInviteCode.setEnabled(!loading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
