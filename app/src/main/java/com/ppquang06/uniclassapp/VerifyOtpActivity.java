package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.ppquang06.uniclassapp.data.model.request.RegisterRequest;
import com.ppquang06.uniclassapp.data.model.request.VerifyOtpRequest;
import com.ppquang06.uniclassapp.databinding.ActivityRegisterOtpBinding;
import com.ppquang06.uniclassapp.ui.auth.AuthViewModel;

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

    private static final long RESEND_INTERVAL = 60_000;

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

        startResendCountdown();
    }

    private void getRegistrationData() {
        email = getIntent().getStringExtra("email");
        fullName = getIntent().getStringExtra("fullName");
        phone = getIntent().getStringExtra("phone");
        password = getIntent().getStringExtra("password");
        role = getIntent().getStringExtra("role");

        if (email == null || email.trim().isEmpty()
                || fullName == null || password == null || role == null) {

            Toast.makeText(
                    this,
                    "Thiếu thông tin đăng ký. Vui lòng đăng ký lại.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
        }
    }

    private void setupWindowInsets() {
        View root = binding.getRoot();

        int initialLeft = root.getPaddingLeft();
        int initialTop = root.getPaddingTop();
        int initialRight = root.getPaddingRight();
        int initialBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {

            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            Insets ime = insets.getInsets(
                    WindowInsetsCompat.Type.ime()
            );

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

            otpInputs[i].addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(
                        CharSequence s,
                        int start,
                        int count,
                        int after
                ) {}

                @Override
                public void onTextChanged(
                        CharSequence s,
                        int start,
                        int before,
                        int count
                ) {
                    if (s.length() == 1
                            && index < otpInputs.length - 1) {

                        otpInputs[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpInputs[i].setOnKeyListener((v, keyCode, event) -> {

                if (keyCode == KeyEvent.KEYCODE_DEL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {

                    if (otpInputs[index].getText().length() == 0
                            && index > 0) {

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

    private void setupClickListeners() {

        binding.btnConfirmRegisterOtp.setOnClickListener(v -> verifyOtp());

        binding.tvResendTimer.setOnClickListener(v -> resendOtp());

        binding.tvBackToRegister.setOnClickListener(v -> finish());

        binding.tvBackToLogin.setOnClickListener(v -> {

            Intent intent = new Intent(
                    VerifyOtpActivity.this,
                    LoginActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            startActivity(intent);
            finish();
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
                setVerifyLoading(false);

                authViewModel.resetVerifyOtpState();

                Toast.makeText(
                        this,
                        "Xác thực thành công! Tài khoản đã được tạo.",
                        Toast.LENGTH_LONG
                ).show();

                Intent intent = new Intent(
                        VerifyOtpActivity.this,
                        RegisterSuccessActivity.class
                );

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                startActivity(intent);
                finish();

            } else if (state.isError()) {

                isVerifying = false;
                setVerifyLoading(false);

                String error = state.getMessage() != null
                        ? state.getMessage()
                        : "Xác thực OTP thất bại";

                authViewModel.resetVerifyOtpState();

                Toast.makeText(
                        this,
                        error,
                        Toast.LENGTH_LONG
                ).show();

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

                Toast.makeText(
                        this,
                        "Mã OTP mới đã được gửi đến email.",
                        Toast.LENGTH_SHORT
                ).show();

                clearOtpInputs();
                startResendCountdown();

            } else if (state.isError()) {

                isResending = false;

                String error = state.getMessage() != null
                        ? state.getMessage()
                        : "Không thể gửi lại OTP";

                authViewModel.resetSendOtpState();

                Toast.makeText(
                        this,
                        error,
                        Toast.LENGTH_LONG
                ).show();

                binding.tvResendTimer.setEnabled(true);
                binding.tvResendTimer.setText("Gửi lại mã");
            }
        });
    }

    private void verifyOtp() {

        if (isVerifying) {
            return;
        }

        String otpCode = getOtpCode();

        if (otpCode.length() != 6) {

            Toast.makeText(
                    this,
                    "Vui lòng nhập đầy đủ 6 chữ số OTP",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!otpCode.matches("[0-9]{6}")) {

            Toast.makeText(
                    this,
                    "Mã OTP chỉ được chứa chữ số",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        VerifyOtpRequest request = new VerifyOtpRequest(
                email,
                otpCode,
                "REGISTER"
        );

        authViewModel.verifyOtp(request);
    }

    private void resendOtp() {

        if (isResending) {
            return;
        }

        if (password == null || fullName == null || role == null) {
            Toast.makeText(
                    this,
                    "Không đủ thông tin để gửi lại OTP",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        RegisterRequest request = new RegisterRequest(
                email,
                password,
                fullName,
                phone == null || phone.trim().isEmpty() ? null : phone,
                role
        );

        authViewModel.sendOtp(request);
    }

    private String getOtpCode() {

        StringBuilder sb = new StringBuilder();

        for (EditText editText : otpInputs) {
            sb.append(editText.getText().toString().trim());
        }

        return sb.toString();
    }

    private void clearOtpInputs() {

        for (EditText editText : otpInputs) {
            editText.setText("");
        }

        otpInputs[0].requestFocus();
    }

    private void startResendCountdown() {

        if (resendTimer != null) {
            resendTimer.cancel();
        }

        binding.tvResendTimer.setEnabled(false);

        resendTimer = new CountDownTimer(
                RESEND_INTERVAL,
                1000
        ) {

            @Override
            public void onTick(long millisUntilFinished) {

                long seconds = millisUntilFinished / 1000;

                binding.tvResendTimer.setText(
                        "Gửi lại mã (" + seconds + "s)"
                );
            }

            @Override
            public void onFinish() {

                binding.tvResendTimer.setText("Gửi lại mã");
                binding.tvResendTimer.setEnabled(true);
            }

        }.start();
    }

    private void setVerifyLoading(boolean loading) {

        binding.btnConfirmRegisterOtp.setEnabled(!loading);

        binding.btnConfirmRegisterOtp.setText(
                loading ? "Đang xác thực..." : "Xác nhận OTP"
        );

        for (EditText editText : otpInputs) {
            editText.setEnabled(!loading);
        }
    }

    @Override
    protected void onDestroy() {
        if (resendTimer != null) {
            resendTimer.cancel();
        }

        super.onDestroy();
        binding = null;
    }
}