package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ppquang06.uniclassapp.databinding.ActivityForgotPassOtpBinding;
import com.ppquang06.uniclassapp.ui.auth.AuthViewModel;
import com.ppquang06.uniclassapp.utils.Constants;

public class ForgotPassOTPActivity extends AppCompatActivity {
    private ActivityForgotPassOtpBinding binding;
    private AuthViewModel authViewModel;
    private String email = "";
    private String pendingOtp = "";
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPassOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        email = getIntent().getStringExtra(Constants.EXTRA_EMAIL);
        if (email == null) email = "";

        if (binding.tvOtpSubtitle != null && !TextUtils.isEmpty(email)) {
            binding.tvOtpSubtitle.setText("Nhập mã gồm 6 chữ số đã được gửi đến " + email);
        }

        setupOtpInputBoxes();
        setupResendTimer();
        setupListeners();
        observeViewModel();
    }

    private void setupOtpInputBoxes() {
        EditText[] otpBoxes = new EditText[]{
                binding.etOtp1, binding.etOtp2, binding.etOtp3,
                binding.etOtp4, binding.etOtp5, binding.etOtp6
        };

        for (int i = 0; i < otpBoxes.length; i++) {
            final int index = i;
            otpBoxes[index].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpBoxes.length - 1) {
                        otpBoxes[index + 1].requestFocus();
                    }
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
                long seconds = millisUntilFinished / 1000;
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
                startCountdown();
            } else if (state.isError()) {
                String errorMsg = state.getMessage() != null ? state.getMessage() : "Gửi lại OTP thất bại";
                authViewModel.resetForgotPasswordState();
                Toast.makeText(ForgotPassOTPActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                binding.tvResendTimer.setText("Gửi lại mã");
                binding.tvResendTimer.setEnabled(true);
            }
        });

        authViewModel.getVerifyOtpState().observe(this, state -> {
            if (state == null || state.isIdle()) {
                return;
            }
            if (state.isLoading()) {
                setLoading(true);
            } else if (state.isSuccess()) {
                setLoading(false);
                authViewModel.resetVerifyOtpState();
                Toast.makeText(ForgotPassOTPActivity.this, "Xác thực OTP thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ForgotPassOTPActivity.this, ForgotPassNewActivity.class);
                intent.putExtra(Constants.EXTRA_EMAIL, email);
                intent.putExtra(Constants.EXTRA_OTP, pendingOtp);
                startActivity(intent);
            } else if (state.isError()) {
                setLoading(false);
                String errorMsg = state.getMessage() != null
                        ? state.getMessage()
                        : "Mã OTP không chính xác hoặc đã hết hạn";
                authViewModel.resetVerifyOtpState();
                Toast.makeText(ForgotPassOTPActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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
        binding.btnConfirmOtp.setOnClickListener(v -> verifyOtp());
        binding.tvBackToPrevious.setOnClickListener(v -> finish());
        binding.tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPassOTPActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void verifyOtp() {
        String o1 = binding.etOtp1.getText().toString().trim();
        String o2 = binding.etOtp2.getText().toString().trim();
        String o3 = binding.etOtp3.getText().toString().trim();
        String o4 = binding.etOtp4.getText().toString().trim();
        String o5 = binding.etOtp5.getText().toString().trim();
        String o6 = binding.etOtp6.getText().toString().trim();

        String otp = o1 + o2 + o3 + o4 + o5 + o6;
        if (otp.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ 6 chữ số mã OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingOtp = otp;
        authViewModel.verifyOtp(email, otp);
    }

    private void setLoading(boolean isLoading) {
        binding.btnConfirmOtp.setEnabled(!isLoading);
        binding.btnConfirmOtp.setText(isLoading ? "Đang xác thực..." : getString(R.string.confirm_otp_button));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
