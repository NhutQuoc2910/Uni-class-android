package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ppquang06.uniclassapp.databinding.ActivityForgotPassNewBinding;
import com.ppquang06.uniclassapp.ui.auth.AuthViewModel;
import com.ppquang06.uniclassapp.utils.Constants;

public class ForgotPassNewActivity extends AppCompatActivity {
    private ActivityForgotPassNewBinding binding;
    private AuthViewModel authViewModel;
    private String email = "";
    private String otp = "";
    private boolean isNewPassVisible = false;
    private boolean isConfirmPassVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPassNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        email = getIntent().getStringExtra(Constants.EXTRA_EMAIL);
        otp = getIntent().getStringExtra(Constants.EXTRA_OTP);
        if (email == null) email = "";
        if (otp == null) otp = "";

        setupPasswordToggles();
        setupListeners();
        observeViewModel();
    }

    private void setupPasswordToggles() {
        binding.ivToggleNewPassword.setOnClickListener(v -> {
            isNewPassVisible = !isNewPassVisible;
            if (isNewPassVisible) {
                binding.edtNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                binding.edtNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            binding.edtNewPassword.setSelection(binding.edtNewPassword.getText().length());
        });

        binding.ivToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPassVisible = !isConfirmPassVisible;
            if (isConfirmPassVisible) {
                binding.edtConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                binding.edtConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            binding.edtConfirmPassword.setSelection(binding.edtConfirmPassword.getText().length());
        });
    }

    private void setupListeners() {
        binding.tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPassNewActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        binding.btnResetPassword.setOnClickListener(v -> performResetPassword());
    }

    private void observeViewModel() {
        authViewModel.getResetPasswordState().observe(this, state -> {
            if (state == null || state.isIdle()) {
                return;
            }
            if (state.isLoading()) {
                setLoading(true);
            } else if (state.isSuccess()) {
                setLoading(false);
                authViewModel.resetResetPasswordState();
                Toast.makeText(ForgotPassNewActivity.this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ForgotPassNewActivity.this, PassChangedSuccessActivity.class);
                startActivity(intent);
                finish();
            } else if (state.isError()) {
                setLoading(false);
                String errorMsg = state.getMessage() != null ? state.getMessage() : "Đặt lại mật khẩu thất bại";
                authViewModel.resetResetPasswordState();
                Toast.makeText(ForgotPassNewActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performResetPassword() {
        String newPassword = binding.edtNewPassword.getText() != null ? binding.edtNewPassword.getText().toString() : "";
        String confirmPassword = binding.edtConfirmPassword.getText() != null ? binding.edtConfirmPassword.getText().toString() : "";

        if (TextUtils.isEmpty(newPassword) || newPassword.length() < 8) {
            binding.edtNewPassword.setError("Mật khẩu mới phải từ 8 ký tự trở lên");
            binding.edtNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            binding.edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            binding.edtConfirmPassword.requestFocus();
            return;
        }

        authViewModel.resetPassword(email, otp, newPassword);
    }

    private void setLoading(boolean isLoading) {
        binding.btnResetPassword.setEnabled(!isLoading);
        binding.btnResetPassword.setText(isLoading ? "Đang đổi mật khẩu..." : getString(R.string.change_password_button));
    }
}
