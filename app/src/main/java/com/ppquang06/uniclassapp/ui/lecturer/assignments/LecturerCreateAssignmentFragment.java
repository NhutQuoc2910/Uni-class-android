package com.ppquang06.uniclassapp.ui.lecturer.assignments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.CreateAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.databinding.FragmentLecturerCreateAssignmentBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerCreateAssignmentFragment extends Fragment {

    private FragmentLecturerCreateAssignmentBinding binding;
    private String classId = "";
    private String classCode = "";
    private String className = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLecturerCreateAssignmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            classId = getArguments().getString("classId", "");
            classCode = getArguments().getString("classCode", "");
            className = getArguments().getString("className", "");
        }

        setupTopBar();
        setupDateTimePickers();
        setupSubmitButton();
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

    private void attemptCreateAssignment() {
        binding.tilAssignmentTitle.setError(null);
        binding.tilDueDate.setError(null);

        String title = binding.etAssignmentTitle.getText() != null ? binding.etAssignmentTitle.getText().toString().trim() : "";
        String description = binding.etAssignmentDescription.getText() != null ? binding.etAssignmentDescription.getText().toString().trim() : "";
        String attachmentUrl = binding.etAttachmentUrl.getText() != null ? binding.etAttachmentUrl.getText().toString().trim() : "";
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

        binding.pbLoading.setVisibility(View.VISIBLE);
        binding.btnSubmitCreateAssignment.setEnabled(false);

        ApiClient.getUserClassApiService(requireContext())
                .createAssignment(classId, request)
                .enqueue(new Callback<ApiResponse<ClassDetailResponse.ClassAssignmentItem>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<ClassDetailResponse.ClassAssignmentItem>> call,
                                           @NonNull Response<ApiResponse<ClassDetailResponse.ClassAssignmentItem>> response) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoading.setVisibility(View.GONE);
                        binding.btnSubmitCreateAssignment.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(requireContext(), R.string.toast_create_assignment_success, Toast.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(LecturerCreateAssignmentFragment.this).popBackStack();
                        } else {
                            String msg = response.body() != null && response.body().getMessage() != null ? response.body().getMessage() : "Tạo bài tập thất bại";
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<ClassDetailResponse.ClassAssignmentItem>> call, @NonNull Throwable t) {
                        if (!isAdded() || binding == null) return;
                        binding.pbLoading.setVisibility(View.GONE);
                        binding.btnSubmitCreateAssignment.setEnabled(true);
                        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
