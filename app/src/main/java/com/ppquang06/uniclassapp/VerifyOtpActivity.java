package com.ppquang06.uniclassapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.ppquang06.uniclassapp.data.model.request.RegisterRequest;
import com.ppquang06.uniclassapp.data.model.request.VerifyOtpRequest;
import com.ppquang06.uniclassapp.data.model.response.AuthResponse;
import com.ppquang06.uniclassapp.databinding.ActivityRegisterOtpBinding;
import com.ppquang06.uniclassapp.ui.auth.AuthViewModel;
import com.ppquang06.uniclassapp.utils.AuthUiHelper;
import com.ppquang06.uniclassapp.utils.FcmTokenHelper;
import com.ppquang06.uniclassapp.utils.RoleRouter;
import com.ppquang06.uniclassapp.utils.SessionManager;

public class VerifyOtpActivity extends AppCompatActivity {

    private ActivityRegisterOtpBinding binding;
    private AuthViewModel authViewModel;

    private EditText[] otpInputs;

    private String email;
    private String fullName;
    private String phone;
    private String password;
    private String role;

    private CountDownTimer resendTimer;
    private boolean isVerifying = false;
    private boolean isResending = false;
    private boolean isAutoFilling = false;

    private static final long RESEND_INTERVAL = 60_000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        getRegistrationData();
        setupWindowInsets();
        setupOtpInputs();
        setupClickListeners();
        observeViewModel();

