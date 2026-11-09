package com.ppquang06.uniclassapp.ui.lecturer.classes;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.request.CreateScheduleExceptionRequest;
import com.ppquang06.uniclassapp.data.model.response.ClassScheduleItem;
import com.ppquang06.uniclassapp.data.model.response.ScheduleExceptionItem;
import com.ppquang06.uniclassapp.databinding.BottomSheetCreateScheduleExceptionBinding;
import com.ppquang06.uniclassapp.ui.common.BaseBottomSheetDialogFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateScheduleExceptionBottomSheetDialogFragment extends BaseBottomSheetDialogFragment {

    public interface OnExceptionSavedListener {
        void onExceptionSaved();
    }

    private static final String ARG_CLASS_ID = "classId";
    private static final String ARG_SCHEDULES = "schedules";

    private BottomSheetCreateScheduleExceptionBinding binding;
    private LecturerManagementViewModel viewModel;
    private OnExceptionSavedListener listener;

    private String classId = "";
    private final List<ClassScheduleItem> schedules = new ArrayList<>();
    private String selectedScheduleId = null;

    public static CreateScheduleExceptionBottomSheetDialogFragment newInstance(String classId, List<ClassScheduleItem> schedules) {
        CreateScheduleExceptionBottomSheetDialogFragment fragment = new CreateScheduleExceptionBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLASS_ID, classId);
        if (schedules != null) {
            args.putSerializable(ARG_SCHEDULES, new ArrayList<>(schedules));
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnExceptionSavedListener(OnExceptionSavedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetCreateScheduleExceptionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getParentFragment() != null) {
            viewModel = new ViewModelProvider(getParentFragment()).get(LecturerManagementViewModel.class);
        } else {
            viewModel = new ViewModelProvider(this).get(LecturerManagementViewModel.class);
        }

        readArguments();
        setupExceptionTypeRadioGroup();
        setupClassScheduleDropdown();
        setupPickers();
        setupSubmitButton();
        observeViewModel();
    }

    @SuppressWarnings("unchecked")
    private void readArguments() {
        Bundle args = getArguments();
        if (args != null) {
            classId = args.getString(ARG_CLASS_ID, "");
            Serializable raw = args.getSerializable(ARG_SCHEDULES);
            if (raw instanceof List) {
                schedules.clear();
                schedules.addAll((List<ClassScheduleItem>) raw);
            }
        }
    }

    private void setupExceptionTypeRadioGroup() {
        binding.rgExceptionType.setOnCheckedChangeListener((group, checkedId) -> updateFieldsVisibility(checkedId));
        updateFieldsVisibility(binding.rgExceptionType.getCheckedRadioButtonId());
    }

    private void updateFieldsVisibility(int checkedId) {
        if (checkedId == R.id.rbCancelled) {
            binding.tilOriginalDate.setVisibility(View.VISIBLE);
            binding.tilOccurrenceDate.setVisibility(View.GONE);
            binding.llTimeRoom.setVisibility(View.GONE);
        } else if (checkedId == R.id.rbModified) {
            binding.tilOriginalDate.setVisibility(View.VISIBLE);
            binding.tilOccurrenceDate.setVisibility(View.GONE);
            binding.llTimeRoom.setVisibility(View.VISIBLE);
            prefillFromSelectedSchedule();
        } else if (checkedId == R.id.rbMakeup) {
            binding.tilOriginalDate.setVisibility(View.GONE);
            binding.tilOccurrenceDate.setVisibility(View.VISIBLE);
            binding.llTimeRoom.setVisibility(View.VISIBLE);
        }
    }

    private void setupClassScheduleDropdown() {
        List<String> displayList = new ArrayList<>();
        for (ClassScheduleItem item : schedules) {
            String label = item.getDayOfWeekLabel();
            String time = item.getFormattedTimeRange();
            String room = !TextUtils.isEmpty(item.getRoom()) ? (" · Phòng " + item.getRoom()) : "";
            displayList.add(label + " (" + time + room + ")");
        }

        if (displayList.isEmpty()) {
            displayList.add("Chưa có lịch học định kỳ");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, displayList);
        binding.spClassSchedule.setAdapter(adapter);

        if (!schedules.isEmpty()) {
            binding.spClassSchedule.setText(displayList.get(0), false);
            selectedScheduleId = schedules.get(0).getId();
        }

        binding.spClassSchedule.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < schedules.size()) {
                selectedScheduleId = schedules.get(position).getId();
                if (binding.rbModified.isChecked()) {
                    prefillFromSelectedSchedule();
                }
            }
        });
    }

    private void prefillFromSelectedSchedule() {
        if (selectedScheduleId == null || schedules.isEmpty()) return;
        ClassScheduleItem found = null;
        for (ClassScheduleItem item : schedules) {
            if (selectedScheduleId.equals(item.getId())) {
                found = item;
                break;
            }
        }
        if (found != null) {
            if (TextUtils.isEmpty(binding.edtStartTime.getText())) {
                binding.edtStartTime.setText(normalizeTime(found.getStartTime()));
            }
            if (TextUtils.isEmpty(binding.edtEndTime.getText())) {
                binding.edtEndTime.setText(normalizeTime(found.getEndTime()));
            }
            if (TextUtils.isEmpty(binding.edtRoom.getText()) && !TextUtils.isEmpty(found.getRoom())) {
                binding.edtRoom.setText(found.getRoom());
            }
        }
    }

    private void setupPickers() {
        binding.edtOriginalDate.setOnClickListener(v -> showDatePicker(true));
        binding.edtOccurrenceDate.setOnClickListener(v -> showDatePicker(false));
        binding.edtStartTime.setOnClickListener(v -> showTimePicker(true));
        binding.edtEndTime.setOnClickListener(v -> showTimePicker(false));
    }

    private void showTimePicker(boolean isStart) {
        if (getContext() == null) return;
        String current = isStart
                ? (binding.edtStartTime.getText() != null ? binding.edtStartTime.getText().toString().trim() : "")
                : (binding.edtEndTime.getText() != null ? binding.edtEndTime.getText().toString().trim() : "");

        int hour = isStart ? 7 : 11;
        int minute = 30;
        if (!TextUtils.isEmpty(current) && current.contains(":")) {
            String[] parts = current.split(":");
            try {
                hour = Integer.parseInt(parts[0].trim());
                minute = Integer.parseInt(parts[1].trim());
            } catch (Exception ignored) {}
        }

        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minuteOfHour) -> {
                    String formatted = String.format(Locale.US, "%02d:%02d", hourOfDay, minuteOfHour);
                    if (isStart) {
                        binding.edtStartTime.setText(formatted);
                        binding.tilStartTime.setError(null);
                    } else {
                        binding.edtEndTime.setText(formatted);
                        binding.tilEndTime.setError(null);
                    }
                },
                hour,
                minute,
                true
        );
        dialog.show();
    }

    private void showDatePicker(boolean isOriginal) {
        if (getContext() == null) return;
        String current = isOriginal
                ? (binding.edtOriginalDate.getText() != null ? binding.edtOriginalDate.getText().toString().trim() : "")
                : (binding.edtOccurrenceDate.getText() != null ? binding.edtOccurrenceDate.getText().toString().trim() : "");

        Calendar cal = Calendar.getInstance();
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);

        if (!TextUtils.isEmpty(current) && current.length() >= 10 && current.charAt(4) == '-' && current.charAt(7) == '-') {
            try {
                y = Integer.parseInt(current.substring(0, 4));
                m = Integer.parseInt(current.substring(5, 7)) - 1;
                d = Integer.parseInt(current.substring(8, 10));
            } catch (Exception ignored) {}
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String formatted = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    if (isOriginal) {
                        binding.edtOriginalDate.setText(formatted);
                        binding.tilOriginalDate.setError(null);
                    } else {
                        binding.edtOccurrenceDate.setText(formatted);
                        binding.tilOccurrenceDate.setError(null);
                    }
                },
                y,
                m,
                d
        );
        dialog.show();
    }

    private void setupSubmitButton() {
        binding.btnSubmitException.setOnClickListener(v -> submitException());
    }

    private void submitException() {
        String exceptionType;
        String origDate = null;
        String occurDate = null;
        String startTime = null;
        String endTime = null;
        String room = null;
        String reason = binding.edtReason.getText() != null ? binding.edtReason.getText().toString().trim() : "";
        boolean hasError = false;

        if (binding.rbCancelled.isChecked()) {
            exceptionType = "CANCELLED";
            origDate = binding.edtOriginalDate.getText() != null ? binding.edtOriginalDate.getText().toString().trim() : "";
            if (TextUtils.isEmpty(origDate)) {
                binding.tilOriginalDate.setError(getString(R.string.err_required_field));
                hasError = true;
            } else {
                binding.tilOriginalDate.setError(null);
            }
            if (TextUtils.isEmpty(selectedScheduleId)) {
                binding.tilClassSchedule.setError(getString(R.string.err_required_field));
                hasError = true;
            } else {
                binding.tilClassSchedule.setError(null);
            }
        } else if (binding.rbModified.isChecked()) {
            exceptionType = "MODIFIED";
            origDate = binding.edtOriginalDate.getText() != null ? binding.edtOriginalDate.getText().toString().trim() : "";
            startTime = binding.edtStartTime.getText() != null ? binding.edtStartTime.getText().toString().trim() : "";
            endTime = binding.edtEndTime.getText() != null ? binding.edtEndTime.getText().toString().trim() : "";
            room = binding.edtRoom.getText() != null ? binding.edtRoom.getText().toString().trim() : "";

            if (TextUtils.isEmpty(origDate)) {
                binding.tilOriginalDate.setError(getString(R.string.err_required_field));
                hasError = true;
            } else {
                binding.tilOriginalDate.setError(null);
            }
            if (TextUtils.isEmpty(startTime)) {
                binding.tilStartTime.setError(getString(R.string.err_required_field));
                hasError = true;
            } else {
                binding.tilStartTime.setError(null);
            }
            if (TextUtils.isEmpty(endTime)) {
                binding.tilEndTime.setError(getString(R.string.err_required_field));
                hasError = true;
            } else {
                binding.tilEndTime.setError(null);
            }
            if (TextUtils.isEmpty(room)) {
                binding.tilRoom.setError(getString(R.string.err_required_field));
                hasError = true;
            } else {
                binding.tilRoom.setError(null);
            }
            if (TextUtils.isEmpty(selectedScheduleId)) {
                binding.tilClassSchedule.setError(getString(R.string.err_required_field));
                hasError = true;
            } else {
                binding.tilClassSchedule.setError(null);
            }
        } else {
            exceptionType = "MAKEUP";
            occurDate = binding.edtOccurrenceDate.getText() != null ? binding.edtOccurrenceDate.getText().toString().trim() : "";
            startTime = binding.edtStartTime.getText() != null ? binding.edtStartTime.getText().toString().trim() : "";
            endTime = binding.edtEndTime.getText() != null ? binding.edtEndTime.getText().toString().trim() : "";
            room = binding.edtRoom.getText() != null ? binding.edtRoom.getText().toString().trim() : "";

            if (TextUtils.isEmpty(occurDate)) {
                binding.tilOccurrenceDate.setError(getString(R.string.err_required_field));
                hasError = true;
            } else {
                binding.tilOccurrenceDate.setError(null);
            }
            if (TextUtils.isEmpty(startTime)) {
                binding.tilStartTime.setError(getString(R.string.err_required_field));
                hasError = true;
            } else {
                binding.tilStartTime.setError(null);
            }
            if (TextUtils.isEmpty(endTime)) {
                binding.tilEndTime.setError(getString(R.string.err_required_field));
                hasError = true;
            } else {
                binding.tilEndTime.setError(null);
            }
        }

        if (hasError || getContext() == null) return;

        CreateScheduleExceptionRequest request = new CreateScheduleExceptionRequest(
                selectedScheduleId,
                origDate,
                occurDate,
                exceptionType,
                startTime,
                endTime,
                room,
                reason,
                null
        );

        viewModel.createScheduleException(classId, request);
    }

    private void observeViewModel() {
        viewModel.getCreateExceptionState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.pbLoading.setVisibility(View.VISIBLE);
                    binding.btnSubmitException.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.btnSubmitException.setEnabled(true);
                    ScheduleExceptionItem created = state.getData();
                    boolean autoPublish = binding.switchAutoPublish.isChecked();
                    viewModel.resetCreateExceptionState();
                    if (autoPublish && created != null && created.getId() != null) {
                        viewModel.publishScheduleException(classId, created.getId());
                    }
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), R.string.toast_create_exception_success, Toast.LENGTH_SHORT).show();
                    }
                    if (listener != null) {
                        listener.onExceptionSaved();
                    }
                    dismiss();
                    break;
                case ERROR:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.btnSubmitException.setEnabled(true);
                    String msg = (state.getMessage() != null && !state.getMessage().trim().isEmpty())
                            ? state.getMessage() : "Không thể tạo thay đổi lịch";
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                    viewModel.resetCreateExceptionState();
                    break;
                case IDLE:
                    break;
            }
        });
    }

    private String normalizeTime(String raw) {
        if (raw == null) return "";
        String t = raw.trim();
        if (t.length() >= 5) {
            return t.substring(0, 5);
        }
        return t;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
