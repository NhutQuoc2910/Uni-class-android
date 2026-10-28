package com.ppquang06.uniclassapp.ui.common;

import static org.junit.Assert.assertEquals;

import com.ppquang06.uniclassapp.R;

import org.junit.Test;

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
}