        long remainingSec = getIntent().getLongExtra("remaining_cooldown", 60);
        startResendCountdown(Math.max(1, remainingSec) * 1000L);
    }

    private void getRegistrationData() {
        email = getIntent().getStringExtra("email");
        fullName = getIntent().getStringExtra("fullName");
        phone = getIntent().getStringExtra("phone");
        password = getIntent().getStringExtra("password");
        role = getIntent().getStringExtra("role");

        if (email == null || email.trim().isEmpty()
                || fullName == null || password == null || role == null) {
            Toast.makeText(this, "Thiếu thông tin đăng ký. Vui lòng đăng ký lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        binding.tvRecipientEmail.setText(email);
    }

    private void setupWindowInsets() {
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
    }

    private void setupOtpInputs() {
        otpInputs = new EditText[]{
                binding.etOtp1,
                binding.etOtp2,
                binding.etOtp3,
                binding.etOtp4,
                binding.etOtp5,
                binding.etOtp6
        };

        for (int i = 0; i < otpInputs.length; i++) {
            final int index = i;

            otpInputs[i].setOnFocusChangeListener((v, hasFocus) -> updateBoxBackground(index, hasFocus));

            otpInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isAutoFilling) return;

                    // Support paste: if user pasted multiple chars
                    if (s != null && s.length() > 1) {
                        String sixDigits = AuthUiHelper.extractSixDigits(s);
                        if (sixDigits != null) {
                            fillOtpCode(sixDigits);
                            return;
                        }
                    }

                    updateBoxBackground(index, otpInputs[index].hasFocus());

                    if (s != null && s.length() == 1 && index < otpInputs.length - 1) {
                        otpInputs[index + 1].requestFocus();
                    }

                    checkAndTriggerAutoVerify();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpInputs[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpInputs[index].getText().length() == 0 && index > 0) {
                        otpInputs[index - 1].requestFocus();
                        otpInputs[index - 1].setText("");
                        return true;
                    }
                }
                return false;
            });
        }

        otpInputs[0].requestFocus();
    }

    private void updateBoxBackground(int index, boolean hasFocus) {
        EditText box = otpInputs[index];
        boolean hasText = box.getText() != null && box.getText().length() > 0;
        if (hasText) {
            box.setBackgroundResource(R.drawable.bg_nordic_otp_filled);
        } else if (hasFocus) {
            box.setBackgroundResource(R.drawable.bg_nordic_otp_active);
        } else {
            box.setBackgroundResource(R.drawable.bg_nordic_otp_idle);
        }
    }

    private void fillOtpCode(String code) {
        if (code == null || code.length() < 6) return;
        isAutoFilling = true;
        for (int i = 0; i < 6; i++) {
            otpInputs[i].setText(String.valueOf(code.charAt(i)));
            otpInputs[i].setBackgroundResource(R.drawable.bg_nordic_otp_filled);
        }
        isAutoFilling = false;
        otpInputs[5].requestFocus();
        otpInputs[5].setSelection(1);
        checkAndTriggerAutoVerify();
    }

    private void checkAndTriggerAutoVerify() {
        if (isVerifying) return;
        String fullCode = getOtpCode();
        if (fullCode.length() == 6) {
            hideKeyboard();
            verifyOtp();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnEditEmail.setOnClickListener(v -> finish());
        binding.tvResendTimer.setOnClickListener(v -> resendOtp());

        binding.btnPasteOtp.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip() != null) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                if (item != null && item.getText() != null) {
                    String sixDigits = AuthUiHelper.extractSixDigits(item.getText());
                    if (sixDigits != null) {
                        fillOtpCode(sixDigits);
                        return;
                    }
                }
            }
            Toast.makeText(this, "Không tìm thấy mã OTP 6 chữ số trong bộ nhớ tạm", Toast.LENGTH_SHORT).show();
        });
    }

    private void observeViewModel() {
        authViewModel.getVerifyOtpState().observe(this, state -> {
            if (state == null || state.isIdle()) {
                return;
            }

            if (state.isLoading()) {
                isVerifying = true;
                setVerifyLoading(true);
            } else if (state.isSuccess()) {
                isVerifying = false;
                authViewModel.resetVerifyOtpState();
                showVerifiedSuccessUI();

                if (password != null && !password.trim().isEmpty()) {
                    authViewModel.login(email, password);
                } else {
                    Toast.makeText(this, "Xác thực thành công! Tài khoản đã được tạo.", Toast.LENGTH_LONG).show();
                    navigateToLoginFallback();
                }
            } else if (state.isError()) {
                isVerifying = false;
                setVerifyLoading(false);

                String error = state.getMessage() != null
                        ? state.getMessage()
                        : "Xác thực OTP thất bại";

                authViewModel.resetVerifyOtpState();
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                AuthUiHelper.shakeView(binding.layoutOtpBoxes);
                clearOtpInputs();
            }
        });

        // Theo dõi kết quả gửi lại OTP
        authViewModel.getSendOtpState().observe(this, state -> {
            if (state == null || state.isIdle()) {
                return;
            }

            if (state.isLoading()) {
                isResending = true;
                binding.tvResendTimer.setEnabled(false);
            } else if (state.isSuccess()) {
                isResending = false;
                authViewModel.resetSendOtpState();
                Toast.makeText(this, "Mã OTP mới đã được gửi đến email.", Toast.LENGTH_SHORT).show();
                clearOtpInputs();
                startResendCountdown(RESEND_INTERVAL);
            } else if (state.isError()) {
                isResending = false;
                String error = state.getMessage() != null ? state.getMessage() : "Không thể gửi lại OTP";
                authViewModel.resetSendOtpState();
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                binding.tvResendTimer.setEnabled(true);
                binding.tvResendTimer.setText(R.string.nordic_otp_resend_now);
            }
        });

        // Theo dõi kết quả tự động đăng nhập sau khi xác thực OTP thành công
        authViewModel.getLoginState().observe(this, state -> {
            if (state == null || state.isIdle()) {
                return;
            }
            if (state.isLoading()) {
                binding.tvVerifyingStatus.setText("Đang đăng nhập vào hệ thống...");
            } else if (state.isSuccess()) {
                setVerifyLoading(false);
                AuthResponse auth = state.getData();
                authViewModel.resetLoginState();
                if (auth != null
                        && auth.getToken() != null
                        && !auth.getToken().trim().isEmpty()
                        && auth.getUser() != null) {
                    SessionManager.getInstance(VerifyOtpActivity.this).saveAuth(auth.getToken(), auth.getUser());
                    FcmTokenHelper.syncFcmToken(VerifyOtpActivity.this);

                    String welcomeName = auth.getUser().getFullName() != null ? auth.getUser().getFullName() : "";
                    Toast.makeText(VerifyOtpActivity.this, "Đăng ký thành công! Chào mừng " + welcomeName, Toast.LENGTH_SHORT).show();

                    RoleRouter.navigateToHomeByRole(VerifyOtpActivity.this);
                } else {
                    navigateToLoginFallback();
                }
            } else if (state.isError()) {
                setVerifyLoading(false);
                authViewModel.resetLoginState();
                Toast.makeText(VerifyOtpActivity.this, "Đăng ký thành công! Vui lòng đăng nhập để tiếp tục.", Toast.LENGTH_LONG).show();
                navigateToLoginFallback();
            }
        });
    }

    private void verifyOtp() {
        if (isVerifying) return;

        String otpCode = getOtpCode();
        if (otpCode.length() != 6) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ 6 chữ số OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        VerifyOtpRequest request = new VerifyOtpRequest(email, otpCode, "REGISTER");
        authViewModel.verifyOtp(request);
    }

    private void resendOtp() {
        if (isResending || isVerifying) return;

        String roleToSend = role != null ? role : "ROLE_STUDENT";
        RegisterRequest request = new RegisterRequest(
                email,
                password,
                fullName,
                TextUtils.isEmpty(phone) ? null : phone,
                roleToSend
        );
        authViewModel.sendOtp(request);
    }

    private void startResendCountdown(long durationMs) {
        binding.tvResendTimer.setEnabled(false);
        if (resendTimer != null) {
            resendTimer.cancel();
        }

        resendTimer = new CountDownTimer(durationMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished + 999) / 1000;
                binding.tvResendTimer.setText(getString(R.string.nordic_otp_resend_format, seconds));
            }

            @Override
            public void onFinish() {
                binding.tvResendTimer.setEnabled(true);
                binding.tvResendTimer.setText(R.string.nordic_otp_resend_now);
            }
        }.start();
    }

    private void setVerifyLoading(boolean isLoading) {
        binding.btnPasteOtp.setEnabled(!isLoading);
        binding.btnEditEmail.setEnabled(!isLoading);
        binding.btnBack.setEnabled(!isLoading);

        for (EditText input : otpInputs) {
            input.setEnabled(!isLoading);
        }

        if (isLoading) {
            binding.layoutIdleStatus.setVisibility(View.GONE);
            binding.layoutVerifiedBanner.setVisibility(View.GONE);
            binding.layoutVerifyingBanner.setVisibility(View.VISIBLE);
        } else {
            binding.layoutVerifyingBanner.setVisibility(View.GONE);
            binding.layoutIdleStatus.setVisibility(View.VISIBLE);
        }
    }

    private void showVerifiedSuccessUI() {
        binding.layoutIdleStatus.setVisibility(View.GONE);
        binding.layoutVerifyingBanner.setVisibility(View.GONE);
        binding.layoutVerifiedBanner.setVisibility(View.VISIBLE);
        for (EditText box : otpInputs) {
            box.setBackgroundResource(R.drawable.bg_nordic_otp_verified);
        }
    }

    private String getOtpCode() {
        StringBuilder sb = new StringBuilder();
        for (EditText input : otpInputs) {
            sb.append(input.getText().toString().trim());
        }
        return sb.toString();
    }

    private void clearOtpInputs() {
        isAutoFilling = true;
        for (EditText input : otpInputs) {
            input.setText("");
            input.setBackgroundResource(R.drawable.bg_nordic_otp_idle);
        }
        isAutoFilling = false;
        if (otpInputs.length > 0) {
            otpInputs[0].requestFocus();
        }
    }

    private void navigateToLoginFallback() {
        Intent intent = new Intent(VerifyOtpActivity.this, LoginActivity.class);
        intent.putExtra(com.ppquang06.uniclassapp.utils.Constants.EXTRA_EMAIL, email);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}
