package com.ppquang06.uniclassapp.utils;

import android.app.Activity;
import android.content.Intent;

import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.ui.lecturer.LecturerMainActivity;
import com.ppquang06.uniclassapp.ui.student.StudentMainActivity;

public final class RoleRouter {

    private RoleRouter() {
        // Utility class
    }

    public static boolean isLecturerRole(String role) {
        if (role == null) {
            return false;
        }
        String normalized = role.trim().toUpperCase();
        return "LECTURER".equals(normalized) || "ROLE_LECTURER".equals(normalized);
    }

    public static Class<? extends Activity> resolveTargetActivity(String role) {
        if (isLecturerRole(role)) {
            return LecturerMainActivity.class;
        }
        return StudentMainActivity.class;
    }

    public static void navigateToHomeByRole(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        UserResponse user = SessionManager.getInstance(activity).getUser();
        String role = user != null ? user.getRole() : null;
        Class<? extends Activity> target = resolveTargetActivity(role);

        Intent intent = new Intent(activity, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
