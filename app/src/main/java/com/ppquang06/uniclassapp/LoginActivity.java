package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.ppquang06.uniclassapp.data.model.response.AuthResponse;
import com.ppquang06.uniclassapp.databinding.ActivityLoginBinding;
import com.ppquang06.uniclassapp.ui.auth.AuthViewModel;
import com.ppquang06.uniclassapp.utils.AuthUiHelper;
import com.ppquang06.uniclassapp.utils.Constants;
import com.ppquang06.uniclassapp.utils.RoleRouter;
import com.ppquang06.uniclassapp.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
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

        handlePreFilledEmail();
        setupErrorClearing();
        setupListeners();
        observeViewModel();
    }

    private void handlePreFilledEmail() {
        Intent intent = getIntent();
        if (intent != null) {
            String prefilledEmail = intent.getStringExtra(Constants.EXTRA_EMAIL);
            if (prefilledEmail != null && !prefilledEmail.trim().isEmpty()) {
                binding.edtEmail.setText(prefilledEmail.trim());
                binding.edtPassword.requestFocus();
            }
        }
    }

    private void setupErrorClearing() {
        binding.edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearEmailError();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearPasswordError();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void clearEmailError() {
        binding.tvErrorEmail.setVisibility(View.GONE);
        binding.containerEmail.setBackgroundResource(R.drawable.bg_nordic_input_selector);
    }

    private void clearPasswordError() {
        binding.tvErrorPassword.setVisibility(View.GONE);
        binding.containerPassword.setBackgroundResource(R.drawable.bg_nordic_input_selector);
    }

    private void showEmailError(String message) {
        binding.tvErrorEmail.setText(message);
        binding.tvErrorEmail.setVisibility(View.VISIBLE);
        binding.containerEmail.setBackgroundResource(R.drawable.bg_nordic_input_error);
        AuthUiHelper.shakeView(binding.containerEmail);
        binding.edtEmail.requestFocus();
    }

    private void showPasswordError(String message) {
        binding.tvErrorPassword.setText(message);
        binding.tvErrorPassword.setVisibility(View.VISIBLE);
        binding.containerPassword.setBackgroundResource(R.drawable.bg_nordic_input_error);
        AuthUiHelper.shakeView(binding.containerPassword);
        binding.edtPassword.requestFocus();
    }

    private void setupListeners() {
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

        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        binding.btnLogin.setOnClickListener(v -> performLogin());
    }

    private void observeViewModel() {
        authViewModel.getLoginState().observe(this, state -> {
            if (state == null || state.isIdle()) {
                return;
            }
            if (state.isLoading()) {
                setLoading(true);
            } else if (state.isSuccess()) {
                setLoading(false);
                AuthResponse auth = state.getData();
                authViewModel.resetLoginState();
                if (auth != null
                        && auth.getToken() != null
                        && !auth.getToken().trim().isEmpty()
                        && auth.getUser() != null) {
                    SessionManager.getInstance(LoginActivity.this).saveAuth(auth.getToken(), auth.getUser());
                    com.ppquang06.uniclassapp.utils.FcmTokenHelper.syncFcmToken(LoginActivity.this);

                    String welcomeName = auth.getUser() != null ? auth.getUser().getFullName() : "";
                    Toast.makeText(LoginActivity.this, getString(R.string.nordic_login_success) + " " + welcomeName, Toast.LENGTH_SHORT).show();

                    RoleRouter.navigateToHomeByRole(LoginActivity.this);
                } else {
                    showPasswordError(getString(R.string.nordic_login_error_credentials));
                }
            } else if (state.isError()) {
                setLoading(false);
                String errorMsg = state.getMessage() != null
                        ? state.getMessage()
                        : getString(R.string.nordic_login_error_credentials);
                authViewModel.resetLoginState();
                showPasswordError(errorMsg);
            }
        });
    }

    private void performLogin() {
        String emailOrPhone = binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString().trim() : "";
        String password = binding.edtPassword.getText() != null ? binding.edtPassword.getText().toString() : "";

        if (TextUtils.isEmpty(emailOrPhone)) {
            showEmailError("Vui lòng nhập email hoặc số điện thoại");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showPasswordError("Vui lòng nhập mật khẩu");
            return;
        }

        clearEmailError();
        clearPasswordError();
        authViewModel.login(emailOrPhone, password);
    }

    private void setLoading(boolean isLoading) {
        binding.btnLogin.setEnabled(!isLoading);
        binding.edtEmail.setEnabled(!isLoading);
        binding.edtPassword.setEnabled(!isLoading);
        binding.tvBtnLoginLabel.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        binding.ivBtnArrow.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
