package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.ppquang06.uniclassapp.data.model.request.RegisterRequest;
import com.ppquang06.uniclassapp.databinding.ActivityRegisterBinding;
import com.ppquang06.uniclassapp.ui.auth.AuthViewModel;
import com.ppquang06.uniclassapp.utils.AuthUiHelper;

public class RegisterActivity extends AppCompatActivity {
    private static final int STEP_ROLE = 1;
    private static final int STEP_FORM = 2;

    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;
    private int currentStep = STEP_ROLE;
    private String selectedRole = "STUDENT";
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    // Cache to prevent 60-second cooldown block if user taps "Sửa" on OTP screen and returns
    private String lastSentEmail = null;
    private long lastOtpSentTimestampMs = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View root = binding.getRoot();

        int initialLeft = root.getPaddingLeft();
        int initialTop = root.getPaddingTop();
        int initialRight = root.getPaddingRight();
        int initialBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            int bottom = Math.max(systemBars.bottom, ime.bottom);

            view.setPadding(
                    initialLeft + systemBars.left,
                    initialTop + systemBars.top,
                    initialRight + systemBars.right,
                    initialBottom + bottom
            );

            return insets;
        });

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupBackNavigation();
        setupRoleSelection();
        setupStepNavigation();
        setupPasswordToggles();
        setupLivePasswordValidation();
        setupErrorClearing();
        observeViewModel();

        showStep(STEP_ROLE);
    }

    private void setupBackNavigation() {
        binding.btnBack.setOnClickListener(v -> handleBackAction());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackAction();
            }
        });
    }

    private void handleBackAction() {
        if (currentStep == STEP_FORM) {
            showStep(STEP_ROLE);
        } else {
            finish();
        }
    }

    private void showStep(int step) {
        currentStep = step;
        if (step == STEP_ROLE) {
            binding.layoutStep1Role.setVisibility(View.VISIBLE);
            binding.layoutStep2Form.setVisibility(View.GONE);
            binding.tvStepTitle.setText(R.string.register_title);
            binding.tvStepBadge.setText(R.string.nordic_reg_step1_badge);
            binding.viewSegment1.setBackgroundResource(R.drawable.bg_nordic_segment_active);
            binding.viewSegment2.setBackgroundResource(R.drawable.bg_nordic_segment_idle);
        } else {
            binding.layoutStep1Role.setVisibility(View.GONE);
            binding.layoutStep2Form.setVisibility(View.VISIBLE);
            binding.tvStepTitle.setText(R.string.nordic_reg_step2_title);
            binding.tvStepBadge.setText(R.string.nordic_reg_step2_badge);
            binding.viewSegment1.setBackgroundResource(R.drawable.bg_nordic_segment_active);
            binding.viewSegment2.setBackgroundResource(R.drawable.bg_nordic_segment_active);
            binding.tvSelectedRolePill.setText("STUDENT".equals(selectedRole) ? "🎓 Sinh viên" : "👨‍🏫 Giảng viên");
        }
    }

    private void setupStepNavigation() {
        binding.btnContinueRole.setOnClickListener(v -> showStep(STEP_FORM));
        binding.btnChangeRole.setOnClickListener(v -> showStep(STEP_ROLE));
        binding.tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
        binding.btnRegister.setOnClickListener(v -> performRegister());
    }

    private void setupRoleSelection() {
        binding.cardStudent.setOnClickListener(v -> selectRole("STUDENT"));
        binding.cardLecturer.setOnClickListener(v -> selectRole("LECTURER"));
        selectRole("STUDENT");
    }

    private void selectRole(String role) {
        selectedRole = role;
        boolean isStudent = "STUDENT".equals(role);

        if (isStudent) {
            binding.cardStudent.setBackgroundResource(R.drawable.bg_nordic_role_card_selected);
            binding.flStudentIcon.setBackgroundResource(R.drawable.bg_nordic_secondary_btn);
            binding.ivStudentRadio.setImageResource(R.drawable.ic_check_circle);
            binding.ivStudentRadio.setColorFilter(ContextCompat.getColor(this, R.color.nordic_midnight_slate));

            binding.cardLecturer.setBackgroundResource(R.drawable.bg_nordic_role_card_idle);
            binding.flLecturerIcon.setBackgroundResource(R.drawable.bg_nordic_feature_chip);
            binding.ivLecturerRadio.setImageResource(R.drawable.bg_step_circle_inactive);
            binding.ivLecturerRadio.clearColorFilter();

            binding.tvBtnContinueRoleLabel.setText(R.string.nordic_reg_step1_continue_student);
        } else {
            binding.cardLecturer.setBackgroundResource(R.drawable.bg_nordic_role_card_selected);
            binding.flLecturerIcon.setBackgroundResource(R.drawable.bg_nordic_secondary_btn);
            binding.ivLecturerRadio.setImageResource(R.drawable.ic_check_circle);
            binding.ivLecturerRadio.setColorFilter(ContextCompat.getColor(this, R.color.nordic_midnight_slate));

            binding.cardStudent.setBackgroundResource(R.drawable.bg_nordic_role_card_idle);
            binding.flStudentIcon.setBackgroundResource(R.drawable.bg_nordic_feature_chip);
            binding.ivStudentRadio.setImageResource(R.drawable.bg_step_circle_inactive);
            binding.ivStudentRadio.clearColorFilter();

            binding.tvBtnContinueRoleLabel.setText(R.string.nordic_reg_step1_continue_lecturer);
        }
    }

    private void setupPasswordToggles() {
        binding.ivTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                binding.edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
            } else {
                binding.edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye);
            }
            binding.edtPassword.setSelection(binding.edtPassword.getText().length());
        });

        binding.ivToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            if (isConfirmPasswordVisible) {
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
        binding.edtPassword.addTextChangedListener(new TextWatcher() {
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
        clearOnType(binding.edtFullName, binding.containerFullName, binding.tvErrorFullName);
        clearOnType(binding.edtEmail, binding.containerEmail, binding.tvErrorEmail);
        clearOnType(binding.edtPhone, binding.containerPhone, binding.tvErrorPhone);
        clearOnType(binding.edtPassword, binding.containerPassword, binding.tvErrorPassword);
        clearOnType(binding.edtConfirmPassword, binding.containerConfirmPassword, binding.tvErrorConfirmPassword);
    }

    private void clearOnType(EditText editText, View container, TextView errorTv) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                errorTv.setVisibility(View.GONE);
                container.setBackgroundResource(R.drawable.bg_nordic_input_selector);
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

    private void observeViewModel() {
        authViewModel.getSendOtpState().observe(this, state -> {
            if (state == null || state.isIdle()) {
                return;
            }
            if (state.isLoading()) {
                setLoading(true);
            } else if (state.isSuccess()) {
                setLoading(false);
                authViewModel.resetSendOtpState();

                String email = binding.edtEmail.getText().toString().trim().toLowerCase();
                lastSentEmail = email;
                lastOtpSentTimestampMs = System.currentTimeMillis();

                Toast.makeText(RegisterActivity.this, "Mã OTP đã được gửi đến email của bạn!", Toast.LENGTH_SHORT).show();
                navigateToVerifyOtp(email, AuthUiHelper.OTP_COOLDOWN_MS / 1000);
            } else if (state.isError()) {
                setLoading(false);
                String errorMsg = state.getMessage() != null ? state.getMessage() : "Gửi mã OTP thất bại";
                authViewModel.resetSendOtpState();
                showError(binding.containerEmail, binding.tvErrorEmail, errorMsg, binding.edtEmail);
            }
        });
    }

    private void performRegister() {
        String fullName = binding.edtFullName.getText() != null ? binding.edtFullName.getText().toString().trim() : "";
        String email = binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString().trim() : "";
        String phone = binding.edtPhone.getText() != null ? binding.edtPhone.getText().toString().trim() : "";
        String password = binding.edtPassword.getText() != null ? binding.edtPassword.getText().toString() : "";
        String confirmPassword = binding.edtConfirmPassword.getText() != null ? binding.edtConfirmPassword.getText().toString() : "";

        if (TextUtils.isEmpty(fullName)) {
            showError(binding.containerFullName, binding.tvErrorFullName, "Vui lòng nhập họ và tên", binding.edtFullName);
            return;
        }

        if (TextUtils.isEmpty(email)) {
            showError(binding.containerEmail, binding.tvErrorEmail, "Vui lòng nhập email", binding.edtEmail);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(binding.containerEmail, binding.tvErrorEmail, "Email không hợp lệ", binding.edtEmail);
            return;
        }

        if (!TextUtils.isEmpty(phone) && !phone.matches("^\\+?[0-9]{9,15}$")) {
            showError(binding.containerPhone, binding.tvErrorPhone, "Số điện thoại không hợp lệ", binding.edtPhone);
            return;
        }

        if (!AuthUiHelper.isPasswordValid(password)) {
            showError(binding.containerPassword, binding.tvErrorPassword, "Mật khẩu chưa đáp ứng đủ 5 tiêu chuẩn bảo mật", binding.edtPassword);
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            showError(binding.containerConfirmPassword, binding.tvErrorConfirmPassword, "Vui lòng xác nhận mật khẩu", binding.edtConfirmPassword);
            return;
        }

        if (!AuthUiHelper.doPasswordsMatch(password, confirmPassword)) {
            showError(binding.containerConfirmPassword, binding.tvErrorConfirmPassword, "Mật khẩu xác nhận không khớp", binding.edtConfirmPassword);
            return;
        }

        String roleToSend = "LECTURER".equalsIgnoreCase(selectedRole) ? "ROLE_LECTURER" : "ROLE_STUDENT";

        // Check if we should reuse active OTP session (e.g. user pressed "Sửa" on OTP screen and returned without changing email)
        long now = System.currentTimeMillis();
        if (AuthUiHelper.shouldReuseActiveOtpCooldown(email, lastSentEmail, now, lastOtpSentTimestampMs, AuthUiHelper.OTP_COOLDOWN_MS)) {
            long remainingSec = AuthUiHelper.getRemainingCooldownSeconds(now, lastOtpSentTimestampMs, AuthUiHelper.OTP_COOLDOWN_MS);
            navigateToVerifyOtp(email, remainingSec);
            return;
        }

        RegisterRequest request = new RegisterRequest(email, password, fullName, TextUtils.isEmpty(phone) ? null : phone, roleToSend);
        authViewModel.sendOtp(request);
    }

    private void navigateToVerifyOtp(String email, long remainingCooldownSeconds) {
        Intent intent = new Intent(RegisterActivity.this, VerifyOtpActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("fullName", binding.edtFullName.getText().toString().trim());
        intent.putExtra("phone", binding.edtPhone.getText().toString().trim());
        intent.putExtra("password", binding.edtPassword.getText().toString());
        intent.putExtra("role", "LECTURER".equalsIgnoreCase(selectedRole) ? "ROLE_LECTURER" : "ROLE_STUDENT");
        intent.putExtra("remaining_cooldown", remainingCooldownSeconds);
        startActivity(intent);
    }

    private void setLoading(boolean isLoading) {
        binding.btnRegister.setEnabled(!isLoading);
        binding.btnBack.setEnabled(!isLoading);
        binding.btnChangeRole.setEnabled(!isLoading);
        binding.tvBtnRegisterLabel.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        binding.ivBtnRegisterArrow.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
