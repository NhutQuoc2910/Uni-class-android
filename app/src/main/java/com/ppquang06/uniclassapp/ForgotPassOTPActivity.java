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
import androidx.lifecycle.ViewModelProvider;

import com.ppquang06.uniclassapp.databinding.ActivityForgotPassOtpBinding;
import com.ppquang06.uniclassapp.ui.auth.AuthViewModel;
import com.ppquang06.uniclassapp.utils.AuthUiHelper;
import com.ppquang06.uniclassapp.utils.Constants;

public class ForgotPassOTPActivity extends AppCompatActivity {
    private ActivityForgotPassOtpBinding binding;
    private AuthViewModel authViewModel;
    private String email = "";
    private String pendingOtp = "";
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private boolean isVerifying = false;
    private boolean isAutoFilling = false;

    private EditText[] otpBoxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPassOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        email = getIntent().getStringExtra(Constants.EXTRA_EMAIL);
        if (email == null) email = "";

        binding.tvRecipientEmail.setText(email);

        setupOtpInputBoxes();
        setupResendTimer();
        setupListeners();
        observeViewModel();
    }

    private void setupOtpInputBoxes() {
        otpBoxes = new EditText[]{
                binding.etOtp1, binding.etOtp2, binding.etOtp3,
                binding.etOtp4, binding.etOtp5, binding.etOtp6
        };

        for (int i = 0; i < otpBoxes.length; i++) {
            final int index = i;

            otpBoxes[index].setOnFocusChangeListener((v, hasFocus) -> updateBoxBackground(index, hasFocus));

            otpBoxes[index].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isAutoFilling) return;

                    // Paste support
                    if (s != null && s.length() > 1) {
                        String sixDigits = AuthUiHelper.extractSixDigits(s);
                        if (sixDigits != null) {
                            fillOtpCode(sixDigits);
                            return;
                        }
                    }

                    updateBoxBackground(index, otpBoxes[index].hasFocus());

                    if (s != null && s.length() == 1 && index < otpBoxes.length - 1) {
                        otpBoxes[index + 1].requestFocus();
                    }

                    checkAndTriggerAutoVerify();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpBoxes[index].setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (otpBoxes[index].getText().length() == 0 && index > 0) {
                        otpBoxes[index - 1].requestFocus();
                        otpBoxes[index - 1].setText("");
                        return true;
                    }
                }
                return false;
            });
        }

        otpBoxes[0].requestFocus();
    }

    private void updateBoxBackground(int index, boolean hasFocus) {
        EditText box = otpBoxes[index];
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
            otpBoxes[i].setText(String.valueOf(code.charAt(i)));
            otpBoxes[i].setBackgroundResource(R.drawable.bg_nordic_otp_filled);
        }
        isAutoFilling = false;
        otpBoxes[5].requestFocus();
        otpBoxes[5].setSelection(1);
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

    private void setupResendTimer() {
        startCountdown();
        binding.tvResendTimer.setOnClickListener(v -> {
            if (!isTimerRunning) {
                resendOtp();
            }
        });
    }

    private void startCountdown() {
        isTimerRunning = true;
        binding.tvResendTimer.setEnabled(false);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished + 999) / 1000;
                binding.tvResendTimer.setText("Gửi lại sau (" + seconds + "s)");
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                binding.tvResendTimer.setText("Gửi lại mã");
                binding.tvResendTimer.setEnabled(true);
            }
        }.start();
    }

    private void observeViewModel() {
        authViewModel.getForgotPasswordState().observe(this, state -> {
            if (state == null || state.isIdle()) {
                return;
            }
            if (state.isLoading()) {
                binding.tvResendTimer.setText("Đang gửi...");
                binding.tvResendTimer.setEnabled(false);
            } else if (state.isSuccess()) {
                authViewModel.resetForgotPasswordState();
                Toast.makeText(ForgotPassOTPActivity.this, "Đã gửi lại mã OTP!", Toast.LENGTH_SHORT).show();
                clearOtpInputs();
                startCountdown();
            } else if (state.isError()) {
                String errorMsg = state.getMessage() != null ? state.getMessage() : "Gửi lại OTP thất bại";
                authViewModel.resetForgotPasswordState();
                Toast.makeText(ForgotPassOTPActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                binding.tvResendTimer.setText("Gửi lại mã");
                binding.tvResendTimer.setEnabled(true);
            }
        });

        authViewModel.getVerifyOtpState().observe(this, state -> {
            if (state == null || state.isIdle()) {
                return;
            }
            if (state.isLoading()) {
                isVerifying = true;
                setLoading(true);
            } else if (state.isSuccess()) {
                isVerifying = false;
                authViewModel.resetVerifyOtpState();
                showVerifiedSuccessUI();
                Toast.makeText(ForgotPassOTPActivity.this, "Xác thực OTP thành công!", Toast.LENGTH_SHORT).show();

                binding.layoutOtpBoxes.postDelayed(() -> {
                    Intent intent = new Intent(ForgotPassOTPActivity.this, ForgotPassNewActivity.class);
                    intent.putExtra(Constants.EXTRA_EMAIL, email);
                    intent.putExtra(Constants.EXTRA_OTP, pendingOtp);
                    startActivity(intent);
                    finish();
                }, 800);
            } else if (state.isError()) {
                isVerifying = false;
                setLoading(false);
                String errorMsg = state.getMessage() != null
                        ? state.getMessage()
                        : "Mã OTP không chính xác hoặc đã hết hạn";
                authViewModel.resetVerifyOtpState();
                Toast.makeText(ForgotPassOTPActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                AuthUiHelper.shakeView(binding.layoutOtpBoxes);
                clearOtpInputs();
            }
        });
    }

    private void resendOtp() {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Không có thông tin email", Toast.LENGTH_SHORT).show();
            return;
        }

        authViewModel.forgotPassword(email);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnEditEmail.setOnClickListener(v -> finish());
        binding.tvBackToLogin.setOnClickListener(v -> navigateToLogin());

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

    private void verifyOtp() {
        if (isVerifying) return;

        String otp = getOtpCode();
        if (otp.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ 6 chữ số mã OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingOtp = otp;
        authViewModel.verifyOtp(email, otp);
    }

    private String getOtpCode() {
        StringBuilder sb = new StringBuilder();
        for (EditText box : otpBoxes) {
            sb.append(box.getText().toString().trim());
        }
        return sb.toString();
    }

    private void clearOtpInputs() {
        isAutoFilling = true;
        for (EditText box : otpBoxes) {
            box.setText("");
            box.setBackgroundResource(R.drawable.bg_nordic_otp_idle);
        }
        isAutoFilling = false;
        if (otpBoxes.length > 0) {
            otpBoxes[0].requestFocus();
        }
    }

    private void setLoading(boolean isLoading) {
        binding.btnPasteOtp.setEnabled(!isLoading);
        binding.btnEditEmail.setEnabled(!isLoading);
        binding.btnBack.setEnabled(!isLoading);

        for (EditText box : otpBoxes) {
            box.setEnabled(!isLoading);
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
        for (EditText box : otpBoxes) {
            box.setBackgroundResource(R.drawable.bg_nordic_otp_verified);
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ForgotPassOTPActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
