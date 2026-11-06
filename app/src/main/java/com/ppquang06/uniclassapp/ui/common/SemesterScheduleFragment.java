package com.ppquang06.uniclassapp.ui.common;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.response.ClassScheduleItem;
import com.ppquang06.uniclassapp.data.model.response.ScheduleExceptionItem;
import com.ppquang06.uniclassapp.data.model.response.SemesterScheduleResponse;
import com.ppquang06.uniclassapp.databinding.FragmentSemesterScheduleBinding;
import com.ppquang06.uniclassapp.ui.common.adapter.ScheduleExceptionAdapter;
import com.ppquang06.uniclassapp.ui.common.adapter.SemesterScheduleAdapter;
import com.ppquang06.uniclassapp.utils.CalendarExportHelper;
import com.ppquang06.uniclassapp.utils.WeeklyTimetableCalculator;
import com.ppquang06.uniclassapp.utils.WeeklyTimetableCalculator.CellType;
import com.ppquang06.uniclassapp.utils.WeeklyTimetableCalculator.PeriodSlot;
import com.ppquang06.uniclassapp.utils.WeeklyTimetableCalculator.WeeklyTimetableCell;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SemesterScheduleFragment extends Fragment {

    private static final int PERIOD_ROW_HEIGHT_DP = 68;
    private static final int DAY_COLUMN_WIDTH_DP = 132;

    private FragmentSemesterScheduleBinding binding;
    private HomeViewModel homeViewModel;
    private SemesterScheduleAdapter semesterScheduleAdapter;
    private ScheduleExceptionAdapter scheduleExceptionAdapter;

    private boolean isWeeklyMode = true;
    private LocalDate currentWeekMonday = WeeklyTimetableCalculator.getMondayOfWeek(LocalDate.now());
    private List<ClassScheduleItem> currentRegularSchedules = new ArrayList<>();
    private List<ScheduleExceptionItem> currentExceptions = new ArrayList<>();

    private final ActivityResultLauncher<String[]> calendarPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean readGranted = result.get(Manifest.permission.READ_CALENDAR);
                Boolean writeGranted = result.get(Manifest.permission.WRITE_CALENDAR);
                if (Boolean.TRUE.equals(readGranted) && Boolean.TRUE.equals(writeGranted)) {
                    performDirectCalendarSync();
                } else if (getContext() != null) {
                    Toast.makeText(requireContext(), R.string.toast_calendar_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSemesterScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        setupTopAppBar();
        setupModeSwitcherAndCalendarExport();
        setupWeekNavigation();
        setupRecyclerViews();
        setupFilterChips();
        setupSwipeRefresh();
        renderWeeklyGrid();
        observeViewModel();

        homeViewModel.loadSemesterSchedule();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupTopAppBar() {
        if (binding == null) return;
        binding.topAppBar.tvTitle.setText(R.string.semester_schedule_title);
        binding.topAppBar.ivBack.setVisibility(View.VISIBLE);
        binding.topAppBar.ivBack.setOnClickListener(v -> {
            try {
                Navigation.findNavController(v).navigateUp();
            } catch (Exception ignored) {
            }
        });
        binding.topAppBar.tvRoleBadge.setVisibility(View.GONE);
        binding.topAppBar.ivAvatar.setVisibility(View.GONE);
    }

    private void setupModeSwitcherAndCalendarExport() {
        if (binding == null) return;

        updateModeToggleUi();

        binding.btnModeWeekly.setOnClickListener(v -> {
            if (!isWeeklyMode) {
                isWeeklyMode = true;
                updateModeToggleUi();
            }
        });

        binding.btnModeList.setOnClickListener(v -> {
            if (isWeeklyMode) {
                isWeeklyMode = false;
                updateModeToggleUi();
            }
        });

        binding.btnExportCalendar.setOnClickListener(v -> showCalendarExportDialog());
    }

    private void updateModeToggleUi() {
        if (binding == null) return;

        if (isWeeklyMode) {
            binding.layoutWeeklyMode.setVisibility(View.VISIBLE);
            binding.layoutListMode.setVisibility(View.GONE);

            binding.btnModeWeekly.setBackgroundResource(R.drawable.bg_badge_pill);
            binding.btnModeWeekly.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1E293B")));
            binding.btnModeWeekly.setTextColor(Color.parseColor("#F8FAFC"));

            binding.btnModeList.setBackground(null);
            binding.btnModeList.setTextColor(Color.parseColor("#94A3B8"));
        } else {
            binding.layoutWeeklyMode.setVisibility(View.GONE);
            binding.layoutListMode.setVisibility(View.VISIBLE);

            binding.btnModeList.setBackgroundResource(R.drawable.bg_badge_pill);
            binding.btnModeList.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1E293B")));
            binding.btnModeList.setTextColor(Color.parseColor("#F8FAFC"));

            binding.btnModeWeekly.setBackground(null);
            binding.btnModeWeekly.setTextColor(Color.parseColor("#94A3B8"));
        }
    }

    private void setupWeekNavigation() {
        if (binding == null) return;

        binding.btnPrevWeek.setOnClickListener(v -> {
            currentWeekMonday = currentWeekMonday.minusWeeks(1);
            renderWeeklyGrid();
        });

        binding.btnNextWeek.setOnClickListener(v -> {
            currentWeekMonday = currentWeekMonday.plusWeeks(1);
            renderWeeklyGrid();
        });

        binding.tvCurrentWeek.setOnClickListener(v -> {
            currentWeekMonday = WeeklyTimetableCalculator.getMondayOfWeek(LocalDate.now());
            renderWeeklyGrid();
        });
    }

    private void renderWeeklyGrid() {
        if (binding == null || getContext() == null) return;

        binding.tvWeekRange.setText(WeeklyTimetableCalculator.formatWeekRangeLabel(currentWeekMonday));

        int periodHeightPx = dpToPx(PERIOD_ROW_HEIGHT_DP);
        int dayColWidthPx = dpToPx(DAY_COLUMN_WIDTH_DP);
        int dividerPx = Math.max(1, dpToPx(1));

        // 1. Render Period Labels Column (Tiết 1 .. Tiết 10)
        binding.llPeriodLabelsContainer.removeAllViews();
        List<PeriodSlot> periods = WeeklyTimetableCalculator.getUitPeriods();
        for (PeriodSlot slot : periods) {
            LinearLayout cell = new LinearLayout(requireContext());
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, periodHeightPx);
            cell.setLayoutParams(lp);

            TextView tvPeriodNum = new TextView(requireContext());
            tvPeriodNum.setText("Tiết " + slot.getPeriodNumber());
            tvPeriodNum.setTextColor(Color.parseColor("#E2E8F0"));
            tvPeriodNum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            tvPeriodNum.setTypeface(tvPeriodNum.getTypeface(), android.graphics.Typeface.BOLD);
            tvPeriodNum.setGravity(Gravity.CENTER);

            TextView tvPeriodTime = new TextView(requireContext());
            tvPeriodTime.setText(slot.getStartTimeLabel());
            tvPeriodTime.setTextColor(Color.parseColor("#64748B"));
            tvPeriodTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            tvPeriodTime.setGravity(Gravity.CENTER);

            cell.addView(tvPeriodNum);
            cell.addView(tvPeriodTime);
            binding.llPeriodLabelsContainer.addView(cell);

            View hDivider = new View(requireContext());
            hDivider.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dividerPx));
            hDivider.setBackgroundColor(Color.parseColor("#1E293B"));
            binding.llPeriodLabelsContainer.addView(hDivider);
        }

        // 2. Render 7 Day Headers + 7 Day Columns
        binding.llDayHeadersRow.removeAllViews();
        binding.llDayColumnsContainer.removeAllViews();

        List<LocalDate> weekDates = WeeklyTimetableCalculator.getWeekDates(currentWeekMonday);
        LocalDate today = LocalDate.now();
        List<FrameLayout> dayColumnFrames = new ArrayList<>(7);
        int totalColumnHeightPx = periods.size() * (periodHeightPx + dividerPx);

        for (int i = 0; i < 7; i++) {
            int dayOfWeek = i + 1;
            LocalDate date = weekDates.get(i);
            boolean isToday = today.equals(date);

            // Header cell
            LinearLayout headerCell = new LinearLayout(requireContext());
            headerCell.setOrientation(LinearLayout.VERTICAL);
            headerCell.setGravity(Gravity.CENTER);
            headerCell.setLayoutParams(new LinearLayout.LayoutParams(
                    dayColWidthPx, ViewGroup.LayoutParams.MATCH_PARENT));
            if (isToday) {
                headerCell.setBackgroundColor(Color.parseColor("#172554"));
            }

            TextView tvDayTitle = new TextView(requireContext());
            tvDayTitle.setText(WeeklyTimetableCalculator.getDayHeaderTitle(dayOfWeek));
            tvDayTitle.setTextColor(isToday ? Color.parseColor("#60A5FA") : Color.parseColor("#E2E8F0"));
            tvDayTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            tvDayTitle.setTypeface(tvDayTitle.getTypeface(), android.graphics.Typeface.BOLD);
            tvDayTitle.setGravity(Gravity.CENTER);

            TextView tvDayDate = new TextView(requireContext());
            tvDayDate.setText(WeeklyTimetableCalculator.formatDateDdMmYyyy(date));
            tvDayDate.setTextColor(isToday ? Color.parseColor("#93C5FD") : Color.parseColor("#64748B"));
            tvDayDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            tvDayDate.setGravity(Gravity.CENTER);

            headerCell.addView(tvDayTitle);
            headerCell.addView(tvDayDate);
            binding.llDayHeadersRow.addView(headerCell);

            View vHeaderDiv = new View(requireContext());
            vHeaderDiv.setLayoutParams(new LinearLayout.LayoutParams(
                    dividerPx, ViewGroup.LayoutParams.MATCH_PARENT));
            vHeaderDiv.setBackgroundColor(Color.parseColor("#1E293B"));
            binding.llDayHeadersRow.addView(vHeaderDiv);

            // Day body column FrameLayout
            FrameLayout dayFrame = new FrameLayout(requireContext());
            dayFrame.setLayoutParams(new LinearLayout.LayoutParams(dayColWidthPx, totalColumnHeightPx));
            if (isToday) {
                dayFrame.setBackgroundColor(Color.parseColor("#0D1B3E"));
            }

            // Background horizontal grid lines for each period
            for (int p = 1; p <= periods.size(); p++) {
                View gridLine = new View(requireContext());
                FrameLayout.LayoutParams lineLp = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dividerPx);
                lineLp.topMargin = p * (periodHeightPx + dividerPx) - dividerPx;
                gridLine.setLayoutParams(lineLp);
                gridLine.setBackgroundColor(Color.parseColor("#1E293B"));
                dayFrame.addView(gridLine);
            }

            dayColumnFrames.add(dayFrame);
            binding.llDayColumnsContainer.addView(dayFrame);

            View vColDiv = new View(requireContext());
            vColDiv.setLayoutParams(new LinearLayout.LayoutParams(dividerPx, totalColumnHeightPx));
            vColDiv.setBackgroundColor(Color.parseColor("#1E293B"));
            binding.llDayColumnsContainer.addView(vColDiv);
        }

        // 3. Place schedule cells onto the corresponding day column FrameLayouts
        List<WeeklyTimetableCell> cells = WeeklyTimetableCalculator.buildWeekCells(
                currentWeekMonday, currentRegularSchedules, currentExceptions);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int rowStridePx = periodHeightPx + dividerPx;
        int cellInsetPx = dpToPx(2);

        for (WeeklyTimetableCell cellData : cells) {
            int colIdx = cellData.getDayOfWeek() - 1;
            if (colIdx < 0 || colIdx >= dayColumnFrames.size()) continue;

            FrameLayout targetColumn = dayColumnFrames.get(colIdx);
            View cardView = inflater.inflate(R.layout.item_weekly_timetable_cell, targetColumn, false);

            int topMargin = (cellData.getStartPeriod() - 1) * rowStridePx + cellInsetPx;
            int cellHeight = cellData.getSpanCount() * rowStridePx - (cellInsetPx * 2);

            FrameLayout.LayoutParams cardLp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, Math.max(periodHeightPx - dpToPx(4), cellHeight));
            cardLp.topMargin = topMargin;
            cardLp.leftMargin = cellInsetPx;
            cardLp.rightMargin = cellInsetPx;
            cardView.setLayoutParams(cardLp);

            bindWeeklyCellView(cardView, cellData);
            targetColumn.addView(cardView);
        }
    }

    private void bindWeeklyCellView(View cardView, WeeklyTimetableCell cellData) {
        MaterialCardView card = cardView.findViewById(R.id.cardWeeklyCell);
        TextView tvBadge = cardView.findViewById(R.id.tvCellBadge);
        TextView tvCode = cardView.findViewById(R.id.tvCellClassCode);
        TextView tvName = cardView.findViewById(R.id.tvCellClassName);
        TextView tvPeriod = cardView.findViewById(R.id.tvCellPeriod);
        TextView tvRoom = cardView.findViewById(R.id.tvCellRoom);

        tvCode.setText(!TextUtils.isEmpty(cellData.getClassCode()) ? cellData.getClassCode() : "");
        tvName.setText(!TextUtils.isEmpty(cellData.getClassName()) ? cellData.getClassName() : "Lớp học");
        tvPeriod.setText(cellData.getPeriodRangeLabel() + " (" + cellData.getStartTime() + "-" + cellData.getEndTime() + ")");

        if (!TextUtils.isEmpty(cellData.getRoom())) {
            String room = cellData.getRoom().trim();
            if (!room.toLowerCase().startsWith("phòng")) {
                room = "Phòng " + room;
            }
            tvRoom.setText(room);
            tvRoom.setVisibility(View.VISIBLE);
        } else {
            tvRoom.setVisibility(View.GONE);
        }

        CellType type = cellData.getCellType();
        if (type == CellType.MAKEUP) {
            card.setCardBackgroundColor(Color.parseColor("#064E3B"));
            card.setStrokeColor(Color.parseColor("#10B981"));
            tvCode.setTextColor(Color.parseColor("#6EE7B7"));
            tvName.setTextColor(Color.parseColor("#ECFDF5"));
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText(cellData.getStatusBadgeLabel());
            tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#059669")));
        } else if (type == CellType.CANCELLED) {
            card.setCardBackgroundColor(Color.parseColor("#450A0A"));
            card.setStrokeColor(Color.parseColor("#EF4444"));
            tvCode.setTextColor(Color.parseColor("#FCA5A5"));
            tvName.setTextColor(Color.parseColor("#FECACA"));
            tvName.setPaintFlags(tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText(cellData.getStatusBadgeLabel());
            tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DC2626")));
        } else if (type == CellType.MODIFIED) {
            card.setCardBackgroundColor(Color.parseColor("#1E3A8A"));
            card.setStrokeColor(Color.parseColor("#60A5FA"));
            tvCode.setTextColor(Color.parseColor("#BFDBFE"));
            tvName.setTextColor(Color.parseColor("#F8FAFC"));
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText(cellData.getStatusBadgeLabel());
            tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2563EB")));
        } else {
            card.setCardBackgroundColor(Color.parseColor("#172554"));
            card.setStrokeColor(Color.parseColor("#3B82F6"));
            tvCode.setTextColor(Color.parseColor("#93C5FD"));
            tvName.setTextColor(Color.parseColor("#F8FAFC"));
            tvBadge.setVisibility(View.GONE);
        }

        card.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(cellData.getClassId())) {
                navigateToClassDetail(cellData.getClassId(), cellData.getClassCode(), cellData.getClassName());
            }
        });
    }

    private void showCalendarExportDialog() {
        if (getContext() == null) return;
        if (currentRegularSchedules.isEmpty() && currentExceptions.isEmpty()) {
            Toast.makeText(requireContext(), R.string.toast_schedule_empty_export, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = new String[]{
                getString(R.string.dialog_calendar_option_direct),
                getString(R.string.dialog_calendar_option_ics)
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_calendar_export_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        requestOrRunDirectCalendarSync();
                    } else if (which == 1) {
                        exportAndOpenIcsFile();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void requestOrRunDirectCalendarSync() {
        if (getContext() == null) return;
        boolean hasRead = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED;
        boolean hasWrite = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CALENDAR)
                == PackageManager.PERMISSION_GRANTED;

        if (hasRead && hasWrite) {
            performDirectCalendarSync();
        } else {
            calendarPermissionLauncher.launch(new String[]{
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
            });
        }
    }

    private void performDirectCalendarSync() {
        if (getContext() == null) return;
        int synced = CalendarExportHelper.syncDirectlyToDeviceCalendar(
                requireContext(), currentRegularSchedules, currentExceptions);
        if (synced >= 0) {
            Toast.makeText(requireContext(),
                    getString(R.string.toast_calendar_sync_success, synced),
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(requireContext(), R.string.toast_calendar_sync_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void exportAndOpenIcsFile() {
        if (getContext() == null) return;
        String icsContent = CalendarExportHelper.generateIcsContent(currentRegularSchedules, currentExceptions);
        Uri uri = CalendarExportHelper.saveIcsFileToDownloads(requireContext(), icsContent);
        if (uri != null) {
            Toast.makeText(requireContext(), R.string.toast_ics_saved_success, Toast.LENGTH_LONG).show();
            CalendarExportHelper.openIcsUri(requireContext(), uri);
        } else {
            Toast.makeText(requireContext(), R.string.toast_ics_saved_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerViews() {
        if (binding == null) return;

        semesterScheduleAdapter = new SemesterScheduleAdapter(item -> {
            if (item != null && !TextUtils.isEmpty(item.getClassId())) {
                navigateToClassDetail(item.getClassId(), item.getClassCode(), item.getClassName());
            }
        });
        binding.rvRegularSchedules.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRegularSchedules.setAdapter(semesterScheduleAdapter);

        scheduleExceptionAdapter = new ScheduleExceptionAdapter(item -> {
            if (item != null && !TextUtils.isEmpty(item.getClassId())) {
                navigateToClassDetail(item.getClassId(), item.getClassCode(), item.getClassName());
            }
        });
        binding.rvScheduleExceptions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvScheduleExceptions.setAdapter(scheduleExceptionAdapter);
    }

    private void setupFilterChips() {
        if (binding == null) return;

        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (binding == null) return;
            int checkedId = binding.chipGroupFilter.getCheckedChipId();
            int dayOfWeek = 0;
            if (checkedId == R.id.chipMon) {
                dayOfWeek = 1;
            } else if (checkedId == R.id.chipTue) {
                dayOfWeek = 2;
            } else if (checkedId == R.id.chipWed) {
                dayOfWeek = 3;
            } else if (checkedId == R.id.chipThu) {
                dayOfWeek = 4;
            } else if (checkedId == R.id.chipFri) {
                dayOfWeek = 5;
            } else if (checkedId == R.id.chipSat) {
                dayOfWeek = 6;
            } else if (checkedId == R.id.chipSun) {
                dayOfWeek = 7;
            }
            semesterScheduleAdapter.filterByDayOfWeek(dayOfWeek);
            updateRegularEmptyState();
        });
    }

    private void setupSwipeRefresh() {
        if (binding == null) return;
        binding.swipeRefresh.setColorSchemeResources(R.color.color_primary);
        binding.swipeRefresh.setOnRefreshListener(() -> homeViewModel.loadSemesterSchedule());
    }

    private void observeViewModel() {
        homeViewModel.getSemesterScheduleState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || binding == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    if (!binding.swipeRefresh.isRefreshing()) {
                        binding.pbLoading.setVisibility(View.VISIBLE);
                    }
                    break;
                case SUCCESS:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    applyScheduleData(state.getData());
                    break;
                case ERROR:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    if (getContext() != null) {
                        String msg = state.getMessage();
                        Toast.makeText(requireContext(), !TextUtils.isEmpty(msg) ? msg : "Không thể tải thời khóa biểu học kỳ", Toast.LENGTH_SHORT).show();
                    }
                    applyScheduleData(state.getData());
                    break;
                case IDLE:
                default:
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    break;
            }
        });
    }

    private void applyScheduleData(@Nullable SemesterScheduleResponse data) {
        if (data != null) {
            currentRegularSchedules = data.getRegularSchedules() != null
                    ? new ArrayList<>(data.getRegularSchedules())
                    : new ArrayList<>();
            currentExceptions = data.getScheduleExceptions() != null
                    ? new ArrayList<>(data.getScheduleExceptions())
                    : new ArrayList<>();
            semesterScheduleAdapter.submitList(data.getRegularSchedules());
            scheduleExceptionAdapter.submitList(data.getScheduleExceptions());
        } else {
            currentRegularSchedules = Collections.emptyList();
            currentExceptions = Collections.emptyList();
            semesterScheduleAdapter.submitList(null);
            scheduleExceptionAdapter.submitList(null);
        }
        updateRegularEmptyState();
        updateExceptionsEmptyState();
        renderWeeklyGrid();
    }

    private void updateRegularEmptyState() {
        if (binding == null) return;
        boolean isEmpty = semesterScheduleAdapter == null || semesterScheduleAdapter.getItemCount() == 0;
        binding.rvRegularSchedules.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyRegularSchedules.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void updateExceptionsEmptyState() {
        if (binding == null) return;
        boolean isEmpty = scheduleExceptionAdapter == null || scheduleExceptionAdapter.getItemCount() == 0;
        binding.rvScheduleExceptions.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyExceptions.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private int dpToPx(int dp) {
        if (getContext() == null) return dp;
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, requireContext().getResources().getDisplayMetrics()));
    }

    private void navigateToClassDetail(String classId, String classCode, String className) {
        if (classId == null || getView() == null) return;
        Bundle args = new Bundle();
        args.putString("classId", classId);
        args.putString("classCode", classCode != null ? classCode : "");
        args.putString("className", className != null ? className : "");
        args.putString("semester", "");
        args.putString("status", "ACTIVE");

        try {
            NavController navController = Navigation.findNavController(requireView());
            try {
                navController.navigate(R.id.action_semester_schedule_to_class_detail, args);
            } catch (Exception eAction) {
                try {
                    navController.navigate(R.id.nav_student_class_detail, args);
                } catch (Exception eStudent) {
                    try {
                        navController.navigate(R.id.nav_lecturer_class_detail, args);
                    } catch (Exception eLecturer) {
                        if (getContext() != null) {
                            Toast.makeText(requireContext(), (classCode != null ? classCode : "") + " - " + (className != null ? className : ""), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
