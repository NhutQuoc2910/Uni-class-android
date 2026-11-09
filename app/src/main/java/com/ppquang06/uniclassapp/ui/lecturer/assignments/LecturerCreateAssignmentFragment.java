package com.ppquang06.uniclassapp.ui.lecturer.assignments;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.request.CreateAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.response.FileUploadResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerCreateAssignmentBinding;
import com.ppquang06.uniclassapp.ui.common.UiState;
import com.ppquang06.uniclassapp.ui.student.classes.PickFileTypeBottomSheetDialogFragment;
import com.ppquang06.uniclassapp.utils.UriFileMultipartHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class LecturerCreateAssignmentFragment extends Fragment {

    private FragmentLecturerCreateAssignmentBinding binding;
    private GradingViewModel viewModel;

    private String classId = "";
    private String classCode = "";
    private String className = "";
    private String lastPickedFileName = "";
    private boolean isUploadingAttachment = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerCreateAssignmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(GradingViewModel.class);

        if (getArguments() != null) {
            classId = getArguments().getString("classId", "");
            classCode = getArguments().getString("classCode", "");
            className = getArguments().getString("className", "");
        }

        setupTopBar();
        setupTargetClassHeader();
        setupDateTimePickers();
        setupSubmitButton();
        observeViewModel();
    }

    private void setupTargetClassHeader() {
        if (!TextUtils.isEmpty(className)) {
            binding.tvTargetClassName.setText(className);
        } else {
            binding.tvTargetClassName.setText(R.string.create_assignment_title);
        }
        if (!TextUtils.isEmpty(classCode)) {
            binding.tvTargetClassCode.setText("Mã lớp: " + classCode);
        } else {
            binding.tvTargetClassCode.setText("Tạo bài tập mới cho lớp");
        }

        binding.btnPickAssignmentFile.setOnClickListener(v -> openAttachmentFilePicker());
        binding.tilAttachmentUrl.setStartIconOnClickListener(v -> openAttachmentFilePicker());
    }

    private void openAttachmentFilePicker() {
        PickFileTypeBottomSheetDialogFragment sheet = PickFileTypeBottomSheetDialogFragment.newInstance();
        sheet.setOnFilePickedListener(this::uploadAttachmentFile);
        sheet.show(getChildFragmentManager(), "PickAssignmentAttachmentSheet");
    }

    private void uploadAttachmentFile(@NonNull Uri uri, @NonNull String fileName) {
        if (!isAdded() || getContext() == null || binding == null) return;
        lastPickedFileName = fileName;
        try {
            MultipartBody.Part filePart = UriFileMultipartHelper.createMultipartPart(requireContext(), uri, "file");
            RequestBody folderPart = UriFileMultipartHelper.createFolderRequestBody("assignments");
            viewModel.uploadFile(filePart, folderPart);
        } catch (Exception e) {
            isUploadingAttachment = false;
            binding.pbLoading.setVisibility(View.GONE);
            binding.btnPickAssignmentFile.setEnabled(true);
            binding.btnSubmitCreateAssignment.setEnabled(true);
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Không thể đọc tệp đính kèm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupTopBar() {
        binding.topAppBar.tvTitle.setText(R.string.create_assignment_title);
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    private void setupDateTimePickers() {
        // Default today's date formatted
        SimpleDateFormat defaultSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 7);
        binding.etDueDate.setText(defaultSdf.format(cal.getTime()));

        View.OnClickListener dateClickListener = v -> showDatePicker();
        binding.etDueDate.setOnClickListener(dateClickListener);
        binding.tilDueDate.setEndIconOnClickListener(dateClickListener);

        View.OnClickListener timeClickListener = v -> showTimePicker();
        binding.etDueTime.setOnClickListener(timeClickListener);
        binding.tilDueTime.setEndIconOnClickListener(timeClickListener);
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.label_due_date))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            utcCal.setTimeInMillis(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            binding.etDueDate.setText(sdf.format(utcCal.getTime()));
            binding.tilDueDate.setError(null);
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER_TAG");
    }

    private void showTimePicker() {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(23)
                .setMinute(59)
                .setTitleText(getString(R.string.label_due_time))
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", timePicker.getHour(), timePicker.getMinute());
            binding.etDueTime.setText(timeStr);
        });

        timePicker.show(getParentFragmentManager(), "TIME_PICKER_TAG");
    }

    private void setupSubmitButton() {
        binding.btnSubmitCreateAssignment.setOnClickListener(v -> attemptCreateAssignment());
    }

    private void observeViewModel() {
        viewModel.getUploadFileState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    isUploadingAttachment = true;
                    binding.pbLoading.setVisibility(View.VISIBLE);
                    binding.btnPickAssignmentFile.setEnabled(false);
                    binding.btnPickAssignmentFile.setText(R.string.msg_uploading_file);
                    binding.btnSubmitCreateAssignment.setEnabled(false);
                    break;
                case SUCCESS:
                    isUploadingAttachment = false;
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.btnPickAssignmentFile.setEnabled(true);
                    binding.btnSubmitCreateAssignment.setEnabled(true);
                    FileUploadResponse data = state.getData();
                    if (data != null) {
                        String fullUrl = UriFileMultipartHelper.resolveFullFileUrl(data.getFileUrl());
                        binding.etAttachmentUrl.setText(fullUrl);
                        String resolvedName = (data.getFileName() != null && !data.getFileName().trim().isEmpty())
                                ? data.getFileName() : lastPickedFileName;
                        binding.btnPickAssignmentFile.setText("Đã đính kèm: " + resolvedName);
                    }
                    viewModel.resetUploadFileState();
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), "Tải tệp đính kèm thành công", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ERROR:
                    isUploadingAttachment = false;
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.btnPickAssignmentFile.setEnabled(true);
                    binding.btnPickAssignmentFile.setText("Chọn tệp đính kèm bài tập");
                    binding.btnSubmitCreateAssignment.setEnabled(true);
                    String msg = state.getMessage() != null && !state.getMessage().trim().isEmpty()
                            ? state.getMessage() : "Tải tệp đính kèm thất bại";
                    viewModel.resetUploadFileState();
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case IDLE:
                    break;
            }
        });

        viewModel.getCreateAssignmentState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.pbLoading.setVisibility(View.VISIBLE);
                    binding.btnSubmitCreateAssignment.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.btnSubmitCreateAssignment.setEnabled(true);
                    Toast.makeText(requireContext(), R.string.toast_create_assignment_success, Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(LecturerCreateAssignmentFragment.this).popBackStack();
                    break;
                case ERROR:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.btnSubmitCreateAssignment.setEnabled(true);
                    String msg = state.getMessage() != null && !state.getMessage().trim().isEmpty()
                            ? state.getMessage() : "Tạo bài tập thất bại";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    break;
                case IDLE:
                    break;
            }
        });
    }

    private void attemptCreateAssignment() {
        if (isUploadingAttachment) {
            Toast.makeText(requireContext(), "Đang tải tệp đính kèm lên máy chủ, vui lòng đợi giây lát", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.tilAssignmentTitle.setError(null);
        binding.tilDueDate.setError(null);

        String title = binding.etAssignmentTitle.getText() != null ? binding.etAssignmentTitle.getText().toString().trim() : "";
        String description = binding.etAssignmentDescription.getText() != null ? binding.etAssignmentDescription.getText().toString().trim() : "";
        String attachmentUrl = binding.etAttachmentUrl.getText() != null ? binding.etAttachmentUrl.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(attachmentUrl)) {
            attachmentUrl = UriFileMultipartHelper.resolveFullFileUrl(attachmentUrl);
        }
        String dueDate = binding.etDueDate.getText() != null ? binding.etDueDate.getText().toString().trim() : "";
        String dueTime = binding.etDueTime.getText() != null ? binding.etDueTime.getText().toString().trim() : "23:59";
        boolean notifyStudents = binding.switchNotifyStudents.isChecked();
        boolean allowLate = binding.switchAllowLate.isChecked();

        boolean hasError = false;
        if (TextUtils.isEmpty(title)) {
            binding.tilAssignmentTitle.setError(getString(R.string.err_empty_assignment_title));
            hasError = true;
        }

        if (TextUtils.isEmpty(dueDate)) {
            binding.tilDueDate.setError(getString(R.string.err_empty_due_date));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        if (TextUtils.isEmpty(dueTime)) {
            dueTime = "23:59";
        }

        String fullDeadline = dueDate + " " + dueTime + ":00";

        CreateAssignmentRequest request = new CreateAssignmentRequest(
                title,
                description,
                fullDeadline,
                attachmentUrl,
                notifyStudents,
                allowLate
        );

        viewModel.createAssignment(classId, request);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
