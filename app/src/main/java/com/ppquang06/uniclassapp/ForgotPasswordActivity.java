package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ppquang06.uniclassapp.databinding.ActivityForgotPasswordBinding;
import com.ppquang06.uniclassapp.ui.auth.AuthViewModel;
import com.ppquang06.uniclassapp.utils.AuthUiHelper;
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

        setupErrorClearing();
        setupListeners();
        observeViewModel();
    }

    private void setupErrorClearing() {
        binding.edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tvErrorEmail.setVisibility(View.GONE);
                binding.containerEmail.setBackgroundResource(R.drawable.bg_nordic_input_selector);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvBackToLogin.setOnClickListener(v -> finish());
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
                showEmailError(errorMsg);
            }
        });
    }

    private void performSendOtp() {
        String email = binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            showEmailError("Vui lòng nhập địa chỉ email");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showEmailError("Địa chỉ email không hợp lệ");
            return;
        }

        binding.tvErrorEmail.setVisibility(View.GONE);
        binding.containerEmail.setBackgroundResource(R.drawable.bg_nordic_input_selector);
        pendingEmail = email;
        authViewModel.forgotPassword(email);
    }

    private void showEmailError(String msg) {
        binding.tvErrorEmail.setText(msg);
        binding.tvErrorEmail.setVisibility(View.VISIBLE);
        binding.containerEmail.setBackgroundResource(R.drawable.bg_nordic_input_error);
        AuthUiHelper.shakeView(binding.containerEmail);
        binding.edtEmail.requestFocus();
    }

    private void setLoading(boolean isLoading) {
        binding.btnSendOTP.setEnabled(!isLoading);
        binding.edtEmail.setEnabled(!isLoading);
        binding.btnBack.setEnabled(!isLoading);
        binding.tvBtnSendOtpLabel.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        binding.ivBtnSendOtpArrow.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
