package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ppquang06.uniclassapp.databinding.ActivityForgotPasswordBinding;
import com.ppquang06.uniclassapp.ui.auth.AuthViewModel;
import com.ppquang06.uniclassapp.utils.Constants;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private AuthViewModel authViewModel;
    private String pendingEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.tvReturnToLogin.setOnClickListener(v -> finish());

        binding.btnSendOTP.setOnClickListener(v -> performSendOtp());
    }

    private void observeViewModel() {
        authViewModel.getForgotPasswordState().observe(this, state -> {
            if (state == null || state.isIdle()) {
                return;
            }
            if (state.isLoading()) {
                setLoading(true);
            } else if (state.isSuccess()) {
                setLoading(false);
                authViewModel.resetForgotPasswordState();
                Toast.makeText(ForgotPasswordActivity.this, "Mã OTP đã được gửi đến email của bạn!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ForgotPasswordActivity.this, ForgotPassOTPActivity.class);
                intent.putExtra(Constants.EXTRA_EMAIL, pendingEmail);
                startActivity(intent);
            } else if (state.isError()) {
                setLoading(false);
                String errorMsg = state.getMessage() != null ? state.getMessage() : "Gửi mã OTP thất bại";
                authViewModel.resetForgotPasswordState();
                Toast.makeText(ForgotPasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performSendOtp() {
        String email = binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            binding.edtEmail.setError("Vui lòng nhập địa chỉ email");
            binding.edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.setError("Địa chỉ email không hợp lệ");
            binding.edtEmail.requestFocus();
            return;
        }

        pendingEmail = email;
        authViewModel.forgotPassword(email);
    }

    private void setLoading(boolean isLoading) {
        binding.btnSendOTP.setEnabled(!isLoading);
        binding.btnSendOTP.setText(isLoading ? "Đang gửi OTP..." : getString(R.string.send_otp_button));
    }
}
