package com.ppquang06.uniclassapp.utils;

import com.ppquang06.uniclassapp.data.model.response.ClassScheduleItem;
import com.ppquang06.uniclassapp.data.model.response.ScheduleExceptionItem;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Calculator for the Weekly Timetable Grid (Tiết 1 - Tiết 10, Thứ 2 - Chủ nhật).
 * Merges recurring {@link ClassScheduleItem} and {@link ScheduleExceptionItem} for any chosen week.
 */
public final class WeeklyTimetableCalculator {

    public static final int TOTAL_PERIODS = 10;

    private static final DateTimeFormatter DATE_FORMAT_DD_MM = DateTimeFormatter.ofPattern("dd/MM", Locale.US);
    private static final DateTimeFormatter DATE_FORMAT_DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US);

    public enum CellType {
        REGULAR,
        MAKEUP,
        CANCELLED,
        MODIFIED
    }

    public static final class PeriodSlot {
        private final int periodNumber;
        private final String startTimeLabel;
        private final String endTimeLabel;
        private final LocalTime startTime;
        private final LocalTime endTime;

        public PeriodSlot(int periodNumber, String startTimeLabel, String endTimeLabel) {
            this.periodNumber = periodNumber;
            this.startTimeLabel = startTimeLabel;
            this.endTimeLabel = endTimeLabel;
            this.startTime = LocalTime.parse(startTimeLabel);
            this.endTime = LocalTime.parse(endTimeLabel);
        }

        public int getPeriodNumber() {
            return periodNumber;
        }

        public String getStartTimeLabel() {
            return startTimeLabel;
        }

        public String getEndTimeLabel() {
            return endTimeLabel;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }
    }

    public static final class WeeklyTimetableCell {
        private final String classId;
        private final String classCode;
        private final String className;
        private final int dayOfWeek;
        private final LocalDate date;
        private final int startPeriod;
        private final int endPeriod;
        private final String startTime;
        private final String endTime;
        private final String room;
        private final CellType cellType;
        private final String reason;

        public WeeklyTimetableCell(String classId,
                                   String classCode,
                                   String className,
                                   int dayOfWeek,
                                   LocalDate date,
                                   int startPeriod,
                                   int endPeriod,
                                   String startTime,
                                   String endTime,
                                   String room,
                                   CellType cellType,
                                   String reason) {
            this.classId = classId;
            this.classCode = classCode;
            this.className = className;
            this.dayOfWeek = dayOfWeek;
            this.date = date;
            this.startPeriod = Math.max(1, Math.min(TOTAL_PERIODS, startPeriod));
            this.endPeriod = Math.max(this.startPeriod, Math.min(TOTAL_PERIODS, endPeriod));
            this.startTime = startTime != null ? startTime : "";
            this.endTime = endTime != null ? endTime : "";
            this.room = room != null ? room : "";
            this.cellType = cellType != null ? cellType : CellType.REGULAR;
            this.reason = reason;
        }

        public String getClassId() {
            return classId;
        }

        public String getClassCode() {
            return classCode;
        }

        public String getClassName() {
            return className;
        }

        public int getDayOfWeek() {
            return dayOfWeek;
        }

        public LocalDate getDate() {
            return date;
        }

        public int getStartPeriod() {
            return startPeriod;
        }

        public int getEndPeriod() {
            return endPeriod;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public String getRoom() {
            return room;
        }

        public CellType getCellType() {
            return cellType;
        }

        public String getReason() {
            return reason;
        }

        public int getSpanCount() {
            return Math.max(1, endPeriod - startPeriod + 1);
        }

        public String getPeriodRangeLabel() {
            if (startPeriod == endPeriod) {
                return "Tiết " + startPeriod;
            }
            return "Tiết " + startPeriod + "–" + endPeriod;
        }

        public String getTimeRangeLabel() {
            if (!startTime.isEmpty() && !endTime.isEmpty()) {
                return startTime + " - " + endTime;
            }
            return startTime + endTime;
        }

        public String getStatusBadgeLabel() {
            switch (cellType) {
                case MAKEUP:
                    return "Lịch bù";
                case CANCELLED:
                    return "Báo nghỉ";
                case MODIFIED:
                    return "Đổi lịch";
                case REGULAR:
                default:
                    return "Lịch thường";
            }
        }
    }

    private static final List<PeriodSlot> UIT_PERIODS;

    static {
        List<PeriodSlot> slots = new ArrayList<>(TOTAL_PERIODS);
        slots.add(new PeriodSlot(1, "07:30", "08:15"));
        slots.add(new PeriodSlot(2, "08:15", "09:00"));
        slots.add(new PeriodSlot(3, "09:00", "09:45"));
        slots.add(new PeriodSlot(4, "10:00", "10:45"));
        slots.add(new PeriodSlot(5, "10:45", "11:30"));
        slots.add(new PeriodSlot(6, "13:00", "13:45"));
        slots.add(new PeriodSlot(7, "13:45", "14:30"));
        slots.add(new PeriodSlot(8, "14:30", "15:15"));
        slots.add(new PeriodSlot(9, "15:30", "16:15"));
        slots.add(new PeriodSlot(10, "16:15", "17:00"));
        UIT_PERIODS = Collections.unmodifiableList(slots);
    }

    private WeeklyTimetableCalculator() {
    }

    public static List<PeriodSlot> getUitPeriods() {
        return UIT_PERIODS;
    }

    public static LocalDate getMondayOfWeek(LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        return target.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static List<LocalDate> getWeekDates(LocalDate anyDateInWeek) {
        LocalDate monday = getMondayOfWeek(anyDateInWeek);
        List<LocalDate> dates = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            dates.add(monday.plusDays(i));
        }
        return dates;
    }

    public static String formatWeekRangeLabel(LocalDate anyDateInWeek) {
        LocalDate monday = getMondayOfWeek(anyDateInWeek);
        LocalDate sunday = monday.plusDays(6);
        return "Tuần " + monday.format(DATE_FORMAT_DD_MM) + " – " + sunday.format(DATE_FORMAT_DD_MM_YYYY);
    }

    public static String formatDateDdMmYyyy(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMAT_DD_MM_YYYY);
    }

    public static String getDayHeaderTitle(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1:
                return "Thứ 2";
            case 2:
                return "Thứ 3";
            case 3:
                return "Thứ 4";
            case 4:
                return "Thứ 5";
            case 5:
                return "Thứ 6";
            case 6:
                return "Thứ 7";
            case 7:
                return "Chủ nhật";
            default:
                return "Thứ " + dayOfWeek;
        }
    }

    public static int mapTimeToStartPeriod(String rawStartTime) {
        LocalTime time = parseLocalTime(rawStartTime);
        if (time == null) {
            return 1;
        }
        for (PeriodSlot slot : UIT_PERIODS) {
            if (time.isBefore(slot.getEndTime())) {
                return slot.getPeriodNumber();
            }
        }
        return TOTAL_PERIODS;
    }

    public static int mapTimeToEndPeriod(String rawEndTime, int startPeriod) {
        int safeStart = Math.max(1, Math.min(TOTAL_PERIODS, startPeriod));
        LocalTime time = parseLocalTime(rawEndTime);
        if (time == null) {
            return safeStart;
        }
        int matched = safeStart;
        for (PeriodSlot slot : UIT_PERIODS) {
            if (time.isAfter(slot.getStartTime())) {
                matched = slot.getPeriodNumber();
            }
        }
        return Math.max(safeStart, Math.min(TOTAL_PERIODS, matched));
    }

    public static List<WeeklyTimetableCell> buildWeekCells(LocalDate anyDateInWeek,
                                                           List<ClassScheduleItem> regularSchedules,
                                                           List<ScheduleExceptionItem> exceptions) {
        LocalDate monday = getMondayOfWeek(anyDateInWeek);
        LocalDate sunday = monday.plusDays(6);

        List<ClassScheduleItem> safeRegular = regularSchedules != null ? regularSchedules : Collections.emptyList();
        List<ScheduleExceptionItem> safeExceptions = exceptions != null ? exceptions : Collections.emptyList();

        List<ScheduleExceptionItem> publishedExceptions = new ArrayList<>();
        for (ScheduleExceptionItem ex : safeExceptions) {
            if (ex == null) continue;
            String status = ex.getStatus();
            if (status == null || status.trim().isEmpty() || "PUBLISHED".equalsIgnoreCase(status.trim())) {
                publishedExceptions.add(ex);
            }
        }

        List<WeeklyTimetableCell> cells = new ArrayList<>();

        // 1. Process regular schedules for each day of the week (1 = Mon .. 7 = Sun)
        for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
            LocalDate currentDate = monday.plusDays(dayOfWeek - 1);

            for (ClassScheduleItem reg : safeRegular) {
                if (reg == null || reg.getDayOfWeek() == null || reg.getDayOfWeek() != dayOfWeek) {
                    continue;
                }
                if (!isDateWithinScheduleBounds(currentDate, reg.getStartDate(), reg.getEndDate())) {
                    continue;
                }

                ScheduleExceptionItem matchingEx = findExceptionForRegularOnDate(reg, currentDate, publishedExceptions);
                if (matchingEx != null) {
                    String exType = matchingEx.getExceptionType() != null
                            ? matchingEx.getExceptionType().trim().toUpperCase(Locale.US)
                            : "";
                    if ("CANCELLED".equals(exType)) {
                        String startStr = formatShortTime(reg.getStartTime());
                        String endStr = formatShortTime(reg.getEndTime());
                        int startP = mapTimeToStartPeriod(startStr);
                        int endP = mapTimeToEndPeriod(endStr, startP);
                        cells.add(new WeeklyTimetableCell(
                                reg.getClassId(),
                                reg.getClassCode(),
                                reg.getClassName(),
                                dayOfWeek,
                                currentDate,
                                startP,
                                endP,
                                startStr,
                                endStr,
                                reg.getRoom(),
                                CellType.CANCELLED,
                                matchingEx.getReason()
                        ));
                        continue;
                    } else if ("MODIFIED".equals(exType)) {
                        String startStr = formatShortTime(nonEmptyOrDefault(matchingEx.getStartTime(), reg.getStartTime()));
                        String endStr = formatShortTime(nonEmptyOrDefault(matchingEx.getEndTime(), reg.getEndTime()));
                        String room = nonEmptyOrDefault(matchingEx.getRoom(), reg.getRoom());
                        int startP = mapTimeToStartPeriod(startStr);
                        int endP = mapTimeToEndPeriod(endStr, startP);
                        cells.add(new WeeklyTimetableCell(
                                reg.getClassId(),
                                reg.getClassCode(),
                                reg.getClassName(),
                                dayOfWeek,
                                currentDate,
                                startP,
                                endP,
                                startStr,
                                endStr,
                                room,
                                CellType.MODIFIED,
                                matchingEx.getReason()
                        ));
                        continue;
                    }
                }

                String startStr = formatShortTime(reg.getStartTime());
                String endStr = formatShortTime(reg.getEndTime());
                int startP = mapTimeToStartPeriod(startStr);
                int endP = mapTimeToEndPeriod(endStr, startP);
                cells.add(new WeeklyTimetableCell(
                        reg.getClassId(),
                        reg.getClassCode(),
                        reg.getClassName(),
                        dayOfWeek,
                        currentDate,
                        startP,
                        endP,
                        startStr,
                        endStr,
                        reg.getRoom(),
                        CellType.REGULAR,
                        null
                ));
            }
        }

        // 2. Process MAKEUP exceptions occurring within [monday..sunday]
        for (ScheduleExceptionItem ex : publishedExceptions) {
            String exType = ex.getExceptionType() != null
                    ? ex.getExceptionType().trim().toUpperCase(Locale.US)
                    : "";
            if (!"MAKEUP".equals(exType)) {
                continue;
            }
            LocalDate occDate = parseLocalDate(ex.getOccurrenceDate());
            if (occDate == null) {
                occDate = parseLocalDate(ex.getOriginalDate());
            }
            if (occDate == null || occDate.isBefore(monday) || occDate.isAfter(sunday)) {
                continue;
            }
            int dayOfWeek = occDate.getDayOfWeek().getValue();
            String startStr = formatShortTime(ex.getStartTime());
            String endStr = formatShortTime(ex.getEndTime());
            int startP = mapTimeToStartPeriod(startStr);
            int endP = mapTimeToEndPeriod(endStr, startP);
            cells.add(new WeeklyTimetableCell(
                    ex.getClassId(),
                    ex.getClassCode(),
                    ex.getClassName(),
                    dayOfWeek,
                    occDate,
                    startP,
                    endP,
                    startStr,
                    endStr,
                    ex.getRoom(),
                    CellType.MAKEUP,
                    ex.getReason()
            ));
        }

        cells.sort(Comparator.comparingInt(WeeklyTimetableCell::getDayOfWeek)
                .thenComparingInt(WeeklyTimetableCell::getStartPeriod));
        return cells;
    }

    private static ScheduleExceptionItem findExceptionForRegularOnDate(ClassScheduleItem reg,
                                                                       LocalDate date,
                                                                       List<ScheduleExceptionItem> exceptions) {
        for (ScheduleExceptionItem ex : exceptions) {
            String exType = ex.getExceptionType() != null
                    ? ex.getExceptionType().trim().toUpperCase(Locale.US)
                    : "";
            if (!"CANCELLED".equals(exType) && !"MODIFIED".equals(exType)) {
                continue;
            }
            LocalDate origDate = parseLocalDate(ex.getOriginalDate());
            if (origDate == null || !origDate.equals(date)) {
                continue;
            }
            if (reg.getId() != null && ex.getClassScheduleId() != null) {
                if (reg.getId().equals(ex.getClassScheduleId())) {
                    return ex;
                }
            } else if (reg.getClassId() != null && reg.getClassId().equals(ex.getClassId())) {
                return ex;
            }
        }
        return null;
    }

    private static boolean isDateWithinScheduleBounds(LocalDate date, String rawStartDate, String rawEndDate) {
        LocalDate start = parseLocalDate(rawStartDate);
        LocalDate end = parseLocalDate(rawEndDate);
        if (start != null && date.isBefore(start)) {
            return false;
        }
        if (end != null && date.isAfter(end)) {
            return false;
        }
        return true;
    }

    public static LocalDate parseLocalDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.length() >= 10) {
            trimmed = trimmed.substring(0, 10);
        }
        try {
            return LocalDate.parse(trimmed);
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalTime parseLocalTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.length() >= 5) {
            trimmed = trimmed.substring(0, 5);
        }
        try {
            return LocalTime.parse(trimmed);
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatShortTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.length() >= 5) {
            return trimmed.substring(0, 5);
        }
        return trimmed;
    }

    private static String nonEmptyOrDefault(String candidate, String fallback) {
        if (candidate != null && !candidate.trim().isEmpty()) {
            return candidate;
        }
        return fallback;
    }
}
