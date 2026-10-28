package com.ppquang06.uniclassapp.ui.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.ppquang06.uniclassapp.R;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class UiBadgeHelper {

    private static final long DUE_SOON_THRESHOLD_MILLIS = 72L * 3600_000L;

    public enum Status {
        PRESENT,
        ABSENT,
        SUBMITTED,
        OPEN,
        DUE_SOON,
        OVERDUE
    }

    public static final class BadgeColors {
        @ColorRes public final int textColorRes;
        @ColorRes public final int bgColorRes;

        public BadgeColors(@ColorRes int textColorRes, @ColorRes int bgColorRes) {
            this.textColorRes = textColorRes;
            this.bgColorRes = bgColorRes;
        }
    }

    private UiBadgeHelper() {}

    public static BadgeColors resolveColors(Status status) {
        if (status == null) {
            return new BadgeColors(R.color.color_primary, R.color.color_primary_container);
        }
        switch (status) {
            case PRESENT:
            case SUBMITTED:
                return new BadgeColors(R.color.color_success, R.color.color_success_container);
            case ABSENT:
                return new BadgeColors(R.color.color_absence, R.color.color_absence_container);
            case DUE_SOON:
                return new BadgeColors(R.color.color_warning, R.color.color_warning_container);
            case OVERDUE:
                return new BadgeColors(R.color.color_error, R.color.color_error_container);
            case OPEN:
            default:
                return new BadgeColors(R.color.color_primary, R.color.color_primary_container);
        }
    }

    public static void applyBadge(TextView badgeView, Status status, String label) {
        if (badgeView == null) return;
        Context context = badgeView.getContext();
        BadgeColors colors = resolveColors(status);
        badgeView.setText(label);
        badgeView.setTextColor(ContextCompat.getColor(context, colors.textColorRes));
        badgeView.setBackgroundResource(R.drawable.bg_badge_pill);
        ViewCompat.setBackgroundTintList(
                badgeView,
                ColorStateList.valueOf(ContextCompat.getColor(context, colors.bgColorRes))
        );
    }

    @SuppressLint("NewApi")
    public static Status resolveAssignmentStatus(boolean submitted, String isoDueDate, long nowEpochMillis) {
        if (submitted) {
            return Status.SUBMITTED;
        }
        if (isoDueDate == null || isoDueDate.trim().isEmpty()) {
            return Status.OPEN;
        }
        try {
            Instant dueInstant = Instant.parse(isoDueDate.trim());
            long diffMillis = dueInstant.toEpochMilli() - nowEpochMillis;
            if (diffMillis < 0) {
                return Status.OVERDUE;
            }
            if (diffMillis <= DUE_SOON_THRESHOLD_MILLIS) {
                return Status.DUE_SOON;
            }
            return Status.OPEN;
        } catch (Exception e) {
            return Status.OPEN;
        }
    }

    public static Status resolveAssignmentBadgeFromIso(boolean submitted, String isoDueDate, long nowEpochMillis) {
        return resolveAssignmentStatus(submitted, isoDueDate, nowEpochMillis);
    }

    public static Status resolveAssignmentBadge(boolean submitted, String isoDueDate, long nowEpochMillis) {
        return resolveAssignmentStatus(submitted, isoDueDate, nowEpochMillis);
    }

    public static String getAssignmentStatusLabel(Status status) {
        if (status == null) {
            return "Còn hạn";
        }
        switch (status) {
            case SUBMITTED:
                return "Đã nộp";
            case DUE_SOON:
                return "Sắp hết hạn";
            case OVERDUE:
                return "Quá hạn";
            case OPEN:
            default:
                return "Còn hạn";
        }
    }

    @SuppressLint("NewApi")
    public static String formatIsoDateShort(String isoDate) {
        if (isoDate == null) {
            return "";
        }
        String trimmed = isoDate.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        try {
            Instant instant = Instant.parse(trimmed);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm", Locale.getDefault())
                    .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
            return formatter.format(instant);
        } catch (Exception e) {
            return isoDate;
        }
    }

    public static Status resolveGradeStatus(Double score) {
        if (score == null) {
            return Status.DUE_SOON;
        }
        if (score >= 8.0) {
            return Status.PRESENT;
        }
        if (score >= 5.0) {
            return Status.OPEN;
        }
        return Status.OVERDUE;
    }

    public static String resolveGradeLabel(Double score) {
        if (score == null) {
            return "Chưa chấm";
        }
        return String.format(Locale.getDefault(), "%.1f / 10", score);
    }

    public static void applyGradeBadge(TextView badgeView, Double score) {
        applyBadge(badgeView, resolveGradeStatus(score), resolveGradeLabel(score));
    }
}
