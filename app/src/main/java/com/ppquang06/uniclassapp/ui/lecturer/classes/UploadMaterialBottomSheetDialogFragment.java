package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.net.Uri;
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
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.FileUploadResponse;
import com.ppquang06.uniclassapp.databinding.DialogUploadMaterialBottomSheetBinding;
import com.ppquang06.uniclassapp.ui.common.UiState;
import com.ppquang06.uniclassapp.ui.student.classes.PickFileTypeBottomSheetDialogFragment;
import com.ppquang06.uniclassapp.utils.UriFileMultipartHelper;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class UploadMaterialBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnMaterialUploadedListener {
        void onMaterialUploaded();
    }

    private static final String ARG_CLASS_ID = "classId";

    private DialogUploadMaterialBottomSheetBinding binding;
    private LecturerManagementViewModel viewModel;
    private String classId = "";
    private OnMaterialUploadedListener listener;
    private String lastPickedFileName = "";

    public static UploadMaterialBottomSheetDialogFragment newInstance(String classId) {
        UploadMaterialBottomSheetDialogFragment fragment = new UploadMaterialBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLASS_ID, classId);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnMaterialUploadedListener(OnMaterialUploadedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogUploadMaterialBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LecturerManagementViewModel.class);

        if (getArguments() != null) {
            classId = getArguments().getString(ARG_CLASS_ID, "");
        }

        binding.btnSubmitMaterial.setOnClickListener(v -> submitMaterial());
        binding.btnPickMaterialFile.setOnClickListener(v -> openFilePicker());
        binding.tilMaterialUrl.setEndIconOnClickListener(v -> openFilePicker());

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getUploadFileState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.pbSubmitting.setVisibility(View.VISIBLE);
                    binding.btnSubmitMaterial.setEnabled(false);
                    binding.btnPickMaterialFile.setEnabled(false);
                    binding.tvUploadedFileName.setVisibility(View.VISIBLE);
                    binding.tvUploadedFileName.setText(getString(R.string.msg_uploading_file));
                    break;
                case SUCCESS:
                    binding.pbSubmitting.setVisibility(View.GONE);
                    binding.btnSubmitMaterial.setEnabled(true);
                    binding.btnPickMaterialFile.setEnabled(true);
                    FileUploadResponse data = state.getData();
                    if (data != null) {
                        String fileUrl = data.getFileUrl();
                        binding.etMaterialUrl.setText(fileUrl);
                        binding.tilMaterialUrl.setError(null);
                        String resolvedName = data.getFileName() != null && !data.getFileName().isEmpty()
                                ? data.getFileName() : lastPickedFileName;
                        binding.tvUploadedFileName.setText("Đã tải lên: " + resolvedName);
                    }
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), "Tải tệp lên thành công", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ERROR:
                    binding.pbSubmitting.setVisibility(View.GONE);
                    binding.btnSubmitMaterial.setEnabled(true);
                    binding.btnPickMaterialFile.setEnabled(true);
                    binding.tvUploadedFileName.setVisibility(View.GONE);
                    String msg = state.getMessage() != null && !state.getMessage().trim().isEmpty()
                            ? state.getMessage() : "Tải tệp lên thất bại";
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case IDLE:
                    break;
            }
        });

        viewModel.getCreateMaterialState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.pbSubmitting.setVisibility(View.VISIBLE);
                    binding.btnSubmitMaterial.setEnabled(false);
                    break;
                case SUCCESS:
                    handleComplete();
                    break;
                case ERROR:
                    binding.pbSubmitting.setVisibility(View.GONE);
                    binding.btnSubmitMaterial.setEnabled(true);
                    String msg = state.getMessage() != null && !state.getMessage().trim().isEmpty()
                            ? state.getMessage() : getString(R.string.network_error);
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case IDLE:
                    break;
            }
        });
    }

    private void openFilePicker() {
        PickFileTypeBottomSheetDialogFragment sheet = PickFileTypeBottomSheetDialogFragment.newInstance();
        sheet.setOnFilePickedListener(this::uploadMaterialFile);
        sheet.show(getChildFragmentManager(), "PickMaterialFileTypeSheet");
    }

    private void uploadMaterialFile(@NonNull Uri uri, @NonNull String fileName) {
        if (!isAdded() || getContext() == null || binding == null) return;

        lastPickedFileName = fileName;

        if (binding.etMaterialTitle.getText() == null || binding.etMaterialTitle.getText().toString().trim().isEmpty()) {
            binding.etMaterialTitle.setText(fileName);
        }

        autoSelectChipByFileName(fileName);

        try {
            MultipartBody.Part filePart = UriFileMultipartHelper.createMultipartPart(requireContext(), uri, "file");
            RequestBody folderPart = UriFileMultipartHelper.createFolderRequestBody("materials");
            viewModel.uploadFile(filePart, folderPart);
        } catch (Exception e) {
            binding.pbSubmitting.setVisibility(View.GONE);
            binding.btnSubmitMaterial.setEnabled(true);
            binding.btnPickMaterialFile.setEnabled(true);
            binding.tvUploadedFileName.setVisibility(View.GONE);
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Không thể đọc tệp đã chọn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void autoSelectChipByFileName(@NonNull String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) {
            binding.chipPdf.setChecked(true);
        } else if (lower.endsWith(".doc") || lower.endsWith(".docx")) {
            binding.chipDocx.setChecked(true);
        } else if (lower.endsWith(".ppt") || lower.endsWith(".pptx")) {
            binding.chipPptx.setChecked(true);
        }
    }

    private void submitMaterial() {
        String title = binding.etMaterialTitle.getText() != null ? binding.etMaterialTitle.getText().toString().trim() : "";
        String url = binding.etMaterialUrl.getText() != null ? binding.etMaterialUrl.getText().toString().trim() : "";

        boolean hasError = false;
        if (title.isEmpty()) {
            binding.tilMaterialTitle.setError(getString(R.string.err_required_field));
            hasError = true;
        } else {
            binding.tilMaterialTitle.setError(null);
        }

        if (url.isEmpty()) {
            binding.tilMaterialUrl.setError(getString(R.string.err_required_field));
            hasError = true;
        } else {
            binding.tilMaterialUrl.setError(null);
        }

        if (hasError || getContext() == null) return;

        String fileType = "PDF";
        int checkedId = binding.chipGroupFileType.getCheckedChipId();
        if (checkedId == R.id.chipDocx) {
            fileType = "DOCX";
        } else if (checkedId == R.id.chipPptx) {
            fileType = "PPTX";
        } else if (checkedId == R.id.chipLink) {
            fileType = "LINK";
        }

        CreateMaterialRequest request = new CreateMaterialRequest(title, url, fileType, false);
        viewModel.createMaterial(classId, request);
    }

    private void handleComplete() {
        if (!isAdded()) return;
        if (getContext() != null) {
            Toast.makeText(requireContext(), "Đã tải tài liệu lên thành công!", Toast.LENGTH_SHORT).show();
        }
        if (listener != null) {
            listener.onMaterialUploaded();
        }
        dismissAllowingStateLoss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
