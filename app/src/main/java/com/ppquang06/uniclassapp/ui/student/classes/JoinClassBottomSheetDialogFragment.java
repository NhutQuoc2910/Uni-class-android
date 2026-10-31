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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.databinding.DialogJoinClassBottomSheetBinding;
import com.ppquang06.uniclassapp.ui.common.UiState;

public class JoinClassBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnClassJoinedListener {
        void onClassJoined(CourseClassItem newClass);
    }

    private DialogJoinClassBottomSheetBinding binding;
    private OnClassJoinedListener listener;
    private ClassDetailViewModel viewModel;

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

        viewModel = new ViewModelProvider(this).get(ClassDetailViewModel.class);

        binding.btnCancelJoin.setOnClickListener(v -> dismiss());
        binding.btnSubmitJoin.setOnClickListener(v -> submitJoin());

        binding.etInviteCode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitJoin();
                return true;
            }
            return false;
        });

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getJoinClassState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    Toast.makeText(requireContext(), R.string.join_class_success, Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onClassJoined(state.getData());
                    }
                    dismiss();
                    break;
                case ERROR:
                    setLoading(false);
                    String msg = state.getMessage();
                    if ("ALREADY_JOINED".equalsIgnoreCase(msg)) {
                        binding.tilInviteCode.setError(getString(R.string.join_class_error_already_joined));
                    } else if ("CLASS_NOT_FOUND".equalsIgnoreCase(msg)) {
                        binding.tilInviteCode.setError(getString(R.string.join_class_error_not_found));
                    } else if (msg != null && !msg.trim().isEmpty()) {
                        binding.tilInviteCode.setError(msg);
                    } else {
                        binding.tilInviteCode.setError(getString(R.string.join_class_error_generic));
                    }
                    break;
                case IDLE:
                    setLoading(false);
                    break;
            }
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
        String normalizedCode = code.toUpperCase();
        viewModel.joinClass(normalizedCode);
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
