package com.ppquang06.uniclassapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ppquang06.uniclassapp.data.api.ApiClient;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.LoginRequest;
import com.ppquang06.uniclassapp.data.model.response.AuthResponse;
import com.ppquang06.uniclassapp.databinding.ActivityLoginBinding;
import com.ppquang06.uniclassapp.utils.RoleRouter;
import com.ppquang06.uniclassapp.utils.SessionManager;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
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

        setLoading(true);

        LoginRequest request = new LoginRequest(emailOrPhone, password);
        ApiClient.getAuthApiService(this).login(request).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AuthResponse auth = response.body().getData();
                    if (auth != null && auth.getToken() != null) {
                        SessionManager.getInstance(LoginActivity.this).saveAuth(auth.getToken(), auth.getUser());

                        String welcomeName = auth.getUser() != null ? auth.getUser().getFullName() : "";
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công! " + welcomeName, Toast.LENGTH_SHORT).show();

                        RoleRouter.navigateToHomeByRole(LoginActivity.this);
                        return;
                    }
                }

                String errorMsg = "Email/Số điện thoại hoặc mật khẩu không chính xác";
                try {
                    if (response.errorBody() != null) {
                        JSONObject errJson = new JSONObject(response.errorBody().string());
                        if (errJson.has("message")) {
                            errorMsg = errJson.getString("message");
                        }
                    } else if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                } catch (Exception ignored) {}
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnLogin.setText(isLoading ? "Đang đăng nhập..." : getString(R.string.login_button));
    }
}
