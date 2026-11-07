package com.ppquang06.uniclassapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Quản lý trạng thái giao diện Sáng (Warm Light #DEDCDC) / Tối (Midnight Dark #06141B)
 * theo chuẩn 12-Color Unified Design System.
 */
public class ThemeHelper {

    public static final String PREFS_NAME = "uniclass_theme_prefs";
    public static final String KEY_DARK_MODE = "dark_mode_enabled";

    /**
     * Kiểm tra xem chế độ Dark Mode hiện tại có đang bật hay không.
     * Mặc định là false (Chế độ Nordic / Warm Light #DEDCDC).
     */
    public static boolean isDarkMode(Context context) {
        if (context == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    /**
     * Bật hoặc tắt chế độ Dark Mode và kích hoạt tức thì AppCompatDelegate.
     */
    public static void setDarkMode(Context context, boolean enabled) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
        applyNightMode(enabled);
    }

    /**
     * Áp dụng chế độ Night Mode cho toàn ứng dụng.
     */
    public static void applyNightMode(boolean enabled) {
        AppCompatDelegate.setDefaultNightMode(
                enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    /**
     * Đọc theme đã lưu từ SharedPreferences và áp dụng khi Activity/App khởi tạo.
     */
    public static void applySavedTheme(Context context) {
        boolean isDark = isDarkMode(context);
        applyNightMode(isDark);
    }
}
