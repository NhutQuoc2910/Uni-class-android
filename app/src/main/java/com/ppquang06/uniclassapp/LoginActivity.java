package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ppquang06.uniclassapp.data.model.response.AuthResponse;
import com.ppquang06.uniclassapp.databinding.ActivityLoginBinding;
import com.ppquang06.uniclassapp.ui.auth.AuthViewModel;
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

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.ivTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                binding.edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                binding.edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
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
                if (auth != null && auth.getToken() != null) {
                    SessionManager.getInstance(LoginActivity.this).saveAuth(auth.getToken(), auth.getUser());

                    String welcomeName = auth.getUser() != null ? auth.getUser().getFullName() : "";
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công! " + welcomeName, Toast.LENGTH_SHORT).show();

                    RoleRouter.navigateToHomeByRole(LoginActivity.this);
                } else {
                    Toast.makeText(LoginActivity.this, "Email/Số điện thoại hoặc mật khẩu không chính xác", Toast.LENGTH_LONG).show();
                }
            } else if (state.isError()) {
                setLoading(false);
                String errorMsg = state.getMessage() != null
                        ? state.getMessage()
                        : "Email/Số điện thoại hoặc mật khẩu không chính xác";
                authViewModel.resetLoginState();
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performLogin() {
        String emailOrPhone = binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString().trim() : "";
        String password = binding.edtPassword.getText() != null ? binding.edtPassword.getText().toString() : "";

        if (TextUtils.isEmpty(emailOrPhone)) {
            binding.edtEmail.setError("Vui lòng nhập email hoặc số điện thoại");
            binding.edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.edtPassword.setError("Vui lòng nhập mật khẩu");
            binding.edtPassword.requestFocus();
            return;
        }

        authViewModel.login(emailOrPhone, password);
    }

    private void setLoading(boolean isLoading) {
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnLogin.setText(isLoading ? "Đang đăng nhập..." : getString(R.string.login_button));
    }
}
