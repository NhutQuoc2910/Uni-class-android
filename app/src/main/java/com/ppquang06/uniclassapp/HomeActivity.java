package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ppquang06.uniclassapp.data.model.response.HomeSummaryResponse;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.databinding.ActivityHomeBinding;
import com.ppquang06.uniclassapp.ui.common.HomeViewModel;
import com.ppquang06.uniclassapp.utils.SessionManager;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private HomeViewModel homeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.getInstance(this).isLoggedIn()) {
            redirectToLogin();
            return;
        }

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        bindUserData();
        setupListeners();
        observeViewModel();
        loadHomeSummary();
    }

    private void bindUserData() {
        UserResponse user = SessionManager.getInstance(this).getUser();
        if (user != null) {
            String name = !TextUtils.isEmpty(user.getFullName()) ? user.getFullName() : "Người dùng";
            binding.tvGreetingUser.setText("Xin chào, " + name + " 👋");

            String roleDisplay = "Sinh viên";
            if ("LECTURER".equalsIgnoreCase(user.getRole())) {
                roleDisplay = "Giảng viên";
            } else if ("ADMIN".equalsIgnoreCase(user.getRole()) || "ROLE_ADMIN".equalsIgnoreCase(user.getRole())) {
                roleDisplay = "Quản trị viên";
            }
            binding.tvGreetingSub.setText("Vai trò: " + roleDisplay);
        }
    }

    private void observeViewModel() {
        homeViewModel.getHomeSummaryState().observe(this, state -> {
            if (state == null || state.isIdle()) {
                return;
            }
            HomeSummaryResponse summary = state.getData();
            if (summary != null) {
                binding.tvCoursesCount.setText(String.valueOf(summary.getActiveCoursesCount()));
                binding.tvAssignmentsCount.setText(String.valueOf(summary.getPendingAssignmentsCount()));
            }
            if (state.isError() && summary == null) {
                Toast.makeText(HomeActivity.this, "Không thể tải dữ liệu trang chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHomeSummary() {
        if (homeViewModel != null) {
            homeViewModel.loadHomeSummary();
        }
    }

    private void setupListeners() {
        binding.btnProfile.setOnClickListener(v -> showProfileDialog());
        binding.navItemProfile.setOnClickListener(v -> showProfileDialog());
    }

    private void showProfileDialog() {
        UserResponse user = SessionManager.getInstance(this).getUser();
        String name = user != null && user.getFullName() != null ? user.getFullName() : "N/A";
        String email = user != null && user.getEmail() != null ? user.getEmail() : "N/A";
        String role = user != null && user.getRole() != null ? user.getRole() : "STUDENT";

        String message = "Họ và tên: " + name + "\n"
                + "Email: " + email + "\n"
                + "Vai trò: " + role;

        new AlertDialog.Builder(this)
                .setTitle("Thông tin tài khoản")
                .setMessage(message)
                .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void performLogout() {
        SessionManager.getInstance(this).clearSession();
        Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
