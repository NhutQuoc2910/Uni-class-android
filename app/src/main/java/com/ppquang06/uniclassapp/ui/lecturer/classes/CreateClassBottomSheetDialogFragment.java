package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.request.CreateClassRequest;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.databinding.DialogCreateClassBottomSheetBinding;
import com.ppquang06.uniclassapp.ui.common.UiState;

public class CreateClassBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnClassCreatedListener {
        void onClassCreated(CourseClassItem newClass);
    }

    private DialogCreateClassBottomSheetBinding binding;
    private OnClassCreatedListener listener;
    private LecturerManagementViewModel viewModel;

    public static CreateClassBottomSheetDialogFragment newInstance() {
        return new CreateClassBottomSheetDialogFragment();
    }

    public void setOnClassCreatedListener(OnClassCreatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogCreateClassBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LecturerManagementViewModel.class);

        binding.btnCancelCreate.setOnClickListener(v -> dismiss());
        binding.btnSubmitCreate.setOnClickListener(v -> submitCreate());

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getCreateClassState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    Toast.makeText(requireContext(), R.string.create_class_success_title, Toast.LENGTH_SHORT).show();
                    CourseClassItem created = state.getData();
                    viewModel.resetCreateClassState();
                    if (listener != null) {
                        listener.onClassCreated(created);
                    }
                    dismiss();
                    break;
                case ERROR:
                    setLoading(false);
                    String msg = (state.getMessage() != null && !state.getMessage().trim().isEmpty())
                            ? state.getMessage() : getString(R.string.cannot_load_classes);
                    binding.tilClassName.setError(msg);
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    break;
                case IDLE:
                    setLoading(false);
                    break;
            }
        });
    }

    private void submitCreate() {
        if (getContext() == null || binding == null) return;

        clearErrors();

        String name = binding.etClassName.getText() != null ? binding.etClassName.getText().toString().trim() : "";
        String code = binding.etClassCode.getText() != null ? binding.etClassCode.getText().toString().trim() : "";
        String semester = binding.etSemester.getText() != null ? binding.etSemester.getText().toString().trim() : "";
        String room = binding.etRoom.getText() != null ? binding.etRoom.getText().toString().trim() : "";

        boolean hasError = false;
        if (TextUtils.isEmpty(name)) {
            binding.tilClassName.setError(getString(R.string.err_required_field));
            hasError = true;
        }
        if (TextUtils.isEmpty(code)) {
            binding.tilClassCode.setError(getString(R.string.err_required_field));
            hasError = true;
        }
        if (TextUtils.isEmpty(semester)) {
            binding.tilSemester.setError(getString(R.string.err_required_field));
            hasError = true;
        }

        if (hasError) return;

        CreateClassRequest request = new CreateClassRequest(name, code, semester, room);
        viewModel.createClass(request);
    }

    private void clearErrors() {
        if (binding == null) return;
        binding.tilClassName.setError(null);
        binding.tilClassCode.setError(null);
        binding.tilSemester.setError(null);
        binding.tilRoom.setError(null);
    }

    private void setLoading(boolean loading) {
        if (binding == null) return;
        binding.pbCreateLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSubmitCreate.setEnabled(!loading);
        binding.btnCancelCreate.setEnabled(!loading);
        binding.etClassName.setEnabled(!loading);
        binding.etClassCode.setEnabled(!loading);
        binding.etSemester.setEnabled(!loading);
        binding.etRoom.setEnabled(!loading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
