package com.ppquang06.uniclassapp;

import com.ppquang06.uniclassapp.data.model.response.ClassScheduleItem;
import com.ppquang06.uniclassapp.data.model.response.ScheduleExceptionItem;
import com.ppquang06.uniclassapp.utils.CalendarExportHelper;
import com.ppquang06.uniclassapp.utils.WeeklyTimetableCalculator;
import com.ppquang06.uniclassapp.utils.WeeklyTimetableCalculator.CellType;
import com.ppquang06.uniclassapp.utils.WeeklyTimetableCalculator.WeeklyTimetableCell;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WeeklyTimetableAndCalendarExportTest {

    @Test
    public void uitPeriods_hasTenStandardPeriodsWithCorrectTimes() {
        List<WeeklyTimetableCalculator.PeriodSlot> periods = WeeklyTimetableCalculator.getUitPeriods();
        assertEquals(10, periods.size());
        assertEquals("07:30", periods.get(0).getStartTimeLabel());
        assertEquals("08:15", periods.get(0).getEndTimeLabel());
        assertEquals("08:15", periods.get(1).getStartTimeLabel());
        assertEquals("09:00", periods.get(2).getStartTimeLabel());
        assertEquals("13:00", periods.get(5).getStartTimeLabel());
        assertEquals("16:15", periods.get(9).getStartTimeLabel());
        assertEquals("17:00", periods.get(9).getEndTimeLabel());
    }

    @Test
    public void mapTimeToPeriods_mapsMorningAndAfternoonSlotsAccurately() {
        // Tiết 1 - 3: 07:30 - 09:45
        int s1 = WeeklyTimetableCalculator.mapTimeToStartPeriod("07:30:00");
        int e1 = WeeklyTimetableCalculator.mapTimeToEndPeriod("09:45:00", s1);
        assertEquals(1, s1);
        assertEquals(3, e1);

        // Tiết 2 - 3: 08:15 - 09:45
        int s2 = WeeklyTimetableCalculator.mapTimeToStartPeriod("08:15");
        int e2 = WeeklyTimetableCalculator.mapTimeToEndPeriod("09:45", s2);
        assertEquals(2, s2);
        assertEquals(3, e2);

        // Tiết 1 - 5: 07:30 - 11:30
        int s3 = WeeklyTimetableCalculator.mapTimeToStartPeriod("07:30");
        int e3 = WeeklyTimetableCalculator.mapTimeToEndPeriod("11:30", s3);
        assertEquals(1, s3);
        assertEquals(5, e3);

        // Tiết 6 - 8: 13:00 - 15:15
        int s4 = WeeklyTimetableCalculator.mapTimeToStartPeriod("13:00");
        int e4 = WeeklyTimetableCalculator.mapTimeToEndPeriod("15:15", s4);
        assertEquals(6, s4);
        assertEquals(8, e4);

        // Tiết 6 - 10: 13:00 - 17:00
        int s5 = WeeklyTimetableCalculator.mapTimeToStartPeriod("13:00");
        int e5 = WeeklyTimetableCalculator.mapTimeToEndPeriod("17:00", s5);
        assertEquals(6, s5);
        assertEquals(10, e5);
    }

    @Test
    public void weekCalculation_computesMondayAndFormatsRangeLabel() {
        LocalDate wednesday = LocalDate.of(2026, 11, 11);
        LocalDate monday = WeeklyTimetableCalculator.getMondayOfWeek(wednesday);
        assertEquals(LocalDate.of(2026, 11, 9), monday);

        List<LocalDate> weekDates = WeeklyTimetableCalculator.getWeekDates(wednesday);
        assertEquals(7, weekDates.size());
        assertEquals(LocalDate.of(2026, 11, 9), weekDates.get(0));
        assertEquals(LocalDate.of(2026, 11, 15), weekDates.get(6));

        assertEquals("Tuần 09/11 – 15/11/2026", WeeklyTimetableCalculator.formatWeekRangeLabel(wednesday));
    }

    @Test
    public void buildWeekCells_mergesRegularCancelledModifiedAndMakeupCorrectly() {
        // Week: 09/11/2026 (Mon) - 15/11/2026 (Sun)
        LocalDate weekRef = LocalDate.of(2026, 11, 9);

        // 1. Regular on Tuesday (2026-11-10): SS007.R15 (Tiết 1-3: 07:30 - 09:45)
        ClassScheduleItem tueRegular = new ClassScheduleItem(
                "sched-1", "cls-1", "SS007.R15", "Triết học Mác – Lênin",
                2, "07:30", "09:45", "B5.14", "2026-09-01", "2026-12-31"
        );

        // 2. Regular on Wednesday (2026-11-11): NT533.R11 (Tiết 2-3: 08:15 - 09:45) -> CANCELLED on 2026-11-11
        ClassScheduleItem wedCancelled = new ClassScheduleItem(
                "sched-2", "cls-2", "NT533.R11", "Hệ tính toán phân bố",
                3, "08:15", "09:45", "B1.06", "2026-09-01", "2026-12-31"
        );

        // 3. Regular out of date bounds (ended in Oct 2026)
        ClassScheduleItem expiredRegular = new ClassScheduleItem(
                "sched-3", "cls-3", "IT001", "Nhập môn lập trình",
                4, "07:30", "11:30", "A101", "2026-09-01", "2026-10-15"
        );

        ScheduleExceptionItem cancelEx = new ScheduleExceptionItem(
                "ex-1", "cls-2", "NT533.R11", "Hệ tính toán phân bố",
                "sched-2", "2026-11-11", null,
                "CANCELLED", "PUBLISHED", null, null, null, "Giảng viên bận công tác", null
        );

        // 4. Makeup on Saturday (2026-11-14): NT533.R11 (Tiết 6-8: 13:00 - 15:15)
        ScheduleExceptionItem makeupEx = new ScheduleExceptionItem(
                "ex-2", "cls-2", "NT533.R11", "Hệ tính toán phân bố",
                "sched-2", "2026-11-11", "2026-11-14",
                "MAKEUP", "PUBLISHED", "13:00", "15:15", "B2.10", "Học bù buổi 11/11", "ex-1"
        );

        List<WeeklyTimetableCell> cells = WeeklyTimetableCalculator.buildWeekCells(
                weekRef,
                Arrays.asList(tueRegular, wedCancelled, expiredRegular),
                Arrays.asList(cancelEx, makeupEx)
        );

        assertEquals(3, cells.size());

        // Cell 0: Tuesday REGULAR
        WeeklyTimetableCell c0 = cells.get(0);
        assertEquals(2, c0.getDayOfWeek());
        assertEquals(CellType.REGULAR, c0.getCellType());
        assertEquals("SS007.R15", c0.getClassCode());
        assertEquals(1, c0.getStartPeriod());
        assertEquals(3, c0.getEndPeriod());
        assertEquals(3, c0.getSpanCount());
        assertEquals("Tiết 1–3", c0.getPeriodRangeLabel());
        assertEquals("Lịch thường", c0.getStatusBadgeLabel());

        // Cell 1: Wednesday CANCELLED (still shown on grid with "Báo nghỉ" badge!)
        WeeklyTimetableCell c1 = cells.get(1);
        assertEquals(3, c1.getDayOfWeek());
        assertEquals(CellType.CANCELLED, c1.getCellType());
        assertEquals("NT533.R11", c1.getClassCode());
        assertEquals(2, c1.getStartPeriod());
        assertEquals(3, c1.getEndPeriod());
        assertEquals("Tiết 2–3", c1.getPeriodRangeLabel());
        assertEquals("Báo nghỉ", c1.getStatusBadgeLabel());

        // Cell 2: Saturday MAKEUP
        WeeklyTimetableCell c2 = cells.get(2);
        assertEquals(6, c2.getDayOfWeek());
        assertEquals(CellType.MAKEUP, c2.getCellType());
        assertEquals("NT533.R11", c2.getClassCode());
        assertEquals(6, c2.getStartPeriod());
        assertEquals(8, c2.getEndPeriod());
        assertEquals("Tiết 6–8", c2.getPeriodRangeLabel());
        assertEquals("Lịch bù", c2.getStatusBadgeLabel());
        assertEquals("B2.10", c2.getRoom());
    }

    @Test
    public void generateIcsContent_producesValidRfc5545WithRruleExdateAndMakeupEvent() {
        ClassScheduleItem sched = new ClassScheduleItem(
                "sched-10", "cls-10", "NT118.P11", "Phát triển ứng dụng trên thiết bị di động",
                2, "07:30:00", "11:30:00", "B3.10", "2026-09-07", "2026-12-31"
        );

        ScheduleExceptionItem cancelEx = new ScheduleExceptionItem(
                "ex-10", "cls-10", "NT118.P11", "Phát triển ứng dụng trên thiết bị di động",
                "sched-10", "2026-11-10", null,
                "CANCELLED", "PUBLISHED", null, null, null, "Nghỉ lễ", null
        );

        ScheduleExceptionItem makeupEx = new ScheduleExceptionItem(
                "ex-11", "cls-10", "NT118.P11", "Phát triển ứng dụng trên thiết bị di động",
                "sched-10", "2026-11-10", "2026-11-14",
                "MAKEUP", "PUBLISHED", "13:00:00", "16:15:00", "B4.02", "Học bù", "ex-10"
        );

        String ics = CalendarExportHelper.generateIcsContent(
                Collections.singletonList(sched),
                Arrays.asList(cancelEx, makeupEx)
        );

        assertNotNull(ics);
        assertTrue(ics.startsWith("BEGIN:VCALENDAR\r\n"));
        assertTrue(ics.endsWith("END:VCALENDAR\r\n"));
        // First Tuesday on or after 2026-09-07 (Monday) is 2026-09-08
        assertTrue(ics.contains("DTSTART;TZID=Asia/Ho_Chi_Minh:20260908T073000"));
        assertTrue(ics.contains("DTEND;TZID=Asia/Ho_Chi_Minh:20260908T113000"));
        assertTrue(ics.contains("RRULE:FREQ=WEEKLY;BYDAY=TU;UNTIL=20261231T235959Z"));
        assertTrue(ics.contains("EXDATE;TZID=Asia/Ho_Chi_Minh:20261110T073000"));
        assertTrue(ics.contains("SUMMARY:[NT118.P11] Phát triển ứng dụng trên thiết bị di động"));
        assertTrue(ics.contains("LOCATION:Phòng B3.10"));
        // Makeup VEVENT
        assertTrue(ics.contains("DTSTART;TZID=Asia/Ho_Chi_Minh:20261114T130000"));
        assertTrue(ics.contains("SUMMARY:[Học bù - NT118.P11] Phát triển ứng dụng trên thiết bị di động"));
        assertTrue(ics.contains("LOCATION:Phòng B4.02"));
        assertFalse(ics.isEmpty());
    }
}
