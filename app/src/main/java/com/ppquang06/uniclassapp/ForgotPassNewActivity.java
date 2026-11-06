package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.ppquang06.uniclassapp.databinding.ActivityForgotPassNewBinding;
import com.ppquang06.uniclassapp.ui.auth.AuthViewModel;
import com.ppquang06.uniclassapp.utils.AuthUiHelper;
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

        setupBackNavigation();
        setupPasswordToggles();
        setupLivePasswordValidation();
        setupErrorClearing();
        setupListeners();
        observeViewModel();
    }

    private void setupBackNavigation() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupPasswordToggles() {
        binding.ivToggleNewPassword.setOnClickListener(v -> {
            isNewPassVisible = !isNewPassVisible;
            if (isNewPassVisible) {
                binding.edtNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                binding.ivToggleNewPassword.setImageResource(R.drawable.ic_eye_off);
            } else {
                binding.edtNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                binding.ivToggleNewPassword.setImageResource(R.drawable.ic_eye);
            }
            binding.edtNewPassword.setSelection(binding.edtNewPassword.getText().length());
        });

        binding.ivToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPassVisible = !isConfirmPassVisible;
            if (isConfirmPassVisible) {
                binding.edtConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                binding.ivToggleConfirmPassword.setImageResource(R.drawable.ic_eye_off);
            } else {
                binding.edtConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                binding.ivToggleConfirmPassword.setImageResource(R.drawable.ic_eye);
            }
            binding.edtConfirmPassword.setSelection(binding.edtConfirmPassword.getText().length());
        });
    }

    private void setupLivePasswordValidation() {
        binding.edtNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSecurityChips(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateSecurityChips(CharSequence password) {
        setChipState(binding.chipRuleMinLength, AuthUiHelper.hasMinLength(password), getString(R.string.nordic_sec_rule_len));
        setChipState(binding.chipRuleUppercase, AuthUiHelper.hasUppercase(password), getString(R.string.nordic_sec_rule_upper));
        setChipState(binding.chipRuleLowercase, AuthUiHelper.hasLowercase(password), getString(R.string.nordic_sec_rule_lower));
        setChipState(binding.chipRuleDigit, AuthUiHelper.hasDigit(password), getString(R.string.nordic_sec_rule_num));
        setChipState(binding.chipRuleSpecial, AuthUiHelper.hasSpecialChar(password), getString(R.string.nordic_sec_rule_special));
    }

    private void setChipState(TextView chip, boolean isMet, String label) {
        if (isMet) {
            chip.setText("✓ " + label);
            chip.setBackgroundResource(R.drawable.bg_nordic_chip_security_active);
            chip.setTextColor(ContextCompat.getColor(this, R.color.nordic_frosted_silver));
        } else {
            chip.setText("○ " + label);
            chip.setBackgroundResource(R.drawable.bg_nordic_chip_security_idle);
            chip.setTextColor(ContextCompat.getColor(this, R.color.nordic_muted_steel));
        }
    }

    private void setupErrorClearing() {
        binding.edtNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tvErrorNewPassword.setVisibility(View.GONE);
                binding.containerNewPassword.setBackgroundResource(R.drawable.bg_nordic_input_selector);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.edtConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tvErrorConfirmPassword.setVisibility(View.GONE);
                binding.containerConfirmPassword.setBackgroundResource(R.drawable.bg_nordic_input_selector);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showError(View container, TextView errorTv, String message, EditText editText) {
        errorTv.setText(message);
        errorTv.setVisibility(View.VISIBLE);
        container.setBackgroundResource(R.drawable.bg_nordic_input_error);
        AuthUiHelper.shakeView(container);
        editText.requestFocus();
    }

    private void setupListeners() {
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
                Toast.makeText(ForgotPassNewActivity.this, "Đã cập nhật mật khẩu mới thành công!", Toast.LENGTH_SHORT).show();

                // Directly navigate to LoginActivity and prefill email as requested
                Intent intent = new Intent(ForgotPassNewActivity.this, LoginActivity.class);
                intent.putExtra(Constants.EXTRA_EMAIL, email);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (state.isError()) {
                setLoading(false);
                String errorMsg = state.getMessage() != null ? state.getMessage() : "Đặt lại mật khẩu thất bại";
                authViewModel.resetResetPasswordState();
                showError(binding.containerNewPassword, binding.tvErrorNewPassword, errorMsg, binding.edtNewPassword);
            }
        });
    }

    private void performResetPassword() {
        String newPassword = binding.edtNewPassword.getText() != null ? binding.edtNewPassword.getText().toString() : "";
        String confirmPassword = binding.edtConfirmPassword.getText() != null ? binding.edtConfirmPassword.getText().toString() : "";

        if (TextUtils.isEmpty(newPassword)) {
            showError(binding.containerNewPassword, binding.tvErrorNewPassword, "Vui lòng nhập mật khẩu mới", binding.edtNewPassword);
            return;
        }

        if (!AuthUiHelper.isPasswordValid(newPassword)) {
            showError(binding.containerNewPassword, binding.tvErrorNewPassword, "Mật khẩu chưa đáp ứng đủ 5 tiêu chuẩn bảo mật", binding.edtNewPassword);
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            showError(binding.containerConfirmPassword, binding.tvErrorConfirmPassword, "Vui lòng xác nhận lại mật khẩu", binding.edtConfirmPassword);
            return;
        }

        if (!AuthUiHelper.doPasswordsMatch(newPassword, confirmPassword)) {
            showError(binding.containerConfirmPassword, binding.tvErrorConfirmPassword, "Mật khẩu xác nhận không khớp", binding.edtConfirmPassword);
            return;
        }

        authViewModel.resetPassword(email, otp, newPassword);
    }

    private void setLoading(boolean isLoading) {
        binding.btnResetPassword.setEnabled(!isLoading);
        binding.edtNewPassword.setEnabled(!isLoading);
        binding.edtConfirmPassword.setEnabled(!isLoading);
        binding.btnBack.setEnabled(!isLoading);
        binding.tvBtnResetLabel.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        binding.ivBtnResetArrow.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
