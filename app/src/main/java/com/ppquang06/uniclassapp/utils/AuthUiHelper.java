package com.ppquang06.uniclassapp.utils;

import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;

import java.util.regex.Pattern;

public final class AuthUiHelper {

    private static final Pattern PATTERN_UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern PATTERN_LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern PATTERN_DIGIT = Pattern.compile(".*[0-9].*");
    private static final Pattern PATTERN_SPECIAL = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

    public static final long OTP_COOLDOWN_MS = 60_000L;

    private AuthUiHelper() {}

    public static boolean hasMinLength(CharSequence s) {
        return s != null && s.length() >= 8;
    }

    public static boolean hasUppercase(CharSequence s) {
        return s != null && PATTERN_UPPER.matcher(s).matches();
    }

    public static boolean hasLowercase(CharSequence s) {
        return s != null && PATTERN_LOWER.matcher(s).matches();
    }

    public static boolean hasDigit(CharSequence s) {
        return s != null && PATTERN_DIGIT.matcher(s).matches();
    }

    public static boolean hasSpecialChar(CharSequence s) {
        return s != null && PATTERN_SPECIAL.matcher(s).matches();
    }

    public static boolean isPasswordValid(CharSequence s) {
        return hasMinLength(s)
                && hasUppercase(s)
                && hasLowercase(s)
                && hasDigit(s)
                && hasSpecialChar(s);
    }

    public static boolean doPasswordsMatch(CharSequence pass, CharSequence confirmPass) {
        if (pass == null || confirmPass == null) return false;
        return pass.toString().equals(confirmPass.toString());
    }

    public static String extractSixDigits(CharSequence input) {
        if (input == null) return null;
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
                if (digits.length() == 6) {
                    return digits.toString();
                }
            }
        }
        return digits.length() == 6 ? digits.toString() : null;
    }

    public static boolean shouldReuseActiveOtpCooldown(String currentEmail, String lastSentEmail, long nowMs, long lastSentAtMs, long cooldownDurationMs) {
        if (currentEmail == null || lastSentEmail == null) return false;
        String cleanCurrent = currentEmail.trim().toLowerCase();
        String cleanLast = lastSentEmail.trim().toLowerCase();
        if (!cleanCurrent.equals(cleanLast)) return false;

        long elapsed = nowMs - lastSentAtMs;
        return elapsed >= 0 && elapsed < cooldownDurationMs;
    }

    public static long getRemainingCooldownSeconds(long nowMs, long lastSentAtMs, long cooldownDurationMs) {
        long elapsed = nowMs - lastSentAtMs;
        if (elapsed < 0 || elapsed >= cooldownDurationMs) {
            return 0;
        }
        return (cooldownDurationMs - elapsed + 999) / 1000;
    }

    public static void shakeView(View view) {
        if (view == null) return;
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(300);
        shake.setInterpolator(new CycleInterpolator(4));
        view.startAnimation(shake);
    }
}
