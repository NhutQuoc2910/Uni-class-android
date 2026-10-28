package com.ppquang06.uniclassapp.ui.common;

import static org.junit.Assert.assertEquals;

import com.ppquang06.uniclassapp.R;

import org.junit.Test;

import java.time.Instant;

public class UiBadgeHelperTest {

    @Test
    public void resolveStyle_returnsSuccessColorsForPresentAndSubmitted() {
        UiBadgeHelper.BadgeColors present = UiBadgeHelper.resolveColors(UiBadgeHelper.Status.PRESENT);
        assertEquals(R.color.color_success, present.textColorRes);
        assertEquals(R.color.color_success_container, present.bgColorRes);

        UiBadgeHelper.BadgeColors submitted = UiBadgeHelper.resolveColors(UiBadgeHelper.Status.SUBMITTED);
        assertEquals(R.color.color_success, submitted.textColorRes);
        assertEquals(R.color.color_success_container, submitted.bgColorRes);
    }

    @Test
    public void resolveStyle_returnsAbsenceColorsForAbsent() {
        UiBadgeHelper.BadgeColors absent = UiBadgeHelper.resolveColors(UiBadgeHelper.Status.ABSENT);
        assertEquals(R.color.color_absence, absent.textColorRes);
        assertEquals(R.color.color_absence_container, absent.bgColorRes);
    }

    @Test
    public void resolveStyle_returnsWarningColorsForDueSoon() {
        UiBadgeHelper.BadgeColors dueSoon = UiBadgeHelper.resolveColors(UiBadgeHelper.Status.DUE_SOON);
        assertEquals(R.color.color_warning, dueSoon.textColorRes);
        assertEquals(R.color.color_warning_container, dueSoon.bgColorRes);
    }

    @Test
    public void resolveStyle_returnsErrorColorsForOverdue() {
        UiBadgeHelper.BadgeColors overdue = UiBadgeHelper.resolveColors(UiBadgeHelper.Status.OVERDUE);
        assertEquals(R.color.color_error, overdue.textColorRes);
        assertEquals(R.color.color_error_container, overdue.bgColorRes);
    }

    @Test
    public void resolveColors_returnsPrimaryColorsForNullAndOpen() {
        UiBadgeHelper.BadgeColors nullStatus = UiBadgeHelper.resolveColors(null);
        assertEquals(R.color.color_primary, nullStatus.textColorRes);
        assertEquals(R.color.color_primary_container, nullStatus.bgColorRes);

        UiBadgeHelper.BadgeColors open = UiBadgeHelper.resolveColors(UiBadgeHelper.Status.OPEN);
        assertEquals(R.color.color_primary, open.textColorRes);
        assertEquals(R.color.color_primary_container, open.bgColorRes);
    }

    @Test
    public void resolveAssignmentStatus_returnsCorrectStatusBySubmissionAndDeadline() {
        long now = Instant.parse("2026-10-28T10:00:00Z").toEpochMilli();

        // Already submitted always returns SUBMITTED even if overdue
        assertEquals(
                UiBadgeHelper.Status.SUBMITTED,
                UiBadgeHelper.resolveAssignmentStatus(true, "2026-10-20T10:00:00Z", now)
        );

        // Past due date -> OVERDUE
        assertEquals(
                UiBadgeHelper.Status.OVERDUE,
                UiBadgeHelper.resolveAssignmentStatus(false, "2026-10-28T09:59:59Z", now)
        );

        // Within 72 hours -> DUE_SOON
        assertEquals(
                UiBadgeHelper.Status.DUE_SOON,
                UiBadgeHelper.resolveAssignmentStatus(false, "2026-10-30T10:00:00Z", now)
        );

        // More than 72 hours -> OPEN
        assertEquals(
                UiBadgeHelper.Status.OPEN,
                UiBadgeHelper.resolveAssignmentStatus(false, "2026-11-05T10:00:00Z", now)
        );

        // Null or invalid ISO date -> OPEN
        assertEquals(
                UiBadgeHelper.Status.OPEN,
                UiBadgeHelper.resolveAssignmentStatus(false, null, now)
        );
        assertEquals(
                UiBadgeHelper.Status.OPEN,
                UiBadgeHelper.resolveAssignmentStatus(false, "invalid-date", now)
        );
    }

    @Test
    public void getAssignmentStatusLabel_returnsVietnameseLabels() {
        assertEquals("Đã nộp", UiBadgeHelper.getAssignmentStatusLabel(UiBadgeHelper.Status.SUBMITTED));
        assertEquals("Sắp hết hạn", UiBadgeHelper.getAssignmentStatusLabel(UiBadgeHelper.Status.DUE_SOON));
        assertEquals("Quá hạn", UiBadgeHelper.getAssignmentStatusLabel(UiBadgeHelper.Status.OVERDUE));
        assertEquals("Còn hạn", UiBadgeHelper.getAssignmentStatusLabel(UiBadgeHelper.Status.OPEN));
        assertEquals("Còn hạn", UiBadgeHelper.getAssignmentStatusLabel(null));
    }

    @Test
    public void formatIsoDateShort_formatsToHoChiMinhTimezoneOrFallback() {
        // 2026-10-28T08:30:00Z in Asia/Ho_Chi_Minh (+07:00) is 28/10/2026 · 15:30
        assertEquals("28/10/2026 · 15:30", UiBadgeHelper.formatIsoDateShort("2026-10-28T08:30:00Z"));
        assertEquals("12/10/2026", UiBadgeHelper.formatIsoDateShort("12/10/2026"));
        assertEquals("", UiBadgeHelper.formatIsoDateShort(null));
    }
}
