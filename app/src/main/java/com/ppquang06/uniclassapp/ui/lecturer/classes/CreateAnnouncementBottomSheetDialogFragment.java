package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.request.CreateMaterialRequest;
import com.ppquang06.uniclassapp.databinding.DialogCreateAnnouncementBottomSheetBinding;
import com.ppquang06.uniclassapp.ui.common.UiState;

public class CreateAnnouncementBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnAnnouncementCreatedListener {
        void onAnnouncementCreated();
    }

    private static final String ARG_CLASS_ID = "classId";

    private DialogCreateAnnouncementBottomSheetBinding binding;
    private LecturerManagementViewModel viewModel;
    private String classId = "";
    private OnAnnouncementCreatedListener listener;

    public static CreateAnnouncementBottomSheetDialogFragment newInstance(String classId) {
        CreateAnnouncementBottomSheetDialogFragment fragment = new CreateAnnouncementBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLASS_ID, classId);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnAnnouncementCreatedListener(OnAnnouncementCreatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogCreateAnnouncementBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LecturerManagementViewModel.class);

        if (getArguments() != null) {
            classId = getArguments().getString(ARG_CLASS_ID, "");
        }

        binding.btnSubmitAnnouncement.setOnClickListener(v -> submitAnnouncement());
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getCreateMaterialState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.pbSubmitting.setVisibility(View.VISIBLE);
                    binding.btnSubmitAnnouncement.setEnabled(false);
                    break;
                case SUCCESS:
                    handleComplete();
                    break;
                case ERROR:
                    binding.pbSubmitting.setVisibility(View.GONE);
                    binding.btnSubmitAnnouncement.setEnabled(true);
                    String msg = state.getMessage() != null && !state.getMessage().trim().isEmpty()
                            ? state.getMessage() : getString(R.string.network_error);
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    break;
                case IDLE:
                    break;
            }
        });
    }

    private void submitAnnouncement() {
        String title = binding.etAnnouncementTitle.getText() != null ? binding.etAnnouncementTitle.getText().toString().trim() : "";
        String content = binding.etAnnouncementContent.getText() != null ? binding.etAnnouncementContent.getText().toString().trim() : "";
        boolean isUrgent = binding.switchUrgent.isChecked();

        boolean hasError = false;
        if (title.isEmpty()) {
            binding.tilAnnouncementTitle.setError(getString(R.string.err_required_field));
            hasError = true;
        } else {
            binding.tilAnnouncementTitle.setError(null);
        }

        if (content.isEmpty()) {
            binding.tilAnnouncementContent.setError(getString(R.string.err_required_field));
            hasError = true;
        } else {
            binding.tilAnnouncementContent.setError(null);
        }

        if (hasError || getContext() == null) return;

        CreateMaterialRequest request = new CreateMaterialRequest(title, content, "ANNOUNCEMENT", isUrgent);
        viewModel.createMaterial(classId, request);
    }

    private void handleComplete() {
        if (!isAdded()) return;
        if (getContext() != null) {
            Toast.makeText(requireContext(), "Đã đăng thông báo thành công!", Toast.LENGTH_SHORT).show();
        }
        if (listener != null) {
            listener.onAnnouncementCreated();
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
