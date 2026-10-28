package com.ppquang06.uniclassapp.ui.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.ppquang06.uniclassapp.R;

public final class UiBadgeHelper {

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
}
