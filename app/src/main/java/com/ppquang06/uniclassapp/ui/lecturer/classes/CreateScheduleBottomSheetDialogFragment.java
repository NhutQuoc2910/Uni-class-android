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

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.request.CreateClassScheduleRequest;
import com.ppquang06.uniclassapp.data.model.response.ClassScheduleItem;
import com.ppquang06.uniclassapp.databinding.BottomSheetCreateScheduleBinding;

import java.util.Calendar;
import java.util.Locale;

public class CreateScheduleBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnScheduleSavedListener {
        void onScheduleSaved();
    }

    private static final String ARG_CLASS_ID = "classId";
    private static final String ARG_SCHEDULE_ID = "scheduleId";
    private static final String ARG_DAY_OF_WEEK = "dayOfWeek";
    private static final String ARG_START_TIME = "startTime";
    private static final String ARG_END_TIME = "endTime";
    private static final String ARG_ROOM = "room";
    private static final String ARG_START_DATE = "startDate";
    private static final String ARG_END_DATE = "endDate";

    private BottomSheetCreateScheduleBinding binding;
    private LecturerManagementViewModel viewModel;
    private OnScheduleSavedListener listener;

    private String classId = "";
    private String scheduleId = null;
    private int selectedDayOfWeek = 1;

    public static CreateScheduleBottomSheetDialogFragment newInstance(String classId) {
        CreateScheduleBottomSheetDialogFragment fragment = new CreateScheduleBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLASS_ID, classId);
        fragment.setArguments(args);
        return fragment;
    }

    public static CreateScheduleBottomSheetDialogFragment newInstanceForEdit(String classId, ClassScheduleItem item) {
        CreateScheduleBottomSheetDialogFragment fragment = new CreateScheduleBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLASS_ID, classId);
        if (item != null) {
            args.putString(ARG_SCHEDULE_ID, item.getId());
            if (item.getDayOfWeek() != null) {
                args.putInt(ARG_DAY_OF_WEEK, item.getDayOfWeek());
            }
            args.putString(ARG_START_TIME, item.getStartTime());
            args.putString(ARG_END_TIME, item.getEndTime());
            args.putString(ARG_ROOM, item.getRoom());
            args.putString(ARG_START_DATE, item.getStartDate());
            args.putString(ARG_END_DATE, item.getEndDate());
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnScheduleSavedListener(OnScheduleSavedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetCreateScheduleBinding.inflate(inflater, container, false);
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
        setupDayOfWeekDropdown();
        setupPickers();
        setupSubmitButton();
        observeViewModel();
    }

    private void readArguments() {
        Bundle args = getArguments();
        if (args != null) {
            classId = args.getString(ARG_CLASS_ID, "");
            scheduleId = args.getString(ARG_SCHEDULE_ID, null);
            selectedDayOfWeek = args.getInt(ARG_DAY_OF_WEEK, 1);
            if (selectedDayOfWeek < 1 || selectedDayOfWeek > 7) {
                selectedDayOfWeek = 1;
            }

            if (scheduleId != null && !scheduleId.trim().isEmpty()) {
                binding.tvScheduleTitleHeader.setText(R.string.bottom_sheet_edit_schedule_title);
                binding.btnSubmitSchedule.setText(R.string.btn_save_schedule);

                String startTime = normalizeTime(args.getString(ARG_START_TIME, ""));
                String endTime = normalizeTime(args.getString(ARG_END_TIME, ""));
                String room = args.getString(ARG_ROOM, "");
                String startDate = normalizeDate(args.getString(ARG_START_DATE, ""));
                String endDate = normalizeDate(args.getString(ARG_END_DATE, ""));

                binding.edtStartTime.setText(startTime);
                binding.edtEndTime.setText(endTime);
                binding.edtRoom.setText(room);
                binding.edtStartDate.setText(startDate);
                binding.edtEndDate.setText(endDate);
            } else {
                binding.tvScheduleTitleHeader.setText(R.string.bottom_sheet_create_schedule_title);
                binding.btnSubmitSchedule.setText(R.string.btn_save_schedule);
            }
        }
    }

    private void setupDayOfWeekDropdown() {
        String[] days = new String[]{
                "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, days);
        binding.actDayOfWeek.setAdapter(adapter);

        int index = Math.max(0, Math.min(days.length - 1, selectedDayOfWeek - 1));
        binding.actDayOfWeek.setText(days[index], false);

        binding.actDayOfWeek.setOnItemClickListener((parent, view, position, id) -> selectedDayOfWeek = position + 1);
    }

    private void setupPickers() {
        binding.edtStartTime.setOnClickListener(v -> showTimePicker(true));
        binding.edtEndTime.setOnClickListener(v -> showTimePicker(false));
        binding.edtStartDate.setOnClickListener(v -> showDatePicker(true));
        binding.edtEndDate.setOnClickListener(v -> showDatePicker(false));
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
                    String formatted = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
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

    private void showDatePicker(boolean isStart) {
        if (getContext() == null) return;
        String current = isStart
                ? (binding.edtStartDate.getText() != null ? binding.edtStartDate.getText().toString().trim() : "")
                : (binding.edtEndDate.getText() != null ? binding.edtEndDate.getText().toString().trim() : "");

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
                    String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    if (isStart) {
                        binding.edtStartDate.setText(formatted);
                        binding.tilStartDate.setError(null);
                    } else {
                        binding.edtEndDate.setText(formatted);
                        binding.tilEndDate.setError(null);
                    }
                },
                y,
                m,
                d
        );
        dialog.show();
    }

    private void setupSubmitButton() {
        binding.btnSubmitSchedule.setOnClickListener(v -> submitSchedule());
    }

    private void submitSchedule() {
        String startTime = binding.edtStartTime.getText() != null ? binding.edtStartTime.getText().toString().trim() : "";
        String endTime = binding.edtEndTime.getText() != null ? binding.edtEndTime.getText().toString().trim() : "";
        String room = binding.edtRoom.getText() != null ? binding.edtRoom.getText().toString().trim() : "";
        String startDate = binding.edtStartDate.getText() != null ? binding.edtStartDate.getText().toString().trim() : "";
        String endDate = binding.edtEndDate.getText() != null ? binding.edtEndDate.getText().toString().trim() : "";

        boolean hasError = false;
        if (startTime.isEmpty()) {
            binding.tilStartTime.setError(getString(R.string.err_required_field));
            hasError = true;
        } else {
            binding.tilStartTime.setError(null);
        }

        if (endTime.isEmpty()) {
            binding.tilEndTime.setError(getString(R.string.err_required_field));
            hasError = true;
        } else {
            binding.tilEndTime.setError(null);
        }

        if (room.isEmpty()) {
            binding.tilRoom.setError(getString(R.string.err_required_field));
            hasError = true;
        } else {
            binding.tilRoom.setError(null);
        }

        if (startDate.isEmpty()) {
            binding.tilStartDate.setError(getString(R.string.err_required_field));
            hasError = true;
        } else {
            binding.tilStartDate.setError(null);
        }

        if (endDate.isEmpty()) {
            binding.tilEndDate.setError(getString(R.string.err_required_field));
            hasError = true;
        } else {
            binding.tilEndDate.setError(null);
        }

        if (hasError || getContext() == null) return;

        CreateClassScheduleRequest request = new CreateClassScheduleRequest(
                selectedDayOfWeek,
                startTime,
                endTime,
                room,
                startDate,
                endDate
        );

        if (scheduleId != null && !scheduleId.trim().isEmpty()) {
            viewModel.updateClassSchedule(classId, scheduleId, request);
        } else {
            viewModel.createClassSchedule(classId, request);
        }
    }

    private void observeViewModel() {
        viewModel.getCreateScheduleState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    binding.pbLoading.setVisibility(View.VISIBLE);
                    binding.btnSubmitSchedule.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.btnSubmitSchedule.setEnabled(true);
                    viewModel.resetCreateScheduleState();
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), R.string.toast_create_schedule_success, Toast.LENGTH_SHORT).show();
                    }
                    if (listener != null) {
                        listener.onScheduleSaved();
                    }
                    dismiss();
                    break;
                case ERROR:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.btnSubmitSchedule.setEnabled(true);
                    String msg = (state.getMessage() != null && !state.getMessage().trim().isEmpty())
                            ? state.getMessage() : "Không thể lưu lịch học";
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                    viewModel.resetCreateScheduleState();
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

    private String normalizeDate(String raw) {
        if (raw == null) return "";
        String t = raw.trim();
        if (t.length() >= 10 && t.charAt(4) == '-' && t.charAt(7) == '-') {
            return t.substring(0, 10);
        }
        return t;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
